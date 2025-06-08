package org.picnmix.max.strategy;

import org.picnmix.max.Cache;
import org.picnmix.max.data.PathedMatch;
import org.picnmix.max.data.Match;

import java.util.*;

import static java.lang.Integer.bitCount;
import static java.lang.Integer.lowestOneBit;

public class FindPathsForMatchesGraph implements FindPathsForMatches {


    private final Cache cache = new Cache();
    private Map<Integer, int[]> graph;

    @Override
    public List<PathedMatch> getPathsForMatches(List<Match> matches, Map<Integer, List<String>> encodingToWords, List<Integer> encoded) {
        graph = new HashMap<>();
        encoded.forEach(e -> graph.put(e, new int[]{0, 0, 0, 0, 0, encodingToWords.get(e).size()}));
        for(int i = 0; i < encoded.size(); i++){
            for (int j = i+1; j < encoded.size(); j++) {
                Integer first = encoded.get(i);
                Integer second = encoded.get(j);

                int switchedBits = first ^ second;
                if (bitCount(switchedBits) == 2){
                    updateGraph(graph, first, switchedBits, second);
                    updateGraph(graph, second, switchedBits, first);
                }
            }
        }
        return matches.stream().parallel().flatMap(combination -> {
            int start = combination.first();
            int end = combination.second();
            int numberOfCombinations = calcFunc(start, end, graph.get(start));
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

    private static void updateGraph(Map<Integer, int[]> graph, int from, int switchedBits, int to) {
        int[] nodeArr = graph.get(from);
        int toBit = switchedBits & ~from;
        int fromBit = switchedBits & ~to;
        int idx = getSetBitIndex(from, fromBit);
        nodeArr[idx] |= toBit;
    }

    private int calculateComboR(int current, int end) {
        int[] neighbourEncoding = graph.get(current);
        if(bitCount(current ^ end) == 2){
            return neighbourEncoding[5];
        }
        return cache.getOrCalculate(current, end, () -> calcFunc(current, end, neighbourEncoding)) * neighbourEncoding[5];
    }


    private int calcFunc(int current, int end, int[] neighbourEncoding) {
        int count = 0;
        int bitsThatCanChange = current & ~end;
        int bitsToChangeTo = end & ~current;
        while(bitsThatCanChange != 0){
            int changeableBit = lowestOneBit(bitsThatCanChange);
            int validNeighbourChangedBits = neighbourEncoding[getSetBitIndex(current, changeableBit)];
            int bitsInNeighbour = validNeighbourChangedBits & bitsToChangeTo;
            while(bitsInNeighbour != 0){
                int goalBit = lowestOneBit(bitsInNeighbour);
                int nextCurrent = current ^ (changeableBit | goalBit);
                count += calculateComboR(nextCurrent, end);
                bitsInNeighbour = bitsInNeighbour ^ goalBit;
            }
            bitsThatCanChange = bitsThatCanChange ^ changeableBit;

        }
        return count;
    }

    private static int getSetBitIndex(int number, int bit) {
        int idx = 0;
        while (number != 0){
            int lowest = lowestOneBit(number);
            if (lowest == bit) {
                return idx;
            }
            number = number ^ lowest;
            idx++;
        }
        throw new IllegalArgumentException("Bit must be set in number");
    }
}

