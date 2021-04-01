package weka.attributeSelection.pas2;

public class Tuple2<K, V> {
  final public K k;
  final public V v;

  public Tuple2(K k, V v) {
    this.k = k;
    this.v = v;
  }

  public static <K, V> Tuple2 of(K k, V v) {
    return new Tuple2<>(k, v);
  }
}
