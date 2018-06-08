package sensetivity;

import weka.attributeSelection.ASEvaluation;
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

public class Experiment {

    public static AttributeSelection getPasFilter(TEvaluator evalMethod, double median) {
        L2RankerSubset search = new L2RankerSubset();
        search.setThreshold(median);

        ASEvaluation evaluator = evalMethod.get();

        AttributeSelection result = new AttributeSelection();
        result.setEvaluator(evaluator);
        result.setSearch(search);
        return result;
    }


    public static AttributeSelection getFilter(TEvaluator evalMethod, double median) {
        L2RankerSubset search = new L2RankerSubset();
        search.setThreshold(median);

        ASEvaluation evaluator = evalMethod.get();

        AttributeSelection result = new AttributeSelection();
        result.setEvaluator(evaluator);
        result.setSearch(search);
        return result;
    }

    public static Instances applyFilter(Instances train,
                                        TEvaluator method,
                                        double median)
            throws Exception {
        Filter filter = getFilter(method, median);
        filter.setInputFormat(train);
        //can be used in batch mode
        return Filter.useFilter(train, filter);
    }

    public static TaskData applyCV(Instances train, TaskData story)
            throws Exception {

        TaskData result = TaskData.of(story.get(TaskKey.dataset))
                .set(TaskKey.numAttributes, story.get(TaskKey.numAttributes))
                .set(TaskKey.evalMethod, story.get(TaskKey.evalMethod))
                .set(TaskKey.classifier, TClassifier.NB)
                .set(TaskKey.attributesToSelect, train.numAttributes() - 1);

        NaiveBayes nb = new NaiveBayes();

        Evaluation eval = new Evaluation(train);
        eval.crossValidateModel(nb, train, 10, new Random(1));

        result.set(TaskKey.errorRate, eval.errorRate())
                .set(TaskKey.precision, eval.weightedPrecision())
                .set(TaskKey.recall, eval.weightedRecall())
                .set(TaskKey.fMeasure, eval.weightedFMeasure());

        return result;

    }

    public static String oneStory(Instances data,
                                  TaskData basicStory,
                                  TEvaluator method,
                                  double median) throws Exception {

        TaskData result = basicStory.copy()
                .set(TaskKey.evalMethod, method);
        Instances dataFiltered = applyFilter(data, method, median);

        dataFiltered.setClassIndex(dataFiltered.numAttributes() - 1);
        result.set(TaskKey.attributesToSelect, dataFiltered.numAttributes() - 1);
        result.set(TaskKey.classifier, TClassifier.NB);
        result.crossValidation(dataFiltered);
        return result.stringValues();
    }

    public static List<String> oneGo(Instances data, TaskData basicStory, double median) {
        List<String> result = new ArrayList<>(3);
        try {
            result.add(oneStory(data, basicStory, TEvaluator.IG, median));
            result.add(oneStory(data, basicStory, TEvaluator.CHI, median));
            result.add(oneStory(data, basicStory, TEvaluator.L2, median));
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


        TaskData basicStory = TaskData.of(dataset);
        String fileName = "data/arff/" + dataset + ".arff";
        final Instances data = new Instances(new FileReader(fileName));

        data.setClassIndex(data.numAttributes() - 1);
        basicStory.set(TaskKey.numAttributes, data.numAttributes() - 1);

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
        result.append(TaskKey.csvHeaders());

        TaskData basicStory = TaskData.of(dataset);
        String fileName = "data/datasets/" + dataset + ".arff";
        Instances data = new Instances(new FileReader(fileName));
        data.setClassIndex(data.numAttributes() - 1);
        basicStory.set(TaskKey.numAttributes, data.numAttributes() - 1);

        for (TEvaluator evalMethod : TEvaluator.values()) {
            for (double median : medians) {
                TaskData story = basicStory
                        .copy()
                        .set(TaskKey.evalMethod, evalMethod);

                Instances dataFiltered = applyFilter(data, evalMethod, median);
                dataFiltered.setClassIndex(dataFiltered.numAttributes() - 1);
                story.set(TaskKey.attributesToSelect, dataFiltered.numAttributes() - 1);
                story.set(TaskKey.classifier, TClassifier.NB);
                story.crossValidation(dataFiltered);
                result.append("\n" + story.stringValues());
            }
        }
        return result;
    }

    public static StringBuilder pasRun(){

        return null;
    }

    public static void runAllTests(String outfile) throws Exception {
        String[] datasets = {"anneal"};

// String[] datasets = {"anneal", "arrhythmia", "audiology", "autos",
//                "breast-cancer", "car", "cleved", "cmc", "colic", "credit-a",
//                "cylinder-bands", "dermatology", "diabetes", "ecoli",
//                "flags", "glass", "heart-c", "hepatitis", "hypothyroid",
//                "ionosphere", "labor", "lymph", "mushroom", "optdigits", "segment",
//                "lung-cancer", "credit-g", "waveform-5000"};
//

        List<String> result = new ArrayList<>(datasets.length * 3 * 22);
        result.add(TaskKey.csvHeaders());

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
