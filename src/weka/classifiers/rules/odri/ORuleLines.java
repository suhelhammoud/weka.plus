package weka.classifiers.rules.odri;

import java.util.Collection;

/**
 * Created by suhel on 23/03/16.
 */
public class ORuleLines {
  final public ORule rule;
  final public int[] lines;

  public ORuleLines(ORule rule,
                    int[] lines) {
    this.rule = rule;
    this.lines = lines;
  }

}