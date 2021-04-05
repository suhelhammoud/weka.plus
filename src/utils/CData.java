package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.cas.CUtils;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.util.Arrays;

import static utils.LSet.*;
import static utils.LSet.count;

public class CData {

  static Logger logger = LoggerFactory.getLogger(CData.class.getName());

  final public int[][] data;
  final public int[] labels;
  final public int labelIdex;
  final public int[] numItems;
  final public int numLabels;
  final public int numAttributes;


  public CData(int[][] data, int[] numItems) {
    this.data = data;
    this.numItems = numItems;
    this.labelIdex = data.length - 1;
    this.labels = data[labelIdex];
    this.numLabels = numItems[labelIdex];
    this.numAttributes = labelIdex;
  }

  public int[] att(int i) {
    return data[i];
  }

  public int[][] countAttLbl(int i) {
    return LSet.count(data[i], numItems[i], labels, numLabels);
  }

  public int[][] countAttLbl(int i, int[] lines) {
    return LSet.count(data[i], lines, numItems[i], labels, numLabels);
  }

  public int[] countItems(int i) {
    return LSet.count(data[i], numItems[i]);
  }

  public int[] countItems(int i, int[] lines) {
    return LSet.count(data[i], lines, numItems[i]);
  }

  public int[][] splitAtt(int i, int[] lines, int[] itemCount) {
    return LSet.split(data[i], lines, itemCount);
  }

  public int[][] splitAtt(int i, int[] lines) {
    return LSet.split(data[i], lines, numItems[i]);
  }

  public int[][] splitAtt(int i) {
    return LSet.split(data[i], numItems[i]);
  }

  public int correct(int i, int[] lines) {
    int[][] itemsLabels = countAttLbl(i, lines);
    return LSet.correct(itemsLabels);
  }

  public static CData of(Instances instances) {
    int[][] data = getData(instances);
    int[] numItems = getItemsNumber(instances);
    return new CData(data, numItems);
  }


  public static CData of(String filename) {
    return of(InstancesUtils.instancesOf(filename));
  }


  /**
   * Return array containing number of items in each corresponding attribute
   *
   * @param data
   * @return number of distinct items in each attributes
   */
  public static int[] getItemsNumber(Instances data) {
    int[] result = new int[data.numAttributes()];
    for (int i = 0; i < result.length; i++) {
      result[i] = data.attribute(i).numValues();
    }
    return result;
  }

  public static int[][] getData(Instances data) {
    assert data.classIndex() == data.numAttributes() - 1;
    int numAttributes = data.numAttributes();
    int numInstances = data.numInstances();
    int[][] result = new int[numAttributes][numInstances];
    for (int line = 0; line < numInstances; line++) {
      Instance instance = data.instance(line);
      for (int attIndex = 0; attIndex < numAttributes; attIndex++) {
        result[attIndex][line] = (int) instance.value(attIndex);
      }
    }
    return result;
  }

  public static void buildEvaluator(
          CData cdata,
          double mxErr) {


    int[] corrects = new int[cdata.data.length - 1];

    for (int attIndex = 0; attIndex < cdata.numAttributes; attIndex++) {
      int[][] labelCounts = cdata.countAttLbl(attIndex);
      logger.info("labelCounts :\n{}", PrintUtils.print(labelCounts));
//      int correct = correct(labelCounts);
//      corrects[attIndex] += correct;
      int cover = sum(labelCounts);
      int[] itemCounts = cdata.countItems(attIndex);

    }


    System.out.println("corrects = " + Arrays.toString(corrects));

  }


  /**
   * @param data
   * @param numItems
   * @param availableAttributes
   * @return counter of frequencies of labels as an array [i][j][k] :
   * i: attribute
   * j: item in attribute i
   * k: label class
   * (att, item, label) -> count
   */
  public static int[][][] countStep(
          int[][] data,
          int[] lines,
          int[] numItems,
          int[] availableAttributes
  ) {

    assert data.length == numItems.length;
    final int labelIndex = numItems.length - 1;
    final int numLabels = numItems[labelIndex];
    int[] labels = data[data.length - 1];

    //create array of One attributes, without the class name;
    int[][][] result = new int[data.length][][];

    for (int attIndex : availableAttributes) {
      result[attIndex] = new int[numItems[attIndex]][numLabels];
    }
    for (int attIndex : availableAttributes) {
      result[attIndex] = count(data[attIndex],
              lines,
              numItems[attIndex],
              labels,
              numLabels);
    }

    //fill remaining attributes with empty arrays //TODO check in maxIndex
    for (int i = 0; i < result.length; i++) {
      if (result[i] == null) result[i] = new int[0][0];
    }

    return result;
  }

  public static int[][][] countStep(
          int[][] data,
          int[] numItems
          ) {

    assert data.length == numItems.length;
    final int labelIndex = numItems.length - 1;
    final int numLabels = numItems[labelIndex];
    int[] labels = data[data.length - 1];

    //create array of One attributes, without the class name;
    int[][][] result = new int[data.length][][];

    for (int attIndex = 0; attIndex < numItems.length ; attIndex++) {
      result[attIndex] = new int[numItems[attIndex]][numLabels];
    }

    for (int attIndex = 0; attIndex < numItems.length ; attIndex++) {
      result[attIndex] = count(data[attIndex],
              numItems[attIndex],
              labels,
              numLabels);
    }
//
//    //fill remaining attributes with empty arrays //TODO check in maxIndex
//    for (int i = 0; i < result.length; i++) {
//      if (result[i] == null) result[i] = new int[0][0];
//    }

    return result;
  }


  public int[][][] countStep( int[] lines,
                              int[] availableAttributes){
    return countStep(data, lines, numItems, availableAttributes);
  }


  public static void main(String[] args) {
    String inFile = "data/arff/contact-lenses.arff";
    CData cdata = CData.of(inFile);
    buildEvaluator(cdata, 0);
    int[][] splitted = cdata.splitAtt(3);


    System.out.println(PrintUtils.print(splitted));
    ;
    System.out.println(Arrays.toString(cdata.att(3)));

    int c1 = cdata.correct(3, splitted[0]);
    System.out.println("c1 = " + c1);
    int c2 = cdata.correct(3, splitted[1]);
    System.out.println("c2 = " + c2);

  }

}
