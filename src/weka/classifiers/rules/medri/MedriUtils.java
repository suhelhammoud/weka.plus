package weka.classifiers.rules.medri;

//import com.google.common.base.Joiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.rules.edri.EDRIUtils;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by suhel on 17/03/16.
 */
public class MedriUtils {

    static Logger logger = LoggerFactory.getLogger(MedriUtils.class.getName());

    /**
     * Return array containing number of items in each corresponding attribute
     * @param data
     * @return number of distinct items in each attributes
     */
    public static int[] mapAttributes(Instances data) {
        int[] iattrs = new int[data.numAttributes()];
        for (int i = 0; i < iattrs.length; i++) {
            iattrs[i] = data.attribute(i).numValues();
        }
        return iattrs;
    }

    public static Pair<Collection<int[]>, int[]> mapIdataAndLabels(Instances data) {
        int labelIndex = data.classIndex();
        assert labelIndex == data.numAttributes() - 1;

        Collection<int[]> lineData = new ArrayList<>(data.numInstances());
        int[] labelsCount = new int[data.attribute(data.classIndex()).numValues()];

        int numAttrs = data.numAttributes();
        for (int i = 0; i < data.numInstances(); i++) {
            Instance instance = data.instance(i);
            final int[] row = new int[numAttrs];

            for (int att = 0; att < row.length; att++) {
                row[att] = (int) instance.value(att);
            }
            labelsCount[row[labelIndex]]++;
            lineData.add(row);
        }
        return new Pair(lineData, labelsCount);
    }

    public static int[] toIntArray(Instance instance) {
        int[] result = new int[instance.numValues()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (int) instance.value(i);
        }
        return result;
    }

    public static String formatIntPattern(int maxDigit) {
        int digits = (int) (Math.ceil(Math.log10(maxDigit)));
        return "%0" + digits + "d";
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


    public static int[][][] countStep(int[] iattrs, Collection<int[]> lineData, int[] avAtts) {

        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];

        //create array of One attributes, withoud the class name;
        int[][][] result = new int[iattrs.length][][];

        for (int attIndex : avAtts) {
            result[attIndex] = new int[iattrs[attIndex]][numLabels];
        }
        //fill remaining attributes with empty arrays
        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) result[i] = new int[0][0];
        }

        //filling with values
        for (int[] row : lineData) {
            int cls = row[labelIndex];

            for (int a : avAtts)
                result[a][row[a]][cls]++;
        }
        return result;
    }

    /**
     * @param lineData
     * @param rule
     * @param resultSize bestCover of the last MaxIndex
     */
    public static Pair<Collection<int[]>, Collection<int[]>> splitAndGetCovered(
            Collection<int[]> lineData, IRule rule, int resultSize) {

//        assert lineData.size() > resultSize;

        Collection<int[]> coveredLines = new ArrayList<>(resultSize);
        Collection<int[]> notCoveredLines = new ArrayList<>(lineData.size() - resultSize);

        for (Iterator<int[]> iter = lineData.iterator(); iter.hasNext(); ) {
            int[] line = iter.next();

            if (rule.classify(line) == IRule.EMPTY) {
                notCoveredLines.add(line);
            } else {
                coveredLines.add(line);
            }
        }
        assert coveredLines.size() == resultSize;
        assert coveredLines.size() + notCoveredLines.size() == lineData.size();
        return new Pair(coveredLines, notCoveredLines);
    }

    /**
     * Used to tranform java enumeration into java8 stream,
     * candidate usage in Instances methods :enumerateInstatnces, enumerateAttributes
     * benefit to do
     *
     * @param <T> class type
     * @param e   enumerateion
     * @return stream streamOf T type
     */
    public static <T> Stream<T> enum2Stream(Enumeration<T> e) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<T>() {
                            public T next() {
                                return e.nextElement();
                            }

                            public boolean hasNext() {
                                return e.hasMoreElements();
                            }
                        },
                        Spliterator.ORDERED), false); //TODO: parallel flag to true later!
    }

    public static int[] intsToArray(Set<Integer> set) {
        return set.stream()
                .mapToInt(Number::intValue)
                .toArray();
    }

    /**
     * @param iattrs   holds number of item for each attribute including the class attribute
     * @param lineData line data, pruned at the end to NOT COVERED instances
     * @param label    label index
     * @return
     */
    public static IRuleLines calcStepPrism(int[] iattrs, Collection<int[]> lineData, final int label) {

        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];

        /** Start with all attributes, does not include the label attribute*/
        Set<Integer> avAtts = new LinkedHashSet<>();
        for (int i = 0; i < labelIndex; i++) avAtts.add(i);
        IRule rule = new IRule(label);


        Collection<int[]> entryLines = lineData;
        Collection<int[]> notCoveredLines = new ArrayList<>(lineData.size());

        long scannedInstances = 0L;
        do {

            scannedInstances += 2 * entryLines.size();

            int[][][] stepCount = countStep(iattrs, entryLines, intsToArray(avAtts));
            MaxIndex mx = MaxIndex.ofOne(stepCount, rule.label);

            assert mx.getLabel() != MaxIndex.EMPTY;
            assert mx.getLabel() == label;


            avAtts.remove(mx.getBestAtt());
            rule.addTest(mx.getBestAtt(), mx.getBestItem());
            rule.updateWith(mx);

            Pair<Collection<int[]>, Collection<int[]>> splitResult = splitAndGetCovered(entryLines, rule, mx.bestCover);
            notCoveredLines.addAll(splitResult.value);

            entryLines = splitResult.key;

        } while (rule.getErrors() > 0 && avAtts.size() > 0);

        return new IRuleLines(rule, notCoveredLines, scannedInstances);
    }


    public static IRuleLines calcStepMeDRI(int[] iattrs, Collection<int[]> lineData,
                                           int minFreq, double minConfidence) {

        if (lineData.size() < minFreq) return null;

        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];

        /** Start with all attributes, does not include the label attribute*/
        Set<Integer> avAtts = new LinkedHashSet<>();
        for (int i = 0; i < labelIndex; i++) avAtts.add(i);
        IRule rule = null;// new IRule(label);// Does not know the label yet
        MaxIndex mx = null;

        Collection<int[]> entryLines = lineData;
        Collection<int[]> notCoveredLines = new ArrayList<>(lineData.size());
//
        long scannedInstances = 0L;
        do {
            scannedInstances += 2 * entryLines.size();

            int[][][] stepCount = countStep(iattrs, entryLines, intsToArray(avAtts));
            if (mx == null) {
                //For the first time
                mx = MaxIndex.ofMeDRI(stepCount, minFreq, minConfidence);
                if (mx.getLabel() == MaxIndex.EMPTY) return null;
                rule = new IRule(mx.getLabel());
            } else {
                mx = MaxIndex.ofSupportConfidence(stepCount, mx.getLabel(), minFreq, minConfidence);
                if (mx.getLabel() == MaxIndex.EMPTY) break;

            }


            assert mx.getLabel() != MaxIndex.EMPTY;
            assert mx.getLabel() == rule.label;
            assert mx.getBestAtt() >= 0;
            assert mx.getBestItem() >= 0;

            avAtts.remove(mx.getBestAtt());
            rule.addTest(mx.getBestAtt(), mx.getBestItem());
            rule.updateWith(mx);

            Pair<Collection<int[]>, Collection<int[]>> splitResult = splitAndGetCovered(entryLines, rule, mx.bestCover);
            notCoveredLines.addAll(splitResult.value);

            entryLines = splitResult.key;

        } while (rule.getErrors() > 0 && avAtts.size() > 0 && rule.getCorrect() >= minFreq);

        if (rule.getLenght() == 0) {//TODO more inspection is needed here
            return null;
        }

        return new IRuleLines(rule, notCoveredLines, scannedInstances);
    }

    /**
     * @param iattrs   holds number of items for each attribute including the class attribute
     * @param lineData line data, pruned at the end to NOT COVERED instances
     * @param label    label index
     * @return
     */
    public static IRuleLines calcStepEDRI(int[] iattrs, Collection<int[]> lineData, final int label,
                                          int minFreq, double minConfidence) {

        if (lineData.size() < minFreq) return null;


        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];

        /** Start with all attributes, does not include the label attribute*/
        Set<Integer> avAtts = new LinkedHashSet<>();
        for (int i = 0; i < labelIndex; i++) avAtts.add(i);
        IRule rule = new IRule(label);


        Collection<int[]> entryLines = lineData;
        Collection<int[]> notCoveredLines = new ArrayList<>(lineData.size());

        long scannedInstances = 0L;

        do {

            scannedInstances += 2 * entryLines.size();
            int[][][] stepCount = countStep(iattrs, entryLines, intsToArray(avAtts));
            MaxIndex mx = MaxIndex.ofSupportConfidence(stepCount,
                    rule.label, minFreq, minConfidence);


            if (mx.getLabel() == MaxIndex.EMPTY) {
                break;
            }

            assert mx.getLabel() != MaxIndex.EMPTY;
            assert mx.getLabel() == label;
            assert mx.getBestAtt() >= 0;
            assert mx.getBestItem() >= 0;

            avAtts.remove(mx.getBestAtt());
            rule.addTest(mx.getBestAtt(), mx.getBestItem());
            rule.updateWith(mx);

            Pair<Collection<int[]>, Collection<int[]>> splitResult = splitAndGetCovered(entryLines, rule, mx.bestCover);
            notCoveredLines.addAll(splitResult.value);

            entryLines = splitResult.key;

        } while (rule.getErrors() > 0 && avAtts.size() > 0 && rule.getCorrect() >= minFreq);

        if (rule.getLenght() == 0) {//TODO more inspection is needed here
            return null;
        }

        return new IRuleLines(rule, notCoveredLines, scannedInstances);
    }

    //TODO delete later
//    public static double[] attribToArray(Attribute att) {
//        double[] result = new double[att.numValues()];
//        for (int i = 0; i < result.length; i++) {
//            result[i] = Double.valueOf(att.value(i));
//        }
//        return result;
//    }

    public static String[] attributValues(Attribute att) {
        String[] result = new String[att.numValues()];
        for (int i = 0; i < result.length; i++) {
            result[i] = att.value(i);
        }
        return result;
    }

    public static int indexOf(double[] arr, double value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) return i;
        }
        return -1;
    }

    public static MeDRIResult buildClassifierMeDRI(int[] iattrs, int[] labelsCount, Collection<int[]> lineData,
                                                   int minFreq, double minConfidence, boolean addDefaultRule) {
        List<IRule> rules = new ArrayList<>();
        long scannedInstance = 0L;

        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];
        labelsCount = labelsCount.clone();

        int lineDataSize = lineData.size();

        Collection<int[]> remainingLines = null;


        Collection<int[]> lines = lineData;//new ArrayList<>(lineData);//defensive copy


        while (lineDataSize > 0) {
            IRuleLines lnrl = calcStepMeDRI(iattrs, lines, minFreq, minConfidence);
            if (lnrl == null) break; // stop adding rules for current class. break out to the new class
            scannedInstance += lnrl.scannedInstances;


            logger.trace("rule {}", lnrl.rule);
            logger.trace("remaining lines={}", lnrl.lines.size());

            lines = lnrl.lines;
            remainingLines = lines;
            lineDataSize -= lnrl.rule.getCorrect();
            logger.trace("took {} , remains {} instances",
                    lnrl.rule.getCorrect(), lineDataSize);

            rules.add(lnrl.rule);
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

    public static MeDRIResult buildClassifierEDRI(int[] iattrs, int[] labelsCount, Collection<int[]> lineData,
                                                  int minFreq, double minConfidence, boolean addDefaultRule) {
        List<IRule> rules = new ArrayList<>();
        long scannedInstances = 0L;

        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];

        Collection<int[]> remainingLines = null;
        for (int cls = 0; cls < numLabels; cls++) {
            logger.trace("****************************************" +
                    "\nfor class = {}", cls);
            int clsCounter = labelsCount[cls];
            logger.trace("cls {} count = {}", cls, clsCounter);
            Collection<int[]> lines = lineData;//new ArrayList<>(lineData);//defensive copy


            while (clsCounter > 0) {
                IRuleLines lnrl = calcStepEDRI(iattrs, lines, cls, minFreq, minConfidence);
                if (lnrl == null) break; // stop adding rules for current class. break out to the new class
                scannedInstances += lnrl.scannedInstances;


                logger.trace("rule {}", lnrl.rule);
                logger.trace("remaining lines={}", lnrl.lines.size());

                lines = lnrl.lines;
                remainingLines = lines;
                clsCounter -= lnrl.rule.getCorrect();
                logger.trace("took {} , remains {} instances",
                        lnrl.rule.getCorrect(), clsCounter);
                rules.add(lnrl.rule);
            }
        }
        if (addDefaultRule) {
            if (remainingLines != null && remainingLines.size() > 0) {
                scannedInstances += remainingLines.size();
                IRule rule = getDefaultRule(remainingLines, labelIndex, numLabels);
                rules.add(rule);
            }
        }

        //TODO check to add defaultRule
        assert rules.size() > 0;
        MeDRIResult result = new MeDRIResult();
        result.setRules(rules);
        result.setScannedInstances(scannedInstances);
        return result;
    }


    public static MeDRIResult buildClassifierPrism(int[] iattrs, int[] labelsCount,
                                                   Collection<int[]> lineData, boolean addDefaultRule) {
        List<IRule> rules = new ArrayList<>();
        long scannedInstances = 0L;
        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];

        for (int cls = 0; cls < numLabels; cls++) {
            logger.trace("****************************************" +
                    "\nfor class = {}", cls);
            int clsCounter = labelsCount[cls];
            logger.trace("cls {} count = {}", cls, clsCounter);
            Collection<int[]> lines = lineData;//new ArrayList<>(lineData);//defensive copy

            while (clsCounter > 0) {
                IRuleLines lnrl = calcStepPrism(iattrs, lines, cls);
                scannedInstances += lnrl.scannedInstances;
                logger.trace("rule {}", lnrl.rule);
                logger.trace("remaining lines={}", lnrl.lines.size());

                lines = lnrl.lines;
                clsCounter -= lnrl.rule.getCorrect();
                logger.trace("took {} , remains {} instances",
                        lnrl.rule.getCorrect(), clsCounter);
                rules.add(lnrl.rule);
            }
        }

        if (addDefaultRule) {
            Collection<int[]> lines = new ArrayList<>();
            for (int[] line : lineData) {
                boolean isCovered = false;
                for (IRule rule : rules) {
                    int cls = rule.classify(line);
                    if (cls != IRule.EMPTY) {
                        isCovered = true;
                        break;
                    }
                }
                if (!isCovered) {
                    lines.add(line);
                }
            }
            if (lines.size() > 0) {
                scannedInstances += lines.size();
                rules.add(getDefaultRule(lines, labelIndex, numLabels));
            }
        }
//        return rules;
        MeDRIResult result = new MeDRIResult();
        result.setRules(rules);
        result.setScannedInstances(scannedInstances);
        return result;
        //
    }

    /**
     * Gets the majority class of the remaining instances as DRIRule
     *
     * @param lines
     * @param labelIndex
     * @param numLabels
     * @return
     */
    private static IRule getDefaultRule(Collection<int[]> lines, int labelIndex, int numLabels) {
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

    public static void main(String[] args) throws IOException {
        logger.info("test logger");

//        String inFile = "/media/suhel/workspace/work/wekaprism/data/fadi.arff";
        String inFile = "/media/suhel/workspace/work/wekaprism/data/cl.arff";

        Instances data = new Instances(EDRIUtils.readDataFile(inFile));
        data.setClassIndex(data.numAttributes() - 1);
        System.out.println(data.numInstances());
        int[] iattrs = MedriUtils.mapAttributes(data);

        Pair<Collection<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.key;
        int[] labelsCount = linesLabels.value;

        logger.trace("original lines size = {}", lineData.size());
        List<IRule> rules = buildClassifierPrism(iattrs, labelsCount, lineData, true).getRules();

        logger.info("rules generated =\n{}",
                rules.stream()
                        .map(rule -> rule.toString())
                        .collect(Collectors.joining("\n")));

    }

}


