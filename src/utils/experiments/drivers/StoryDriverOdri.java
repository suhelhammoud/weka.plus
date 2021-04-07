package utils.experiments.drivers;

import odri.experiments.PropsUtils;
import odri.experiments.StoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FilesUtils;
import utils.experiments.Story;
import utils.experiments.StoryKey;
import weka.core.Instances;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class StoryDriverOdri {

  static Logger logger = LoggerFactory.getLogger(StoryDriverOdri.class.getName());

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
                StoryUtils.playStoryOdri(story, data);
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

  public static void main(String[] args) throws IOException {
    experimentOdri("data/odri/conf/conf.properties");
//        experimentSami("data/sami.final.properties");
//        experiment1("data/sami.final.properties");
//        experimentL2Unbalanced("conf_l2_unbalanced.properties");
  }

}
