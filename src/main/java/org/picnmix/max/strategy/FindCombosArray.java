package org.picnmix.max.strategy;

import org.picnmix.max.Cache;
import org.picnmix.max.data.Combos;
import org.picnmix.max.data.Match;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindCombosArray implements FindCombos {

    private final Cache cache = new Cache();

    @Override
    public List<Combos> getCombos(List<Match> matches, Map<Integer, List<String>> encodingToWords, List<Integer> encoded) {
        Map<Integer, List<Match>> collect = matches.stream().collect(Collectors.groupingBy(match -> match.first() | match.second()));
        ThreadLocal<int[]> localArray = ThreadLocal.withInitial(() -> encoded.stream().mapToInt(Integer::intValue).toArray());
        return collect.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).parallel().flatMap(entry -> {
            int[] encodedArr = localArray.get();
            int index = sortNeeded(encodedArr, encodedArr.length - 1, entry.getKey());
            return entry.getValue().stream().flatMap(match -> getCombinationsForMatch(encodingToWords, encodedArr, index, match));
        }).toList();
    }

    private Stream<Combos> getCombinationsForMatch(Map<Integer, List<String>> encodingToWords, int[] encoded, int index, Match match) {
        int firstWord = match.first();
        int secondWord = match.second();
        int sum = calculateValue(firstWord, secondWord, encoded, index, encodingToWords);
        if (sum == 0) {
            return Stream.empty();
        }
        return convertEncodingToCombos(encodingToWords, firstWord, secondWord, sum);
    }

    private Integer calculateValue(int firstWord, int secondWord, int[] encoded, int index, Map<Integer, List<String>> encodingToWords) {
        int comboBits = firstWord | secondWord;
        if(Integer.bitCount(comboBits) == 6){ // When one away there is no way
            return 1;
        }
        int lastValid = sortNeeded(encoded, index, comboBits, firstWord, secondWord);
        if (lastValid < 0) { // There are no valid words left
            return 0;
        }
        int firstNext = sortNext(encoded, lastValid, firstWord); // Index of the first word that is a next word.
        if (firstNext == 0) { //The first word in the array is always the goal word by the end, return 1
            return 1;
        }
        int sum = 0;
        for (int i = firstNext; i <= lastValid; i++) { // If there are no next words then first next is > lastValid so the loop will be skipped and we just return.
            int nextWord = encoded[i];
            sum += cache.getOrCalculate(nextWord, secondWord, () -> calculateValue(nextWord, secondWord, encoded, firstNext - 1, encodingToWords)) * encodingToWords.get(nextWord).size();
        }
        return sum;
    }

    /**
     * @param possibleMatches The array to be sorted: WARNING - MUTATES ARRAY!!!!
     * @param end             the point to sort up to in the array.
     * @param firstWord       the word we are going from
     * @return the index of the first word in the list that is a next word.
     */
    private int sortNext(int[] possibleMatches, int end, int firstWord) {
        int start = 0;
        while (start <= end) {
            while (end >= start && Integer.bitCount(possibleMatches[end] ^ firstWord) == 2) {

                end--;
            }
            while (start <= end && Integer.bitCount(possibleMatches[start] ^ firstWord) != 2) {

                start++;
            }
            if (start < end) {
                int possibleMatch = possibleMatches[start];
                possibleMatches[start] = possibleMatches[end];
                possibleMatches[end] = possibleMatch;
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
            while ((end >= start && (encoded[end] | comboBits) != comboBits) || (end >= start && Integer.bitCount(secondWord & encoded[end]) <= sharedBits)) {
                end--;
            }
            while (start <= end && (encoded[start] | comboBits) == comboBits && Integer.bitCount(secondWord & encoded[start]) > sharedBits) {
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
