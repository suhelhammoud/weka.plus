package weka.classifiers.rules.early;

import java.util.Collection;

/**
 * Created by suhel on 23/03/16.
 */
public class ERuleLines {
  final public ERule rule;
  final public Collection<int[]> lines;
  final public long scannedInstances;

  public ERuleLines(ERule rule, Collection<int[]> lines, long scannedInstances) {
    this.rule = rule;
    this.lines = lines;
    this.scannedInstances = scannedInstances;
  }

  public ERuleLines(ERule rule, Collection<int[]> lines) {
    this(rule, lines, 0);
  }
}