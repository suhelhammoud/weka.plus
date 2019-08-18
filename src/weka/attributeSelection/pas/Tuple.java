package weka.attributeSelection.pas;

public class Tuple<K, V> {
  final public K k;
  final public V v;

  public Tuple(K k, V v) {
    this.k = k;
    this.v = v;
  }

  public static <K, V> Tuple of(K k, V v) {
    return new Tuple<>(k, v);
  }
}
