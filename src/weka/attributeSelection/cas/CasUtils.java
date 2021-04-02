package weka.attributeSelection.cas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.rules.odri.OdriUtils;
import weka.classifiers.rules.odri.Pair;

import java.lang.reflect.Array;
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


  /**
   * Optimized
   * entrylines is subset of allines
   * ! IMPORTANT assume unique sorted arrays
   *
   * @param allLines
   * @param entryLines
   * @return
   */
  public static int[] getNotCovered(int[] allLines, int[] entryLines) {
    //unique sorted arrays
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

  /**
   *
   * @param attributeData
   * @param entryLines should be sorted
   * @param numItems
   * @return
   */
  public static int[][] splitToItemLines(int[] attributeData, //
                                         int[] entryLines,
                                         int[] numItems) {
    int[][] result = new int[numItems.length][];
    int[] indexes = new int[numItems.length];

    for (int itemIndex = 0; itemIndex < numItems.length; itemIndex++) {
      result[itemIndex] = new int[numItems[itemIndex]];
    }

    for (int line : entryLines) {
      int itemIndex = attributeData[line];
      result[itemIndex][indexes[itemIndex]++] = line;
    }
    for (int i = 0; i < result.length; i++) {
      result[i] = Arrays.copyOf(result[i], indexes[i]);
    }
    return result;
  }

  public static void main(String[] args) {
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
