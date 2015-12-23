package cache.strategies;

import java.util.Comparator;
import java.util.Map;

public class ComparatorImpl<KeyType> implements Comparator<KeyType> {
    private final Map<KeyType, Long> map;

    public ComparatorImpl(Map<KeyType, Long> map) {
        this.map = map;
    }

    @Override
    public int compare(KeyType key1, KeyType key2) {
        if(map.get(key1) > map.get(key2)) {
            return 1;   // key1 must be later than key2
        } else if(map.get(key1) < map.get(key2)) {
            return -1;  // key1 must be earlier than key2
        } else {
            return 0;   // key1 and key2 are equals
        }
    }
}