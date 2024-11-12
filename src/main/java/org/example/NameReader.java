package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NameReader {
    private static int totalCount;
    private static int vowelEndingCount;
    private static int femaleNamesCount;

    public static List<NameEntry> readNameFile(String fileName) {
        List<NameEntry> nameEntries = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0];
                    Gender gender = Gender.valueOf(parts[1]);
                    int occurrences = Integer.parseInt(parts[2]);

                    NameEntry entry = new NameEntry(name, gender, occurrences);
                    nameEntries.add(entry);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        return nameEntries;
    }

    private static void count(List<NameEntry> nameEntries) {
        for (NameEntry entry : nameEntries) {
            if (entry.endsWithVowel() && entry.isFemale()) {
                vowelEndingCount += entry.getOccurrences();
                totalCount += entry.getOccurrences();
                femaleNamesCount += entry.getOccurrences();
            } else if (entry.endsWithVowel()) {
                vowelEndingCount += entry.getOccurrences();
                totalCount += entry.getOccurrences();
            } else {
                totalCount += entry.getOccurrences();
            }
        }
    }


    public static void main(String[] args) {
        String fileName = "src/main/resources/names/yob1880.txt";
        List<NameEntry> nameEntries = readNameFile(fileName);

        // Count occurrences for names ending with a vowel
        count(nameEntries);

        System.out.println("Total occurrences of names ending with a vowel sound: " + vowelEndingCount);
        System.out.println("Total occurrences of names NOT ending with a vowel sound: " + (totalCount - vowelEndingCount));
        System.out.println("Percentage of names ending with a vowel sound: " + ((double) ((int) (((double) vowelEndingCount / totalCount)*10000))) / 100 + "%");


        System.out.println("Total number of female names ending with a vowel sound: " + femaleNamesCount);
        System.out.println("Total number of male names ending with a vowel sound: " + (vowelEndingCount - femaleNamesCount));
        double percentage = 100 - ((double) ((int) (((double) femaleNamesCount/vowelEndingCount)*10000))) / 100;
        System.out.println("Of all names ending with a vowel sound, " + ((double) ((int) (((double) femaleNamesCount/vowelEndingCount)*10000))) / 100 + "% were female, and " + ((double)(Math.round(percentage * 100))/ 100) + "% were male.");
    }

}

