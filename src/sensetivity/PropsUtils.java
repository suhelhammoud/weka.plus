package sensetivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.pas.PasMethod;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PropsUtils extends Properties {
    static Logger logger = LoggerFactory.getLogger(PropsUtils.class.getName());

    public static PropsUtils of(String fileName) throws IOException {
        PropsUtils result = new PropsUtils();
        result.load(new FileReader(fileName));
        result.init();
        return result;
    }

    private List<TEvaluator> evaluatorMethods;
    private List<TClassifier> classifiers;

    private List<Double> evalSupports;
    private List<Double> evalConfidences;

    private List<Double> supports;
    private List<Double> confidences;
    private double cutoffThreshold;

    private double l2ClassResampleSize;
    private List<Double> l2ClassRatios;
    private int l2ClassRepeat;
    private boolean l2ClassRandomSeed;

    private String arffDir;
    private List<String> datasets;
    private String outDir;

    private boolean printRanks;

    private List<PasMethod> pasMethods;

    public double getL2ClassResampleSize() {
        return l2ClassResampleSize;
    }

    public void setL2ClassResampleSize(double l2ClassResampleSize) {
        this.l2ClassResampleSize = l2ClassResampleSize;
    }

    public boolean isL2ClassRandomSeed() {
        return l2ClassRandomSeed;
    }

    public void setL2ClassRandomSeed(boolean l2ClassRandomSeed) {
        this.l2ClassRandomSeed = l2ClassRandomSeed;
    }



    public List<PasMethod> getPasMethods() {
        return pasMethods;
    }

    public void setPrintRanks(boolean printRanks) {
        this.printRanks = printRanks;
    }

    public boolean getPrintRanks() {
        return printRanks;
    }

    public List<Double> getEvalSupports() {
        return evalSupports;
    }

    public List<Double> getEvalConfidences() {
        return evalConfidences;
    }

    public List<String> getDatasets() {
        return datasets;
    }

    public String getArffDir() {
        return arffDir;
    }

    public List<TEvaluator> getEvaluatorMethods() {
        return evaluatorMethods;
    }

    public List<TClassifier> getClassifiers() {
        return classifiers;
    }

    public List<Double> getSupports() {
        return supports;
    }

    public List<Double> getConfidences() {
        return confidences;
    }

    public double getCutoffThreshold() {
        return cutoffThreshold;
    }

    public int getL2ClassRepeat() {
        return l2ClassRepeat;
    }

    public void setL2ClassRepeat(int l2ClassRepeat) {
        this.l2ClassRepeat = l2ClassRepeat;
    }

    public List<Double> getL2ClassRatios() {
        return l2ClassRatios;
    }

    public void setL2ClassRatios(List<Double> l2ClassRatios) {
        this.l2ClassRatios = l2ClassRatios;
    }

    public void setL2ClassRatios(double ... ratios) {
        this.l2ClassRatios = Arrays.stream(ratios).boxed().collect(Collectors.toList());
    }

    public String getOutDir() {
        return outDir;
    }


    private Stream<String> getStream(String property) {
        return getStream(property, "");
    }

    private Stream<String> getStream(String property, String defaultValue) {
        return Arrays.stream(getProperty(property, defaultValue)
                .trim().split("\\s+"))
                .map(s -> s.trim())
                .filter(s -> s.length() > 0);
    }


    private void init() {

        evaluatorMethods = getStream("eval.methods")
                .map(s -> TEvaluator.valueOf(s.toUpperCase()))
                .collect(Collectors.toList());

        classifiers = getStream("classifiers")
                .map(s -> TClassifier.valueOf(s.toUpperCase()))
                .collect(Collectors.toList());

        logger.debug("classifiers : {}", classifiers);

        evalSupports = getStream("eval.supports")
                .mapToDouble(i -> Double.valueOf(i))
                .boxed()
                .collect(Collectors.toList());

        evalConfidences = getStream("eval.confidences")
                .mapToDouble(i -> Double.valueOf(i))
                .boxed()
                .collect(Collectors.toList());

        cutoffThreshold = Double.parseDouble(getProperty("cutoff.threshold", "0.5"));

        pasMethods = getStream("pas.methods",
                PasMethod.items.name())
                .map(m -> PasMethod.of(m))
                .collect(Collectors.toList());


        supports = getStream("supports")
                .mapToDouble(i -> Double.valueOf(i))
                .boxed()
                .collect(Collectors.toList());

        confidences = getStream("confidences")
                .mapToDouble(i -> Double.valueOf(i))
                .boxed()
                .collect(Collectors.toList());

        l2ClassResampleSize = Double.parseDouble(
                getProperty("l2.class.resample.size", "1.0"));
        logger.debug("l2.class.resample.size = {}", l2ClassResampleSize);

        l2ClassRatios = getStream("l2.class.ratios")
                .mapToDouble(i -> Double.valueOf(i))
                .boxed()
                .collect(Collectors.toList());
        logger.debug("l2.class.ratios : {}", getL2ClassRatios());

        l2ClassRepeat = Integer.parseInt(getProperty("l2.class.repeat"), 10);
        logger.debug("l2.class.repeat : {}", getL2ClassRepeat());

        l2ClassRandomSeed = Boolean.parseBoolean(getProperty("l2.class.random.seed", "False"));
        logger.debug("l2.class.random.seed : {}", l2ClassRandomSeed);

        arffDir = getProperty("arff.dir").trim();

        datasets = Arrays.asList(getProperty("datasets", "")
                .trim().split("\\s+"));

        outDir = getProperty("out.dir", "data/results");

        printRanks = Boolean.valueOf(getProperty("print.ranks", "false"));
    }

    public static void main(String[] args)
            throws IOException {
        PropsUtils params = PropsUtils.of("data/conf_l2_unbalanced.properties");

        System.out.println("params.getPasMethods() = " + params.getPasMethods());
        System.out.println("evalSupports = " + params.getEvalSupports());

        System.out.println("evalConfidences = " + params.getEvalConfidences());
        System.out.println("arff.dir = " + params.getArffDir());
        System.out.println("datasets = " + params.getDatasets());
        System.out.println("params.getEvaluatorMethods() = "
                + params.getEvaluatorMethods());
        System.out.println("params.getClassifiers() = "
                + params.getClassifiers());
        System.out.println("params.getCutoffThreshold() = " + params.getCutoffThreshold());

        logger.info("l2.class.repeat = {}", params.getL2ClassRepeat());
        logger.info("l2.class.ratios = {}", params.getL2ClassRatios());
        logger.info("l2.class.random.seed = {}", params.isL2ClassRandomSeed());
        logger.info("l2.class.resample.size = {}", params.getL2ClassResampleSize());
    }
}
