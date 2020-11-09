package weka.classifiers.rules.odri;


import java.util.StringJoiner;

/**
 * Created by suhel on 21/03/16.
 */

public class OMaxIndex {
  public final static int EMPTY = -1;
  private int bestCorrect = -1;//Should start with negative value
  private int bestCover = 0;
  private int bestAtt = EMPTY, bestItem = EMPTY;
  private int label = EMPTY;

  final public int minFreq;
  final public double minConfidence;

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

  private OMaxIndex(int minFreq, double minConfidence) {
    this.minFreq = minFreq;
    this.minConfidence = minConfidence;
  }

  public OMaxIndex copy() {
    OMaxIndex result = new OMaxIndex(this.minFreq, this.minConfidence);
    result.bestAtt = this.bestAtt;
    result.bestItem = this.bestItem;
    result.label = this.label;
    result.bestCorrect = this.bestCorrect;
    result.bestCover = this.bestCover;
    return result;
  }

  /**
   * Find best (att, item, label) based on m_confidence and m_support, no prior label condition
   *
   * @param count (att, item, label) -> count
   * @return MaxIndex instance with values of (att, item, label) that maximize m_confidence/m_support
   */
  public static OMaxIndex of(int[][][] count) {
    OMaxIndex mi = new OMaxIndex(0, 0);
    for (int at = 0; at < count.length; at++) {
      for (int itm = 0; itm < count[at].length; itm++) {
        mi.max(count[at][itm], at, itm);
      }
    }
    return mi;
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
      if (itemCorrect < minFreq) continue;

      if (minConfidence > (double) itemCorrect / (double) sum) continue;

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
    if (itemCorrect < minFreq) return;
    int sum = sum(itemLabels);

    if (minConfidence > (double) itemCorrect / (double) sum) return;

    int diff = itemCorrect * bestCover - bestCorrect * sum;
    if (diff > 0 || diff == 0 && itemCorrect > bestCorrect) {
      this.bestAtt = attIndex;
      this.bestItem = itemIndex;
      this.label = label;
      this.bestCorrect = itemCorrect;
      this.bestCover = sum;
    }
  }


  public static OMaxIndex ofOne(int[][][] count, int label) {
    OMaxIndex mi = new OMaxIndex(0, 0);
    for (int at = 0; at < count.length; at++) {
      for (int itm = 0; itm < count[at].length; itm++) {
        mi.maxOne(count[at][itm], at, itm, label);
      }
    }
    return mi;
  }


  private void maxOne(int[] itemLabels, int attIndex, int itemIndex, int label) {
    int sum = sum(itemLabels);
    int diff = itemLabels[label] * bestCover - bestCorrect * sum;
    if (diff > 0 || diff == 0 && itemLabels[label] > bestCorrect) {
      this.bestAtt = attIndex;
      this.bestItem = itemIndex;
      this.label = label;
      this.bestCorrect = itemLabels[label];
      this.bestCover = sum;
    }
  }


  private boolean max(int[] itemLabels, int attIndex, int itemIndex) {
    int sum = sum(itemLabels);
    boolean changed = false;
    for (int i = 0; i < itemLabels.length; i++) {
      int diff = itemLabels[i] * bestCover - bestCorrect * sum;
      if (diff > 0 || diff == 0 && itemLabels[i] > bestCorrect) {
        this.bestAtt = attIndex;
        this.bestItem = itemIndex;
        this.label = i;
        this.bestCorrect = itemLabels[i];
        this.bestCover = sum;
        changed = true;
      }
    }
    return changed;
  }


  @Override
  public String toString() {
    return new StringJoiner(", ",
            this.getClass().getSimpleName() + "[", "]")
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
}
