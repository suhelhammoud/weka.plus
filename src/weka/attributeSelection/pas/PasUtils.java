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


    public static double[] buildEvaluator1st(Instances data,
                                             double support,
                                             double confidence) throws Exception {

        int classIndex = data.classIndex();
        int numInstances = data.numInstances();
        int numClasses = data.attribute(classIndex).numValues();

        //TODO look into Chi implementation of contingency tables
        logger.debug("buildEvaluator1st with data ={} of size={}", data.relationName(), data.numInstances());

        assert data.classIndex() == data.numAttributes() - 1;

        data.setClassIndex(data.numAttributes() - 1);

        Pair<Collection<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.key;
        int[] labelsCount = linesLabels.value;
//
        logger.trace("original lines size ={}", lineData.size());

        int[] numItems = MedriUtils.countItemsInAttributes(data);

        int minFreq = (int) Math.ceil( support * data.numInstances() + 1.e-6);
        logger.debug("minFreq used = {}", minFreq);

        List<PasItem> items = PasUtils.evaluateAttributesItems(numItems,
                labelsCount,
                lineData,
                minFreq,
                confidence,
                false);

        double[] rawRanks = PasUtils.rankAttributesFromItems(
                items,
                data.numAttributes() - 1);//exclude label class attribute


        return PasUtils.normalizeVector(rawRanks);
//
//        if (m_debug) {
//            String msg = printResult(result.getRules(),
//                    data,
//                    Arrays.stream(rawRanks).sum(),
//                    data.numAttributes() - 1);
//            logger.info(msg);
//        }
    }

    public static double[] rankAttributes(List<PasItem> rules,
                                          int numAttributes) {

        int totalLines = rules.stream()
                .mapToInt(rule -> rule.getCovers())
                .sum();

        double[] result = new double[numAttributes];

        for (IRule rule : rules) {
            int[] corrects = ((PasItem) rule).getCorrects();
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

    public static double[] rankAttributesFromItems(List<PasItem> items,
                                                   int numAttributes) {

        int totalLines = items.stream()
                .mapToInt(rule -> rule.getCovers())
                .sum();

        double[] result = new double[numAttributes];

        for (PasItem item : items) {
            final double weight = item.getCorrect() * totalLines;
            totalLines -= item.getCovers();

            result[item.getAttIndexes()[0]] += weight;
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

            Tuple<PasItem, Collection<int[]>> itmlns = calcNextItem(numItems, lines, minFreq, minConfidence);
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

    public static List<PasItem> evaluateAttributes(int[] numItems,
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

            IRuleLines rllns = calcStepPas(numItems, lines, minFreq, minConfidence);
            if (rllns == null) break; // stop adding rules for current class. break out to the new class


            logger.trace("rule {}", rllns.rule);
            logger.trace("remaining lines={}", rllns.lines.size());

            lines = rllns.lines;
            remainingLines = lines;
            lineDataSize -= rllns.rule.getCorrect();
            logger.trace("took {} , remains {} instances",
                    rllns.rule.getCorrect(), lineDataSize);

            result.add((PasItem) rllns.rule);
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

    public static List<PasItem> evaluateAttributesDemo(int[] itemsInAttribute,
                                                     int[] labelsCount,
                                                     Collection<int[]> lineData,
                                                     int minFreq,
                                                     double minConfidence,
                                                     boolean addDefaultRule) {
        List<PasItem> rules = new ArrayList<>();
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

            rules.add((PasItem) rllns.rule);
            iteration++;
        }

        if (addDefaultRule) {
            logger.trace("add default rule that covers {} instances"
                    , remainingLines.size());
            if (remainingLines != null && remainingLines.size() > 0) {
                scannedInstance += remainingLines.size();
                PasItem rule = getDefaultPasItem(remainingLines, labelIndex, numLabels);
                rules.add(rule);
            }
        }

        //TODO check to add defaultRule
        assert rules.size() > 0;
        return rules;
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

    public static Tuple<PasItem, Collection<int[]>> calcNextItem(int[] numItemsInAtt,
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
                intsToArray((availableAttributes)));

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
        if (item.getLenght() == 0) {//TODO more inspection is needed here
            return null;
        }

        return Tuple.of(item, notCoveredLines);
    }


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

        PasItem rule = null;// null, Does not know the label yet
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
                rule = new PasItem(mx.getLabel());
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

        PasItem rule = null;// null, Does not know the label yet
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
                rule = new PasItem(mx.getLabel());
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
        return new PasItem(maxIndex, maxVal, PasMax.sum(freqs));
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
        for (IRule rule : rules) {
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

        sj.add(PasUtils.arrayToString(PasUtils.normalizeVector(rawRanks),
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


    /**
     * Map each instance in data into its internal presentation values, cast double into int because
     * the data type is "nominal", and "numeric" attributes should be "discretized" first
     *
     * @param data
     * @return pair of
     * key: List of int arrays represent the internal values of data items
     * value: int array to hold the frequency of each label
     */
    public static Tuple<Collection<int[]>, int[]> mapIdataAndLabels(Instances data) {
        final int labelIndex = data.classIndex();
        assert labelIndex == data.numAttributes() - 1;

        Collection<int[]> lineData = data.stream()
                .map(MedriUtils::toIntArray)
                .collect(Collectors.toList());

        int[] labelsCount = new int[data.attribute(data.classIndex()).numValues()];
        lineData.stream()
                .mapToInt(row -> row[labelIndex])
                .forEach(index -> labelsCount[index]++);

        return Tuple.of(lineData, labelsCount);
    }

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


}
