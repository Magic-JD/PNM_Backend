package org.picnmix.max.strategy;

import org.picnmix.max.data.PathedMatch;
import org.picnmix.max.data.Match;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface FindPathsForMatches {
    List<PathedMatch> getPathsForMatches(List<Match> matches, Map<Integer, List<String>> encodingToWords, List<Integer> encoded);


    default Stream<PathedMatch> convertEncodedPathsToWordValues(Map<Integer, List<String>> encodingToWords, int firstWord, int secondWord, int numberOfCombos) {
        return encodingToWords.get(firstWord).stream()
                .flatMap(w1 -> encodingToWords.get(secondWord).stream()
                        .map(w2 -> new PathedMatch(numberOfCombos, w1, w2)));
    }
}
