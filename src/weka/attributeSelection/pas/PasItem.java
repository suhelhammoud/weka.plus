package weka.attributeSelection.pas;


import utils.LSet;
import utils.PrintUtils;
import weka.core.Attribute;
import weka.core.Instances;

import java.util.Arrays;
import java.util.StringJoiner;

import static utils.LSet.addElement;

public class PasItem {

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

  private int firstCorrect;

  public int getFirstCorrect() {
    return firstCorrect;
  }

  public PasItem(int label, int correct, int covers) {
    this(label);
    this.correct = correct;
    this.covers = covers;
    this.errors = covers - correct;
  }

  public PasItem(int label) {
    assert label != EMPTY;
    this.label = label;
    this.attIndexes = new int[0];
    this.attValues = new int[0];
  }


  public boolean addTest(int att, int val, int correct) {
    if (LSet.contains(attIndexes, att))
      return false;

    if (attIndexes.length == 0) {
      firstCorrect = correct;
    }
    attIndexes = addElement(attIndexes, att);
    attValues = addElement(attValues, val);

    return true;
  }


  public PasItem copy() {
    PasItem result = new PasItem(this.label);
    result.attIndexes = this.attIndexes.clone();
    result.attValues = this.attValues.clone();
    result.correct = this.correct;
    result.errors = this.errors;
    result.covers = this.covers;
    result.firstCorrect = this.firstCorrect;
    return result;
  }


  public boolean addTest(int att, int val) {
    if (LSet.contains(attIndexes, att))
      return false;
    attIndexes = addElement(attIndexes, att);
    attValues = addElement(attValues, val);
    return true;
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

  public int getLength() {
    return attIndexes.length;
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

    String pattern = "( " + PrintUtils.formatIntPattern(maxDigits) + " , %.2f ) ";


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

}
