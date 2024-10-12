package org.picnmix.max;

import org.junit.jupiter.api.Test;
import org.picnmix.FileAccess;
import org.picnmix.FileAccessImpl;
import org.picnmix.max.strategy.FindPathsForMatchesArray;
import org.picnmix.max.strategy.FindPathsForMatchesArray2;
import org.picnmix.max.strategy.FindPathsForMatchesTree;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaxPerWordTest {

    @Test
    void testThatCalculateBuildsTheCorrectResponsesToInputArray1() throws URISyntaxException, IOException {
        FileAccess fileAccess = new FileAccessImpl("test_dictionary.csv", "test_out.txt");
        MaxPerWord maxPerWord = new MaxPerWordImpl(fileAccess, new FindPathsForMatchesArray());
        maxPerWord.calculate();
        List<String> outputExpected = Files.lines(
                Paths.get(ClassLoader.getSystemResource("test_out_expected.txt").toURI())
        ).toList();
        List<String> outputActual = Files.lines(
                Paths.get(ClassLoader.getSystemResource("test_out.txt").toURI())
        ).toList();
        assertEquals(outputExpected.size(), outputActual.size());
        for (int i = 0; i < outputExpected.size(); i++) {
            assertEquals(outputExpected.get(i), outputActual.get(i));
        }
    }

    @Test
    void testThatCalculateBuildsTheCorrectResponsesToInputArray2() throws URISyntaxException, IOException {
        FileAccess fileAccess = new FileAccessImpl("test_dictionary.csv", "test_out.txt");
        MaxPerWord maxPerWord = new MaxPerWordImpl(fileAccess, new FindPathsForMatchesArray2());
        maxPerWord.calculate();
        List<String> outputExpected = Files.lines(
                Paths.get(ClassLoader.getSystemResource("test_out_expected.txt").toURI())
        ).toList();
        List<String> outputActual = Files.lines(
                Paths.get(ClassLoader.getSystemResource("test_out.txt").toURI())
        ).toList();
        assertEquals(outputExpected.size(), outputActual.size());
        for (int i = 0; i < outputExpected.size(); i++) {
            assertEquals(outputExpected.get(i), outputActual.get(i));
        }
    }

    @Test
    void testThatCalculateBuildsTheCorrectResponsesToInputTree() throws URISyntaxException, IOException {
        FileAccess fileAccess = new FileAccessImpl("test_dictionary.csv", "test_out.txt");
        MaxPerWord maxPerWord = new MaxPerWordImpl(fileAccess, new FindPathsForMatchesTree());
        maxPerWord.calculate();
        List<String> outputExpected = Files.lines(
                Paths.get(ClassLoader.getSystemResource("test_out_expected.txt").toURI())
        ).toList();
        List<String> outputActual = Files.lines(
                Paths.get(ClassLoader.getSystemResource("test_out.txt").toURI())
        ).toList();
        assertEquals(outputExpected.size(), outputActual.size());
        for (int i = 0; i < outputExpected.size(); i++) {
            assertEquals(outputExpected.get(i), outputActual.get(i));
        }
    }
}