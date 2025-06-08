package org.picnmix;

import static java.lang.System.currentTimeMillis;
public class CalcPathRunner {
    public static void main(String[] args) {
        long start = currentTimeMillis();
        CalculatePathsPerPair calculateMaxPerWord = new CalculatePathsPerPair();
        calculateMaxPerWord.calculate();
        long end = currentTimeMillis();
        System.out.println("TIME: " + (end - start));
    }


}