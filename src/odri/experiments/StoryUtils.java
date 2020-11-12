package odri.experiments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.ODRI;
import weka.classifiers.rules.odri.ORule;
import weka.classifiers.rules.odri.ORuleStats;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static odri.experiments.TClassifier.MEDRI;
import static odri.experiments.TClassifier.ODRI_T;

public class StoryUtils {

  static Logger logger = LoggerFactory.getLogger(StoryUtils.class.getName());


  public static Classifier getClassifier(Story story) {
    TClassifier tClassifier = (TClassifier) story.get(StoryKey.classifier);
    switch (tClassifier) {
      case MEDRI:
        double support = (double) story.get(StoryKey.support);
        double confidence = (double) story.get(StoryKey.confidence);
        return MEDRI.getWith(support, confidence);
      case ODRI_T:
        int minOcc = (int) story.get(StoryKey.minOcc);
        boolean addDefaultRule = (boolean) story.get(StoryKey.addDefaultRule);
        return ODRI_T.getWith(minOcc, addDefaultRule);
      default:
        tClassifier.get();
    }
    return tClassifier.get(); //never reached !
  }


  public static Story applyOdriCrossValidation
          (Instances data,
           Classifier classifier) throws Exception {
    data.setClassIndex(data.numAttributes() - 1);
    Story result = Story.get();
    ODRI odri = (ODRI) classifier;
    odri.buildClassifier(data);
    List<ORule> rules = odri.resultORules();
    ORuleStats rs = new ORuleStats(rules,
            data.numInstances(),
            data.numAttributes() - 1);

    result.set(StoryKey.numGeneratedRules, rs.numRules())
            .set(StoryKey.numGeneratedRulesPC, rs.numRulesPC())
            .set(StoryKey.numGeneratedRulesPC, rs.numRulesPC())
            .set(StoryKey.correctPC, rs.correctPC())
            .set(StoryKey.coverPC, rs.coversPC())
            .set(StoryKey.expectedDimensionality, rs.expectedDimensionality())
            .set(StoryKey.expectedDimensionalityPC, rs.expectedDimensionalityPC())
            .set(StoryKey.expectedErrorsPC, rs.expectedErrorsPercent());


    Evaluation eval = new Evaluation(data);
    //TODO change seed selection method
    eval.crossValidateModel(odri, data, 10, new Random(1));
    result.set(StoryKey.errorRate, eval.errorRate());
    result.set(StoryKey.precision, eval.weightedPrecision());
    result.set(StoryKey.recall, eval.weightedRecall());
    result.set(StoryKey.fMeasure, eval.weightedFMeasure());
    result.set(StoryKey.weightedAreaUnderROC, eval.weightedAreaUnderROC());

    return result;
  }

  public static Story playStoryOdri(Story story, Instances data) {
    assert story.get(StoryKey.dataset).equals(data.relationName());
    assert story.get(StoryKey.classifier).equals(ODRI_T);

    Story result = story; //mutual data structure
    try {

      Classifier classifier = getClassifier(story);
      Story cvStory = applyOdriCrossValidation(data, classifier);
      result.update(cvStory);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }


  public static List<Story> storiesForDefaultRules(List<Story> stories) {
    List<Story> result = new ArrayList<>(stories.size() * 2);
    for (Story st : stories) {
      result.add(st.copy().set(StoryKey.addDefaultRule, false));
      result.add(st.copy().set(StoryKey.addDefaultRule, true));
    }
    return result;
  }


  public static List<Story> generateStoriesODRI(Instances data) {
    Story bs = Story.get()
            .set(StoryKey.dataset, data.relationName())
            .set(StoryKey.numInstances, data.numInstances())
            .set(StoryKey.numAttributes, data.numAttributes() - 1);
    bs.set(StoryKey.classifier, ODRI_T);

    List<Story> result = new ArrayList<>(data.numInstances() * 2);
    List<Story> tmp = propStoriesMinOcc(bs);
    result.addAll(storiesForDefaultRules(tmp));

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


  public static List<Story> propStoriesMinOcc(Story story) {
    final int numInstances = (int) story.get(StoryKey.numInstances);
    return IntStream.range(1, numInstances)
            .mapToObj(i -> story.copy(
                    StoryKey.minOcc, i,
                    StoryKey.minOccPC, (double) i / numInstances))
            .collect(Collectors.toList());
  }

  public static void main(String[] args) throws Exception {
//        runDemo("data/demo1.properties");
//        experiment1("data/conf.properties");
  }
}
