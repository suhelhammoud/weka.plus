package weka.attributeSelection.cas;

public class CasTuple<K, V> {
  final public K k;
  final public V v;

  public CasTuple(K k, V v) {
    this.k = k;
    this.v = v;
  }

  public static <K, V> CasTuple of(K k, V v) {
    return new CasTuple<>(k, v);
  }
}
