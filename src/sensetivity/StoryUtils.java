package sensetivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.pas.CutOffPoint;
import weka.attributeSelection.pas.PasMethod;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static sensetivity.TClassifier.MEDRI;
import static sensetivity.TEvaluator.PAS;

public class StoryUtils {

    static Logger logger = LoggerFactory.getLogger(StoryUtils.class.getName());


    public static <T extends ASEvaluation & AttributeEvaluator> T getASEvaluation(Story story) {
        TEvaluator tEvaluator = (TEvaluator) story.get(StoryKey.evalMethod);


        switch (tEvaluator) {
            case PAS:
                double evalSupport = (double) story.get(StoryKey.evalSupport);
                double evalConfidence = (double) story.get(StoryKey.evalConfidence);
                double cutoffThreshold = (double) story.get(StoryKey.cutoffThreshold);

                PasMethod pasmethod = (PasMethod) story.get(StoryKey.pasMethod);


                return (T) PAS.getWith(evalSupport,
                        evalConfidence,
                        pasmethod,
                        cutoffThreshold);
//            case CHI:
//                // ChiSquaredAttributeEval settings
//                break;
            default:
                return (T) tEvaluator.get();
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

        train.setClassIndex(train.numAttributes() - 1);
        Story result = Story.get();

        Evaluation eval = new Evaluation(train);
        //TODO change seed selection method
        eval.crossValidateModel(classifier, train, 10, new Random(1));
//        result.set(StoryKey.classifier, TClassifier.NB);
        result.set(StoryKey.errorRate, eval.errorRate());
        result.set(StoryKey.precision, eval.weightedPrecision());
        result.set(StoryKey.recall, eval.weightedRecall());
        result.set(StoryKey.fMeasure, eval.weightedFMeasure());
        return result;
    }

    public static Story playStory(Story story,
                                  Instances data,
                                  boolean withEntropy) {
//        Story result = story.copy(StoryKey.dataset, data.relationName());

        Story result = story; //mutual data structure


        try {


            Instances dataFiltered = applyFilter(story, data);
            assert (int) result.get(StoryKey.numAttributesToSelect) == dataFiltered.numAttributes() - 1;

            if (withEntropy) {
                List<Double> ranks = calcAndGetRanks(story, dataFiltered);

                double entropy = CutOffPoint.entropy(ranks);
                double huffman = CutOffPoint.huffman(ranks);
                double variableNumThreshold = CutOffPoint.threshold(ranks,
                        (Double) story.get(StoryKey.cutoffThreshold));
                result.set(StoryKey.entropy, entropy);
                result.set(StoryKey.huffman, huffman);
                result.set(StoryKey.variablesThreshold, variableNumThreshold);
            }

            Classifier classifier = getClassifier(story);

            Story cvStory = applyCrossValidation(dataFiltered, classifier);

            result.update(cvStory);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
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
                                StoryKey.evalConfidence,
                                props.getEvalConfidences());

                        for (Story s : confStories) {
                            List<Story> pasMethodStories = propStories(s,
                                    StoryKey.pasMethod,
                                    props.getPasMethods());
                            evalStories.addAll(pasMethodStories);
                        }
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

    public static List<Story> generateSami(Story story,
                                           PropsUtils props,
                                           TEvaluator eval,
                                           TClassifier classifier) {

        List<Story> result = new ArrayList<>();
        //set evalMethod and classifier
        final int numAttributes = (int) story.get(StoryKey.numAttributes);

        Story bs = story.copy()
                .set(StoryKey.evalMethod, eval)
                .set(StoryKey.classifier, classifier)
                .set(StoryKey.numAttributesToSelect, numAttributes);

        List<Story> storiesNumSelect = new ArrayList<>();
        storiesNumSelect.add(bs.copy());

        List<Story> evalStories = new ArrayList<>();

        switch (eval) {
            case PAS:
                for (Story numStory : storiesNumSelect) {
                    List<Story> evalSupportStories = propStories(
                            numStory, StoryKey.evalSupport, props.getEvalSupports());

                    for (Story supStory : evalSupportStories) {
                        List<Story> confStories = propStories(supStory,
                                StoryKey.evalConfidence,
                                props.getEvalConfidences());

                        for (Story s : confStories) {
                            List<Story> pasMethodStories = propStories(s,
                                    StoryKey.pasMethod,
                                    props.getPasMethods());
                            evalStories.addAll(pasMethodStories);
                        }
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


    private static double evaluateAttribute(AttributeEvaluator attEval, int index) {
        try {
            return attEval.evaluateAttribute(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<Double> calcAndGetRanks(Story story, Instances data) {
        data.setClassIndex(data.numAttributes() - 1);

        ASEvaluation eval = getASEvaluation(story);
        try {
            eval.buildEvaluator(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AttributeEvaluator attEval = (AttributeEvaluator) eval;
        return IntStream.range(0, data.numAttributes() - 1)
                .mapToDouble(i -> evaluateAttribute(attEval, i))
                .boxed()
                .collect(Collectors.toList());
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
                .set(StoryKey.numAttributes, data.numAttributes() - 1)
                .set(StoryKey.cutoffThreshold, props.getCutoffThreshold());


        for (TEvaluator eval : props.getEvaluatorMethods()) {
            for (TClassifier classifier : props.getClassifiers()) {
                List<Story> gStories = generate(bs, props, eval, classifier);
                result.addAll(gStories);
            }
        }

        int expectedNum = (int) bs.get(StoryKey.numAttributes)
                * calculateEvalClassifier(props);
        if (expectedNum != result.size()) {
            logger.error("expected = {}, actual = {}",
                    expectedNum, result.size());
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
    public static List<Story> generateStoriesSami(PropsUtils props, Instances data) {
        List<Story> result = new ArrayList<>();

        Story bs = Story.get()
                .set(StoryKey.dataset, data.relationName())
                .set(StoryKey.numInstances, data.numInstances())
                .set(StoryKey.numAttributes, data.numAttributes() - 1)
                .set(StoryKey.cutoffThreshold, props.getCutoffThreshold());


        for (TEvaluator eval : props.getEvaluatorMethods()) {
            for (TClassifier classifier : props.getClassifiers()) {
                List<Story> gStories = generateSami(bs, props, eval, classifier);
                result.addAll(gStories);
            }
        }

        int expectedNum = (int) bs.get(StoryKey.numAttributes)
                * calculateEvalClassifier(props);
        if (expectedNum != result.size()) {
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
                * props.getEvalConfidences().size()
                * props.getPasMethods().size();

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


    //TODO does it work with this general method?
    public static <T> List<Story> propStories(Story story,
                                              StoryKey skey,
                                              List<T> skeyValues) {
        return skeyValues.stream()
                .map(s -> story.copy(skey, s))
                .collect(Collectors.toList());
    }

//    public static List<Story> propStories(Story story,
//                                          StoryKey skey,
//                                          List<Double> skeyValues) {
//        return skeyValues.stream()
//                .map(s -> story.copy(skey, s))
//                .collect(Collectors.toList());
//    }

    public static List<Story> propStoriesNumAttSelected(Story story) {
        final int numAttributes = (int) story.get(StoryKey.numAttributes);
        return IntStream.rangeClosed(1, numAttributes)
                .mapToObj(i -> story.copy(StoryKey.numAttributesToSelect, i))
                .collect(Collectors.toList());
    }


    public static void main(String[] args) throws Exception {
//        runDemo("data/demo1.properties");
//        experiment1("data/conf.properties");
    }

}
