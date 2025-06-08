package org.picnmix.max;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordMappingCreator {
    
    private WordMappingCreator(){
        //Empty for utility class
    }

    public static Map<Integer, List<String>> createEncodeToWordsMapping(List<String> stringWords) {
        Map<Integer, List<String>> encodingToWords = new HashMap<>();
        for (String s : stringWords) {
            char[] chars = s.toCharArray();
            // This will become a binary representation of the word, with the character a being 1, b being 10 and so on.
            int sum = 0;
            int or = 0;
            for (char c : chars) {
                c -= 'A';
                int shift = 1 << c;
                sum += shift;
                or |= shift;
            }
            if (sum != or) {
                // This word has multiple identical characters
                continue;
            }
            List<String> anagrams = encodingToWords.computeIfAbsent(sum, (_) -> new ArrayList<>());
            anagrams.add(s);
        }
        return encodingToWords;
    }
}
