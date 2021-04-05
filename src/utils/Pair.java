package utils;

public class Pair<K, V> {
  final public K k;
  final public V v;

  public Pair(K k, V v) {
    this.k = k;
    this.v = v;
  }

  public static <K, V> Pair of(K k, V v) {
    return new Pair<>(k, v);
  }


}
