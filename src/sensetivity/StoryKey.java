package sensetivity;

import java.util.Arrays;
import java.util.stream.Collectors;

import static sensetivity.StoryKey.KeyType.*;


public enum StoryKey {

  dataset(STRING), //relation name, or dataset filename
  numInstances(INT),
  numAttributes(INT),// excluding the label class attribute
  attEvalMethod(ENUM), //PAS, L2, CHI, ..etc
  pasMethod(ENUM), //rules, rules1st, and items,
  evalSupport(DOUBLE),// for PAS attribute selector only
  evalConfidence(DOUBLE), // for PAS attribute selector only
  numAttributesToSelect(INT), //for attribute selection filter
  entropy(DOUBLE), //entropy threshold for num of attributes
  huffman(DOUBLE), //huffman threshold for num of attributes
  cutoffThreshold(DOUBLE), // threshold in
  variablesThreshold(DOUBLE), //threshold out
  classifier(ENUM), // NB, MeDRI
  support(DOUBLE), //
  confidence(DOUBLE), //
  l2ResampleSizeRatio(DOUBLE), // compared to original numInstances
  l2ClassRatio(DOUBLE), // for binary class labels only
  l2ClassRepeat(INT),
  experimentID(INT),
  l2ClassExperimentIteration(INT),

  /* Classification results */
  errorRate(DOUBLE), errorRateVariance(DOUBLE),
  precision(DOUBLE), precisionVariance(DOUBLE),
  recall(DOUBLE), recallVariance(DOUBLE),
  fMeasure(DOUBLE), fMeasureVariance(DOUBLE),

  /*ROC */
  weightedAreaUnderROC(DOUBLE), weightedAreaUnderROCVariance(DOUBLE),
  areaUnderROC0(DOUBLE), areaUnderROC0Variance(DOUBLE),
  areaUnderROC1(DOUBLE), areaUnderROC1Variance(DOUBLE);

  StoryKey(KeyType keyType) {
    this.keyType = keyType;
  }

  public static enum KeyType {
    DOUBLE, STRING, INT, OBJECT, BOOLEAN, ENUM
  }

  public final KeyType keyType;


  public static boolean contains(String keyName){
    return Arrays.stream(values())
            .anyMatch(storyKey -> storyKey.name().equals(keyName));
  }

  public static String csvHeaders(StoryKey... keys) {
    return Arrays.stream(keys.length > 0 ? keys : StoryKey.values())
            .map(key -> key.toString())
            .collect(Collectors.joining(", "));
  }
}
