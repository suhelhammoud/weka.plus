package l2Sensitivity;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.L2AttributeEval;
import weka.attributeSelection.L2RankerSubset;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class RTest {

    /*
    anneal, audiology, breast-cancer
    car, cleve-d
     */
    public static String getFilterCommand(RMethod method, double median) {
        final String filterCommand = "weka.filters.supervised.attribute.AttributeSelection " +
                "-E \"weka.attributeSelection.AAAAAA \" " +
                "-S \"weka.attributeSelection.L2AboveFrequencySubset -T TTTTTT -N -1\"";

        String filter = "l2string";
        switch (method) {
            case IG:
                filter = "InfoGainAttributeEval";
                break;
            case L2:
                filter = "L2AttributeEval";
                break;
            case CHI:
                filter = "ChiSquaredAttributeEval";
                break;
            default:
                System.err.println("unknown method name");
        }
        return filterCommand
                .replace("AAAAAA", filter)
                .replace("TTTTTT", String.valueOf(median));
    }


    public static AttributeSelection getFilter(RMethod method, double median) {
        L2RankerSubset search = new L2RankerSubset();
        search.setThreshold(median);

        ASEvaluation evaluator = null;
        switch (method) {
            case IG:
                evaluator = new InfoGainAttributeEval();
                break;
            case L2:
                evaluator = new L2AttributeEval();
                break;
            case CHI:
                evaluator = new L2AttributeEval();
                break;
            default:
                System.err.println("unknown method name");
        }
        AttributeSelection result = new AttributeSelection();
        result.setEvaluator(evaluator);
        result.setSearch(search);
        return result;
    }

    public static Instances filtered(Instances train, RMethod method, double median)
            throws Exception {
        Filter filter = getFilter(method, median);
        filter.setInputFormat(train);
        //can be used in batch mode
        return Filter.useFilter(train, filter);
    }

    public static RLine applyCV(Instances train, RLine story)
            throws Exception {

        RLine result = RLine.of(story.get(KEYS.dataset))
                .set(KEYS.numAttributes, story.get(KEYS.numAttributes))
                .set(KEYS.method, story.get(KEYS.method))
                .set(KEYS.median, story.get(KEYS.median))
                .set(KEYS.classifier, RClassifier.NB)
                .set(KEYS.variables, train.numAttributes() - 1);

        NaiveBayes nb = new NaiveBayes();

        Evaluation eval = new Evaluation(train);
        eval.crossValidateModel(nb, train, 10, new Random(1));

        result.set(KEYS.errorRate, eval.errorRate())
                .set(KEYS.precision, eval.weightedPrecision())
                .set(KEYS.recall, eval.weightedRecall())
                .set(KEYS.fMeasure, eval.weightedFMeasure());

        return result;

    }

    public static String oneStory(Instances data,
                                  RLine basicStory,
                                  RMethod method,
                                  double median) throws Exception {

        RLine result = basicStory.copy()
                .set(KEYS.method, method)
                .set(KEYS.median, median);
        Instances dataFiltered = filtered(data, method, median);

        dataFiltered.setClassIndex(dataFiltered.numAttributes() - 1);
        result.set(KEYS.variables, dataFiltered.numAttributes() - 1);
        result.set(KEYS.classifier, RClassifier.NB);
        result.crossValidation(dataFiltered);
        return result.stringValues();
    }

    public static List<String> oneGo(Instances data, RLine basiceStory, double median) {
        List<String> result = new ArrayList<>(3);
        try {
            result.add(oneStory(data, basiceStory, RMethod.IG, median));
            result.add(oneStory(data, basiceStory, RMethod.CHI, median));
            result.add(oneStory(data, basiceStory, RMethod.L2, median));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<String> oneTest(String dataset)
            throws Exception {
        DoubleStream medians = DoubleStream
                .iterate(0, n -> n + .05)
                .limit(20);


        RLine basicStory = RLine.of(dataset);
        String fileName = "data/arff/" + dataset + ".arff";
        final Instances data = new Instances(new FileReader(fileName));

        data.setClassIndex(data.numAttributes() - 1);
        basicStory.set(KEYS.numAttributes, data.numAttributes() - 1);

//    List<String> result = Arrays.stream(medians)
        List<String> result = medians
                .parallel()
                .mapToObj(median -> oneGo(data, basicStory, median))
                .flatMap(lst -> lst.stream())
                .collect(Collectors.toList());

//    result.stream()
//            .forEach(i -> System.out.println(i));
        return result;
    }

    public static StringBuilder allTests(String dataset, double[] medians)
            throws Exception {
        StringBuilder result = new StringBuilder();
        result.append(RLine.HEADERS());

        RLine basicStory = RLine.of(dataset);
        String fileName = "data/datasets/" + dataset + ".arff";
        Instances data = new Instances(new FileReader(fileName));
        data.setClassIndex(data.numAttributes() - 1);
        basicStory.set(KEYS.numAttributes, data.numAttributes() - 1);

        for (RMethod method : RMethod.values()) {
            for (double median : medians) {
                RLine story = basicStory
                        .copy()
                        .set(KEYS.method, method)
                        .set(KEYS.median, median);
                Instances dataFiltered = filtered(data, method, median);
                dataFiltered.setClassIndex(dataFiltered.numAttributes() - 1);
                story.set(KEYS.variables, dataFiltered.numAttributes() - 1);
                story.set(KEYS.classifier, RClassifier.NB);
                story.crossValidation(dataFiltered);
                result.append("\n" + story.stringValues());
            }
        }
        return result;
    }

    public static void runAllTests(String outfile) throws Exception {
        String[] datasets = {"anneal"};


// String[] datasets = {"anneal", "arrhythmia", "audiology", "autos",
//            "breast-cancer", "car", "cleved", "cmc", "colic", "credit-a",
//            "cylinder-bands", "dermatology", "diabetes", "ecoli",
//            "flags", "glass", "heart-c", "hepatitis", "hypothyroid",
//            "ionosphere", "labor", "lymph", "mushroom", "optdigits", "segment",
//            "lung-cancer", "credit-g", "waveform-5000"};
//
//
        List<String> result = new ArrayList<>(datasets.length * 3 * 22);
        result.add(RLine.HEADERS());

        for (String dataset : datasets) {
            System.out.println("running on dataset: " + dataset);
            List<String> dsResults = oneTest(dataset);
            result.addAll(dsResults);
        }
        System.out.println("total results : " + (result.size() - 1));
        Files.write(Paths.get(outfile), result);
    }

    public static void main(String[] args) throws Exception {

        runAllTests("data/out/out.csv");
    }

}
