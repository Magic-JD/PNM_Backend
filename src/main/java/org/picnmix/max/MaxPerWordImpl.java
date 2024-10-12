package org.picnmix.max;

import org.picnmix.FileAccess;
import org.picnmix.max.data.Combos;
import org.picnmix.max.data.Match;
import org.picnmix.max.strategy.FindCombos;

import java.util.*;

import static org.picnmix.max.CombosToString.convertToOutput;
import static org.picnmix.max.FindMatches.getMatches;
import static org.picnmix.max.WordMappingCreator.createEncodeToWordsMapping;


public class MaxPerWordImpl implements MaxPerWord {


    private final FileAccess fileAccess;
    private final FindCombos findCombos;

    public MaxPerWordImpl(FileAccess fileAccess, FindCombos findCombos){
        this.fileAccess = fileAccess;
        this.findCombos = findCombos;
    }

    public void calculate() {
        List<String> words = fileAccess.extractWordsFromFile();
        Map<Integer, List<String>> encodeToWords = createEncodeToWordsMapping(words);
        List<Integer> encoded = new ArrayList<>(encodeToWords.keySet());
        List<Match> matches = getMatches(encoded);
        List<Combos> comboList = findCombos.getCombos(matches, encodeToWords, encoded);
        fileAccess.writeToOutput(convertToOutput(comboList));
    }
}
