package org.picnmix.max.strategy;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private Node[] children;
    private int value;

    private static int maskLettersTo(int letters, int position) {
        return letters & (Integer.MAX_VALUE << (position + 1));
    }

    public void addValue(int value){
        addValue(value, value);
    }

    private void addValue(int value, int remainder) {
        if (remainder == 0) {
            this.value = value;
            return;
        }
        if (children == null) children = new Node[26];
        int position = Integer.numberOfTrailingZeros(remainder);
        if (children[position] == null) children[position] = new Node();
        children[position].addValue(value, maskLettersTo(remainder, position));
    }

    public List<Integer> getValues(int firstWord, int secondWord){
        ArrayList<Integer> values = new ArrayList<>();
        getValue(firstWord, secondWord, firstWord, (firstWord | secondWord) ^ firstWord, false, values);
        return values;
    }

    private void getValue(int firstWord, int secondWord, int currentLetters, int lettersToAdd, boolean usedOld, List<Integer> values) {
        if (children == null) {
            if (value == firstWord) {
                return; // This is the case we have only added letters from the current letters.
            }
            if(currentLetters != 0 && (currentLetters | secondWord) == secondWord){
                return;
            }
            values.add(value);
            return;
        }
        // Note if either number is zero trailing = 33 - node there will always be null
        int position1 = Integer.numberOfTrailingZeros(currentLetters);
        int positionX = Integer.numberOfTrailingZeros(lettersToAdd);
        while (positionX < position1 && positionX < 26) { // Try and add the letters that we don't have yet
            lettersToAdd = maskLettersTo(lettersToAdd, positionX); // Overwrite one up to the point we are examining
            Node next = children[positionX];
            if (next != null) { // if this letter order exists in the map, update using it
                next.getValue(firstWord, secondWord, currentLetters, 0, usedOld, values);
            }
            positionX = Integer.numberOfTrailingZeros(lettersToAdd);
        }
        if(position1 > 26){
            return;
        }
        currentLetters = maskLettersTo(currentLetters, position1); // remove current letter in the number
        Node next = children[position1];
        if (next != null) {
            next.getValue(firstWord, secondWord, currentLetters, lettersToAdd, usedOld, values);
        }
        if (usedOld || (secondWord | 1 << (position1)) == secondWord) {
            return; // We have already skipped a letter, or the letter that would be skipped is needed in the end word.
        }
        //Repeat with the masked values. This means that we have skipped the first letter.
        getValue(firstWord, secondWord, currentLetters, lettersToAdd, true, values);
    }
}
