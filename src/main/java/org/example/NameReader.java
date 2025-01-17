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
    private static long fakeVowelSoundCount = 0;
    public static List<NameEntry> nameEntries = new ArrayList<>();
    public static List<NameEntry> vowelNameEntries = new ArrayList<>();
    private static final List<NameEntry> fmVowelNameEntries = new ArrayList<>();
    private static final List<NameEntry> mlVowelNameEntries = new ArrayList<>();
    private static final List<NameEntry> topFNames = new ArrayList<>();
    private static final List<NameEntry> topMNames = new ArrayList<>();
    private static Map<String, NameEntry> yearNameMap = new HashMap<>();
    private static Map<String, NameEntry> nameMap = new HashMap<>(); // Map to store unique (name, gender) pairs and their corresponding NameEntry
    public static void addFakeVowelSound() {
        fakeVowelSoundCount++;
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
        combineSameNames();
        countTopNames();
        printResults();
        searchForAName();

        /*
        System.out.println("Occurrences by Year (female):");

        yearVowelSoundCounts.forEach((year, entries) -> {
            // Filter entries ending with a vowel sound and sort by occurrences in descending order
            List<NameEntry> topVowelEndingNames = entries.stream()
                    .filter(NameEntry::endsWithVowelSound)
                    .filter(NameEntry::isFemale)
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
        */
    }

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

                    NameEntry entry = new NameEntry(name, occurrences, gender, year);
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



    private static void combineSameNames() {
        // Iterate through the list and merge entries with the same name, gender and year
        for (NameEntry entry : vowelNameEntries) {
            // Create a composite key using both name and gender
            String key = entry.getName() + "_" + entry.getGender() + "_" + entry.getYear();
            String shortKey = entry.getName() + "_" + entry.getGender();
            //System.out.println("--------------------------------------");
            //System.out.println("key=" +key + "shortKey=" + shortKey);

            // If the composite key is already in the map, combine occurrences
            if (yearNameMap.containsKey(key) && nameMap.containsKey(shortKey)) {
                yearNameMap.get(key).addOccurrences(entry.getOccurrences());
                nameMap.get(shortKey).addOccurrences(entry.getOccurrences());
            } else if (nameMap.containsKey(shortKey) && !yearNameMap.containsKey(key)) {
                nameMap.get(shortKey).addOccurrences(entry.getOccurrences());
                yearNameMap.put(key, entry);
                //System.out.println("Adding new entry |key=" + key);
            } else if (yearNameMap.containsKey(key) && !nameMap.containsKey(shortKey)) {
                yearNameMap.get(key).addOccurrences(entry.getOccurrences());
                nameMap.put(shortKey, entry);
                //System.out.println("Adding short|shortKey=" + shortKey);
            } else {
                // Otherwise, add the entry to the map
                yearNameMap.put(key, entry);
                nameMap.put(shortKey, entry);
                //System.out.println("Adding new entry to both|shortKey=" + shortKey + "key="+key);
            }
        }

        // Replace the original list with the combined entries
        vowelNameEntries.clear();
        vowelNameEntries.addAll(nameMap.values());
    }


    private static void countTopNames() {
        int topCount = 0;
        int secondCount = 0;
        int thirdCount = 0;

        for (NameEntry entry : fmVowelNameEntries) {
            if (topFNames.size() == 3) {
                //System.out.println("Top Three so far\n" + topFNames.getFirst() + "\n" + topFNames.get(1) + "\n" + topFNames.getLast());
            }
                if (entry.getOccurrences() > topCount) {
                    thirdCount = secondCount;
                    secondCount = topCount;
                    topCount = entry.getOccurrences();
                    topFNames.addFirst(entry);
                } else if (entry.getOccurrences() > secondCount) {
                    thirdCount = secondCount;
                    secondCount = entry.getOccurrences();
                    topFNames.add(1, entry);
                } else if (entry.getOccurrences() > thirdCount) {
                    thirdCount = entry.getOccurrences();
                    topFNames.removeLast();
                    topFNames.addLast(entry);
                }
        }
        topCount = 0;
        secondCount = 0;
        thirdCount = 0;
        for (NameEntry entry : mlVowelNameEntries) {
            if (entry.getOccurrences() > topCount) {
                thirdCount = secondCount;
                secondCount = topCount;
                topCount = entry.getOccurrences();
                topMNames.addFirst(entry);
            } else if (entry.getOccurrences() > secondCount) {
                thirdCount = secondCount;
                secondCount = entry.getOccurrences();
                topMNames.add(1, entry);
            } else if (entry.getOccurrences() > thirdCount) {
                thirdCount = entry.getOccurrences();
                topMNames.removeLast();
                topMNames.addLast(entry);
            }
        }
    }

    private static void printResults() {
        double vowelCountAsPercent = (double) Math.round(((double) vowelEndingCount / totalCount) * 10000) / 100;
        double fm = (double) Math.round(((double) femaleNamesCount / vowelEndingCount) * 10000) / 100;
        double ml = (double) Math.round(((double) (vowelEndingCount - femaleNamesCount) / vowelEndingCount) * 10000) / 100;
        System.out.println("Total name occurrences counted: " + totalCount);
        System.out.println("Total occurrences of names ending with a vowel sound across all years: " + vowelEndingCount);
        System.out.println("Percentage of all names that end with a vowel sound: " + vowelCountAsPercent + "%");
        System.out.println("Of the names that end with a vowel sound, " + fm + "% are female and " + ml + "% are male");
        System.out.println("Top Male Names are:");
        System.out.println(topMNames.getFirst() + "\n" + topMNames.get(1) + "\n" + topMNames.getLast());
        System.out.println("Top Female Names are:");
        System.out.println(topFNames.getFirst() + "\n" + topFNames.get(1) + "\n" + topFNames.getLast());
    }

    private static void searchForAName() {
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("Search for a name. Type exit to leave. ");
            System.out.print("Format is NAME_GENDER ex. Henry_M or Tessa_F:");
            String name = input.nextLine();
            if (name.contains("1") || name.contains("2") || name.equalsIgnoreCase("exit")) {
                System.out.println("Name contains year - using yearNameMap");
                if (yearNameMap.containsKey(name)) {
                    System.out.println(yearNameMap.get(name));
                } else if (name.equalsIgnoreCase("exit")) {
                    break;
                } else {
                    System.out.println("Name not found");
                }
            } else {
                if (nameMap.containsKey(name)) {
                    System.out.println(nameMap.get(name));
                } else if (name.equalsIgnoreCase("exit")) {
                    break;
                } else {
                    System.out.println("Name not found");
                }
            }
        }
    }


    private static void totalVowelCount(NameEntry entry) {
        if (entry.endsWithVowelSound() && entry.isFemale()) {
            vowelNameEntries.add(entry);
            fmVowelNameEntries.add(entry);
            vowelEndingCount += entry.getOccurrences();
            totalCount += entry.getOccurrences();
            femaleNamesCount += entry.getOccurrences();
        } else if (entry.endsWithVowelSound()) {
            vowelNameEntries.add(entry);
            mlVowelNameEntries.add(entry);
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

    public static void setNameMap(Map<String, NameEntry> nameMap) {
        NameReader.nameMap = nameMap;
    }
}
