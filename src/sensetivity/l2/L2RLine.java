package sensetivity.l2;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class L2RLine {


//  enum METHOD {IG, CHI, L2}


  public static String HEADERS() {
    //TODO check EnumSet.allOf(L2KEYS)
    return Arrays.stream(L2KEYS.values())
            .map(item -> item.toString())
            .collect(Collectors.joining(", "));
  }

  Map<L2KEYS, Object> values;

  public static L2RLine of(Object dataset) {
    return new L2RLine(dataset.toString());
  }

  private L2RLine(String data) {
    values = new HashMap<>(L2KEYS.values().length);
    values.put(L2KEYS.dataset, data);
//    values.put(L2KEYS.numAttributes, numAttributes);
//    values.put(L2KEYS.method, method);
  }

  public Object get(L2KEYS key) {
    return values.get(key);
  }

  public L2RLine set(L2KEYS key, Object value) {
    values.put(key, value);
    return this;
  }

  public L2RLine copy() {
    L2RLine result = L2RLine.of(this.values.get(L2KEYS.dataset));
    for (L2KEYS key : values.keySet()) {
      result.values.put(key, values.get(key));
    }
    return result;
  }

//  public L2RLine copyBasic() {
//    return new L2RLine((String) values.get(L2KEYS.dataset))
//            .set(L2KEYS.numAttributes, values.get(L2KEYS.numAttributes));
//  }

  public L2RLine crossValidation(Instances train) throws Exception {
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
    System.out.println(L2RLine.HEADERS());

    L2RLine srun = L2RLine.of("irirs")
            .set(L2KEYS.att_method, L2AttributeEvalMethod.IG);
    srun.set(L2KEYS.errorRate, 77);
    System.out.println(srun.stringValues());
  }

}
