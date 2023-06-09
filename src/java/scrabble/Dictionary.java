package scrabble;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;

// This class is an ADT representing a dictionary object, which can load itself from a file and check if it contains a word
// Theoretically, this could be used to add multiple dictionaries for different languages or versions of the game
public class Dictionary {
    // The words of the dictionary
    private final HashSet<String> words = new HashSet<>();

    // Loads the dictionary line by line from a file resourceName
    public static Dictionary loadFromFile(String resourceName) throws IOException {
        Dictionary dictionary = new Dictionary();

        // Actually open the file from an input stream corresponding to the file name passed in
        BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Dictionary.class.getResourceAsStream(resourceName))));
        String word = br.readLine();
        while (word != null) {
            // Add the word to the dictionary
            dictionary.words.add(word);
            word = br.readLine();
        }

        return dictionary;
    }

    // Check if the dictionary contains this word
    public boolean contains(String word) {
        return words.contains(word.toLowerCase());
    }

    // Return the size of the dictionary
    public int size() {
        return words.size();
    }
}
