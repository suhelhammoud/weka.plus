package sensetivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.PasAttributeEval;
import weka.attributeSelection.PasAttributeEvalDemo;
import weka.core.Instances;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static sensetivity.FilesUtils.fileNameNoSuffix;

public class Demo {
    static Logger logger = LoggerFactory.getLogger(Demo.class.getName());



    public static void runDemo(String... args) throws Exception {
        String confName = args.length > 0 ? args[0] : "data/demo2.properties";
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
            PasAttributeEvalDemo eval = new PasAttributeEvalDemo();
            eval.setSupport(support);
            eval.setConfidence(confidence);

            eval.setShowDebugMessages(props.getPrintRanks());

            logger.info("PAS with support = {}, and confidence = {} ",
                    support,
                    confidence
            );
            int minimumFrequency = (int) Math.ceil(data.numInstances() * support);
            logger.info("minimum frequency (support) = {} instances ", minimumFrequency);

            eval.buildEvaluator(data);
        }
    }

    public static void main(String[] args) throws Exception {
        runDemo("data/demo2.properties");
    }
}
