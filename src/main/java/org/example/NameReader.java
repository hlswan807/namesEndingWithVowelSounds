package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class NameReader {
    public static long vowelEndingCount;
    public static long totalCount;
    public static long femaleNamesCount;
    public static List<Integer> years = new ArrayList<>();
    public static TreeMap<Integer, List<NameEntry>> yearVowelSoundCounts = new TreeMap<>();
    public static int year = 1879;
    public static long fakeVowelSoundCount = 0;
    public static List<NameEntry> nameEntries = new ArrayList<>();

    public static List<NameEntry> readNameFile(String fileName, int year) {
        List<NameEntry> nameEntries = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0];
                    Gender gender = Gender.valueOf(parts[1]);
                    int occurrences = Integer.parseInt(parts[2]);

                    NameEntry entry = new NameEntry(name, gender, occurrences, year);
                    nameEntries.add(entry);

                    // Add the entry to the yearVowelSoundCounts TreeMap
                    yearVowelSoundCounts.computeIfAbsent(year, k -> new ArrayList<>()).add(entry);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        return nameEntries;
    }

    private static void totalVowelCount(NameEntry entry) {
        if (entry.endsWithVowelSound() && entry.isFemale()) {
            vowelEndingCount += entry.getOccurrences();
            totalCount += entry.getOccurrences();
            femaleNamesCount += entry.getOccurrences();
        } else if (entry.endsWithVowelSound()) {
            vowelEndingCount += entry.getOccurrences();
            totalCount += entry.getOccurrences();
        } else {
            totalCount += entry.getOccurrences();
        }
    }

    private static void count(List<NameEntry> nameEntries) {
        for (NameEntry entry : nameEntries) {
            totalVowelCount(entry);
        }
    }

    public static void initYears() {
        for (int i = 1880; i < 2024; i++) {
            years.add(i);
        }
    }

    public static void main(String[] args) {
        String folderPath = "src/main/resources/names";
        initYears();

        try {
            Files.list(Paths.get(folderPath)).forEach(filePath -> {
                year++;
                if (Files.isRegularFile(filePath)) {
                    // Read the file and store entries by year
                    nameEntries = readNameFile(filePath.toString(), year);
                    count(nameEntries);
                }
            });

        } catch (IOException e) {
            System.err.println("Error reading files from folder: " + e.getMessage());
        }

        System.out.println("Total name occurrences counted: " + totalCount);
        System.out.println("Total occurrences of names ending with a vowel sound across all years: " + vowelEndingCount);

        System.out.println("Occurrences by Year:");
        yearVowelSoundCounts.forEach((year, entries) -> {
            // Filter entries ending with a vowel sound and sort by occurrences in descending order
            List<NameEntry> topVowelEndingNames = entries.stream()
                    .filter(NameEntry::endsWithVowelSound)
                    .sorted(Comparator.comparingInt(NameEntry::getOccurrences).reversed())
                    .limit(3)  // Limit to top 3
                    .toList();

            System.out.println("Year " + year + ":");

            if (topVowelEndingNames.isEmpty()) {
                System.out.println("  No names ending with a vowel sound.");
            } else {
                for (NameEntry entry : topVowelEndingNames) {
                    System.out.println("  " + entry.getName() + " - " + entry.getOccurrences() + " occurrences");
                }
            }
        });
    }
}
