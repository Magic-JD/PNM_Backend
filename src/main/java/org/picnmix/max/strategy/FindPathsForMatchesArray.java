package org.picnmix.max.strategy;

import org.picnmix.max.Cache;
import org.picnmix.max.data.PathedMatch;
import org.picnmix.max.data.Match;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Integer.bitCount;
import static java.lang.ThreadLocal.withInitial;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;

public class FindPathsForMatchesArray implements FindPathsForMatches {

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

    private Stream<PathedMatch> getPathsForMatch(Map<Integer, List<String>> encodingToWords, int[] encoded, int lastValidIndex, Match match) {
        int firstWord = match.first();
        int secondWord = match.second();
        int numberOfPaths = calculateNumberOfPaths(firstWord, secondWord, encoded, lastValidIndex, encodingToWords);
        if (numberOfPaths == 0) {
            return Stream.empty();
        }
        return convertEncodedPathsToWordValues(encodingToWords, firstWord, secondWord, numberOfPaths);
    }

    private Integer calculateNumberOfPaths(int firstWord, int secondWord, int[] encoded, int lastValidIndex, Map<Integer, List<String>> encodingToWords) {
        int comboBits = firstWord | secondWord;
        if(bitCount(comboBits) == 6){ // When one away there is no way
            return 1;
        }
        int updatedLastValidIndex = sortNeeded(encoded, lastValidIndex, comboBits, firstWord, secondWord);
        if (updatedLastValidIndex < 0) { // There are no valid words left
            return 0;
        }
        int indexOfFirstPotentialNext = sortNext(encoded, updatedLastValidIndex, firstWord); // Index of the first word that is a next word.
        if (indexOfFirstPotentialNext == 0) { //The first word in the array is always the goal word by the end, return 1
            return 1;
        }
        int countPaths = 0;
        for (int i = indexOfFirstPotentialNext; i <= updatedLastValidIndex; i++) {
            // If there are no next words then first next is > lastValid so the loop will be skipped and we just return.
            int nextWord = encoded[i];
            countPaths += cache.getOrCalculate(nextWord, secondWord, () -> calculateNumberOfPaths(nextWord, secondWord, encoded, indexOfFirstPotentialNext - 1, encodingToWords)) * encodingToWords.get(nextWord).size();
        }
        return countPaths;
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
            while (end >= start && bitCount(encoded[end] ^ firstWord) == 2) {

                end--;
            }
            while (start <= end && bitCount(encoded[start] ^ firstWord) != 2) {

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
        int sharedBits = bitCount(secondWord & firstWord); // The number of bits that the first and last word have in common.
        while (start <= end) {
            while ((end >= start && (encoded[end] | comboBits) != comboBits) || (end >= start && bitCount(secondWord & encoded[end]) <= sharedBits)) {
                end--;
            }
            while (start <= end && (encoded[start] | comboBits) == comboBits && bitCount(secondWord & encoded[start]) > sharedBits) {
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
