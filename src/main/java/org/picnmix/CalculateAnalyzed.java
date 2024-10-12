package org.picnmix;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalculateAnalyzed {

    private final List<Analyzed> words;

    private record SnE(String startWord, String endWord){}

    public CalculateAnalyzed(){
        Path path = null;
        try {
            path = Paths.get(ClassLoader.getSystemResource("analized-german.txt").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
        try(Stream<String> lines = Files.lines(path)) {
            List<Analyzed> words2 = lines.map(line -> line.split(" ")).map(split -> {
                String start = split[1];
                String end = split[3];
                List<String> words = Stream.of(start, end).sorted().toList();
                return new Analyzed(words.get(0).toUpperCase(), words.get(1).toUpperCase(), Integer.parseInt(split[5]), 0.0);
            }).collect(Collectors.toSet()).stream().toList();
            words = new ArrayList<>();
            Map<SnE, List<Analyzed>> map = new HashMap<>();
            for(Analyzed word : words2){
                SnE snE = new SnE(word.start, word.end);
                List<Analyzed> analyzeds = map.computeIfAbsent(snE, _ -> new ArrayList<>());
                analyzeds.add(word);
                map.put(snE, analyzeds);
            }
            map.forEach((_, value) -> words.add(value.stream().sorted(Comparator.comparingInt(v -> v.combinations)).toList().getFirst()));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private record Analyzed(String start, String end, int combinations, double semantic){}

    public void calculate() {

        System.out.println(Stream.of(1, 2, 3).sorted().map(String::valueOf).collect(Collectors.joining(", ")));

        for (Analyzed analyzed : words.stream().sorted(Comparator.comparingDouble(Analyzed::semantic)).toList()) {

            Path path = null;
            try {
                path = Paths.get(ClassLoader.getSystemResource("common-german-out.txt").toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            try {
                Files.writeString(path, STR. "\{ analyzed }\n", StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }




        List<Analyzed> ordered = words.stream().sorted(Comparator.comparingInt(Analyzed::combinations)).toList();

        int size = ordered.size();
        int subSize = size / 7;
        List<List<Analyzed>> lists = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            int offset = i * subSize;
            List<Analyzed> list = new ArrayList<>(ordered.subList(offset, offset + subSize));
            list = list.stream().sorted(Comparator.comparingDouble(Analyzed::semantic).reversed()).toList();
            list = list.stream().limit(52).toList();
            list  = new ArrayList<>(list);
            Collections.shuffle(list);
            lists.add(list);
        }
        List<Integer> order = List.of(3, 5, 0, 1, 6, 4, 2); //1, 6, 4, 2, 3, 5, 0);
        String dt = "2024-09-19";  // Start date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {

            c.setTime(sdf.parse(dt));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < 52; i++) {
            for (Integer integer : order) {
                Analyzed analyzed = lists.get(integer).get(i);
                System.out.println(STR. "dateToChallengeGerman['\{ sdf.format(c.getTime()) }'] = {startWord: '\{ analyzed.start }', endWord: '\{ analyzed.end }'}" );
                c.add(Calendar.DATE, 1);  // number of days to add
            }

        }
    }
}
