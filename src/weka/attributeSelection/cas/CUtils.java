package weka.attributeSelection.cas;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import static weka.attributeSelection.cas.LSet.*;

public class CUtils {

  static Logger logger = LoggerFactory.getLogger(CUtils.class.getName());

  public static void buildEvaluator(
          int[][] data,
          int[] numItems,
          double mxErr) {

    final int labelIndex = data.length - 1;
    final int[] labels = data[labelIndex];
    final int numLabels = numItems[labelIndex];

    int[] corrects = new int[data.length - 1];

    for (int attIndex = 0; attIndex < data.length - 1; attIndex++) {
      int[] att = data[attIndex];
      int[][] labelCounts = count(att, numItems[attIndex], labels, numLabels);
      logger.info("labelCounts :\n{}", print(labelCounts));
      int correct = correct(labelCounts);
      corrects[attIndex] += correct;
      int cover = sum(labelCounts);
      int[] itemCounts = count(att, numItems[attIndex]);

    }
    System.out.println("corrects = " + Arrays.toString(corrects));

  }


  public static int[][] mapIdataAndLabelsToArrays(Instances data) {
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


  public static String IntsJoin(String delimeter, int[] arr) {
    StringJoiner result = new StringJoiner(", ");
    for (int i : arr) {
      result.add(String.valueOf(i));
    }
    return result.toString();
  }

  public static StringBuilder print(int[][] arr) {
    StringBuilder sb = new StringBuilder();
    if (arr == null || arr.length == 0) return sb;
    for (int i = 0; i < arr.length; i++) {
      sb.append(IntsJoin(", ", arr[i]));
      sb.append("\n");
    }
    return sb;
  }

  public static StringBuilder print(int[][][] d) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < d.length; i++) {
      sb.append("********** " + i + " *********\n");
      sb.append(print(d[i]));
    }
    return sb;
  }


  public static BufferedReader readDataFile(String filename) {
    BufferedReader inputReader = null;

    try {
      inputReader = new BufferedReader(new FileReader(filename));
    } catch (FileNotFoundException ex) {
      System.err.println("File not found: " + filename);
    }

    return inputReader;
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


  public static void main(String[] args) throws Exception {
    logger.info("test logger");

//        String inFile = "/media/suhel/workspace/work/wekaprism/data/fadi.arff";
//    String inFile = "/media/suhel/workspace/work/wekaprism/data/cl.arff";
    String inFile = "data/arff/contact-lenses.arff";
//    String inFile = "data/arff/tic-tac-toe.arff";

    Instances instances = new Instances(readDataFile(inFile));
    final int[] numItems = getItemsNumber(instances);
    int[][] data = mapIdataAndLabelsToArrays(instances);
    buildEvaluator(data, numItems, 0.01);

  }
}
