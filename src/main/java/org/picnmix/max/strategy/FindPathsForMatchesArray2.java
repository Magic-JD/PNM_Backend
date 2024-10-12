package org.picnmix.max.strategy;

import org.picnmix.max.Cache;
import org.picnmix.max.data.PathedMatch;
import org.picnmix.max.data.Match;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.ThreadLocal.withInitial;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;

public class FindPathsForMatchesArray2 implements FindPathsForMatches {

    public static final int NUMBER_OF_LEVELS = 4; // 0th indexed the number of levels we have to check
    private final Cache cache = new Cache();

    @Override
    public List<PathedMatch> getPathsForMatches(List<Match> matches, Map<Integer, List<String>> encodingToWords, List<Integer> encoded) {
        Map<Integer, List<Match>> collect = matches.stream().collect(groupingBy(match -> match.first() | match.second()));
        ThreadLocal<int[]> localArray = withInitial(() -> encoded.stream().mapToInt(Integer::intValue).toArray());
        return collect.entrySet().stream().sorted(comparingInt(Map.Entry::getKey)).parallel().flatMap(entry -> {
            int[] encodedArray = localArray.get();
            int lastValidIndex = sortNeeded(encodedArray, encodedArray.length - 1, entry.getKey());
            return entry.getValue().stream().flatMap(match -> getPathsForMatch(encodingToWords, encodedArray, lastValidIndex, match));
        }).toList();
    }

    private Stream<PathedMatch> getPathsForMatch(Map<Integer, List<String>> encodingToWords, int[] encoded, int index, Match match) {
        int firstWord = match.first();
        int secondWord = match.second();
        int paths = calculatePaths(firstWord, secondWord, encoded, index, encodingToWords);
        if (paths == 0) {
            return Stream.empty();
        }
        return convertEncodedPathsToWordValues(encodingToWords, firstWord, secondWord, paths);
    }

    private Integer calculatePaths(int firstWord, int secondWord, int[] encoded, int lastValidIndex, Map<Integer, List<String>> encodingToWords) {
        // Used to store the state while iterating to prevent recursive calls.
        int[] firstIndexPointers = new int[NUMBER_OF_LEVELS];
        int[] currentIndexPointers = new int[NUMBER_OF_LEVELS];
        int[] lastIndexForPosition = new int[NUMBER_OF_LEVELS];
        int[] currentPathsForPosition = new int[NUMBER_OF_LEVELS]; // Already correctly initialized to 0
        int pointer = NUMBER_OF_LEVELS - 1;
        int firstIndexForNextWord = sortNext(encoded, lastValidIndex, firstWord); // Index of the first word that is a next word.
        currentIndexPointers[pointer] = firstIndexForNextWord;
        firstIndexPointers[pointer] = firstIndexForNextWord;
        lastIndexForPosition[pointer] = lastValidIndex;
        while (true) { // keep looping until all conditions are checked and the loop is broken.
            if (currentIndexPointers[pointer] > lastIndexForPosition[pointer]) {
                pointer++; // Have checked all valid words for this level
                if (pointer == NUMBER_OF_LEVELS) { // If we have checked all values break out of the loop;
                    break;
                }
                int pathsForWord = currentPathsForPosition[pointer - 1];
                cache.put(encoded[currentIndexPointers[pointer]], secondWord, pathsForWord);
                currentPathsForPosition[pointer] += pathsForWord * encodingToWords.get(encoded[currentIndexPointers[pointer]]).size();
                currentPathsForPosition[pointer-1] = 0;
                currentIndexPointers[pointer]++;
                continue; // Back up one level and continue with the next words.
            }
            int nextWord = encoded[currentIndexPointers[pointer]]; // get the next word at the current level.
            Integer currentValue = cache.get(nextWord, secondWord);
            if(currentValue != null){ // Check cache
                currentPathsForPosition[pointer] += currentValue * encodingToWords.get(nextWord).size();
                currentIndexPointers[pointer]++;
                continue;
            }
            pointer--; // Decrease the pointer to go to the next level
            if (pointer == -1) { // We are one letter away from the goal word, so we can stop now.
                pointer++;
                currentPathsForPosition[pointer] += encodingToWords.get(encoded[currentIndexPointers[pointer]]).size();
                currentIndexPointers[pointer]++;
                continue; // Back up one level and continue with the next words
            }
            int lastValidIndexForLevel = sortNeeded(encoded, firstIndexPointers[pointer + 1] - 1, nextWord | secondWord, nextWord, secondWord);
            lastIndexForPosition[pointer] = lastValidIndexForLevel;
            currentIndexPointers[pointer] = sortNext(encoded, lastValidIndexForLevel, nextWord);
            firstIndexPointers[pointer] = currentIndexPointers[pointer]; // Store the values so that we can check again as we come up.
        }
        return currentPathsForPosition[NUMBER_OF_LEVELS-1];
    }

    /**
     * @param encoded The array to be sorted: WARNING - MUTATES ARRAY!!!!
     * @param end             the point to sort up to in the array.
     * @param firstWord       the word we are going from
     * @return the index of the first word in the list that is a next word.
     */
    private int sortNext(int[] encoded, int end, int firstWord) {
        int start = 0;
        while (start <= end) {
            while (end >= start && Integer.bitCount(encoded[end] ^ firstWord) == 2) {
                end--;
            }
            while (start <= end && Integer.bitCount(encoded[start] ^ firstWord) != 2) {
                start++;
            }
            if (start < end) {
                int possibleMatch = encoded[start];
                encoded[start] = encoded[end];
                encoded[end] = possibleMatch;
                end--;
                start++;
            }
        }
        return end + 1;
    }


    private int sortNeeded(int[] encoded, int index, int comboBits, int firstWord, int secondWord) {
        //Remove all words that have letters that are not in the first or second word.
        int start = 0;
        int end = index;
        int sharedBits = Integer.bitCount(secondWord & firstWord); // The number of bits that the first and last word have in common.
        while (start <= end) {
            int i = encoded[end];
            while ((end >= start && (i | comboBits) != comboBits) || (end >= start && Integer.bitCount(secondWord & i) <= sharedBits)) {
                end--;
                i = encoded[end];
            }
            i = encoded[start];
            while (start <= end && (encoded[start] | comboBits) == comboBits && Integer.bitCount(secondWord & i) > sharedBits) {
                start++;
                i = encoded[start];
            }
            if (start < end) {
                int current = encoded[end];
                encoded[end] = encoded[start];
                encoded[start] = current;
                start++;
                end--;
            }
        }
        // Return the last valid index.
        return end;
    }


    private int sortNeeded(int[] encoded, int index, int comboBits) {
        //Remove all words that have letters that are not in the first or second word.
        int start = 0;
        int end = index;
        while (start <= end) {
            while ((end >= start && (encoded[end] | comboBits) != comboBits)) {
                end--;
            }
            while (start <= end && (encoded[start] | comboBits) == comboBits) {
                start++;
            }
            if (start < end) {
                int current = encoded[end];
                encoded[end] = encoded[start];
                encoded[start] = current;
                start++;
                end--;
            }
        }
        // Return the last valid index.
        return end;
    }


}
