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
        // Adding the total number of anagrams to the array
        // Not neat, but removing the repeated calls to encodingToWords.get(word).size() shaved 20% off the time.
        encoded.forEach(e -> graph.put(e, new int[]{0, 0, 0, 0, 0, encodingToWords.get(e).size()}));
        for(int i = 0; i < encoded.size(); i++){
            for (int j = i+1; j < encoded.size(); j++) {
                Integer first = encoded.get(i);
                Integer second = encoded.get(j);

                int switchedBits = first ^ second;
                if (bitCount(switchedBits) == 2){
                    // Update the graph to reflect the neighbor relationship
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
            // Add all anagram combinations of the first and second word.
            for (String firstWord : firstWords) {
                for (String secondWord : secondWords) {
                    pathedMatches.add(new PathedMatch(numberOfCombinations, firstWord, secondWord));
                }
            }
            return pathedMatches.stream();
        }).toList();

    }

    private static void updateGraph(Map<Integer, int[]> graph, int from, int switchedBits, int to) {
        // Set the neighbor mask to reflect the words that can be created by changing the from bit.
        int[] nodeArr = graph.get(from);
        int toBit = switchedBits & ~from;
        int fromBit = switchedBits & ~to;
        int idx = getSetBitIndex(from, fromBit);
        nodeArr[idx] |= toBit;
    }

    private int calculateComboR(int current, int end) {
        int[] neighbourEncoding = graph.get(current);
        // If we are one step away we know the only valid word is the end word and we know it exists.
        // Therefore, we can return.
        if(bitCount(current ^ end) == 2){
            return neighbourEncoding[5];
        }
        return cache.getOrCalculate(current, end, () -> calcFunc(current, end, neighbourEncoding)) * neighbourEncoding[5];
    }


    private int calcFunc(int current, int end, int[] neighbourEncoding) {
        int count = 0;
        int bitsThatCanChange = current & ~end;
        int bitsToChangeTo = end & ~current;
        // For each bit that can change we retrieve the relevant bitmask.
        while(bitsThatCanChange != 0){
            int changeableBit = lowestOneBit(bitsThatCanChange);
            int validNeighbourChangedBits = neighbourEncoding[getSetBitIndex(current, changeableBit)];
            // Find all valid next words that contain the letters we need.
            int bitsInNeighbour = validNeighbourChangedBits & bitsToChangeTo;
            // For each bit, calculate the next word and recurse.
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
        // Gets the relevant bit index. Technically it is faster to have an array of size 26 and then use the
        // bit position directly, but this increases the space and the cost of this operation is fairly low.
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

