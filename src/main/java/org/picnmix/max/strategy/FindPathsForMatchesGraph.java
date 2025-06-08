package org.picnmix.max.strategy;

import org.picnmix.max.Cache;
import org.picnmix.max.data.PathedMatch;
import org.picnmix.max.data.Match;

import java.util.*;

import static java.lang.Integer.bitCount;

public class FindPathsForMatchesGraph implements FindPathsForMatches {


    private final Cache cache = new Cache();
    private Map<Integer, List<String>> etw;
    private Map<Integer, List<Integer>> graph;

    @Override
    public List<PathedMatch> getPathsForMatches(List<Match> matches, Map<Integer, List<String>> encodingToWords, List<Integer> encoded) {
        etw = encodingToWords;
        Map<Integer, List<Integer>> ints = new HashMap<>();
        encoded.forEach(e -> ints.put(e, new ArrayList<>()));
        for(int i = 0; i < encoded.size(); i++){
            for (int j = i+1; j < encoded.size(); j++) {
                Integer na = encoded.get(i);
                Integer nb = encoded.get(j);

                if (bitCount(na ^ nb) == 2){
                    ints.get(na).add(nb);
                    ints.get(nb).add(na);
                }
            }
        }
        graph = ints;


        List<PathedMatch> list = matches.parallelStream().flatMap(c -> {
            int num_comb = calculateCombo(c.first(), c.second());
            var firstWords = encodingToWords.get(c.first());
            var secondWords = encodingToWords.get(c.second());
            List<PathedMatch> pathedMatches = new ArrayList<>();
            for (String sa : firstWords) {
                for (String sb : secondWords) {
                    pathedMatches.add(new PathedMatch(num_comb, sa, sb));
                }
            }
            return pathedMatches.stream();
        }).toList();
        return list;

    }

    private int calculateCombo(int na, int nb) {
        return cache.getOrCalculate(na, nb, () -> calcFunc(na, nb));
    }

    private int calculateComboR(int na, int nb) {
        if(bitCount(na ^ nb) == 2){
            return etw.get(na).size();
        }
        return cache.getOrCalculate(na, nb, () -> calcFunc(na, nb)) * etw.get(na).size();
    }


    private int calcFunc(int na, int nb) {
        int curr = bitCount(na ^ nb);

        List<Integer> integers = graph.get(na);
        return integers.stream()
                .filter(n -> bitCount(n ^ nb) < curr)
                .map(n -> calculateComboR(n, nb))
                .mapToInt(i -> i).sum();

    }
}

