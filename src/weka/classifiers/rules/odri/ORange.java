package weka.classifiers.rules.odri;

import java.util.Arrays;

public class ORange {

  //  private final int numLabels;
  final int[] labels;
  double lower;
  double upper;

  public ORange(int numLabels) {
    this.labels = new int[numLabels];
  }

  public ORange(int[] labels) {
    this.labels = labels;
    this.lower = Double.MIN_VALUE;
    this.upper = Double.MAX_VALUE;
  }

  public boolean isIn(double value) {
    return value >= lower && value <= upper;
  }

  public double rank() {
    return OMaxIndex.rank(labels, labels.length);
  }

  public int sum() {
    return Arrays.stream(labels).sum();
  }

  @Override
  public String toString() {
    return "ORange[" + (lower == Double.MIN_VALUE? "-inf": lower) +
            ", " + (upper == Double.MAX_VALUE? "+inf": upper) +
            "], counts=" + Arrays.toString(labels);
  }
}
