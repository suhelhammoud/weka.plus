package weka.classifiers.rules.odri;
import java.util.StringJoiner;

/**
 * Created by suhel on 09/11/2020.
 */

public class OMaxIndex {
  public final static int EMPTY = -1;
  private double bestRank = Double.MIN_VALUE;
  private int bestCorrect = EMPTY;//Should start with negative value
  private int bestCover = EMPTY;
  private int bestAtt = EMPTY;
  private int bestItem = EMPTY;
  private int label = EMPTY;

  final public int minOcc;
  final public double numLabels;
  final public double maxPossibleRank;

  public double getBestRank() {
    return bestRank;
  }

  public int getBestAtt() {
    return bestAtt;
  }

  public int getBestItem() {
    return bestItem;
  }

  public int getLabel() {
    return label;
  }

  public int getBestCover() {
    return bestCover;
  }

  public int getBestCorrect() {
    return bestCorrect;
  }

  private OMaxIndex(int minOcc, double numLabels) {
    this.minOcc = minOcc;
    this.numLabels = numLabels;
    this.maxPossibleRank = Math.log(numLabels) / Math.log(2);
  }

  public OMaxIndex copy() {
    OMaxIndex result = new OMaxIndex(this.minOcc, this.numLabels);
    result.bestRank = this.bestRank;
    result.bestAtt = this.bestAtt;
    result.bestItem = this.bestItem;
    result.label = this.label;
    result.bestCorrect = this.bestCorrect;
    result.bestCover = this.bestCover;
    return result;
  }

  /**
   * Find best ranked item with no prior label selection
   *
   * @param count         counter matrix [att][item][label] -> count
   * @param minFreq
   * @param minConfidence
   * @return MaxIndex with best item and corresponding label selected
   */
  public static OMaxIndex ofMeDRI(int[][][] count,
                                  int minFreq,
                                  double minConfidence) {
    OMaxIndex mi = new OMaxIndex(minFreq, minConfidence);
    for (int at = 0; at < count.length; at++) {
      for (int itm = 0; itm < count[at].length; itm++) {
        mi.maxMeDRI(count[at][itm], at, itm);
      }
    }
    return mi;
  }

  /**
   * Find best item (with predefined label) which meets the m_support and m_confidence rankings.
   *
   * @param count
   * @param label
   * @param minFreq
   * @param minConfidence
   * @return
   */
  public static OMaxIndex ofMeDRI(int[][][] count,
                                  int minFreq,
                                  double minConfidence,
                                  int label) {
    OMaxIndex mi = new OMaxIndex(minFreq, minConfidence);
    for (int at = 0; at < count.length; at++) {
      for (int itm = 0; itm < count[at].length; itm++) {
        mi.maxMeDRI(count[at][itm], at, itm, label);
      }
    }
    return mi;
  }

  /**
   * Find item that has the max value based on chosen ranking criteria
   * local max value will be updated
   *
   * @param itemLabels frequencies of labels occurred with this item
   * @param attIndex   which attribute this items belongs to
   * @param itemIndex  item index
   */
  private void maxMeDRI(int[] itemLabels, int attIndex, int itemIndex) {
    //TODO try using immutable results rather than local mutual ones.
    int sum = sum(itemLabels);
    for (int i = 0; i < itemLabels.length; i++) {
      int itemCorrect = itemLabels[i];
      if (itemCorrect < minOcc) continue;

      if (numLabels > (double) itemCorrect / (double) sum) continue;

      int diff = itemCorrect * bestCover - bestCorrect * sum; //TODO nice to allow plugging in ranking criteria
      if (diff > 0 || diff == 0 && itemCorrect > bestCorrect) {
        //switch (att,item,label)
        this.bestAtt = attIndex;
        this.bestItem = itemIndex;
        this.label = i;
        this.bestCorrect = itemCorrect;
        this.bestCover = sum;
      }
    }
  }


  /**
   * @param itemLabels
   * @param attIndex
   * @param itemIndex
   * @param label
   */
  private void maxMeDRI(int[] itemLabels, int attIndex, int itemIndex, int label) {
    int itemCorrect = itemLabels[label];
    if (itemCorrect < minOcc) return;
    int sum = sum(itemLabels);

    if (numLabels > (double) itemCorrect / (double) sum) return;

    int diff = itemCorrect * bestCover - bestCorrect * sum;
    if (diff > 0 || diff == 0 && itemCorrect > bestCorrect) {
      this.bestAtt = attIndex;
      this.bestItem = itemIndex;
      this.label = label;
      this.bestCorrect = itemCorrect;
      this.bestCover = sum;
    }
  }

  double rank(int[] labels) {
    return rank(labels, maxPossibleRank);
  }


  public static OMaxIndex ofOdri(int[][][] count,
                                 int minOcc,
                                 int numLabels) {
    OMaxIndex mi = new OMaxIndex(minOcc, numLabels);
    for (int at = 0; at < count.length; at++) {
      for (int itm = 0; itm < count[at].length; itm++) {
        mi.maxOdri(count[at][itm], at, itm);
      }
    }
    return mi;
  }


  public static OMaxIndex ofOdri(int[][][] count,
                                 int minOcc,
                                 int numLabels,
                                 int label) {
    OMaxIndex mi = new OMaxIndex(minOcc, numLabels);
    for (int at = 0; at < count.length; at++) {
      for (int itm = 0; itm < count[at].length; itm++) {
        mi.maxOdri(count[at][itm], at, itm, label);
      }
    }
    return mi;
  }


  private void maxOdri(int[] itemLabels,
                       int attIndex,
                       int itemIndex) {
    int sum = sum(itemLabels);
    if (sum < minOcc || sum == 0) return;
    double tRank = rank(itemLabels);

    if (tRank < this.bestRank) return;
    if (tRank == this.bestRank && sum < this.bestCover) return;

    /* switch contents (att,item,label, correct, cover) */
    this.bestRank = tRank;
    this.bestAtt = attIndex;
    this.bestItem = itemIndex;
    //find best label
    this.label = argMax(itemLabels);
    this.bestCorrect = itemLabels[this.label];
    this.bestCover = sum;

  }

  /**
   * @param itemLabels
   * @param attIndex
   * @param itemIndex
   * @param label
   */
  private void maxOdri(int[] itemLabels,
                       int attIndex,
                       int itemIndex,
                       int label) {

    int sum = sum(itemLabels);
    if (sum < minOcc || sum == 0) return;
    int mxLabel = argMax(itemLabels);

    if (mxLabel != label) return;

    double tRank = rank(itemLabels);

    if (tRank < this.bestRank) return;
//    if (tRank == this.bestRank && sum < this.bestCover) return; //TODO check later
    if (tRank == this.bestRank && sum < this.bestCover) return; //TODO check later

    /* switch contents (att,item, correct, cover), assert same label */
    this.bestRank = tRank;
    this.bestAtt = attIndex;
    this.bestItem = itemIndex;
    this.label = label;
    this.bestCorrect = itemLabels[this.label];
    this.bestCover = sum;
  }


  @Override
  public String toString() {
    return new StringJoiner(", ",
            this.getClass().getSimpleName() + "[", "]")
            .add("bestRank=" + bestRank)
            .add("bestAtt=" + bestAtt)
            .add("bestItem= " + bestItem)
            .add("lbl=" + label)
            .add("correct=" + bestCorrect)
            .add("cover= " + bestCover)
            .toString();
  }

  public static int sum(int[] a) {
//        return Arrays.stream(a).sum(); // TODO performance comparision
    int result = 0;
    for (int i : a) result += i;
    return result;
  }

  public static double rank(int[] data, double maxEntropy) {
    int sum = sum(data);
    if (sum == 0) return 0;
    double result = 0;
    for (int val : data) {
      if (val == 0) continue;
      double p = (double) val / sum;
      result += p * Math.log(p);//negative value
    }
    return maxEntropy + result / Math.log(2);
  }

  /**
   * data.length > 0
   *
   * @param data
   * @return
   */
  public static int argMax(int[] data) {
    int result = 0;

    for (int i = 1; i < data.length; i++) {
      result = data[i] > data[result] ? i : result;
    }
    return result;
  }

  public static void main(String[] args) {
    int[] data = new int[]{8, 4, 2, 2};
    double rank = rank(data, 0);
    System.out.println("rank = " + rank);
  }
}
