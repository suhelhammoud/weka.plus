package sensetivity;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

enum TaskKey {
//  dataset, numAttributes, method, median, variables,
//  classifier, errorRate, precision, recall, fMeasure

    dataset,
    numInstances,
    numAttributes,
    evalMethod,
    evalSupport,
    evalConfidence,
    attributesToSelect,

    classifier,
    support,
    confidence,

    errorRate,
    precision,
    recall,
    fMeasure;

    public static String csvHeaders() {
        //TODO check EnumSet.allOf(TaskKey)
        return Arrays.stream(TaskKey.values())
                .map(item -> item.toString())
                .collect(Collectors.joining(", "));
    }
}

public class TaskData {

    Map<TaskKey, Object> data;

    public static TaskData of(Object datasetFileName) {
        return new TaskData(datasetFileName.toString());
    }

    private TaskData(String datasetFileName) {
        data = new HashMap<>(TaskKey.values().length);
        data.put(TaskKey.dataset, datasetFileName);
    }

    public Object get(TaskKey key) {
        return data.get(key);
    }

    public TaskData set(TaskKey key, Object value) {
        data.put(key, value);
        return this;
    }

    public TaskData copy() {
        TaskData result = TaskData.of(this.data.get(TaskKey.dataset));
        for (TaskKey key : data.keySet()) {
            result.data.put(key, data.get(key));
        }
        return result;
    }


    public TaskData crossValidation(Instances train) throws Exception {
        NaiveBayes nb = new NaiveBayes();

        Evaluation eval = new Evaluation(train);
        eval.crossValidateModel(nb, train, 10, new Random(1));

        set(TaskKey.classifier, TClassifier.NB);
        set(TaskKey.errorRate, eval.errorRate());
        set(TaskKey.precision, eval.weightedPrecision());
        set(TaskKey.recall, eval.weightedRecall());
        set(TaskKey.fMeasure, eval.weightedFMeasure());
        return this;
    }

    public String stringValues() {
        return Arrays.stream(TaskKey.values())
                .map(key -> data.containsKey(key) ?
                        data.get(key).toString() :
                        ""
                )
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return stringValues();
    }

    public static void main(String[] args) {
        System.out.println(TaskKey.csvHeaders());

        TaskData srun = TaskData.of("irirs")
                .set(TaskKey.evalMethod, TEvaluator.IG);
        srun.set(TaskKey.errorRate, 77);
        System.out.println(srun.stringValues());
    }

}
