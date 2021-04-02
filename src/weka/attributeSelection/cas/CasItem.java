package weka.attributeSelection.cas;

import weka.classifiers.rules.odri.OdriUtils;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.Serializable;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Created by suhel on 19/03/16.
 */


public class CasItem implements Serializable {
  static final long serialVersionUID = 424878435065750583L;

  public final static int EMPTY = -1;
  protected final int[] attIndexes;
  protected final int[] attValues;

  protected int correct;
  protected int errors;
  protected int covers;


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CasItem casItem = (CasItem) o;
    return Arrays.equals(attIndexes, casItem.attIndexes)
            && Arrays.equals(attValues, casItem.attValues);
  }


  @Override
  public int hashCode() {
    int result = Arrays.hashCode(attIndexes);
    result = 31 * result + Arrays.hashCode(attValues);
    return result;
  }

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

  public int size() {
    return attIndexes.length;
  }


  public CasItem(int[] attIndexes, int[] attValues) {
    assert attIndexes.length == attValues.length;
    this.attIndexes = Arrays.copyOf(attIndexes, attIndexes.length);
    this.attValues = Arrays.copyOf(attValues, attValues.length);
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


  public static boolean contains(int[] arr, int att) {
    for (int i = 0; i < arr.length; i++)
      if (arr[i] == att) return true;
    return false;
  }

  public CasItem addTest(int att, int val) {
    if (contains(attIndexes, att))
      return null;
    return new CasItem(addElement(attIndexes, att),
            addElement(attValues, val));
  }

  public void updateErrorsWith(CasMaxIndex maxIndex) {
    this.correct = maxIndex.getBestCorrect();
    this.covers = maxIndex.getBestCover();
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


  public double getLenghtCorrectWeighted() {
    return this.correct * this.size();
  }

  public double getLenghtCoverWeighted() {
    return this.covers * this.size();
  }

  @Override
  public String toString() {

    return new StringJoiner(", ",
            this.getClass().getSimpleName() + "[", "]")
            .add("index= " + Arrays.toString(attIndexes))
            .add("val=" + Arrays.toString(attValues))
            .add("correct=" + correct)
            .add("errors= " + errors)
            .add("covers=" + covers)
            .toString();

  }

  public String toString(Instances data, int maxDigits) {

    String pattern = "( " + OdriUtils.formatIntPattern(maxDigits) + " , %.2f ) ";


    StringBuilder sb = new StringBuilder();
    sb.append(String.format(pattern, correct, getConfidence()));
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


