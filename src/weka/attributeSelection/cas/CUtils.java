package weka.attributeSelection.cas;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CData;
import utils.FilesUtils;
import utils.InstancesUtils;
import weka.core.Instance;
import weka.core.Instances;
import static utils.PrintUtils.print;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.StringJoiner;

import static utils.LSet.*;

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




  public static void main(String[] args) throws Exception {
    logger.info("test logger");

//        String inFile = "/media/suhel/workspace/work/wekaprism/data/fadi.arff";
//    String inFile = "/media/suhel/workspace/work/wekaprism/data/cl.arff";
    String inFile = "data/arff/contact-lenses.arff";
//    String inFile = "data/arff/tic-tac-toe.arff";

    Instances instances = InstancesUtils.instancesOf(inFile);
    final int[] numItems = CData.getItemsNumber(instances);
    int[][] data = CData.getData(instances);
    buildEvaluator(data, numItems, 0.01);

  }
}
