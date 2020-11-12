package odri.experiments.odricsv;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class ORLine {




  public static String HEADERS() {
    //TODO check EnumSet.allOf(L2KEYS)
    return Arrays.stream(L2KEYS.values())
            .map(item -> item.toString())
            .collect(Collectors.joining(", "));
  }

  Map<L2KEYS, Object> values;

  public static ORLine of(Object dataset) {
    return new ORLine(dataset.toString());
  }

  private ORLine(String data) {
    values = new HashMap<>(L2KEYS.values().length);
    values.put(L2KEYS.dataset, data);
//    values.put(L2KEYS.numAttributes, numAttributes);
//    values.put(L2KEYS.method, method);
  }

  public Object get(L2KEYS key) {
    return values.get(key);
  }

  public ORLine set(L2KEYS key, Object value) {
    values.put(key, value);
    return this;
  }

  public ORLine copy() {
    ORLine result = ORLine.of(this.values.get(L2KEYS.dataset));
    for (L2KEYS key : values.keySet()) {
      result.values.put(key, values.get(key));
    }
    return result;
  }

//  public L2RLine copyBasic() {
//    return new L2RLine((String) values.get(L2KEYS.dataset))
//            .set(L2KEYS.numAttributes, values.get(L2KEYS.numAttributes));
//  }

  public ORLine crossValidation(Instances train) throws Exception {
    NaiveBayes nb = new NaiveBayes();

    Evaluation eval = new Evaluation(train);
    eval.crossValidateModel(nb, train, 10, new Random(1));

    set(L2KEYS.classifier, L2RClassifier.NB);
    set(L2KEYS.errorRate, eval.errorRate());
    set(L2KEYS.precision, eval.weightedPrecision());
    set(L2KEYS.recall, eval.weightedRecall());
    set(L2KEYS.fMeasure, eval.weightedFMeasure());
    return this;
  }

  public String stringValues() {
    return Arrays.stream(L2KEYS.values())
            .map(key -> values.containsKey(key) ?
                    values.get(key).toString() :
                    "")
            .collect(Collectors.joining(", "));
  }

  @Override
  public String toString() {
    return stringValues();
  }

  public static void main(String[] args) {
    System.out.println(ORLine.HEADERS());

    ORLine srun = ORLine.of("irirs")
            .set(L2KEYS.att_method, L2AttributeEvalMethod.IG);
    srun.set(L2KEYS.errorRate, 77);
    System.out.println(srun.stringValues());
  }

}
