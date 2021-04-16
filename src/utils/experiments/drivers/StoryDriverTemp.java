package utils.experiments.drivers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FilesUtils;
import utils.experiments.*;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.UnbalancedClassSampler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static utils.experiments.StoryUtils.getASEvaluation;
import static utils.experiments.TClassifier.NB;


public class StoryDriverTemp {

  static Logger logger = LoggerFactory.getLogger(StoryDriverTemp.class.getName());


  public static Instances applyFilter(Story story, Instances data) {

    int numAttributes = (int) story.get(StoryKey.numAttributes);
    int numToSelect = (int) story.get(StoryKey.numAttributesToSelect);

    if (numAttributes == numToSelect) {
      return new Instances(data);
    } else {

      AttributeSelection attEval = getAttributeSelectionFilter(story);

      try {
        attEval.setInputFormat(data);
        return Filter.useFilter(data, attEval);
      } catch (Exception e) {
        e.printStackTrace();
        logger.error("exception in numAttributesToSelect filter");
      }
      return new Instances(data);//TODO , never reached
    }
  }

  public static Instances applyAttSelectionFilter(
          AttributeSelection attEval, Instances data) {
    int numAttributes = data.numAttributes() - 1;//minus class attribute
    int numToSelect = ((Ranker) attEval.getSearch()).getNumToSelect();

    if (numAttributes <= numToSelect)
      return new Instances(data);
    try {
      attEval.setInputFormat(data);
      return Filter.useFilter(data, attEval);
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("exception in numAttributesToSelect filter");
    }
    return null;//TODO , change to throw exception
  }

  public static AttributeSelection getAttributeSelectionFilter(Story story) {

    int numToSelect = (int) story.get(StoryKey.numAttributesToSelect);

    Ranker search = new Ranker();
    search.setNumToSelect(numToSelect);

    ASEvaluation evaluator = getASEvaluation(story);

    AttributeSelection result = new AttributeSelection();
    result.setEvaluator(evaluator);
    result.setSearch(search);
    return result;
  }

  public static Instances applyResampleClassFilter(Instances data,
                                                   double resampleSize,
                                                   double classRatio,
                                                   long seed) {
    try {

      UnbalancedClassSampler filter = new UnbalancedClassSampler();
      filter.setRandomSeed((int) seed);
      filter.setSampleSizeRatio(resampleSize);
      filter.setSampleClassRatio(classRatio);
      filter.setInputFormat(data);
      return Filter.useFilter(data, filter);
    } catch (Exception e) {
      e.printStackTrace();
    }
    logger.error("This line should not be reached");
    return null;
  }


  public static List<Story> propStoriesNumAttSelected(Story story) {
    final int numAttributes = (int) story.get(StoryKey.numAttributes);
    return IntStream.rangeClosed(1, numAttributes)
            .mapToObj(i -> story.copy(StoryKey.numAttributesToSelect, i))
            .map(s -> s.set(StoryKey.experimentID, s.id))
            .collect(Collectors.toList());
  }

  //TODO does it work with this general method?
  public static <T> List<Story> propStories(Story story,
                                            StoryKey skey,
                                            List<T> skeyValues) {
    return skeyValues.stream()
            .map(s -> story.copy(skey, s))
            .collect(Collectors.toList());
  }


  /**
   * generate all test stories related to one dataset
   *
   * @param
   * @return
   */
  public static List<Story> generateBaseStories(
          String relationName,
          int numInstances,
          int numAttributes,
          double resampleSizeRatio,
          double classRatio,
          int repeat,
          TClassifier classifier,
          TEvaluator eval
  ) {
    Story bs = Story.get()
            .set(StoryKey.dataset, relationName)
            .set(StoryKey.numInstances, numInstances)
            .set(StoryKey.numAttributes, numAttributes)
            .set(StoryKey.attEvalMethod, eval)
            .set(StoryKey.classifier, classifier)
            .set(StoryKey.l2ResampleSizeRatio, resampleSizeRatio)
            .set(StoryKey.l2ClassRatio, classRatio)
            .set(StoryKey.l2ClassRepeat, repeat)
            .set(StoryKey.attEvalMethod, eval);

    bs.set(StoryKey.experimentID, bs.id);
    return propStoriesNumAttSelected(bs);
  }

  public static List<Story> setRepeat(List<Story> stories, int iteration) {
    return stories.stream()
            .map(s -> s.copy()
                    .set(StoryKey.l2ClassExperimentIteration, iteration + 1)
                    .set(StoryKey.experimentID, s.id)
            ).collect(Collectors.toList());
  }

  public static Story applyCrossValidation(
          Instances train,
          Classifier classifier)
          throws Exception {

    train.setClassIndex(train.numAttributes() - 1);
    Story result = Story.get();

    Evaluation eval = new Evaluation(train);
    //TODO change seed selection method
    eval.crossValidateModel(classifier, train, 10, new Random(1)); //TODO check random seed
    //result.set(StoryKey.classifier, TClassifier.NB);
    result.set(StoryKey.errorRate, eval.errorRate());
    result.set(StoryKey.precision, eval.weightedPrecision());
    result.set(StoryKey.recall, eval.weightedRecall());
    result.set(StoryKey.fMeasure, eval.weightedFMeasure());
    //added for the autism dataset test updates


    result.set(StoryKey.areaUnderROC0, eval.areaUnderROC(0));//
    result.set(StoryKey.areaUnderROC1, eval.areaUnderROC(1));//
    result.set(StoryKey.weightedAreaUnderROC, eval.weightedAreaUnderROC());

    return result;
  }


  public static void experimentL2Unbalanced(String... args) throws IOException {
    Random rnd = new Random(0);
    StoryKey[] headers = getStorykeysHeaders();

    String confName = args.length > 0 ? args[0] : "data/conf_l2_aut.properties";
    PropsUtils params = PropsUtils.of(confName);

    Path confPath = Paths.get(confName);

    Path resultDir = FilesUtils.createOutDir(params.getOutDir());
    logger.info("result directory : {}", resultDir.toString());

    //copy config file to output
    Files.copy(confPath,
            resultDir.getParent().resolve(resultDir.getFileName() + "_" + confPath.getFileName()));
//            resultDir.getParent().resolve(resultDir.getFileName() + ".properties"));

    List<String> dataSetsNames = params.getDatasets();

    List<Path> arffDatasets = FilesUtils.listFiles(
            params.getArffDir(),
            ".arff").stream()
            .filter(path -> dataSetsNames.contains(
                    path.getFileName().toString().replace(".arff", ""))
            ).collect(Collectors.toList());

    for (Path datasetPath : arffDatasets) {

      TEvaluator eval = params.getEvaluatorMethods().get(0); //Dirty one eval value
      TClassifier classifier = params.getClassifiers().get(0);

      Instances data = FilesUtils.instancesOf(datasetPath);
      logger.info("processing dataset: {}", data.relationName());
      data.setClassIndex(data.numAttributes() - 1);

      List<Story> stories = new ArrayList<>();

      double resampleSize = params.getL2ClassResampleSize();
      List<Double> classRatios = params.getL2ClassRatios();
      int repeat = params.getL2ClassRepeat();

      for (int ratioIndex = 0; ratioIndex < classRatios.size(); ratioIndex++) {
        //stories with one experimentID
        List<Story> expStories = generateBaseStories(data.relationName(),
                data.numInstances(),
                data.numAttributes() - 1,
                resampleSize,
                classRatios.get(ratioIndex),
                repeat,
                classifier,
                eval);

        for (int iteration = 0; iteration < repeat; iteration++) {
          Instances unbalancedData = applyResampleClassFilter(data,
                  resampleSize,
                  classRatios.get(ratioIndex),
                  rnd.nextLong()
          );

          unbalancedData.setRelationName(data.relationName());
          List<Story> iterStories = setRepeat(expStories, iteration);
          iterStories.parallelStream()
                  .forEach(
                          s -> playStory(s, unbalancedData)
                  );
          stories.addAll(iterStories);
        }
      }

//      List<Story> avgStories = stories;
      List<Story> avgStories = storiesWithAvg(stories,
              StoryKey.errorRate,
              StoryKey.precision,
              StoryKey.recall,
              StoryKey.fMeasure,
              StoryKey.weightedAreaUnderROC,
              StoryKey.areaUnderROC0,
              StoryKey.areaUnderROC1);

      FilesUtils.writeStoriesToFile(resultDir,
              datasetPath.getFileName().toString() + ".csv"
              , avgStories, headers);
    }
  }

  public static StoryKey[] getStorykeysHeaders() {
    return new StoryKey[]{
            StoryKey.dataset,
            StoryKey.numInstances,
            StoryKey.numAttributes,
            StoryKey.attEvalMethod,
            StoryKey.classifier,
            StoryKey.l2ClassRepeat,
            StoryKey.l2ResampleSizeRatio,
            StoryKey.numAttributesToSelect,
            StoryKey.experimentID,
            StoryKey.l2ClassRatio,
            StoryKey.errorRate,
            StoryKey.errorRateVariance,
            StoryKey.precision,
            StoryKey.precisionVariance,
            StoryKey.recall,
            StoryKey.recallVariance,
            StoryKey.fMeasure,
            StoryKey.fMeasureVariance,
            StoryKey.weightedAreaUnderROC,
            StoryKey.weightedAreaUnderROCVariance,
            StoryKey.areaUnderROC0,
            StoryKey.areaUnderROC0Variance,
            StoryKey.areaUnderROC1,
            StoryKey.areaUnderROC1Variance,
    };
  }


  private static Story storyWithAvgVar(List<Story> subStories, StoryKey... keys) {
    Story outStory = subStories.get(0).copy();

    //calc Average
    for (StoryKey key : keys) {
      double avg = subStories.stream()
              .mapToDouble(s -> (Double) s.get(key))
              .average()
              .getAsDouble();
      outStory.set(key, avg);

      if (StoryKey.contains(key.name() + "Variance")) {
        double variance = subStories.stream()
                .mapToDouble(s -> Math.pow((Double) s.get(key) - avg, 2))
                .sum() / subStories.size();

        StoryKey keyVar = StoryKey.valueOf(key.name() + "Variance");
        outStory.set(keyVar, variance);
      }
    }
    return outStory;
  }

  public static List<Story> storiesWithAvg(List<Story> stories, StoryKey... keys) {

    Map<Long, List<Story>> mappedStories = stories.stream()
            .collect(Collectors.groupingBy(item -> (Long) item.get(StoryKey.experimentID)));

    return mappedStories.values().stream()
            .parallel()
            .map(stories1 -> storyWithAvgVar(stories1, keys))
            .collect(Collectors.toList());
  }

  public static void playStory(Story story, Instances data) {
//        if(true) return;
    AttributeSelection attSelectionFilter = getAttributeSelectionFilter(story);
    Instances dataFiltered = applyAttSelectionFilter(attSelectionFilter, data);
//    Instances dataFiltered = applyFilter(story, data);
    assert (int) story.get(StoryKey.numAttributesToSelect) == dataFiltered.numAttributes() - 1;
    Classifier classifier = NB.get(); //Can initiate it dynamically from story
    try {
      Story cvStory = applyCrossValidation(dataFiltered, classifier);
      story.update(cvStory);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    experimentL2Unbalanced("data/conf_l2_aut.properties");
  }
}
