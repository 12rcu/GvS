package uulm.in.vs.consistency;

import java.util.Collection;
import java.util.Set;

public interface ConsistentMap<K,V> {
    int size();
    int hashCode();
    boolean isEmpty();
    boolean containsKey(K k);
    boolean containsValue(V v);
    void remove(K key);
    void put(K key, V value);
    V get(K key);
    Collection<V> Values();
    Set<K> keySet();
}
