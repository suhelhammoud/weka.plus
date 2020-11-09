package weka.classifiers.rules.odri;

import java.util.Collection;

/**
 * Created by suhel on 23/03/16.
 */
public class ORuleLines {
  final public ORule rule;
  final public Collection<int[]> lines;
  final public long scannedInstances;

  public ORuleLines(ORule rule, Collection<int[]> lines, long scannedInstances) {
    this.rule = rule;
    this.lines = lines;
    this.scannedInstances = scannedInstances;
  }

  public ORuleLines(ORule rule, Collection<int[]> lines) {
    this(rule, lines, 0);
  }
}