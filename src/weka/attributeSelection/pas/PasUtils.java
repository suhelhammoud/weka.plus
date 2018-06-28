package weka.attributeSelection.pas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.rules.medri.*;
import weka.core.Instances;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PasUtils {

    //TODO use IRule now with one condition test, create separate class later.

    static Logger logger = LoggerFactory.getLogger(PasUtils.class.getName());

    public static List<Integer> buildEvaluator(Instances data,
                                               double support,
                                               double confidence) {

        logger.debug("build pas evaluator");

        Pair<Collection<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.key;
        int[] labelsCount = linesLabels.value;
//
        logger
                .trace("original lines size ={}", lineData.size());

        int[] numItems = MedriUtils.countItemsInAttributes(data);


        return null;
    }

    public static String arrayToString(double[] arr, String format) {
        return Arrays.stream(arr)
                .boxed()
                .map(d -> String.format(format, d))
                .collect(Collectors.joining(", ", "[", "]"));
    }


    public static double[] rankAttributes(List<IRule> rules,
                                          int numAttributes) {

        int totalLines = rules.stream()
                .mapToInt(rule -> rule.getCovers())
                .sum();

        double[] result = new double[numAttributes];

        for (IRule rule : rules) {
            int[] corrects = ((PasRule) rule).getCorrects();
            int finalTotalLines = totalLines;
            double[] weights = Arrays.stream(corrects)
                    .mapToDouble(c -> c * finalTotalLines)
                    .toArray();
//            final double weight = rule.getCorrect() * totalLines;
            totalLines -= rule.getCovers();

//            for (int attIndex: rule.getAttIndexes()) {
//                result[attIndex] += weight;
//            }
//
            for (int i = 0; i < rule.getAttIndexes().length; i++) {
                int attIndex = rule.getAttIndexes()[i];
                result[attIndex] += weights[i];
                break;
            }

        }

//        return IntStream.range(0, numAttributes)
//                .boxed()
//                .collect(Collectors.toMap(i -> i, j -> ra[j]));
        return result;

    }

    public static MeDRIResult evaluateAttributes(int[] numItems,
                                                 int[] labelsCount,
                                                 Collection<int[]> lineData,
                                                 int minFreq,
                                                 double minConfidence,
                                                 boolean addDefaultRule) {
        List<IRule> rules = new ArrayList<>();
        long scannedInstance = 0L;

        int labelIndex = numItems.length - 1;
        int numLabels = numItems[labelIndex];
        assert numItems[labelIndex] == labelsCount.length;

        int lineDataSize = lineData.size();

        Collection<int[]> remainingLines = null;


        Collection<int[]> lines = lineData;//new ArrayList<>(lineData);//defensive copy


        while (lineDataSize > 0) {

            IRuleLines rllns = calcStepPas(numItems, lines, minFreq, minConfidence);
            if (rllns == null) break; // stop adding rules for current class. break out to the new class
            scannedInstance += rllns.scannedInstances;


            logger.trace("rule {}", rllns.rule);
            logger.trace("remaining lines={}", rllns.lines.size());

            lines = rllns.lines;
            remainingLines = lines;
            lineDataSize -= rllns.rule.getCorrect();
            logger.trace("took {} , remains {} instances",
                    rllns.rule.getCorrect(), lineDataSize);

            rules.add(rllns.rule);
        }

        if (addDefaultRule) {
            if (remainingLines != null && remainingLines.size() > 0) {
                scannedInstance += remainingLines.size();
                IRule rule = getDefaultRule(remainingLines, labelIndex, numLabels);
                rules.add(rule);
            }
        }

        //TODO check to add defaultRule
        assert rules.size() > 0;
        MeDRIResult result = new MeDRIResult();
        result.setRules(rules);
        result.setScannedInstances(scannedInstance);
        return result;
    }

    public static MeDRIResult evaluateAttributesDemo(int[] itemsInAttribute,
                                                     int[] labelsCount,
                                                     Collection<int[]> lineData,
                                                     int minFreq,
                                                     double minConfidence,
                                                     boolean addDefaultRule) {
        List<IRule> rules = new ArrayList<>();
        long scannedInstance = 0L;

        int labelIndex = itemsInAttribute.length - 1;
        int numLabels = itemsInAttribute[labelIndex];
        assert itemsInAttribute[labelIndex] == labelsCount.length;

        int lineDataSize = lineData.size();
        logger.debug("initial number of instances = {}", lineData.size());

        Collection<int[]> remainingLines = null;


        Collection<int[]> lines = lineData;//new ArrayList<>(lineData);//defensive copy

        int iteration = 1;
        while (lineDataSize > 0) {
            IRuleLines rllns = calcStepPasDemo(itemsInAttribute, lines, minFreq, minConfidence);
            if (rllns == null) break; // stop adding rules for current class. break out to the new class

            logger.debug("iteration = {}", iteration);
            logger.debug("Rule number {} = {}", iteration, rllns.rule.toString());
            scannedInstance += rllns.scannedInstances;


            logger.trace("remaining lines = {}", rllns.lines.size());

            lines = rllns.lines;
            remainingLines = lines;
            lineDataSize -= rllns.rule.getCorrect();
            logger.trace("took {} , remains {} instances",
                    rllns.rule.getCorrect(), lineDataSize);

            rules.add(rllns.rule);
            iteration++;
        }

        if (addDefaultRule) {
            logger.trace("add default rule that covers {} instances"
                    , remainingLines.size());
            if (remainingLines != null && remainingLines.size() > 0) {
                scannedInstance += remainingLines.size();
                IRule rule = getDefaultRule(remainingLines, labelIndex, numLabels);
                rules.add(rule);
            }
        }

        //TODO check to add defaultRule
        assert rules.size() > 0;
        MeDRIResult result = new MeDRIResult();
        result.setRules(rules);
        result.setScannedInstances(scannedInstance);
        return result;
    }


    public static double[] normalizeVector(double[] values) {
        double sum = Arrays.stream(values).sum();
        return Arrays.stream(values)
                .map(value -> value / sum)
                .toArray();
    }
// public static double[] normalizeVector(double[] values) {
//        double sqrtSumSquares = Math.sqrt(
//                Arrays.stream(values)
//                        .map(value -> value * value)
//                        .sum());
//        return Arrays.stream(values)
//                .map(value -> value / sqrtSumSquares)
//                .toArray();
//    }

    public static IRuleLines calcStepPas(int[] numItemsInAtt,
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

        PasRule rule = null;// null, Does not know the label yet
        MaxIndex mx = null;

        Collection<int[]> entryLines = lineData; // start with all lines
        Collection<int[]> notCoveredLines = new ArrayList<>(lineData.size());//none covered
        do {

            int[][][] stepCount = countStep(numItemsInAtt,
                    entryLines,
                    intsToArray(availableAttributes));
            if (mx == null) {
                //For the first time
                mx = MaxIndex.ofMeDRI(stepCount, minFreq, minConfidence);

                if (mx.getLabel() == MaxIndex.EMPTY) return null;
                rule = new PasRule(mx.getLabel());
            } else {
                mx = MaxIndex.ofMeDRI(stepCount,
                        minFreq,
                        minConfidence,
                        mx.getLabel());
                if (mx.getLabel() == MaxIndex.EMPTY) break;

            }

            //found best next item
            assert mx.getLabel() != MaxIndex.EMPTY;
            assert mx.getLabel() == rule.label;
            assert mx.getBestAtt() >= 0;
            assert mx.getBestItem() >= 0;

            availableAttributes.remove(mx.getBestAtt());

            //refine rule with more attributes conditions
            rule.addTest(mx.getBestAtt(), mx.getBestItem(), mx.getBestCorrect());
            rule.updateWith(mx);

            IRule finalRule = rule;
            Map<Boolean, List<int[]>> coveredLines = entryLines.stream()
                    .collect(Collectors.partitioningBy(row -> finalRule.canCoverInstance(row)));

            notCoveredLines.addAll(coveredLines.get(false));

            entryLines = coveredLines.get(true);

        } while (rule.getErrors() > 0
                && availableAttributes.size() > 0
                && rule.getCorrect() >= minFreq);

        if (rule.getLenght() == 0) {//TODO more inspection is needed here
            return null;
        }

        return new IRuleLines(rule, notCoveredLines, 0);
    }


    public static IRuleLines calcStepPasDemo(int[] numItemsInAtt,
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

        PasRule rule = null;// null, Does not know the label yet
        MaxIndex mx = null;

        Collection<int[]> entryLines = lineData; // start with all lines
        Collection<int[]> notCoveredLines = new ArrayList<>(lineData.size());//none covered
        do {

            int[][][] stepCount = countStep(numItemsInAtt,
                    entryLines,
                    intsToArray(availableAttributes));
            if (mx == null) {
                //For the first time
                mx = MaxIndex.ofMeDRI(stepCount, minFreq, minConfidence);

                if (mx.getLabel() == MaxIndex.EMPTY) return null;
                rule = new PasRule(mx.getLabel());
            } else {
                mx = MaxIndex.ofMeDRI(stepCount,
                        minFreq,
                        minConfidence,
                        mx.getLabel());
                if (mx.getLabel() == MaxIndex.EMPTY) break;

            }

            //found best next item
            assert mx.getLabel() != MaxIndex.EMPTY;
            assert mx.getLabel() == rule.label;
            assert mx.getBestAtt() >= 0;
            assert mx.getBestItem() >= 0;

            availableAttributes.remove(mx.getBestAtt());

            //refine rule with more attributes conditions
            rule.addTest(mx.getBestAtt(), mx.getBestItem(), mx.getBestCorrect());
            rule.updateWith(mx);

            IRule finalRule = rule;
            Map<Boolean, List<int[]>> coveredLines = entryLines.stream()
                    .collect(Collectors.partitioningBy(row -> finalRule.canCoverInstance(row)));

            notCoveredLines.addAll(coveredLines.get(false));

            entryLines = coveredLines.get(true);

        } while (rule.getErrors() > 0
                && availableAttributes.size() > 0
                && rule.getCorrect() >= minFreq);

        if (rule.getLenght() == 0) {//TODO more inspection is needed here
            return null;
        }

        return new IRuleLines(rule, notCoveredLines, 0);
    }

    /**
     * Gets the majority class in the labels of the remaining instances, do not check attributes
     *
     * @param lines
     * @param labelIndex
     * @param numLabels
     * @return
     */
    private static IRule getDefaultRule(Collection<int[]> lines, int labelIndex, int numLabels) {
        //TODO find default reamining attribute instead
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
        IRule rule = new IRule(maxIndex, maxVal, MaxIndex.sum(freqs));

        return rule;
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


    public static int[] intsToArray(Set<Integer> set) {
        return set.stream()
                .mapToInt(Number::intValue)
                .toArray();
    }

}
