package sensetivity;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EvalProps extends Properties {

    public static EvalProps of(String fileName) throws IOException {
        EvalProps result = new EvalProps();
        result.load(new FileReader(fileName));
        result.init();
        return result;
    }

    private TEvaluator[] evaluatorMethods;
    private TClassifier[] classifiers;

    private double[] evalSupports;
    private double[] evalConfidences;

    private double[] supports;
    private double[] confidences;


    private String arffDir;
    private String[] datasets;
    private String outDir;


    public double[] getEvalSupports() {
        return evalSupports;
    }

    public double[] getEvalConfidences() {
        return evalConfidences;
    }

    public String[] getDatasets() {
        return datasets;
    }

    public String getArffDir() {
        return arffDir;
    }

    public TEvaluator[] getEvaluatorMethods() {
        return evaluatorMethods;
    }

    public TClassifier[] getClassifiers() {
        return classifiers;
    }

    public double[] getSupports() {
        return supports;
    }

    public double[] getConfidences() {
        return confidences;
    }

    public String getOutDir() {
        return outDir;
    }

    private Stream<String> getStream(String property) {
        return Arrays.stream(getProperty(property, "").trim().split("\\s+"))
                .map(s -> s.trim())
                .filter(s -> s.length() > 0);
    }


    private void init() {

        evaluatorMethods = getStream("eval.methods")
                .map(s -> TEvaluator.valueOf(s.toUpperCase()))
                .toArray(TEvaluator[]::new);

        classifiers = getStream("classifiers")
                .map(s -> TClassifier.valueOf(s.trim().toUpperCase()))
                .toArray(TClassifier[]::new);

        evalSupports = getStream("eval.supports")
                .mapToDouble(i -> Double.valueOf(i))
                .toArray();

        evalConfidences = getStream("eval.confidences")
                .mapToDouble(i -> Double.valueOf(i))
                .toArray();

        supports = getStream("supports")
                .mapToDouble(i -> Double.valueOf(i))
                .toArray();

        confidences = getStream("confidences")
                .mapToDouble(i -> Double.valueOf(i))
                .toArray();

        arffDir = getProperty("arff.dir").trim();

        datasets = getProperty("datasets", "").trim().split("\\s+");

        outDir = getProperty("out.dir", "data/results");
    }

    public static void main(String[] args)
            throws IOException {
        EvalProps params = EvalProps.of("data/eval.properties");

        System.out.println("evalSupports = " + Arrays.toString(params.getEvalSupports()));
        System.out.println("evalConfidences = " + Arrays.toString(params.getEvalConfidences()));
        System.out.println("arff.dir = " + params.getArffDir());
        System.out.println("datasets = " + Arrays.toString(params.getDatasets()));
        System.out.println("params.getEvaluatorMethods() = " + Arrays.toString(params.getEvaluatorMethods()));
        System.out.println("params.getClassifiers() = " + Arrays.toString(params.getClassifiers()));
    }
}
