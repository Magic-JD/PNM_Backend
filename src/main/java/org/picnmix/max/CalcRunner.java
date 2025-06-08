package org.picnmix.max;

import org.picnmix.FileAccessImpl;
import org.picnmix.max.strategy.FindPathsForMatchesArray;
import org.picnmix.max.strategy.FindPathsForMatchesGraph;

import static java.lang.System.nanoTime;

// Correct answer for small data 45548829932
// Correct answer for big data 1048270397830
public class CalcRunner {
    public static void main(String[] args) {
        long start = nanoTime();
        MaxPerWord maxPerWord = new MaxPerWordImpl(new FileAccessImpl("wordsAll.txt", "a1.txt"), new FindPathsForMatchesGraph());
        maxPerWord.calculate();
        long end = nanoTime();
        System.out.println("TIME: " + (end - start)/1_000_000);
    }


}