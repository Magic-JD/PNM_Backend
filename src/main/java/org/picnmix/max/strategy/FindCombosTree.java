package org.picnmix.max.strategy;

import org.picnmix.max.Cache;
import org.picnmix.max.data.Combos;
import org.picnmix.max.data.Match;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FindCombosTree implements FindCombos {

    private final Cache cache = new Cache();

    @Override
    public List<Combos> getCombos(List<Match> matches, Map<Integer, List<String>> encodingToWords, List<Integer> encoded) {
        Node node = new Node();
        encoded.forEach(node::addValue);
        return matches.parallelStream().flatMap(match -> getCombinationsForMatch(encodingToWords, match, node)).toList();
    }

    private Stream<Combos> getCombinationsForMatch(Map<Integer, List<String>> encodingToWords, Match match, Node node) {
        int firstWord = match.first();
        int secondWord = match.second();
        int sum = node.getValues(firstWord, secondWord).stream()
                .mapToInt(i -> recursivelyCalculateCombinations(i, secondWord, encodingToWords, node))
                .sum();
        return convertEncodingToCombos(encodingToWords, firstWord, secondWord, sum);
    }

    private int recursivelyCalculateCombinations(int firstWord, int secondWord, Map<Integer, List<String>> encodingToWords, Node node) {
        if (firstWord == secondWord) { // This is our base case - we have found one valid combination.
            return 1;
        }
        int sum = cache.getOrCalculate(firstWord, secondWord, () -> ifNotFound(firstWord, secondWord, encodingToWords, node));
        return sum * encodingToWords.get(firstWord).size();
    }

    private int ifNotFound(int firstWord, int secondWord, Map<Integer, List<String>> encodingToWords, Node node) {
        return node.getValues(firstWord, secondWord).stream()
                .mapToInt(i -> recursivelyCalculateCombinations(i, secondWord, encodingToWords, node))
                .sum();
    }
}
