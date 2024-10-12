package org.picnmix.max;

import org.picnmix.max.data.Match;

import java.util.ArrayList;
import java.util.List;

public class FindMatches {

    public static List<Match> getMatches(List<Integer> encoded) {
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < encoded.size(); i++) {
            int firstWord = encoded.get(i);
            for (int j = i + 1; j < encoded.size(); j++) {
                int secondWord = encoded.get(j);
                if ((firstWord & secondWord) == 0) {
                    matches.add(new Match(firstWord, secondWord));
                }
            }
        }
        return matches;
    }
}
