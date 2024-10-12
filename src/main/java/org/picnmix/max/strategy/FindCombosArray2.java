package org.picnmix.max.strategy;

import org.picnmix.max.Cache;
import org.picnmix.max.data.Combos;
import org.picnmix.max.data.Match;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindCombosArray2 implements FindCombos {

    public static final int NUMBER_OF_LEVELS = 4;
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
        int[] firstIndexPointers = new int[NUMBER_OF_LEVELS];
        int[] currentIndexPointers = new int[NUMBER_OF_LEVELS];
        int[] lastIndexForPosition = new int[NUMBER_OF_LEVELS];
        int[] currentSumForPosition = new int[NUMBER_OF_LEVELS];
        int pointer = NUMBER_OF_LEVELS - 1;
        int firstNext = sortNext(encoded, index, firstWord); // Index of the first word that is a next word.
        currentIndexPointers[pointer] = firstNext;
        firstIndexPointers[pointer] = firstNext;
        lastIndexForPosition[pointer] = index;
        while (true) { // keep looping until all conditions are checked and the loop is broken.
            if (currentIndexPointers[pointer] > lastIndexForPosition[pointer]) {
                pointer++; // Have checked all subvalues here
                if (pointer == NUMBER_OF_LEVELS) { // If we have checked all values break out of the loop;
                    break;
                }
                int sizeForWord = currentSumForPosition[pointer - 1];
                cache.put(encoded[currentIndexPointers[pointer]], secondWord, sizeForWord);
                currentSumForPosition[pointer] += sizeForWord * encodingToWords.get(encoded[currentIndexPointers[pointer]]).size();
                currentSumForPosition[pointer-1] = 0;
                currentIndexPointers[pointer]++;
                continue; // Back up one level and continue with the next words.
            }
            int nextWord = encoded[currentIndexPointers[pointer]]; // get the next word at the current level.
            Integer currentValue = cache.get(nextWord, secondWord);
            if(currentValue != null){
                currentSumForPosition[pointer] += currentValue * encodingToWords.get(nextWord).size();
                currentIndexPointers[pointer]++;
                continue;
            }
            pointer--;
            if (pointer == -1) {
                pointer++;
                currentSumForPosition[pointer] += encodingToWords.get(encoded[currentIndexPointers[pointer]]).size();
                currentIndexPointers[pointer]++;
                continue; // Back up one level and continue with the next words
            }
            int currentIndex = sortNeeded(encoded, firstIndexPointers[pointer + 1] - 1, nextWord | secondWord, nextWord, secondWord);
            lastIndexForPosition[pointer] = currentIndex;
            currentIndexPointers[pointer] = sortNext(encoded, currentIndex, nextWord);
            firstIndexPointers[pointer] = currentIndexPointers[pointer];
        }
        return currentSumForPosition[NUMBER_OF_LEVELS-1];
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
