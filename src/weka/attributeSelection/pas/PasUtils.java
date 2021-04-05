package weka.attributeSelection.pas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FilesUtils;
import utils.LSet;
import utils.Pair;
import utils.PrintUtils;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static utils.LSet.sum;

public class PasUtils {

  //TODO use IRule now with one condition test, create separate class later.

  static Logger logger = LoggerFactory.getLogger(PasUtils.class.getName());

  public static double[] rankAttributes(List<PasItem> items,
                                        int numAttributes,
                                        PasMethod pasMethod) {

    int totalLines = items.stream()
            .mapToInt(rule -> rule.getCovers())
            .sum();

    double[] result = new double[numAttributes];

    for (PasItem item : items) {
      final double weight = item.getCorrect() * totalLines;
      final double weightFirst = item.getFirstCorrect() * totalLines;

      totalLines -= item.getCovers();

      switch (pasMethod) {
        case rules:
          for (int aIndex : item.getAttIndexes()) {
            result[aIndex] += weight;
          }
          break;

        case rules1st:
          result[item.getAttIndexes()[0]] += weightFirst;
          break;

        case items:
          result[item.getAttIndexes()[0]] += weight;
      }
    }
    return result;

  }

  public static List<PasItem> evaluateAttributesItems(int[] numItems,
                                                      int[] labelsCount,
                                                      Collection<int[]> lineData,
                                                      int minFreq,
                                                      double minConfidence,
                                                      boolean addDefaultItem) {
    List<PasItem> items = new ArrayList<>();

    int labelIndex = numItems.length - 1;
    int numLabels = numItems[labelIndex];
    assert numItems[labelIndex] == labelsCount.length;

    int lineDataSize = lineData.size();

    Collection<int[]> remainingLines = null;


    Collection<int[]> lines = lineData;//new ArrayList<>(lineData);//defensive copy


    while (lineDataSize > 0) {

      Pair<PasItem, Collection<int[]>> itmlns = calcStepItem(numItems, lines, minFreq, minConfidence);
      if (itmlns == null) break; // stop adding rules for current class. break out to the new class

      logger.trace("rule {}", itmlns.k);
      logger.trace("remaining lines={}", itmlns.v.size());

      lines = itmlns.v;
      remainingLines = lines;
      lineDataSize -= itmlns.k.getCorrect();
      logger.trace("took {} , remains {} instances",
              itmlns.k.getCorrect(), lineDataSize);

      items.add(itmlns.k);
    }

    if (addDefaultItem) {
      if (remainingLines != null && remainingLines.size() > 0) {
        PasItem item = getDefaultPasItem(remainingLines, labelIndex, numLabels);
        items.add(item);
      }
    }

    //TODO check to add defaultRule
    assert items.size() > 0;
    return items;
  }


  public static List<PasItem> evaluateAttributesRules(int[] numItems,
                                                      int[] labelsCount,
                                                      Collection<int[]> lineData,
                                                      int minFreq,
                                                      double minConfidence,
                                                      boolean addDefaultRule) {
    List<PasItem> result = new ArrayList<>();

    int labelIndex = numItems.length - 1;
    int numLabels = numItems[labelIndex];
    assert numItems[labelIndex] == labelsCount.length;

    int lineDataSize = lineData.size();

    Collection<int[]> remainingLines = null;


    Collection<int[]> lines = lineData;//new ArrayList<>(lineData);//defensive copy


    while (lineDataSize > 0) {

      Pair<PasItem, Collection<int[]>> rllns = calcStepRule(numItems, lines, minFreq, minConfidence);
      if (rllns == null) break; // stop adding rules for current class. break out to the new class


      logger.trace("rule {}", rllns.k);
      logger.trace("remaining lines={}", rllns.v.size());

      lines = rllns.v;
      remainingLines = lines;
      lineDataSize -= rllns.k.getCorrect();
      logger.trace("took {} , remains {} instances",
              rllns.k.getCorrect(), lineDataSize);

      result.add(rllns.k);
    }

    if (addDefaultRule) {
      if (remainingLines != null && remainingLines.size() > 0) {
        PasItem rule = getDefaultPasItem(remainingLines, labelIndex, numLabels);
        result.add(rule);
      }
    }

    //TODO check to add defaultRule
    assert result.size() > 0;
    return result;
  }


  public static List<PasItem> evaluateAttributesRules1st(int[] numItems,
                                                         int[] labelsCount,
                                                         Collection<int[]> lineData,
                                                         int minFreq,
                                                         double minConfidence,
                                                         boolean addDefaultRule) {
    List<PasItem> result = new ArrayList<>();

    int labelIndex = numItems.length - 1;
    int numLabels = numItems[labelIndex];
    assert numItems[labelIndex] == labelsCount.length;

    int lineDataSize = lineData.size();

    Collection<int[]> remainingLines = null;


    Collection<int[]> lines = lineData;//new ArrayList<>(lineData);//defensive copy


    while (lineDataSize > 0) {

      Pair<PasItem, Collection<int[]>> rllns = calcStepRule(numItems, lines, minFreq, minConfidence);
      if (rllns == null) break; // stop adding rules for current class. break out to the new class


      logger.trace("rule {}", rllns.k);
      logger.trace("remaining lines={}", rllns.v.size());

      lines = rllns.v;
      remainingLines = lines;
      lineDataSize -= rllns.k.getCorrect();
      logger.trace("took {} , remains {} instances",
              rllns.k.getCorrect(), lineDataSize);

      result.add(rllns.k);
    }

    if (addDefaultRule) {
      if (remainingLines != null && remainingLines.size() > 0) {
        PasItem rule = getDefaultPasItem(remainingLines, labelIndex, numLabels);
        result.add(rule);
      }
    }

    //TODO check to add defaultRule
    assert result.size() > 0;

    return result;
  }


  public static Pair<PasItem, Collection<int[]>> calcStepItem(int[] numItemsInAtt,
                                                              Collection<int[]> lineData,
                                                              int minFreq,
                                                              double minConfidence) {

    if (lineData.size() < minFreq) return null;

    /** Start with all attributes, does not include the label attribute*/
    Set<Integer> availableAttributes = IntStream.range(0, numItemsInAtt.length - 1)
            .boxed()
            .collect(Collectors.toSet());

    int[][][] stepCount = countStep(numItemsInAtt,
            lineData,
            LSet.intsToArray((availableAttributes)));

    PasMax mx = PasMax.ofThreshold(stepCount, minFreq, minConfidence);
    if (mx.getLabel() == PasMax.EMPTY)
      return null; //TODO not reached, check carefully

    //found best next item
    assert mx.getLabel() != PasMax.EMPTY;
    assert mx.getBestAtt() >= 0;
    assert mx.getBestItem() >= 0;

    //rule with more attributes conditions
    final PasItem item = new PasItem(mx.getLabel(), mx.getBestCorrect(), mx.getBestCover());
    item.addTest(mx.getBestAtt(), mx.getBestItem(), mx.getBestCorrect());

    List<int[]> notCoveredLines = lineData.stream()
            .filter(row -> !item.canCoverInstance(row))
            .collect(Collectors.toList());
    if (item.getLength() == 0) {//TODO more inspection is needed here
      return null;
    }

    return Pair.of(item, notCoveredLines);
  }


  public static Pair<PasItem, Collection<int[]>> calcStepRule(int[] numItemsInAtt,
                                                              Collection<int[]> lineData,
                                                              int minFreq,
                                                              double minConfidence) {

    if (lineData.size() < minFreq) return null;

//        int labelIndex = countItemsInAttributes.length - 1;
//        int numLabels = countItemsInAttributes[labelIndex];

    /** Start with all attributes, does not include the label attribute*/
    Set<Integer> availableAttributes = IntStream.range(0, numItemsInAtt.length - 1)
            .boxed()
            .collect(Collectors.toSet());

//        Set<Integer> avAtts = new LinkedHashSet<>();
//        for (int i = 0; i < labelIndex; i++) avAtts.add(i);

    PasItem rule = null;// null, Does not know the label yet
    PasMax mx = null;

    Collection<int[]> entryLines = lineData; // start with all lines
    Collection<int[]> notCoveredLines = new ArrayList<>(lineData.size());//none covered
    do {

      int[][][] stepCount = countStep(numItemsInAtt,
              entryLines,
              LSet.intsToArray(availableAttributes));
      if (mx == null) {
        //For the first time
        mx = PasMax.ofThreshold(stepCount, minFreq, minConfidence);

        if (mx.getLabel() == PasMax.EMPTY) return null;
        rule = new PasItem(mx.getLabel());
      } else {
        mx = PasMax.ofThreshold(stepCount,
                minFreq,
                minConfidence,
                mx.getLabel());
        if (mx.getLabel() == PasMax.EMPTY) break;

      }

      //found best next item
      assert mx.getLabel() != PasMax.EMPTY;
      assert mx.getLabel() == rule.label;
      assert mx.getBestAtt() >= 0;
      assert mx.getBestItem() >= 0;

      availableAttributes.remove(mx.getBestAtt());

      //refine rule with more attributes conditions
      rule.addTest(mx.getBestAtt(), mx.getBestItem(), mx.getBestCorrect());
      rule.updateWith(mx);

      PasItem finalRule = rule;
      Map<Boolean, List<int[]>> coveredLines = entryLines.stream()
              .collect(Collectors.partitioningBy(row -> finalRule.canCoverInstance(row)));

      notCoveredLines.addAll(coveredLines.get(false));

      entryLines = coveredLines.get(true);

    } while (rule.getErrors() > 0
            && availableAttributes.size() > 0
            && rule.getCorrect() >= minFreq);

    if (rule.getLength() == 0) {//TODO more inspection is needed here
      return null;
    }

    return Pair.of(rule, notCoveredLines);
  }


  /**
   * Gets the majority class in the labels of the remaining instances, do not check attributes
   *
   * @param lines
   * @param labelIndex
   * @param numLabels
   * @return
   */
  private static PasItem getDefaultPasItem(Collection<int[]> lines,
                                           int labelIndex,
                                           int numLabels) {
    int[] freqs = new int[numLabels];
    for (int[] line : lines) {
      freqs[line[labelIndex]]++;
    }
    int maxVal = Integer.MIN_VALUE;
    int maxIndex = Integer.MIN_VALUE;
    for (int i = 0; i < freqs.length; i++) {
      if (freqs[i] > maxVal) {
        maxVal = freqs[i];
        maxIndex = i;
      }
    }
    return new PasItem(maxIndex, maxVal, sum(freqs));
  }

  /**
   * @param attValues
   * @param lineData
   * @param availableAttributes
   * @return counter of frequencies of labels as an array [i][j][k] :
   * i: attribute
   * j: item in attribute i
   * k: label class
   * (att, item, label) -> count
   */
  public static int[][][] countStep(int[] attValues, Collection<int[]> lineData, int[] availableAttributes) {

    int labelIndex = attValues.length - 1;
    int numLabels = attValues[labelIndex];

    //create array of One attributes, without the class name;
    int[][][] result = new int[attValues.length][][];

    for (int att : availableAttributes) {
      result[att] = new int[attValues[att]][numLabels];
    }
    //fill remaining attributes with empty arrays
    for (int i = 0; i < result.length; i++) {
      if (result[i] == null) result[i] = new int[0][0];
    }

    //filling with values
    for (int[] row : lineData) {
      int cls = row[labelIndex];

      for (int a : availableAttributes)
        result[a][row[a]][cls]++;
    }
    return result;
  }



  //TODO delete later
  public static String printResult(List<PasItem> rules,
                                   Instances data,
                                   double sumWeights,
                                   int numAttributes) {
    StringBuilder result = new StringBuilder();

    StringJoiner sj = new StringJoiner("\n\t",
            "\n" + data.relationName() + "\n\t",
            "\n\n");
//        double sumWeights = Arrays.stream(rawRanks).sum();

    final int totalLines = rules.stream()
            .mapToInt(rule -> rule.getCovers())
            .sum();
    int availableLines = totalLines;

    double[] rawRanks = new double[numAttributes];
    for (PasItem rule : rules) {
//            sj.add("rule: " + rule.toString(data, 3));
      sj.add("rule: " + rule.toString());
      final double linesRatio = (double) availableLines / totalLines;
      final double weight = rule.getCorrect()
              * availableLines;
      sj.add(String.format("weight = %06.1f , lines = %04d, lines ratio = %3.3f",
              weight,
              availableLines,
              linesRatio));
      availableLines -= rule.getCovers();
      for (int attIndex : rule.getAttIndexes()) {
        rawRanks[attIndex] += weight;
      }
    }

    sj.add("Result attributes weights");
    sj.add(Arrays.toString(rawRanks));

    sj.add("Normalized Attributes weights");

    sj.add(PrintUtils.arrayToString(LSet.normalizeVector(rawRanks),
            "%1.3f"));

    return sj.toString();
  }

  public static String printRanks(double[] ranks) {
    StringJoiner sj = new StringJoiner("\n\t\t");
    sj.add("Attributes Ranks:");
    sj.add("att\t\tweight");
    sj.add("-------------------------");
    for (int i = 0; i < ranks.length; i++) {
      sj.add(String.format("%02d\t\t%1.3f", i + 1, ranks[i]));
    }
    return sj.toString();
  }

  public static String printCutOffPoint(double[] ranks, double threshold) {
    List<Double> pasRanks = Arrays.stream(ranks).boxed().collect(Collectors.toList());

    StringBuilder sb = new StringBuilder();
    sb.append(String.format("Cutoff point using entropy measure : %02.3f attributes",
            CutOffPoint.entropy(pasRanks)));
    sb.append("\n");
    sb.append(String.format("Cutoff point using Huffman measure : %02.3f attributes",
            CutOffPoint.huffman(pasRanks)));
    sb.append("\n");
    sb.append(String.format("Cutoff point using threshold measure : %02.3f attributes",
            CutOffPoint.threshold(pasRanks, threshold)));
    return sb.toString();
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
            .map(FilesUtils::toIntArray)
            .collect(Collectors.toList());

    int[] labelsCount = new int[data.attribute(data.classIndex()).numValues()];
    lineData.stream()
            .mapToInt(row -> row[labelIndex])
            .forEach(index -> labelsCount[index]++);

    return Pair.of(lineData, labelsCount);
  }



}
