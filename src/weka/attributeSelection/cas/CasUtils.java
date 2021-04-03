package weka.attributeSelection.cas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.rules.odri.*;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CasUtils {
  static Logger logger = LoggerFactory.getLogger(CasUtils.class.getName());


  public static List<CasItem> evaluateAttributes(
          int[] labelCount,
          int[] numItems,
          int[][] data,
          int minOcc,
          boolean addDefault) {

    int labelIndex = numItems.length - 1;
    int numLabels = numItems[labelIndex];

    int numInstances = data[0].length;

    final int[] allLines = IntStream.range(0, numInstances).toArray();
    int[] remainingLines = null;
    int[] lines = allLines;

    while (lines.length > 0) {
      //calc next best attribute

    }
    return null;
  }

  public Pair<CasItem, int[]> calcStepCas(int[] numItemsInAtt,
                                          int[][] data,
                                          int[] lines,
                                          int minOcc) {

    if (lines.length < minOcc) return null;

    Set<Integer> availableAttributes = IntStream.range(0, numItemsInAtt.length - 1)
            .boxed()
            .collect(Collectors.toSet());
    final int numLabels = numItemsInAtt[numItemsInAtt.length - 1];

    int[] entryLines = lines; // start with all lines

    CasMaxIndex mx = CasMaxIndex.NONE;

    do {

      int[][][] stepCount = OdriUtils.countStepOdri(
              numItemsInAtt,
              data,
              OdriUtils.intsToArray(availableAttributes),
              entryLines);

      CasMaxIndex cmi = CasMaxIndex.of(stepCount);

      int errors = countErrors(stepCount);

      return null;
    } while (true);

          /*

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

     */
  }


  private int countErrors(int[][][] stepCount) {
    return Arrays.stream(stepCount)
            .flatMapToInt(a -> Arrays.stream(a)
                    .mapToInt(item -> max(item))).sum();
  }


  public static int max(int[] a) {
    int result = a[0];
    for (int i = 1; i < a.length; i++) {
      if (result < a[i]) result = a[i];
    }
    return result;
  }

  public static int sum(int[] a) {
    int result = 0;
    for (int i : a) result += i;
    return result;
  }


  public static int[] filter(int[] entryLines, int[] att, int item) {
    return Arrays.stream(entryLines)
            .filter(line -> att[line] == item)
            .toArray();
  }

  public static int[] fancyIndex(int[] all, int[] indices) {
    return Arrays.stream(indices)
            .map(i -> all[i])
            .toArray();
//    int[] result = new int[indices.length];
//    for (int i = 0; i < indices.length; i++) {
//      result[i] = all[indices[i]];
//    }
//    return result;
  }

  List<CasItem> buildEvaluator(int[][] data,
                               int[] numItemsInAttribute,
                               double margin) {


    return null;
  }

  public static ORuleLines calcStepOdri(int[] numItemsInAtt,
                                        int[][] data,
                                        int minOcc,
                                        int[] lines) {

    if (lines.length < minOcc) return null;
//
//
//    /** Start with all attributes, does not include the label attribute*/
//    Set<Integer> availableAttributes = IntStream.range(0, numItemsInAtt.length - 1)
//            .boxed()
//            .collect(Collectors.toSet());
//
//    final int numLabels = numItemsInAtt[numItemsInAtt.length - 1];
//
//    CasItem rule = null;// null, Does not know the label yet
//    CasMaxIndex mx = null;
//
//    int[] entryLines = lines; // start with all lines
////
//    do {
//
//      int[][][] stepCount = countStepCas(
//              numItemsInAtt,
//              data,
//              intsToArray(availableAttributes),
//              entryLines);
//      if (mx == null) {
//        //TODO what about minimum confidence here?
//
//        mx = CasMaxIndex.of(stepCount);//   .ofOdri(stepCount, minOcc, numLabels);
//
//        if (mx.getLabel() == OMaxIndex.EMPTY) return null; //should never reach this
//        rule = new ORule(mx.getLabel());
//      } else {
//        mx = OMaxIndex.ofOdri(stepCount,
//                minOcc,
//                numLabels,
//                mx.getLabel());
//        if (mx.getLabel() == OMaxIndex.EMPTY) break;
//
//      }
//
//      //found best next item
//      assert mx.getLabel() != OMaxIndex.EMPTY;
//      assert mx.getLabel() == rule.label;
//      assert mx.getBestAtt() >= 0;
//      assert mx.getBestItem() >= 0;
//
//      availableAttributes.remove(mx.getBestAtt());
//
//      //refine rule with more attributes conditions
//      rule.addTest(mx.getBestAtt(), mx.getBestItem());
//      rule.updateErrorsWith(mx);
//
//      ORule finalRule = rule;
//
//      entryLines = filter(entryLines, data[mx.getBestAtt()], mx.getBestItem());
//
//
//    } while (rule.getErrors() > 0
//            && availableAttributes.size() > 0
//            && rule.getCorrect() >= minOcc
//            && entryLines.length > 0); // TODO check this condition
//
//    if (rule.getLength() == 0) {//TODO more inspection is needed here
//      return null;
//    }
//
//    return new ORuleLines(rule, difference(lines, entryLines));
    return null;
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
  public static int[][][] countStepCas(
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
   * @param data
   * @param lines          should be sorted
   * @param distinctCounts
   * @return
   */
  public static int[][] splitToItemLines(int[] data, //
                                         int[] lines,
                                         int[] distinctCounts) {
    int[][] result = new int[distinctCounts.length][];
    int[] indexes = new int[distinctCounts.length];

    for (int itemIndex = 0; itemIndex < distinctCounts.length; itemIndex++) {
      result[itemIndex] = new int[distinctCounts[itemIndex]];
    }

    for (int line : lines) {
      int itemIndex = data[line];
      result[itemIndex][indexes[itemIndex]++] = line;
    }
    for (int i = 0; i < result.length; i++) {
      result[i] = Arrays.copyOf(result[i], indexes[i]);
    }
    return result;
  }


  /**
   * Find the remaining set of items, lines \ sub
   * ! IMPORTANT this method assumes unique sorted arrays
   *
   * @param lines
   * @param sub
   * @return
   */
  public static int[] difference(int[] lines, int[] sub) {
    //unique sorted arrays
    int[] result = new int[lines.length - sub.length];
    int allIndex = 0;
    int outIndex = 0;
    for (int line : sub) {
      while (lines[allIndex] != line) {
        result[outIndex++] = lines[allIndex++];
      }
      allIndex++;
    }
    while (allIndex < lines.length)
      result[outIndex++] = lines[allIndex++];
    return result;
  }


  public static int[] intsToArray(Set<Integer> set) {
    return set.stream()
            .mapToInt(Number::intValue)
            .toArray();
  }





  public static void testsplit(String[] args) {
    int[] attributeData = new int[]{0, 2, 3, 3, 0, 0, 3, 1, 2, 3};
//    int[] entryLines = IntStream.range(0, 9).toArray();
    int[] entryLines = IntStream.range(0, 5).toArray();
    int[] numItems = new int[]{3, 1, 2, 4};
    int[][] splits = splitToItemLines(attributeData, entryLines, numItems);
    for (int[] split : splits) {
      System.out.println(Arrays.toString(split));
    }
  }

}
