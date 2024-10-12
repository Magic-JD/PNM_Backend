package org.picnmix;

import java.util.List;

public interface FileAccess {
    List<String> extractWordsFromFile();

    void writeToOutput(String output);
}
