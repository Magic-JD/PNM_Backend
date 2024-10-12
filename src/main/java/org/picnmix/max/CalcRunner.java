package org.picnmix.max;

import org.picnmix.FileAccessImpl;
import org.picnmix.max.strategy.FindPathsForMatchesArray2;

import static java.lang.System.nanoTime;

public class CalcRunner {
    public static void main(String[] args) {
        long start = nanoTime();
        MaxPerWord maxPerWord = new MaxPerWordImpl(new FileAccessImpl("wordsAll2.txt", "compWords.txt"), new FindPathsForMatchesArray2());
        maxPerWord.calculate();
        long end = nanoTime();
        System.out.println("TIME: " + (end - start)/1_000_000);
    }


}