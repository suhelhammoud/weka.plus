package sensetivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.io.IOException;
import java.nio.file.Path;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static sensetivity.TClassifier.MEDRI;
import static sensetivity.TEvaluator.PAS;

public class StoryUtils {

    static Logger logger = LoggerFactory.getLogger(StoryUtils.class.getName());


    public static ASEvaluation getASEvaluation(Story story) {
        TEvaluator tEvaluator = (TEvaluator) story.get(StoryKey.evalMethod);


        switch (tEvaluator) {
            case PAS:
                double evalSupport = (double) story.get(StoryKey.evalSupport);
                double evalConfidence = (double) story.get(StoryKey.evalConfidence);
                return PAS.getWith(evalSupport, evalConfidence);
//            case CHI:
//                // ChiSquaredAttributeEval settings
//                break;
            default:
                return tEvaluator.get();


        }
    }

    public static AttributeSelection getAttributeSelection(Story story) {

        int numToSelect = (int) story.get(StoryKey.numAttributesToSelect);

        Ranker search = new Ranker();
        search.setNumToSelect(numToSelect);

        ASEvaluation evaluator = getASEvaluation(story);

        AttributeSelection result = new AttributeSelection();
        result.setEvaluator(evaluator);
        result.setSearch(search);
        return result;
    }

    public static Classifier getClassifier(Story story) {

        TClassifier tClassifier = (TClassifier) story.get(StoryKey.classifier);
        switch (tClassifier) {
            case MEDRI:
                double support = (double) story.get(StoryKey.support);
                double confidence = (double) story.get(StoryKey.confidence);
                return MEDRI.getWith(support, confidence);
            default:
                tClassifier.get();
        }
        return tClassifier.get(); //never reached !
    }

    public static Instances applyFilter(Story story, Instances data) throws Exception {

        int numAttributes = (int) story.get(StoryKey.numAttributes);
        int numToSelect = (int) story.get(StoryKey.numAttributesToSelect);

        if (numAttributes == numToSelect) {
            return new Instances(data);
        } else {

            AttributeSelection attEval = getAttributeSelection(story);
            attEval.setInputFormat(data);

            return Filter.useFilter(data, attEval);
        }
    }

    public static Story applyCrossValidation(
            Instances train,
            Classifier classifier)
            throws Exception {
        Story result = Story.get().set(StoryKey.dataset, train.relationName());

        Evaluation eval = new Evaluation(train);
        //TODO change seed selection method
        eval.crossValidateModel(classifier, train, 10, new Random(1));
        result.set(StoryKey.classifier, TClassifier.NB);
        result.set(StoryKey.errorRate, eval.errorRate());
        result.set(StoryKey.precision, eval.weightedPrecision());
        result.set(StoryKey.recall, eval.weightedRecall());
        result.set(StoryKey.fMeasure, eval.weightedFMeasure());
        return result;
    }

    public static Story playStory(Story story, Instances data) {
//        Story result = story.copy(StoryKey.dataset, data.relationName());

        Story result = story.copy();

        try {


            Instances dataFiltered = applyFilter(story, data);
            assert (int) result.get(StoryKey.numAttributesToSelect) == dataFiltered.numAttributes() - 1;

            Classifier classifier = getClassifier(story);

            Story cvStory = applyCrossValidation(dataFiltered, classifier);

            result.update(cvStory);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static List<Story> playStories(List<Story> stories, Instances data) {

        return stories.stream()
                .parallel()
                .map(s -> playStory(s, data))
                .collect(Collectors.toList());
    }

    public static List<Story> generate(Story story,
                                       PropsUtils props,
                                       TEvaluator eval,
                                       TClassifier classifier) {

        List<Story> result = new ArrayList<>();
        //set evalMethod and classifier
        Story bs = story.copy()
                .set(StoryKey.evalMethod, eval)
                .set(StoryKey.classifier, classifier);
        List<Story> storiesNumSelect = propStoriesNumAttSelected(bs);

        List<Story> evalStories = new ArrayList<>();

        switch (eval) {
            case PAS:
                for (Story numStory : storiesNumSelect) {
                    List<Story> evalSupportStories = propStories(
                            numStory, StoryKey.evalSupport, props.getEvalSupports());
                    for (Story supStory : evalSupportStories) {
                        List<Story> confStories = propStories(supStory,
                                StoryKey.evalConfidence, props.getEvalConfidences());
                        evalStories.addAll(confStories);
                    }
                }
                break;

            default:
                evalStories.addAll(storiesNumSelect);

        }

        switch (classifier) {
            case MEDRI:
                for (Story evalStory : evalStories) {
                    List<Story> supportStories = propStories(evalStory,
                            StoryKey.support, props.getSupports());
                    for (Story supStory : supportStories) {
                        List<Story> confStories = propStories(supStory,
                                StoryKey.confidence, props.getConfidences());
                        result.addAll(confStories);
                    }
                }
                break;
            default:
                result.addAll(evalStories);

        }
        return result;

    }

    /**
     * generate all test stories related to one dataset
     *
     * @param props configuration file
     * @param data
     * @return
     */
    public static List<Story> generateStories(PropsUtils props, Instances data) {
        List<Story> result = new ArrayList<>();

        Story bs = Story.get()
                .set(StoryKey.dataset, data.relationName())
                .set(StoryKey.numInstances, data.numInstances())
                .set(StoryKey.numAttributes, data.numAttributes() - 1);


        for (TEvaluator eval : props.getEvaluatorMethods()) {
            for (TClassifier classifier : props.getClassifiers()) {
                List<Story> gStories = generate(bs, props, eval, classifier);
                result.addAll(gStories);
            }
        }

        int expectedNum = (int) bs.get(StoryKey.numAttributes)
                * calculateEvalClassifier(props);
        if (expectedNum != result.size() ) {
            logger.error("expected = {}, actual = {}",
                    expectedNum, result.size());
        }
        return result;
    }

    /**
     * Calculate expected for one dataset for one numAttributesToSelect only
     *
     * @param props
     * @return
     */
    public static int calculateEvalClassifier(PropsUtils props) {

        int result = 0; //non PAS nor MEDRI


        int pasProduct = props.getEvalSupports().size()
                * props.getEvalConfidences().size();
        int medriProduct = props.getSupports().size()
                * props.getConfidences().size();

        boolean hasPas = props.getEvaluatorMethods().contains(PAS);
        boolean hasMedri = props.getClassifiers().contains(MEDRI);

        int methods = props.getEvaluatorMethods().size();
        if (hasPas) methods--;
        int classifiers = props.getClassifiers().size();
        if (hasMedri) classifiers--;

        result += methods * classifiers;
        if (hasPas) {
            result += pasProduct * classifiers;
        }

        if (hasMedri) {
            result += medriProduct * methods;
        }

        if (hasPas && hasMedri) {
            result += pasProduct * medriProduct;
        }
        return result;
    }


    public static List<Story> propStories(Story story,
                                          StoryKey skey,
                                          List<Double> skeyValues) {
        return skeyValues.stream()
                .map(s -> story.copy(skey, s))
                .collect(Collectors.toList());
    }

    public static List<Story> propStoriesNumAttSelected(Story story) {
        final int numAttributes = (int) story.get(StoryKey.numAttributes);
        return IntStream.rangeClosed(1, numAttributes)
                .mapToObj(i -> story.copy(StoryKey.numAttributesToSelect, i))
                .collect(Collectors.toList());
    }


    private static double entropyValue(double p) {
        if (p < 1e-8 || p > (1 - 1e-8))
            return 0;
        return -p * Math.log(p) / Math.log(2);
    }

    public static double entropy(double[] ranks) {
        //normalize dataset
        final double sumValue = Arrays.stream(ranks).sum();
        if (sumValue == 0) throw new NullPointerException("Max Rank Can not be Zero !!");
        double[] ranksNormalized = Arrays.stream(ranks)
                .map(v -> v / sumValue)
                .toArray();

        double hV = Arrays.stream(ranksNormalized)
                .map(StoryUtils::entropyValue)
                .sum();
        return Math.pow(2, hV);
    }

    public static void main(String[] args) throws IOException {
        PropsUtils params = PropsUtils.of("data/conf.properties");


        Path resultDir = FilesUtils.createOutDir(params.getOutDir());
        logger.info("result directory : {}", resultDir.toString());

        List<String> dataSetsNames = params.getDatasets();

        List<Path> arffDatasets = FilesUtils.listFiles(
                params.getArffDir(),
                ".arff").stream()
                .filter(path -> dataSetsNames.contains(
                        path.getFileName().toString().replace(".arff", ""))
                ).collect(Collectors.toList());


        for (Path datasetPath : arffDatasets) {

            Instances data = FilesUtils.instancesOf(datasetPath);
            logger.debug("data {} contains {} attributes",
                    datasetPath.getFileName(), data.numAttributes());
            List<Story> stories = generateStories(params, data);

            FilesUtils.writeStoriesToFile(resultDir,
                    datasetPath.getFileName().toString() + ".csv"
                    , stories);
        }


        //        List<Path> datasetsPaths = listArffFiles(params.getArffDir());
//
//
//        String result = listArffFiles("data/arff").stream()
//                .map(p -> instancesOf(p))
//                .map(i -> i.relationName())
//                .sorted()
//                .collect(Collectors.joining("\n"));
//        System.out.println("result = " + result);
    }
}
