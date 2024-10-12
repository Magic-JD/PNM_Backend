package org.picnmix.max.strategy;

import org.picnmix.max.data.Combos;
import org.picnmix.max.data.Match;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface FindCombos {
    List<Combos> getCombos(List<Match> matches, Map<Integer, List<String>> encodingToWords, List<Integer> encoded);


    default Stream<Combos> convertEncodingToCombos(Map<Integer, List<String>> encodingToWords, int firstWord, int secondWord, int numberOfCombos) {
        return encodingToWords.get(firstWord).stream()
                .flatMap(w1 -> encodingToWords.get(secondWord).stream()
                        .map(w2 -> new Combos(numberOfCombos, w1, w2)));
    }
}
