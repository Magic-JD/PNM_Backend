package org.picnmix.max;

import org.picnmix.FileAccess;
import org.picnmix.max.data.PathedMatch;
import org.picnmix.max.data.Match;
import org.picnmix.max.strategy.FindPathsForMatches;

import java.util.*;

import static org.picnmix.max.CombosToString.convertToOutputString;
import static org.picnmix.max.FindMatches.getMatches;
import static org.picnmix.max.WordMappingCreator.createEncodeToWordsMapping;


public class MaxPerWordImpl implements MaxPerWord {


    private final FileAccess fileAccess;
    private final FindPathsForMatches findPathsForMatches;

    public MaxPerWordImpl(FileAccess fileAccess, FindPathsForMatches findPathsForMatches){
        this.fileAccess = fileAccess;
        this.findPathsForMatches = findPathsForMatches;
    }

    /**
     * This method reads a list of 5 letter words from a file, removes the ones with repeating characters, and then
     * converts them into a mapping of integers list of strings. The integers are an encoding of the word, where:
     * a = 1 << 0
     * b = 1 << 1
     * c = 1 << 2
     * ...
     * added together.
     * <p>
     * Example:
     * "ABCDE" = ...00011111
     * "CDEFG" = ...01111100
     * The mappings is needed to know the number of anagrams for each word, and to convert it back to the actual strings
     * when returning the values.
     * <p>
     * Next, all possible start and end words are found. These are the encoded words that have no letters in common.
     * Each of these pairs are then converted to pathed matches - a pathed match is the start and end non encoded string
     * with the number of possible paths between the two words. There are a number of different strategies tried for this.
     * <p>
     * Finally the output is converted to a string and then written to a file.
     */
    public void calculate() {
        List<String> words = fileAccess.extractWordsFromFile();
        Map<Integer, List<String>> encodeToWords = createEncodeToWordsMapping(words);
        List<Integer> encoded = new ArrayList<>(encodeToWords.keySet());
        List<Match> matches = getMatches(encoded);
        List<PathedMatch> comboList = findPathsForMatches.getPathsForMatches(matches, encodeToWords, encoded);
        fileAccess.writeToOutput(convertToOutputString(comboList));
    }
}
