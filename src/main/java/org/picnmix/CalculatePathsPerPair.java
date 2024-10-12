package org.picnmix;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalculatePathsPerPair {

    private final List<String> words;
    private final List<Pair> pairs;

    private record Pair(String start, String end, int combinations){};

    public CalculatePathsPerPair(){
        Path path = null;
        try {
            path = Paths.get(ClassLoader.getSystemResource("dictionary.csv").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
        try(Stream<String> lines = Files.lines(path)) {
            words = lines.map(String::toUpperCase).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException();
        }
        try {
            path = Paths.get(ClassLoader.getSystemResource("compWords.txt").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
        try(Stream<String> lines = Files.lines(path)) {
            pairs = lines.map(String::toUpperCase).map(s -> s.split(" ")).map(arr -> new Pair(arr[1], arr[3], Integer.parseInt(arr[5]))).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public void calculate() {
        List<Pair> list = pairs.stream().filter(pair -> pair.combinations == 1).filter(pair -> words.contains(pair.start) && words.contains(pair.end)).toList();
        list.forEach(pair -> calculate(pair.start, pair.end));
    }

    private void calculate(String word, String endWord) {
        Queue<Result> queueToCheck = new ArrayDeque<>();
        queueToCheck.add(new Result(word, endWord, word, List.of(word)));
        for (int i = 1; i < 5; i++) {
            int finalI =i;
            queueToCheck = queueToCheck
                    .stream()
                    .flatMap(r -> words.stream()
                            .filter(w -> this.isNextWord(finalI, r.startWord, r.endWord, r.currentWord, w, r.path))
                            .map(w -> {
                                List<String> chosen = new ArrayList<>(r.path);
                                chosen.add(w);
                                return new Result(r.startWord, r.endWord, w, chosen);
                            }))
                    .distinct()
                    .collect(Collectors.toCollection(ArrayDeque::new));
        }
        if(!queueToCheck.isEmpty()){
            String collect = queueToCheck.stream()
                    .map(r -> STR. "START: \{ r.startWord } END: \{ r.endWord } PATH \{ String.join(", ", r.path) }" )
                    .collect(Collectors.joining("\n"));
            System.out.println(collect);
            System.out.println("Match number: " + queueToCheck.size());
        }


    }


    private record Result(String startWord, String endWord, String currentWord, List<String> path){}

    private boolean isNextWord(int count, String startWord, String endWord, String current, String check, List<String> usedWords){
        return !usedWords.contains(check) && calculateDiff(1, current, check) && calculateDiff(count, startWord, check) && calculateDiff(5-count, endWord, check);
    }

    private boolean calculateDiff(int desiredDiff, String current, String next){
        int diff = 0;
        List<Integer> checkChars = new ArrayList<>(next.chars().boxed().toList());
        for (int i = 0; i < 5; i++) {
            Integer c = (int) current.charAt(i);
            if(!checkChars.remove(c)){
                diff++;
                if(diff > desiredDiff){
                    return false;
                }
            }
        }
        return diff == desiredDiff;
    }
}
