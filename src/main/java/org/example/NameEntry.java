package org.example;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NameEntry {

    private String name;
    private final Gender gender;
    private int occurrences;
    private int year;



    public NameEntry(String name, Gender gender, int occurrences, int y) {
        this.name = name;
        this.gender = gender;
        this.occurrences = occurrences;
        year = y;
    }

    public boolean endsWithVowelSound() {
        String lowerName = name.toLowerCase();
        char lastChar = Character.toLowerCase(name.charAt(name.length() - 1));

        if (lastChar == 'e' && !lowerName.endsWith("ie")) {
            NameReader.addFakeVowelSound();
            //System.out.println("Found names ending with a fake vowel: " + name + " with " + occurrences + " occurrences.");
            return false;
        }

        return lastChar == 'a' || lastChar == 'e' || lastChar == 'i' || lastChar == 'o' || lastChar == 'u' || lastChar == 'y';
    }


    @Override
    public String toString() {
        return "NameEntry{name='" + name + "', gender=" + gender + ", occurrences=" + occurrences + '}';
    }

    public boolean isFemale() {
        return this.gender == Gender.F;
    }

    public void addOccurrences(int add) {
        occurrences += add;
    }
}

