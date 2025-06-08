package org.picnmix.max.strategy;

import org.picnmix.max.Cache;
import org.picnmix.max.data.PathedMatch;
import org.picnmix.max.data.Match;

import java.util.*;

import static java.lang.Integer.bitCount;

public class FindPathsForMatchesGraph implements FindPathsForMatches {


    private final Cache cache = new Cache();
    private Map<Integer, List<String>> encodingToWords;
    private Map<Integer, int[]> graph;

    @Override
    public List<PathedMatch> getPathsForMatches(List<Match> matches, Map<Integer, List<String>> encodingToWords, List<Integer> encoded) {
        this.encodingToWords = encodingToWords;
        graph = new HashMap<>();
        encoded.forEach(e -> graph.put(e, new int[5]));
        for(int i = 0; i < encoded.size(); i++){
            for (int j = i+1; j < encoded.size(); j++) {
                Integer na = encoded.get(i);
                Integer nb = encoded.get(j);

                int switchedBits = na ^ nb;
                if (bitCount(switchedBits) == 2){
                    updateGraph(graph, na, switchedBits, nb);
                    updateGraph(graph, nb, switchedBits, na);
                }
            }
        }
        return matches.stream().parallel().flatMap(combination -> {
            int start = combination.first();
            int end = combination.second();
            int numberOfCombinations = cache.getOrCalculate(start, end, () -> calcFunc(start, end));
            var firstWords = encodingToWords.get(combination.first());
            var secondWords = encodingToWords.get(combination.second());
            List<PathedMatch> pathedMatches = new ArrayList<>();
            for (String firstWord : firstWords) {
                for (String secondWord : secondWords) {
                    pathedMatches.add(new PathedMatch(numberOfCombinations, firstWord, secondWord));
                }
            }
            return pathedMatches.stream();
        }).toList();

    }

    private static void updateGraph(Map<Integer, int[]> graph, Integer first, int switchedBits, Integer second) {
        int[] nodeArr = graph.get(first);
        int toBit = switchedBits & ~first;
        int fromBit = switchedBits & ~second;
        int idx = getSetBitIndex(first, fromBit);
        nodeArr[idx] |= toBit;
    }

    private int calculateComboR(int current, int end) {
        if(bitCount(current ^ end) == 2){
            return encodingToWords.get(current).size();
        }
        return cache.getOrCalculate(current, end, () -> calcFunc(current, end)) * encodingToWords.get(current).size();
    }


    private int calcFunc(int current, int end) {
        int count = 0;
        int[] neighbourEncoding = graph.get(current);
        int bitsThatCanChange = current & ~end;
        int bitsToChangeTo = end & ~current;
        while(bitsThatCanChange != 0){
            int mutableBitsToChangeTo = bitsToChangeTo;
            int highestChangeableBit = Integer.highestOneBit(bitsThatCanChange);
            int validNeighbourChangedBits = neighbourEncoding[getSetBitIndex(current, highestChangeableBit)];
            while(mutableBitsToChangeTo != 0){
                int highestBitToCheck = Integer.highestOneBit(mutableBitsToChangeTo);
                if((validNeighbourChangedBits | highestBitToCheck) == validNeighbourChangedBits){
                    int nextCurrent = current ^ (highestChangeableBit | highestBitToCheck);
                    count += calculateComboR(nextCurrent, end);
                }
                mutableBitsToChangeTo = mutableBitsToChangeTo ^ highestBitToCheck;
            }
            bitsThatCanChange = bitsThatCanChange ^ highestChangeableBit;

        }
        return count;
    }

    private static int getSetBitIndex(int number, int bit) {
        int count = 0;
        while (number != 0){
            int highest = Integer.highestOneBit(number);
            if (highest == bit) {
                return count;
            }
            number = number ^ highest;
            count++;
        }
        throw new IllegalArgumentException("Not enough set bits");
    }
}

