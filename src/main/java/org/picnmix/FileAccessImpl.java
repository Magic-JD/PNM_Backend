package org.picnmix;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileAccessImpl implements FileAccess {

    private final String in;
    private final String out;


    public FileAccessImpl(String in, String out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public List<String> extractWordsFromFile() {
        Path path = getPath(in);
        try (Stream<String> lines = Files.lines(path)) {
            Set<String> allWords = lines.collect(Collectors.toSet());
            return allWords.stream().toList();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void writeToOutput(String output) {
//        Path path = getPath(out);
//        try {
//            Files.writeString(path, output, StandardOpenOption.TRUNCATE_EXISTING);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    private Path getPath(String name) {
        try {
            return Paths.get(ClassLoader.getSystemResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
    }

}
