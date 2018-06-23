package sensetivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Experiment {

    static Logger logger = LoggerFactory.getLogger(Experiment.class.getName());


//    /**
//     * Get filter of type AttributeSelection
//     *
//     * @param evalMethod
//     * @param args       optional parameters to pass to the filter,
//     *                   args[0]: number to select
//     *                   case PAS:
//     *                   args[1]: support
//     *                   args[2]: confidence
//     * @return
//     */
//    public static AttributeSelection getAttributeSelectionFilter(TEvaluator evalMethod,
//                                                                 Object... args) {
//        Ranker search = new Ranker();
//        search.setNumToSelect((Integer) args[0]);
//
//
//        ASEvaluation evaluator = evalMethod.get();
//        //set parameters to evaluator
//        switch (evalMethod) {
//            case PAS:
//                PasAttributeEval eval = (PasAttributeEval) evaluator;
//                eval.setSupport((Double) args[1]);
//                eval.setConfidence((Double) args[2]);
//                break;
//            case CHI:
//                // ChiSquaredAttributeEval settings
//                break;
//            default:
//                logger.debug("evaluator = {} ", evaluator.getClass().getName());
//        }
//
//        AttributeSelection result = new AttributeSelection();
//        result.setEvaluator(evaluator);
//        result.setSearch(search);
//        return result;
//    }

    public static Instances applyFilter(Instances train,
                                        AttributeSelection eval) throws Exception {
        eval.setInputFormat(train);
        //can be used in batch mode
        return Filter.useFilter(train, eval);

    }

//    public static Instances applyFilter(Instances train,
//                                        TEvaluator method,
//                                        Object... args)
//            throws Exception {
//        return applyFilter(train,
//                getAttributeSelectionFilter(method, args),
//                args);
//    }

    public static Story applyCrossValidation(Story story,
                                             Instances train,
                                             Classifier classifier)
            throws Exception {
        Evaluation eval = new Evaluation(train);
        //TODO change seed selection method
        eval.crossValidateModel(classifier, train, 10, new Random(1));
        story.set(StoryKey.classifier, TClassifier.NB);
        story.set(StoryKey.errorRate, eval.errorRate());
        story.set(StoryKey.precision, eval.weightedPrecision());
        story.set(StoryKey.recall, eval.weightedRecall());
        story.set(StoryKey.fMeasure, eval.weightedFMeasure());
        return story;
    }


//    public static Story atomicRun(Story basicStory, Instances data) {
//        Story result = basicStory.copy();
//
//
//        int numToSelect = (int) basicStory.get(StoryKey.numAttributesToSelect);
//        double evalSuppot = (double) basicStory.get(StoryKey.evalSupport);
//        double evalConfidence = (double) basicStory.get(StoryKey.evalConfidence);
//
//        TEvaluator tEvaluator = (TEvaluator) basicStory.get(StoryKey.evalMethod);
//
//        Instances dataFiltered = null;
//        Classifier classifier;
//
//        switch (tEvaluator) {
//            case PAS:
//                try {
//                    dataFiltered = applyFilter(data,
//                            tEvaluator,
//                            numToSelect, evalSuppot, evalConfidence);
//                    (t)basicStory.get(StoryKey.classifier);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//        }
//        return result;
//    }

//    public static String oneStory(Instances data,
//                                  Story basicStory,
//                                  TEvaluator method,
//                                  Classifier classifier)
//            throws Exception {
//
//        Story result = basicStory.copy()
//                .set(StoryKey.evalMethod, method);
//        Instances dataFiltered = applyFilter(data, method, median);
//
//        dataFiltered.setClassIndex(dataFiltered.numAttributes() - 1);
//        result.set(StoryKey.numAttributesToSelect, dataFiltered.numAttributes() - 1);
//        result.set(StoryKey.classifier, TClassifier.NB);
//
////        result.crossValidation(dataFiltered);
//        applyCrossValidation(result, dataFiltered, classifier);
//        return result.stringValues();
//    }


//    public static List<String> oneGo(Instances data, Story basicStory, double median) {
//        List<String> result = new ArrayList<>(3);
//        try {
//            result.add(oneStory(data, basicStory, TEvaluator.IG, median));
//            result.add(oneStory(data, basicStory, TEvaluator.CHI, median));
//            result.add(oneStory(data, basicStory, TEvaluator.L2, median));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }


//    public static List<String> oneTest(String dataset)
//            throws Exception {
//        DoubleStream medians = DoubleStream
//                .iterate(0, n -> n + .05)
//                .limit(20);
//
//
//        Story basicStory = Story.of(dataset);
//        String fileName = "data/arff/" + dataset + ".arff";
//        final Instances data = new Instances(new FileReader(fileName));
//
//        data.setClassIndex(data.numAttributes() - 1);
//        basicStory.set(StoryKey.numAttributes, data.numAttributes() - 1);
//
////    List<String> result = Arrays.stream(medians)
//        List<String> result = medians
//                .parallel()
//                .mapToObj(median -> oneGo(data, basicStory, median))
//                .flatMap(lst -> lst.stream())
//                .collect(Collectors.toList());
//
////    result.stream()
////            .forEach(i -> System.out.println(i));
//        return result;
//    }


    public static List<Story> generateStories(Story basicStory,
                                              int numAttributes) {

        List<Story> result = new ArrayList<>(numAttributes);
        for (int numAtt = 0; numAtt < numAttributes; numAtt++) {
            result.add(basicStory.copy()
                    .set(StoryKey.numAttributesToSelect, numAtt));
        }
        return result;
    }

    public static List<Story> storiesWithParamsPas(Story basicStory,
                                                   double[] evalSupports,
                                                   double[] evalConfidences) {

        List<Story> result = new ArrayList<>(
                evalSupports.length
                        * evalConfidences.length);

        for (double support : evalSupports) {
            for (double confidence : evalConfidences) {
                Story story = basicStory.copy()
                        .set(StoryKey.evalSupport, support)
                        .set(StoryKey.evalConfidence, confidence);
                result.add(story);
            }
        }
        return result;
    }

    public static StringBuilder allPasTests(String dataset)
            throws Exception {
        StringBuilder result = new StringBuilder();
        result.append(StoryKey.csvHeaders());

        String fileName = "data/datasets/" + dataset + ".arff";
        Instances data = new Instances(new FileReader(fileName));
        data.setClassIndex(data.numAttributes() - 1);

        Story basicStory = Story.get()
                .set(StoryKey.dataset, dataset);

        basicStory.set(StoryKey.numInstances, data.numInstances());
        basicStory.set(StoryKey.numAttributes, data.numAttributes() - 1);

        return result;
    }

//    public static StringBuilder allTests(String dataset, double[] medians)
//            throws Exception {
//        StringBuilder result = new StringBuilder();
//        result.append(StoryKey.csvHeaders());
//
//        String fileName = "data/datasets/" + dataset + ".arff";
//        Instances data = new Instances(new FileReader(fileName));
//        data.setClassIndex(data.numAttributes() - 1);
//
//        Story basicStory = Story.of()
//                .set(StoryKey.dataset, dataset);
//        basicStory.set(StoryKey.numInstances, data.numInstances());
//        basicStory.set(StoryKey.numAttributes, data.numAttributes() - 1);
//
//        for (TEvaluator evalMethod : TEvaluator.values()) {
//            for (double median : medians) {
//                Story story = basicStory
//                        .copy()
//                        .set(StoryKey.evalMethod, evalMethod);
//
//                Instances dataFiltered = applyFilter(data, evalMethod, median);
//                dataFiltered.setClassIndex(dataFiltered.numAttributes() - 1);
//                story.set(StoryKey.numAttributesToSelect, dataFiltered.numAttributes() - 1);
//                story.set(StoryKey.classifier, TClassifier.NB);
//                story.crossValidation(dataFiltered);
//                result.append("\n" + story.stringValues());
//            }
//        }
//        return result;
//    }

    public static StringBuilder pasRun() {

        return null;
    }

//    public static void runAllTests(String outfile) throws Exception {
//        String[] datasets = {"anneal"};
//
//// String[] datasets = {"anneal", "arrhythmia", "audiology", "autos",
////                "breast-cancer", "car", "cleved", "cmc", "colic", "credit-a",
////                "cylinder-bands", "dermatology", "diabetes", "ecoli",
////                "flags", "glass", "heart-c", "hepatitis", "hypothyroid",
////                "ionosphere", "labor", "lymph", "mushroom", "optdigits", "segment",
////                "lung-cancer", "credit-g", "waveform-5000"};
////
//
//        List<String> result = new ArrayList<>(datasets.length * 3 * 22);
//        result.add(StoryKey.csvHeaders());
//
//        for (String dataset : datasets) {
//            System.out.println("running on dataset: " + dataset);
//            List<String> dsResults = oneTest(dataset);
//            result.addAll(dsResults);
//        }
//        System.out.println("total results : " + (result.size() - 1));
//        Files.write(Paths.get(outfile), result);
//    }
//
//    public static void main(String[] args) throws Exception {
//
//        runAllTests("data/out/out.csv");
//    }

}
