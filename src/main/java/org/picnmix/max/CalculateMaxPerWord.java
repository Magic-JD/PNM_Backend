package org.picnmix.max;

import org.picnmix.FileAccess;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CalculateMaxPerWord implements MaxPerWord {

    private final List<String> words;

    public CalculateMaxPerWord(){
        Path path;
        try {
            path = Paths.get(ClassLoader.getSystemResource("cleanedWords1k.txt").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
        try(Stream<String> lines = Files.lines(path);) {
            Set<String> allWords = lines.collect(Collectors.toSet());
            words = allWords.stream().toList();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private boolean possibleMatch(String word, String match){
        if(match.chars().boxed().collect(Collectors.toSet()).size() < 5){
            return false;
        } else {
            return calculateDiff(5, word, match);
        }
    }


    public void calculate() {
        Path path = null;
        try {
            path = Paths.get(ClassLoader.getSystemResource("cleanedWords1k.txt").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
        List<String> beginAndEndWords = words.stream().filter(w -> w.chars().boxed().collect(Collectors.toSet()).size() == 5).toList();
        Path finalPath = path;
        beginAndEndWords.parallelStream().forEach(word -> {
            beginAndEndWords.stream().filter(pm -> possibleMatch(word, pm)).forEach(endWord -> {
                Queue<Result> queueToCheck = new ArrayDeque<>();
                queueToCheck.add(new Result(endWord, word));
                for (int i = 1; i < 6; i++) {
                    int finalI =i;
                    queueToCheck = queueToCheck
                            .stream()
                            .flatMap(r -> beginAndEndWords.stream()
                                    .filter(w -> this.isNextWord(finalI, r.endWord, r.currentWord, w))
                                    .map(w -> new Result(r.endWord, w)))
                            .collect(Collectors.toCollection(ArrayDeque::new));
                }
                try {
                    getPath(finalPath, word, endWord, queueToCheck.size());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private static synchronized void getPath(Path finalPath, String word, String endWord, int size) throws IOException {
        Files.writeString(finalPath, STR. "START: \{ word } END: \{ endWord } DIFFICULTY: \{ size }\n", StandardOpenOption.APPEND);
    }

    private record Result(String endWord, String currentWord){}

    private boolean isNextWord(int count, String endWord, String current, String check){
        return calculateDiff(1, current, check) && calculateDiff(5-count, endWord, check);
    }

    private boolean calculateDiff(int desiredDiff, String current, String next){
        int diff = 0;
        char[] nextArray = next.toCharArray();
        char[] currentArray = current.toCharArray();
        for (int i = 0; i < 5; i++) {
            char c = currentArray[i];
            int j;
            for (j = 0; j < 5; j++) {
                if(nextArray[j] == c){
                    nextArray[j] = 1;
                    break;
                }
            }
            if(j == 5){
                diff++;
                if(diff > desiredDiff){
                    return false;
                }
            }
        }
        return diff == desiredDiff;
    }
}
