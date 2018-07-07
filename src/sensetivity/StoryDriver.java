package sensetivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class StoryDriver {

    static Logger logger = LoggerFactory.getLogger(StoryDriver.class.getName());


    public static void experiment1(String... args) throws IOException {

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

            List<Story> stories = StoryUtils.generateStories(params, data);

            logger.info("processing dataset: {}", data.relationName());
            logger.info("expected stories = {}", stories.size());

            stories.parallelStream()
                    .forEach(story -> {
                        StoryUtils.playStory(story, data, true);
                    });

            FilesUtils.writeStoriesToFile(resultDir,
                    datasetPath.getFileName().toString() + ".csv"
                    , stories);
        }
    }



    public static void experimentSami(String... args) throws IOException {

        String confName = args.length > 0 ? args[0] : "data/sami.final.properties";
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

            List<Story> stories = StoryUtils.generateStoriesSami(params, data);

            logger.info("processing dataset: {}", data.relationName());
            logger.info("expected stories = {}", stories.size());

            stories.parallelStream()
                    .forEach(story -> {
                        StoryUtils.playStory(story, data, true);
                    });

            FilesUtils.writeStoriesToFile(resultDir,
                    datasetPath.getFileName().toString() + ".csv"
                    , stories);
        }
    }



    public static void main(String[] args) throws IOException {
//        experiment1("data/conf_pas_methods.properties");
//        experimentSami("data/sami.final.properties");
        experiment1("data/sami.final.properties");
    }

}
