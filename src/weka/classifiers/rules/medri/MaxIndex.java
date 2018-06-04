package weka.classifiers.rules.medri;


import java.util.StringJoiner;

/**
 * Created by suhel on 21/03/16.
 */

public class MaxIndex {
    public final static int EMPTY = -1;
    protected int bestCorrect = -1;//Should start with negative value
    int bestCover = 0;
    protected int bestAtt = EMPTY, bestItem = EMPTY, label = EMPTY;

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

    private MaxIndex(int minFreq, double minConfidence) {
        this.minFreq = minFreq;
        this.minConfidence = minConfidence;
    }

    public MaxIndex copy() {
        MaxIndex result = new MaxIndex(this.minFreq, this.minConfidence);
        result.bestAtt = this.bestAtt;
        result.bestItem = this.bestItem;
        result.label = this.label;
        result.bestCorrect = this.bestCorrect;
        result.bestCover = this.bestCover;

        return result;
    }

    public static MaxIndex of(int[][][] count) {
        MaxIndex mi = new MaxIndex(0, 0);
        for (int at = 0; at < count.length; at++) {
            for (int itm = 0; itm < count[at].length; itm++) {
                mi.max(count[at][itm], at, itm);
            }
        }
        return mi;
    }

    public static MaxIndex ofMeDRI(int[][][] count, int minFreq, double minConfidence) {
        MaxIndex mi = new MaxIndex(minFreq, minConfidence);
        for (int at = 0; at < count.length; at++) {
            for (int itm = 0; itm < count[at].length; itm++) {
                mi.maxMeDRI(count[at][itm], at, itm);
            }
        }
        return mi;
    }

    public static MaxIndex ofOne(int[][][] count, int label) {
        MaxIndex mi = new MaxIndex(0, 0);
        for (int at = 0; at < count.length; at++) {
            for (int itm = 0; itm < count[at].length; itm++) {
                mi.maxOne(count[at][itm], at, itm, label);
            }
        }
        return mi;
    }


    public static MaxIndex ofSupportConfidence(int[][][] count, int label,
                                               int minFreq, double minConfidence) {
        MaxIndex mi = new MaxIndex(minFreq, minConfidence);
        for (int at = 0; at < count.length; at++) {
            for (int itm = 0; itm < count[at].length; itm++) {
                mi.maxSupportConfidence(count[at][itm], at, itm, label);
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

    private void maxSupportConfidence(int[] itemLabels, int attIndex, int itemIndex, int label) {
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


    private void maxMeDRI(int[] itemLabels, int attIndex, int itemIndex) {
        int sum = sum(itemLabels);
        for (int i = 0; i < itemLabels.length; i++) {
            int itemCorrect = itemLabels[i];
            if (itemCorrect < minFreq) continue;

            if (minConfidence > (double) itemCorrect / (double) sum) continue;

            int diff = itemCorrect * bestCover - bestCorrect * sum;
            if (diff > 0 || diff == 0 && itemCorrect > bestCorrect) {
                this.bestAtt = attIndex;
                this.bestItem = itemIndex;
                this.label = i;
                this.bestCorrect = itemCorrect;
                this.bestCover = sum;
            }
        }
    }

    @Override
    public String toString() {

//        return MoreObjects.toStringHelper(this)
//                .add("bestAtt", bestAtt)
//                .add("bestItem", bestItem)
//                .add("lbl", label)
//                .add("correct", bestCorrect)
//                .add("cover", bestCover)
//                .toString();

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
        int result = 0;
        for (int i : a) result += i;
        return result;
    }
}
