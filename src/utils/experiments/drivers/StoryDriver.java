package utils.experiments.drivers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import utils.experiments.StoryUtils;
//import utils.experiments.StoryUtils;
import utils.experiments.PropsUtils;
import utils.experiments.Story;
import utils.experiments.StoryKey;
import utils.experiments.StoryUtils;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static utils.FilesUtils.*;
import static utils.InstancesUtils.instancesOf;
import static utils.experiments.StoryUtils.getASEvaluation;
import static utils.experiments.TClassifier.NB;

public class StoryDriver {

  static Logger logger = LoggerFactory.getLogger(StoryDriver.class.getName());



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

  public static Instances applyFilter(Story story, Instances data) {

    int numAttributes = (int) story.get(StoryKey.numAttributes);
    int numToSelect = (int) story.get(StoryKey.numAttributesToSelect);

    if (numAttributes == numToSelect) {
      return new Instances(data);
    } else {

      AttributeSelection attEval = getAttributeSelection(story);
      try {
        attEval.setInputFormat(data);

        return Filter.useFilter(data, attEval);
      } catch (Exception e) {
        e.printStackTrace();
        logger.error("exception in numAttributesToSelect filter");
      }
      return new Instances(data);//this line should be never reached
    }
  }

  public static void experiment1(String... args) throws IOException {

    String confName = args.length > 0 ? args[0] : "data/conf.properties";
    PropsUtils params = PropsUtils.of(confName);

    Path confPath = Paths.get(confName);

    Path resultDir = createOutDir(params.getOutDir());
    logger.info("result directory : {}", resultDir.toString());

    //copy config file to output
    Files.copy(confPath,
            resultDir.getParent().resolve(resultDir.getFileName() + ".properties"));

    List<String> dataSetsNames = params.getDatasets();

    List<Path> arffDatasets = listFiles(
            params.getArffDir(),
            ".arff").stream()
            .filter(path -> dataSetsNames.contains(
                    path.getFileName().toString().replace(".arff", ""))
            ).collect(Collectors.toList());


    for (Path datasetPath : arffDatasets) {

      Instances data = instancesOf(datasetPath);
      data.setClassIndex(data.numAttributes() - 1);

      List<Story> stories = StoryUtils.generateStories(params, data);

      logger.info("processing dataset: {}", data.relationName());
      logger.info("expected stories = {}", stories.size());

      stories.parallelStream()
              .forEach(story -> {
                StoryUtils.playStory(story, data, true);
              });

      writeStoriesToFile(resultDir,
              datasetPath.getFileName().toString() + ".csv"
              , stories);
    }
  }


  public static void experimentSami(String... args) throws IOException {

    String confName = args.length > 0 ? args[0] : "data/sami.final.properties";
    PropsUtils params = PropsUtils.of(confName);

    Path confPath = Paths.get(confName);

    Path resultDir = createOutDir(params.getOutDir());
    logger.info("result directory : {}", resultDir.toString());

    //copy config file to output
    Files.copy(confPath,
            resultDir.getParent().resolve(resultDir.getFileName() + ".properties"));

    List<String> dataSetsNames = params.getDatasets();

    List<Path> arffDatasets = listFiles(
            params.getArffDir(),
            ".arff").stream()
            .filter(path -> dataSetsNames.contains(
                    path.getFileName().toString().replace(".arff", ""))
            ).collect(Collectors.toList());


    for (Path datasetPath : arffDatasets) {

      Instances data = instancesOf(datasetPath);
      data.setClassIndex(data.numAttributes() - 1);

      List<Story> stories = StoryUtils.generateStoriesSami(params, data);

      logger.info("processing dataset: {}", data.relationName());
      logger.info("expected stories = {}", stories.size());

      stories.parallelStream()
              .forEach(story -> {
                StoryUtils.playStory(story, data, true);
              });

      utils.FilesUtils.writeStoriesToFile(resultDir,
              datasetPath.getFileName().toString() + ".csv"
              , stories);
    }
  }



  public static void main(String[] args) throws IOException {
//        experiment1("data/conf_pas_methods.properties");
//        experimentSami("data/sami.final.properties");
//        experiment1("data/sami.final.properties");
//        experimentL2Unbalanced("conf_l2_unbalanced.properties");
  }

}
