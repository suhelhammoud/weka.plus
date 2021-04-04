package weka.attributeSelection.cas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;

import java.io.IOException;
import java.util.Arrays;

import static weka.attributeSelection.cas.LSet.*;
import static weka.attributeSelection.cas.LSet.count;

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
    int[][] data = CUtils.mapIdataAndLabelsToArrays(instances);
    int[] numItems = CUtils.getItemsNumber(instances);
    return new CData(data, numItems);
  }


  public static CData of(String filename) {
    try {
      return of(new Instances(CUtils.readDataFile(filename)));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return null; //TODO
  }


  public static void buildEvaluator(
          CData cdata,
          double mxErr) {


    int[] corrects = new int[cdata.data.length - 1];

    for (int attIndex = 0; attIndex < cdata.numAttributes; attIndex++) {
      int[][] labelCounts = cdata.countAttLbl(attIndex);
      logger.info("labelCounts :\n{}", CUtils.print(labelCounts));
//      int correct = correct(labelCounts);
//      corrects[attIndex] += correct;
      int cover = sum(labelCounts);
      int[] itemCounts = cdata.countItems(attIndex);

    }


    System.out.println("corrects = " + Arrays.toString(corrects));

  }

  public static void main(String[] args) {
    String inFile = "data/arff/contact-lenses.arff";
    CData cdata = CData.of(inFile);
    buildEvaluator(cdata, 0);
    int[][] splitted = cdata.splitAtt(3);


    System.out.println(CUtils.print(splitted));
    ;
    System.out.println(Arrays.toString(cdata.att(3)));

    int c1 = cdata.correct(3, splitted[0]);
    System.out.println("c1 = " + c1);
    int c2 = cdata.correct(3, splitted[1]);
    System.out.println("c2 = " + c2);

  }

}
