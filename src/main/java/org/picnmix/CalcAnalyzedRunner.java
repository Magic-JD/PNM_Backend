package org.picnmix;

import static java.lang.System.currentTimeMillis;

public class CalcAnalyzedRunner {
    public static void main(String[] args) {
        long start = currentTimeMillis();
        CalculateAnalyzed calculateMaxPerWord = new CalculateAnalyzed();
        calculateMaxPerWord.calculate();
        long end = currentTimeMillis();
        System.out.println("TIME: " + (end - start));
    }


}