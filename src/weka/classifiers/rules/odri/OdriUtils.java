package weka.classifiers.rules.odri;

//import com.google.common.base.Joiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Suhel on 09/11/2020.
 */
public class OdriUtils {

  static Logger logger = LoggerFactory.getLogger(OdriUtils.class.getName());

  /**
   * Return array containing number of items in each corresponding attribute
   *
   * @param data
   * @return number of distinct items in each attributes
   */
  public static int[] countItemsInAttributes(Instances data) {
    int[] result = new int[data.numAttributes()];
    for (int i = 0; i < result.length; i++) {
      result[i] = data.attribute(i).numValues();
    }
    return result;
  }

  /**
   * Map each instance in data into its internal presentation values, cast double into int because
   * the data type is "nominal", and "numeric" attributes should be "discretized" first
   *
   * @param data
   * @return pair of
   * key: List of int arrays represent the internal values of data items
   * value: int array to hold the frequency of each label
   */
  public static Pair<Collection<int[]>, int[]> mapIdataAndLabels(Instances data) {
    final int labelIndex = data.classIndex();
    assert labelIndex == data.numAttributes() - 1;

    Collection<int[]> lineData = data.stream()
            .map(OdriUtils::toIntArray)
            .collect(Collectors.toList());

    int[] labelsCount = new int[data.attribute(data.classIndex()).numValues()];
    lineData.stream()
            .mapToInt(row -> row[labelIndex])
            .forEach(index -> labelsCount[index]++);

    return new Pair(lineData, labelsCount);
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

  /**
   * map and instance to ints internal representation in Instances class in "int" format rather than double
   *
   * @param instance
   * @return
   */
  public static int[] toIntArray(Instance instance) {
    int[] result = new int[instance.numValues()]; //assert numValues == numAttributes data is not sparse
    for (int i = 0; i < result.length; i++) {
      result[i] = (int) instance.value(i);
    }
    return result;
  }

  public static String formatIntPattern(int maxDigit) {
    int digits = (int) (Math.ceil(Math.log10(maxDigit)));
    return "%0" + digits + "d";
  }

  public static String[] attributeValues(Attribute att) {
    String[] result = new String[att.numValues()];
    for (int i = 0; i < result.length; i++) {
      result[i] = att.value(i);
    }
    return result;
  }

  public static StringBuilder print(Collection<int[]> c) {
    StringBuilder sb = new StringBuilder();
    for (Iterator<int[]> iter = c.iterator(); iter.hasNext(); ) {
      sb.append(Arrays.toString(iter.next()) + "\n");
    }
    return sb;
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


  /**
   * @param numItems
   * @param data
   * @param availableAttributes
   * @return counter of frequencies of labels as an array [i][j][k] :
   * i: attribute
   * j: item in attribute i
   * k: label class
   * (att, item, label) -> count
   */
  public static int[][][] countStepOdri(
          int[] numItems,
          int[][] data,
          int[] availableAttributes,
          int[] availableLines) {

    int labelIndex = numItems.length - 1;
    int numLabels = numItems[labelIndex];
    int[] labels = data[data.length - 1];

    //create array of One attributes, without the class name;
    int[][][] result = new int[numItems.length][][];

    for (int attIndex : availableAttributes) {
      result[attIndex] = new int[numItems[attIndex]][numLabels];
    }
    //fill remaining attributes with empty arrays //TODO check in maxIndex
    for (int i = 0; i < result.length; i++) {
      if (result[i] == null) result[i] = new int[0][0];
    }

    //filling
    for (int attIndex : availableAttributes) {
      int[] oneAttributeData = data[attIndex];
      for (int line : availableLines) {
        result[attIndex][oneAttributeData[line]][labels[line]]++;
      }
    }
    return result;
  }

  /**
   * @param lineData
   * @param rule
   * @param resultSize bestCover of the last MaxIndex
   */
  public static Map<Boolean, List<int[]>> coveredByRule(
          Collection<int[]> lineData, ORule rule, int resultSize) {

//        assert lineData.size() > resultSize;

//        Collection<int[]> coveredLines = new ArrayList<>(resultSize);
//        Collection<int[]> notCoveredLines = new ArrayList<>(lineData.size() - resultSize);

//        for (Iterator<int[]> iter = lineData.iterator(); iter.hasNext(); ) {
//            int[] line = iter.next();
//
//            if (rule.classify(line) == IRule.EMPTY) {
//                notCoveredLines.add(line);
//            } else {
//                coveredLines.add(line);
//            }
//        }

    Map<Boolean, List<int[]>> result = lineData.stream()
            .collect(Collectors.partitioningBy(row -> rule.canCoverInstance(row)));

//        assert coveredLines.size() == resultSize;
//        assert coveredLines.size() + notCoveredLines.size() == lineData.size();
//        return new Pair(coveredLines, notCoveredLines);
//        return new Pair(result.get(true), result.get(false));
    return result;
  }


  public static int[] intsToArray(Set<Integer> set) {
    return set.stream()
            .mapToInt(Number::intValue)
            .toArray();
  }

  public static ORuleLines calcStepOdri(int[] numItemsInAtt,
                                        int[][] data,
                                        int minOcc,
                                        int[] lines) {

    if (lines.length < minOcc) return null;


    /** Start with all attributes, does not include the label attribute*/
    Set<Integer> availableAttributes = IntStream.range(0, numItemsInAtt.length - 1)
            .boxed()
            .collect(Collectors.toSet());

    final int numLabels = numItemsInAtt[numItemsInAtt.length - 1];

    ORule rule = null;// null, Does not know the label yet
    OMaxIndex mx = null;

    int[] entryLines = lines; // start with all lines
//
    do {

      int[][][] stepCount = countStepOdri(
              numItemsInAtt,
              data,
              intsToArray(availableAttributes),
              entryLines);
      if (mx == null) {
        //For the first time
        mx = OMaxIndex.ofOdri(stepCount, minOcc, numLabels);

        if (mx.getLabel() == OMaxIndex.EMPTY) return null; //should never reach this
        rule = new ORule(mx.getLabel());
      } else {
        mx = OMaxIndex.ofOdri(stepCount,
                minOcc,
                numLabels,
                mx.getLabel());
        if (mx.getLabel() == OMaxIndex.EMPTY) break;

      }

      //found best next item
      assert mx.getLabel() != OMaxIndex.EMPTY;
      assert mx.getLabel() == rule.label;
      assert mx.getBestAtt() >= 0;
      assert mx.getBestItem() >= 0;

      availableAttributes.remove(mx.getBestAtt());

      //refine rule with more attributes conditions
      rule.addTest(mx.getBestAtt(), mx.getBestItem());
      rule.updateErrorsWith(mx);

      ORule finalRule = rule;

      entryLines = filter(entryLines, data[mx.getBestAtt()], mx.getBestItem());


    } while (rule.getErrors() > 0
            && availableAttributes.size() > 0
            && rule.getCorrect() >= minOcc
            && entryLines.length > 0); // TODO check this condition

    if (rule.getLength() == 0) {//TODO more inspection is needed here
      return null;
    }

    return new ORuleLines(rule, getNotCovered(lines, entryLines));
  }


  public static List<ORule> buildForNumRules(int[][] data,
                                             int[] numItems,
                                             boolean addDefaultRule,
                                             final int numRules,
                                             final int numInstances,
                                             final int maxNumTries) {

    OInterpolation oin = new OInterpolation();

    //start with minOcc=1
    int minOcc = 1;
    List<ORule> oRules = buildClassifierOdri(data,
            numItems,
            minOcc,
            addDefaultRule);
    logger.info("numRules for minOcc = 1 is {}", oRules.size());
    if (oRules.size() < numRules) return oRules;

    oin.addPoint(1, oRules.size());
    oin.interpolate();

    //then with minOcc= 0.02
    minOcc = (int) (numInstances * 0.02);
    oRules = buildClassifierOdri(data,
            numItems,
            minOcc,
            addDefaultRule);
    if (oRules.size() == numRules) return oRules;
    oin.addPoint(minOcc, oRules.size());
    oin.interpolate();
    logger.info("numRules for minOcc = {} is {}", minOcc, oRules.size());

    for (int i = 0; i < maxNumTries - 2; i++) {
      logger.info("iteration i = {}", i);
      minOcc = (int) Math.ceil(oin.minOcc(numRules));
      logger.info("expected minOcc = {}, for numRules = {}, rule.size={}", minOcc, numRules, oRules.size());
      oRules = buildClassifierOdri(data,
              numItems,
              minOcc,
              addDefaultRule);
      logger.info("numRules for minOcc = {} is {}", minOcc, oRules.size());
      if (oRules.size() > numRules)
        minOcc++;
      else
        minOcc--;

//      if (oRules.size() == numRules) return oRules;

      oin.addPoint(minOcc, oRules.size());
      oin.interpolate();
    }
    logger.info("final minOCC value = {}", minOcc);
    return oRules;
  }

  public static List<ORule> buildClassifierOdri(int[][] data,
                                                int[] numItems,
                                                int minOcc,
                                                boolean addDefaultRule) {
    List<ORule> rules = new ArrayList<>();
    int labelIndex = numItems.length - 1;
    int numLabels = numItems[labelIndex];

    int numInstances = data[0].length;
    final int[] allLines = IntStream.range(0, numInstances).toArray();

    int[] remainingLines = null;


    int[] lines = allLines;//new ArrayList<>(lineData);//defensive copy


    while (lines.length > 0) {

      ORuleLines rllns = calcStepOdri(numItems, data, minOcc, lines);
      if (rllns == null) break; // stop adding rules for current class. break out to the new class


      logger.trace("rule {}", rllns.rule);
      logger.trace("remaining lines={}", rllns.lines.length);

      lines = rllns.lines;
      remainingLines = lines;
      logger.trace("took {} , remains {} instances",
              rllns.rule.getCorrect(), lines.length);

      rules.add(rllns.rule);
    }

    if (addDefaultRule) {
      if (remainingLines != null && remainingLines.length > 0) {
//        ORule rule = getDefaultRule(remainingLines, data[labelIndex], numLabels);
        ORule rule = getDefaultRule(fancyIndex(data[labelIndex], lines), numLabels);

        rules.add(rule);
      }
    }

    //TODO check to add defaultRule
    assert rules.size() > 0;

    return rules;
  }


  public static int[] filter(int[] entryLines, int[] att, int item) {
    return Arrays.stream(entryLines)
            .filter(line -> att[line] == item)
            .toArray();
  }


  /**
   * entrylines is subset of allines
   *
   * @param allLines
   * @param entryLines
   * @return
   */
  public static int[] getNotCovered(int[] allLines, int[] entryLines) {
    //assume unique sorted arrays
    int[] result = new int[allLines.length - entryLines.length];
    int allIndex = 0;
    int outIndex = 0;
    for (int line : entryLines) {
      while (allLines[allIndex] != line) {
        result[outIndex++] = allLines[allIndex++];
      }
      allIndex++;
    }
    while (allIndex < allLines.length)
      result[outIndex++] = allLines[allIndex++];
    return result;
  }


  private static ORule getDefaultRule(int[] labels, int numLabels) {
    int[] freqs = new int[numLabels];
    for (int lbl : labels) {
      freqs[lbl]++;
    }

    int maxVal = Integer.MIN_VALUE;
    int maxIndex = Integer.MIN_VALUE;
    for (int i = 0; i < freqs.length; i++) {
      if (freqs[i] > maxVal) {
        maxVal = freqs[i];
        maxIndex = i;
      }
    }
    ORule rule = new ORule(maxIndex, maxVal, OMaxIndex.sum(freqs));

    return rule;
  }

  public static int[] fancyIndex(int[] all, int[] indices) {
    int[] result = new int[indices.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = all[indices[i]];
    }
    return result;
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

  public static void main(String[] args) throws IOException {
    logger.info("test logger");

//        String inFile = "/media/suhel/workspace/work/wekaprism/data/fadi.arff";
//    String inFile = "/media/suhel/workspace/work/wekaprism/data/cl.arff";
//    String inFile = "data/arff/contact-lenses.arff";
    String inFile = "data/arff/tic-tac-toe.arff";

    Instances instances = new Instances(readDataFile(inFile));

    instances.setClassIndex(instances.numAttributes() - 1);
    System.out.println(instances.numInstances());
    final int[] numberOfItems = OdriUtils.countItemsInAttributes(instances);
    int[][] data = OdriUtils.mapIdataAndLabelsToArrays(instances);

    logger.trace("original lines size = {}", data[0].length);

    List<ORule> rules = buildClassifierOdri(
            data, numberOfItems, 1, true);

    logger.info("rules generated =\n{}",
            rules.stream()
                    .map(rule -> rule.toString())
                    .collect(Collectors.joining("\n")));

  }

}


