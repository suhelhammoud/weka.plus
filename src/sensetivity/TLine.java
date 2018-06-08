package sensetivity;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

enum TKeys {
//  dataset, numAttributes, method, median, variables,
//  classifier, errorRate, precision, recall, fMeasure

    dataset, numAttributes, method, median, variables,
    classifier, errorRate, precision, recall, fMeasure;

    public static String csvHeaders() {
        //TODO check EnumSet.allOf(TKeys)
        return Arrays.stream(TKeys.values())
                .map(item -> item.toString())
                .collect(Collectors.joining(", "));
    }
}

public class TLine {

    Map<TKeys, Object> values;

    public static TLine of(Object dataset) {
        return new TLine(dataset.toString());
    }

    private TLine(String datasetFileName) {
        values = new HashMap<>(TKeys.values().length);
        values.put(TKeys.dataset, datasetFileName);
    }

    public Object get(TKeys key) {
        return values.get(key);
    }

    public TLine set(TKeys key, Object value) {
        values.put(key, value);
        return this;
    }

    public TLine copy() {
        TLine result = TLine.of(this.values.get(TKeys.dataset));
        for (TKeys key : values.keySet()) {
            result.values.put(key, values.get(key));
        }
        return result;
    }


    public TLine crossValidation(Instances train) throws Exception {
        NaiveBayes nb = new NaiveBayes();

        Evaluation eval = new Evaluation(train);
        eval.crossValidateModel(nb, train, 10, new Random(1));

        set(TKeys.classifier, TClassifier.NB);
        set(TKeys.errorRate, eval.errorRate());
        set(TKeys.precision, eval.weightedPrecision());
        set(TKeys.recall, eval.weightedRecall());
        set(TKeys.fMeasure, eval.weightedFMeasure());
        return this;
    }

    public String stringValues() {
        return Arrays.stream(TKeys.values())
                .map(key -> values.containsKey(key) ?
                        values.get(key).toString() :
                        ""
                )
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return stringValues();
    }

    public static void main(String[] args) {
        System.out.println(TKeys.csvHeaders());

        TLine srun = TLine.of("irirs")
                .set(TKeys.method, TEvaluator.IG);
        srun.set(TKeys.errorRate, 77);
        System.out.println(srun.stringValues());
    }

}
