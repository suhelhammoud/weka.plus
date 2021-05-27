package utils.experiments.drivers;

//import odri.experiments.PropsUtils;
//import odri.experiments.StoryUtils;
//import odri.experiments.StoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FilesUtils;
import utils.experiments.PropsUtils;
import utils.experiments.Story;
import utils.experiments.StoryKey;
import utils.experiments.StoryUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.ODRI;
import weka.classifiers.rules.odri.ORule;
import weka.classifiers.rules.odri.ORuleStats;
import weka.core.Instances;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static utils.experiments.TClassifier.ODRI_T;

public class StoryDriverOdri {

  static Logger logger = LoggerFactory.getLogger(StoryDriverOdri.class.getName());
  static Random rnd = new Random(System.nanoTime());

  static StoryKey[] HEADERS = getStoreysHeaders();



  public static void experimentOdri(String... args) throws IOException {

    String confName = args.length > 0 ? args[0] : "data/conf.properties";
    PropsUtils params = PropsUtils.of(confName);

    Path confPath = Paths.get(confName);

    Path resultDir = FilesUtils.createOutDir(params.getOutDir());
    logger.info("result directory : {}", resultDir.toString());

    //copy config file to output
    Files.copy(confPath,
            resultDir.getParent().resolve(resultDir.getFileName() + ".properties"));

    List<String> dataSetsNames = params.getDatasets();

    List<Path> arffDatasets = FilesUtils.listFiles(
            params.getArffDir(),
            ".arff").stream()
            .filter(path -> dataSetsNames.contains(
                    path.getFileName().toString().replace(".arff", ""))
            ).collect(Collectors.toList());


    for (Path datasetPath : arffDatasets) {

      Instances data = FilesUtils.instancesOf(datasetPath);
      data.setClassIndex(data.numAttributes() - 1);

      List<Story> stories = StoryUtils.generateStoriesODRI(data);

      logger.info("processing dataset: {}", data.relationName());
      logger.info("expected stories = {}", stories.size());

      stories.parallelStream()
              .forEach(story -> {
                playStoryOdri(story, data);
              });

      FilesUtils.writeStoriesToFile(resultDir,
              datasetPath.getFileName().toString() + ".csv"
              , stories,
              HEADERS);
    }
  }

  public static StoryKey[] getStoreysHeaders() {
    return new StoryKey[]{
            StoryKey.dataset,
            StoryKey.numInstances,
            StoryKey.numAttributes,
            StoryKey.classifier,

            StoryKey.minOcc,
            StoryKey.minOccPC,

            StoryKey.addDefaultRule,
            StoryKey.pctCorrect,
            StoryKey.errorRate,
            StoryKey.precision,
            StoryKey.recall,
            StoryKey.fMeasure,
            StoryKey.weightedAreaUnderROC,

            StoryKey.numGeneratedRules,
            StoryKey.numGeneratedRulesPC,
            StoryKey.correctPC,
            StoryKey.coverPC,
            StoryKey.expectedDimensionality,
            StoryKey.expectedDimensionalityPC,
            StoryKey.expectedErrorsPC
    };
  }



  public static Story playStoryOdri(Story story, Instances data) {
    assert story.get(StoryKey.dataset).equals(data.relationName());
    assert story.get(StoryKey.classifier).equals(ODRI_T);

    Story result = story; //mutual data structure
    try {

      Classifier classifier = StoryUtils.getClassifier(story);
      Story cvStory = applyOdriCrossValidation(data, classifier);
      result.update(cvStory);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }


  public static Story applyOdriCrossValidation
          (Instances data,
           Classifier classifier) throws Exception {
    data.setClassIndex(data.numAttributes() - 1);
    Story result = Story.create();
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
    eval.crossValidateModel(odri, data, 10, rnd);

    result.set(StoryKey.pctCorrect, eval.pctCorrect());
    result.set(StoryKey.errorRate, eval.errorRate());
    result.set(StoryKey.precision, eval.weightedPrecision());
    result.set(StoryKey.recall, eval.weightedRecall());
    result.set(StoryKey.fMeasure, eval.weightedFMeasure());
    result.set(StoryKey.weightedAreaUnderROC, eval.weightedAreaUnderROC());

    return result;
  }



  public static void main(String[] args) throws IOException {
    experimentOdri("data/odri/conf/conf.properties");
//        experimentSami("data/sami.final.properties");
//        experiment1("data/sami.final.properties");
//        experimentL2Unbalanced("conf_l2_unbalanced.properties");
  }

}
