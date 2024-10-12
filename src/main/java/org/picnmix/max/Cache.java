package org.picnmix.max;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntSupplier;

public class Cache {

    private final Map<Long, Integer> store;

    public Cache(){ //Cache created with enormous initial capacity because I want to optimize for speed.
        store = new ConcurrentHashMap<>(10_000_000, 0.75f, 8);
    }

    public int getOrCalculate(int firstWord, int secondWord, IntSupplier supplier){
        long key = calculateTwoWordKey(firstWord, secondWord);
        Integer current = store.get(key);
        if(current != null){
            return current;
        }
        int updated = supplier.getAsInt();
        store.put(key, updated);
        return updated;
    }

    public Integer get(int firstWord, int secondWord){
        return store.get(calculateTwoWordKey(firstWord, secondWord));
    }
    public void put(int firstWord, int secondWord, int value){
        store.put(calculateTwoWordKey(firstWord, secondWord), value);
    }

    private long calculateTwoWordKey(int firstWord, int secondWord) {
        // This key will be used to identify the combination of these two words for hashing
        if (firstWord > secondWord) {
            return ((long) firstWord << 30) | secondWord;
        } else {
            return ((long) secondWord << 30) | firstWord;
        }
    }
}
