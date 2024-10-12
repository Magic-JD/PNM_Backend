package org.picnmix.max;

import org.picnmix.max.data.Combos;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CombosToString {

    public static String convertToOutput(List<Combos> comboList) {
        return comboList.stream()
                .filter(combos -> combos.combinations() > 0)
                .sorted(Comparator.comparingInt(Combos::combinations).thenComparing(Combos::firstWord).thenComparing(Combos::secondWord))
                .map(c -> STR. "START: \{ c.firstWord() } END: \{ c.secondWord() } DIFFICULTY: \{ c.combinations() }\n" )
                .collect(Collectors.joining());
    }
}
