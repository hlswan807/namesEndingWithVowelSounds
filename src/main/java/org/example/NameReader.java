package org.example;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.*;
@Getter
@Setter
public class NameReader {
    private static long startTime;
    private static long endTime;


    private static long vowelEndingCount;
    private static long totalCount;
    private static long femaleNamesCount;
    private static long maleNamesCount;
    private static final List<Integer> years = new ArrayList<>();
    private static final TreeMap<Integer, List<NameEntry>> yearVowelSoundCounts = new TreeMap<>();
    private static int year = 1879;
    private static List<NameEntry> nameEntries = new ArrayList<>();
    private static final List<NameEntry> vowelNameEntries = new ArrayList<>();
    private static final List<NameEntry> fmVowelNameEntries = new ArrayList<>();
    private static final List<NameEntry> mlVowelNameEntries = new ArrayList<>();
    private static final List<NameEntry> topFNames = new ArrayList<>();
    private static final List<NameEntry> topMNames = new ArrayList<>();
    private static final Map<String, List<NameEntry>> fmYearlyTopNames = new HashMap<>();
    private static final Map<String, List<NameEntry>> mlYearlyTopNames = new HashMap<>();
    private static final Map<String, NameEntry> yearNameMap = new HashMap<>();
    private static final Map<String, NameEntry> nameMap = new HashMap<>();// Map to store unique (name, gender) pairs and their corresponding NameEntry
    private static final Map<NameEntry, Double> percentageOfTotal = new HashMap<>();
    private static int percentComplete = 0;
    private static int a;
    private static double vowelCountAsPercent;
    private static double fm;
    private static double ml;






    private static void mapPercentageOfTotal() {
        for (NameEntry nameEntry : fmVowelNameEntries) {
            percentageOfTotal.put(nameEntry, (double) Math.round(((double) nameEntry.getOccurrences() / femaleNamesCount) * 1000000) / 10000);
        }
        for (NameEntry nameEntry : mlVowelNameEntries) {
            percentageOfTotal.put(nameEntry, (double) Math.round(((double) nameEntry.getOccurrences() / maleNamesCount) * 1000000) / 10000);
        }
    }

    public static void main(String[] args) throws IOException {
        String folderPath = "src/main/resources/names";
        startTime = System.currentTimeMillis();
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



        getNamesByYear();
        combineSameNames();
        countTopNames();
        mapPercentageOfTotal();
        printResults();
        endTime = System.currentTimeMillis();
        //NameWriter.start();
        printTimeToRun();
        search();





    }
    private static void printTimeToRun() {
        long tPassed = -(startTime - endTime);
        System.out.println("Time to run " + tPassed +  " milliseconds");
    }





    private static void getNamesByYear() {
        yearVowelSoundCounts.forEach((year, entries) -> {
            // Filter entries ending with a vowel sound and sort by occurrences in descending order
            List<NameEntry> topVowelEndingNames = entries.stream()
                    .filter(NameEntry::endsWithVowelSound)
                    .filter(NameEntry::isFemale)
                    .sorted(Comparator.comparingInt(NameEntry::getOccurrences).reversed())
                    .limit(11)
                    .toList();


            if (topVowelEndingNames.isEmpty()) {
                System.out.println("  No names ending with a vowel sound.");
            } else {
                int countM = 0;
                int countF = 0;
                List<NameEntry> tempF = new ArrayList<>();
                List<NameEntry> tempM = new ArrayList<>();
                for (NameEntry entry : topVowelEndingNames) {
                    if (entry.isFemale()) {
                        tempF.add(entry);
                        countF++;
                    } else {
                        tempM.add(entry);
                        countM++;
                    }
                    if (countM % 11 == 0) {
                        mlYearlyTopNames.put(year.toString() + "_M", tempM);
                        tempM.clear();
                    }
                    if (countF % 11 == 0) {
                        fmYearlyTopNames.put(year.toString() + "_F", tempM);
                        tempF.clear();
                    }



                }
            }
        });
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
                    yearVowelSoundCounts.computeIfAbsent(year, _ -> new ArrayList<>()).add(entry);
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
        vowelCountAsPercent = (double) Math.round(((double) vowelEndingCount / totalCount) * 10000) / 100;
        fm = (double) Math.round(((double) femaleNamesCount / vowelEndingCount) * 10000) / 100;
        ml = (double) Math.round(((double) (vowelEndingCount - femaleNamesCount) / vowelEndingCount) * 10000) / 100;


        System.out.println("Total name occurrences counted: " + totalCount);
        System.out.println("Total occurrences of names ending with a vowel sound across all years: " + vowelEndingCount);
        System.out.println("Percentage of all names that end with a vowel sound: " + vowelCountAsPercent + "%");
        System.out.println("Of the names that end with a vowel sound, " + fm + "% are female and " + ml + "% are male");
        System.out.println("Top Male Names are:");
        String s = "% of all male names";
        String t = "% of all female names";
        System.out.println(topMNames.getFirst() + " - " + percentageOfTotal.get(topMNames.getFirst()) + s + "\n" + topMNames.get(1) + " - " + percentageOfTotal.get(topMNames.get(1)) + s + "\n" + topMNames.getLast() + " - " + percentageOfTotal.get(topMNames.getLast()) + s);
        System.out.println("Top Female Names are:");
        System.out.println(topFNames.getFirst() + " - " + percentageOfTotal.get(topFNames.getFirst()) + t + "\n" + topFNames.get(1) + " - " + percentageOfTotal.get(topFNames.get(1)) + t + "\n" + topFNames.getLast() + " - " + percentageOfTotal.get(topFNames.getLast()) + t);
    }

    private static String returnPercentageOfTotal(NameEntry entry) {
        if (entry.isFemale()) {
            return " - " + percentageOfTotal.get(entry) + "% of all female names";
        } else {
            return " - " + percentageOfTotal.get(entry) + "% of all male names";
        }
    }

    static String returnResults() {
        return "Total name occurrences counted: " + totalCount + "\n"
        + "\nTotal occurrences of names ending with a vowel sound across all years: " + vowelEndingCount
        + "\nPercentage of all names that end with a vowel sound: " + vowelCountAsPercent + "%"
        + "\nOf the names that end with a vowel sound, " + fm + "% are female and " + ml + "% are male"
        + "\nTop Male Names are:\n"
        + topMNames.getFirst() + "\n" + topMNames.get(1) + "\n" + topMNames.getLast()
        + "\nTop Female Names are:\n"
        + topFNames.getFirst() + "\n" + topFNames.get(1) + "\n" + topFNames.getLast();
    }

    private static void search() {
        Scanner input = new Scanner(System.in);
        int count = 0;
        while (true) {
            System.out.print("Search for a name or top names from a year. Type exit to leave. ");
            System.out.print("Format is NAME_GENDER or YYYY_GENDER ex.Tessa_F or 2023_F:");
            String name = input.nextLine();
            if (name.equalsIgnoreCase("exit")) {
                break;
            }
            if (name.startsWith("2") || name.startsWith("1")) {
                System.out.println("Listing top " + name.charAt(5) + " names from " + name.substring(0, 4));
                if (name.endsWith("M")) {
                    for (NameEntry entry : mlYearlyTopNames.get(name)) {
                        count++;
                        System.out.println(count + ". " + entry + returnPercentageOfTotal(entry));
                    }
                    count = 0;
                } else if (name.endsWith("F")) {
                    for (NameEntry entry : fmYearlyTopNames.get(name)) {
                        count++;
                        System.out.println(count + ". " + entry + returnPercentageOfTotal(entry));
                    }
                    count = 0;
                } else {
                    System.out.println("Invalid format. Correct Format: YYYY_Gender (2023_F)");
                }
            } else if (name.contains("1") || name.contains("2")) {

                if (yearNameMap.containsKey(name)) {
                    System.out.println(yearNameMap.get(name));
                } else {
                    System.out.println("Name not found - either you made a typo or there were less than five people with that name that year");
                }
            }  else {
                if (nameMap.containsKey(name)) {
                    System.out.println(nameMap.get(name));
                } else {
                    System.out.println("Name not found - either you made a typo or there are less than five people with that name in the US.");
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
            maleNamesCount += entry.getOccurrences();
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
}
