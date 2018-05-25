package l2Sensitivity;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class RLine {


//  enum METHOD {IG, CHI, L2}


  public static String HEADERS() {
    //TODO check EnumSet.allOf(KEYS)
    return Arrays.stream(KEYS.values())
            .map(item -> item.toString())
            .collect(Collectors.joining(", "));
  }

  Map<KEYS, Object> values;

  public static RLine of(Object dataset) {
    return new RLine(dataset.toString());
  }

  private RLine(String data) {
    values = new HashMap<>(KEYS.values().length);
    values.put(KEYS.dataset, data);
//    values.put(KEYS.numAttributes, numAttributes);
//    values.put(KEYS.method, method);
  }

  public Object get(KEYS key) {
    return values.get(key);
  }

  public RLine set(KEYS key, Object value) {
    values.put(key, value);
    return this;
  }

  public RLine copy() {
    RLine result = RLine.of(this.values.get(KEYS.dataset));
    for (KEYS key : values.keySet()) {
      result.values.put(key, values.get(key));
    }
    return result;
  }

//  public RLine copyBasic() {
//    return new RLine((String) values.get(KEYS.dataset))
//            .set(KEYS.numAttributes, values.get(KEYS.numAttributes));
//  }

  public RLine crossValidation(Instances train) throws Exception {
    NaiveBayes nb = new NaiveBayes();

    Evaluation eval = new Evaluation(train);
    eval.crossValidateModel(nb, train, 10, new Random(1));

    set(KEYS.classifier, RClassifier.NB);
    set(KEYS.errorRate, eval.errorRate());
    set(KEYS.precision, eval.weightedPrecision());
    set(KEYS.recall, eval.weightedRecall());
    set(KEYS.fMeasure, eval.weightedFMeasure());
    return this;
  }

  public String stringValues() {
    return Arrays.stream(KEYS.values())
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
    System.out.println(RLine.HEADERS());

    RLine srun = RLine.of("irirs")
            .set(KEYS.method, RMethod.IG);
    srun.set(KEYS.errorRate, 77);
    System.out.println(srun.stringValues());
  }

}
