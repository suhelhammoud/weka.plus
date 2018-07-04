package sensetivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.PasAttributeEval;
import weka.core.Instances;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static sensetivity.FilesUtils.fileNameNoSuffix;

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

    public static void runDemo(String... args) throws Exception {
        String confName = args.length > 0 ? args[0] : "data/demo1.properties";
        PropsUtils props = PropsUtils.of(confName);

        List<String> dataSetsNames = props.getDatasets();
        logger.info(dataSetsNames.toString());

        List<Path> arffDatasets = FilesUtils.listFiles(
                props.getArffDir(),
                ".arff").stream()
                .filter(path -> dataSetsNames.contains(
                        fileNameNoSuffix(path)))
                .sorted((o1, o2) -> dataSetsNames.indexOf(fileNameNoSuffix(o1))
                        - dataSetsNames.indexOf(fileNameNoSuffix(o2)))
                .collect(Collectors.toList());


        for (Path datasetPath : arffDatasets) {

            Instances data = FilesUtils.instancesOf(datasetPath);
            data.setClassIndex(data.numAttributes() - 1);

            logger.info("dataset = {}", datasetPath.getFileName());
            logger.info("num attributes = {}", data.numAttributes() - 1);

            double support = props.getEvalSupports().get(0);
            double confidence = props.getEvalConfidences().get(0);
            PasAttributeEval eval = (PasAttributeEval) TEvaluator
                    .PAS.getWith(support, confidence);

            eval.getPasOptions().setShowDebugMessages(props.getPrintRanks());

            logger.info("PAS with support = {}, and confidence = {} ",
                    support,
                    confidence
            );
            int minimumFrequency = (int) Math.ceil(data.numInstances() * support);
            logger.info("minimum frequency (support) = {} instances ", minimumFrequency);

            eval.buildEvaluator(data);

        }
    }

    public static void main(String[] args) throws IOException {
        experiment1("data/conf_anneal.properties");
    }

}
