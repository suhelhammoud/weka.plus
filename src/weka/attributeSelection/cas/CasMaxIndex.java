package weka.attributeSelection.cas;

import java.util.Arrays;

public class CasMaxIndex {

  final public static CasMaxIndex NONE = null;
  final public static int EMPTY = -1;

  private double bestRank = Double.MIN_VALUE;
  private int bestCorrect = EMPTY;
  private int bestCover = EMPTY;
  private int bestAttribute = EMPTY;


  public int getBestCorrect() {
    return bestCorrect;
  }

  public int getBestCover() {
    return bestCover;
  }

  public static CasMaxIndex of(int[][][] counts) {
    CasMaxIndex cmi = new CasMaxIndex();
    for (int att = 0; att < counts.length; att++) {
      cmi.maxCas(counts[att], att);
    }
    return cmi;
  }

  public void maxCas(int[][] attData, int attIndex) {
    int correct = max(attData);
    int cover = sum(attData);
    if (correct > bestCorrect) {
      bestCorrect = correct;
      bestCover = cover;
      bestAttribute = attIndex;
    }
  }


  //get all correct values
  public static int max(int[][] data) {
    return Arrays.stream(data)
            .mapToInt(sa -> Arrays.stream(sa).max().getAsInt())
            .sum();
  }

  public static int max(int[] a) {
    return Arrays.stream(a).max().getAsInt();
  }

  public static int sum(int[][] data) {
    return Arrays.stream(data)
            .mapToInt(sa -> Arrays.stream(sa).sum())
            .sum();
  }

  public static int sum(int[] a) {
    return Arrays.stream(a).sum();
  }

  public static double errorRate(int[] a) {
    return 1.0 - max(a) / sum(a);
  }
}
