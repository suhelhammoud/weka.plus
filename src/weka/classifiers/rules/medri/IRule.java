package weka.classifiers.rules.medri;

import weka.attributeSelection.pas.PasMax;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.Serializable;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Created by suhel on 19/03/16.
 */


public class IRule implements Serializable {
    static final long serialVersionUID = 424878435065750583L;

    public final static int EMPTY = -1;
    public final int label;
    //TODO what about using List ??, check the update to read ratio and measure performance to decide
    protected int[] attIndexes;
    protected int[] attValues;

    protected int correct;
    protected int errors;
    protected int covers;


    public int getErrors() {
        return errors;
    }

    public int getCorrect() {
        return correct;
    }

    public int getCovers() {
        return covers;
    }

    public int[] getAttIndexes() {
        return Arrays.copyOf(attIndexes, attIndexes.length);
    }

    public double getConfidence() {
        return (double) correct / (double) covers;
    }

    public int getLength() {
        return attIndexes.length;
    }

    private void resetCounters() {
        this.correct = 0;
        this.errors = 0;
        this.covers = 0;
    }

    public IRule(int label, int correct, int covers) {
        this(label);
        this.correct = correct;
        this.covers = covers;
        this.errors = covers - correct;
    }

    public IRule(int label) {
        assert label != EMPTY;
        this.label = label;
        this.attIndexes = new int[0];
        this.attValues = new int[0];
    }

    /**
     * @param a
     * @param e
     * @return
     */
    protected static int[] addElement(int[] a, int e) {
        a = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = e;
        return a;
    }


    public IRule copy() {
        IRule result = new IRule(this.label);
        result.attIndexes = this.attIndexes.clone();
        result.attValues = this.attValues.clone();
        result.correct = this.correct;
        result.errors = this.errors;
        result.covers = this.covers;
        return result;
    }

    public static boolean contains(int[] arr, int att) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i] == att) return true;
        return false;
    }

    public boolean addTest(int att, int val) {
        if (contains(attIndexes, att))
            return false;
        attIndexes = addElement(attIndexes, att);
        attValues = addElement(attValues, val);
        return true;
    }

    public void updateWith(MaxIndex maxIndex) {
        assert this.label == maxIndex.getLabel();
        this.correct = maxIndex.getBestCorrect();
        this.covers = maxIndex.getBestCover();
        this.errors = covers - correct;
    }

   public void updateWith(PasMax pasIndex) {
        assert this.label == pasIndex.getLabel();
        this.correct = pasIndex.getBestCorrect();
        this.covers = pasIndex.getBestCover();
        this.errors = covers - correct;
    }

    public boolean canCoverInstance(int[] cond) {
        if (attIndexes.length == 0)
            return true;

        for (int index = 0; index < attIndexes.length; index++) {
            if (attValues[index] != cond[attIndexes[index]]) {
                return false;
            }
        }
        return true;
    }

    public int classify(int[] cond) {
        return canCoverInstance(cond) ?
                label :
                EMPTY;
    }


    public double getLenghtWeighted() {
        return this.correct * this.getLength();
    }

    @Override
    public String toString() {

        return new StringJoiner(", ",
                this.getClass().getSimpleName() + "[", "]")
                .add("label=" + label)
                .add("index= " + Arrays.toString(attIndexes))
                .add("val=" + Arrays.toString(attValues))
                .add("correct=" + correct)
                .add("errors= " + errors)
                .add("covers=" + covers)
                .toString();

    }

    public String toString(Instances data, int maxDigits) {

        String pattern = "( " + MedriUtils.formatIntPattern(maxDigits) + " , %.2f ) ";


        StringBuilder sb = new StringBuilder();
        sb.append(String.format(pattern, correct, getConfidence()));
        sb.append("Label = " + data.classAttribute().value(label));
        if (attIndexes.length > 0) {
            sb.append(" when \t");
            for (int i = 0; i < attIndexes.length; i++) {
                Attribute att = data.attribute(attIndexes[i]);
                String attValue = att.value(attValues[i]);
                if (i == 0)
                    sb.append(att.name() + " = " + attValue);
                else
                    sb.append(" , " + att.name() + " = " + attValue);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("done");
    }
}


