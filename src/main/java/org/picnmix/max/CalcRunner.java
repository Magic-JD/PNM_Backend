package org.picnmix.max;

import org.picnmix.FileAccessImpl;
import org.picnmix.max.strategy.FindCombosArray;
import org.picnmix.max.strategy.FindCombosArray2;
import org.picnmix.max.strategy.FindCombosList;
import org.picnmix.max.strategy.FindCombosTree;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;

public class CalcRunner {
    public static void main(String[] args) {
        long start = nanoTime();
        MaxPerWord maxPerWord = new MaxPerWordImpl(new FileAccessImpl("wordsAll2.txt", "compWords.txt"), new FindCombosArray2());
        maxPerWord.calculate();
        long end = nanoTime();
        System.out.println("TIME: " + (end - start)/1_000_000);
    }


}