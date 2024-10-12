package org.picnmix.max;

import org.picnmix.max.data.PathedMatch;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;

public class CombosToString {

    public static String convertToOutputString(List<PathedMatch> comboList) {
        return comboList.stream()
                .filter(combos -> combos.combinations() > 0)
                .sorted(comparingInt(PathedMatch::combinations).thenComparing(PathedMatch::firstWord).thenComparing(PathedMatch::secondWord))
                .map(c -> STR. "START: \{ c.firstWord() } END: \{ c.secondWord() } DIFFICULTY: \{ c.combinations() }\n" )
                .collect(joining());
    }
}
