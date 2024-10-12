package org.picnmix.max.strategy;

import org.picnmix.max.Cache;
import org.picnmix.max.data.PathedMatch;
import org.picnmix.max.data.Match;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class FindPathsForMatchesList implements FindPathsForMatches {

    private Cache cache = new Cache();

    @Override
    public List<PathedMatch> getPathsForMatches(List<Match> matches, Map<Integer, List<String>> encodingToWords, List<Integer> encoded) {
        Map<Integer, List<Integer>> comboMapping = new ConcurrentHashMap<>();
        return matches.parallelStream().flatMap(match -> getCombinationsForMatch(comboMapping, encodingToWords, encoded, match)).toList();
    }

    private Stream<PathedMatch> getCombinationsForMatch(Map<Integer, List<Integer>> comboMapping, Map<Integer, List<String>> encodingToWords, List<Integer> encoded, Match match) {
        int firstWord = match.first();
        int secondWord = match.second();
        int comboBits = firstWord | secondWord;
        List<Integer> possibleMatches = comboMapping.computeIfAbsent(comboBits, _ -> encoded.stream().filter(i -> (i | comboBits) == comboBits).toList());
        int numberOfCombos = possibleMatches.stream()
                .mapToInt(i -> i)
                .filter(i -> isNextWord(firstWord, secondWord, i))
                .map(i -> recursivelyCalculateCombinations(i, secondWord, possibleMatches, encodingToWords) * encodingToWords.get(i).size())
                .sum();
        if (numberOfCombos == 0) {
            return Stream.empty();
        }
        return convertEncodedPathsToWordValues(encodingToWords, firstWord, secondWord, numberOfCombos);
    }



    private int recursivelyCalculateCombinations(int firstWord, int secondWord, List<Integer> encoded, Map<Integer, List<String>> encodingToWords) {
        if (firstWord == secondWord) {
            return 1;
        }

        return cache.getOrCalculate(firstWord, secondWord, () -> calculateValue(firstWord, secondWord, encoded, encodingToWords)) * encodingToWords.get(firstWord).size();
    }

    private int calculateValue(int firstWord, int secondWord, List<Integer> encoded, Map<Integer, List<String>> encodingToWords) {
        int comboBits = firstWord | secondWord;
        //Remove all words that have letters that are not in the first or second word.
        List<Integer> possibleMatches = encoded.stream().filter(i -> (i | comboBits) == comboBits).toList();
        return possibleMatches.stream()
                .mapToInt(i -> i)
                .filter(i -> isNextWord(firstWord, secondWord, i))
                .map(i -> recursivelyCalculateCombinations(i, secondWord, possibleMatches, encodingToWords))
                .sum();
    }


    private boolean isNextWord(int firstWord, int secondWord, int middleWord) {
        int comboBits = (middleWord | firstWord);
        int remainForward = comboBits ^ firstWord;
        int remainBackwards = comboBits ^ middleWord;

        boolean oneLetterDifference = (remainForward & (remainForward - 1)) == 0 && remainForward != 0 ;
        boolean addedLetterPresent = (secondWord | remainForward) == secondWord;
        boolean removedLetterNotPresent = (secondWord | remainBackwards) != secondWord;

        return oneLetterDifference && addedLetterPresent && removedLetterNotPresent;
    }
}
