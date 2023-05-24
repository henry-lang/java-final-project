package scrabble;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;

public class Dictionary {
    private final HashSet<String> words = new HashSet<>();

    public static Dictionary loadFromFile(String resourceName) throws IOException {
        var dictionary = new Dictionary();

        var br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Dictionary.class.getResourceAsStream(resourceName))));
        var word = br.readLine();
        while (word != null) {
            dictionary.words.add(word);
            word = br.readLine();
        }

        return dictionary;
    }

    public boolean contains(String word) {
        return words.contains(word);
    }
    public int size() {
        return words.size();
    }
}
