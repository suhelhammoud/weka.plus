package weka.classifiers.rules.early;

/**
 * Created by suhel on 23/03/16.
 */

/**
 * Helper class
 *
 * @param <K> key
 * @param <V> value
 */
public class EPair<K, V> {
  final public K key;
  final public V value;

  public EPair(K key, V value) {
    this.key = key;
    this.value = value;
  }
}
