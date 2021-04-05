package utils.experiments;

import java.util.Arrays;
import java.util.stream.Collectors;

import static utils.experiments.StoryKey.KeyType.*;

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
  classifier(ENUM), // NB, MeDRI, ODRI
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

  weightedAreaUnderROC(DOUBLE),
  weightedAreaUnderROCVariance(DOUBLE),


  /*ROC */
  /* ( True | False) Rate, (Positive | Negative) Rate */
//  tpr(DOUBLE),tnr(DOUBLE), fpr(DOUBLE), fnr(DOUBLE),
//
//  /* Weighted ( True | False) Rate , Weighted (Positive | Negative) Rate */,
//  wtpr(DOUBLE),wtnr(DOUBLE), wfpr(DOUBLE), wfnr(DOUBLE),

  areaUnderROC0(DOUBLE), areaUnderROC0Variance(DOUBLE),
  areaUnderROC1(DOUBLE), areaUnderROC1Variance(DOUBLE),

  /* odri */
  minOcc(INT),
  addDefaultRule(BOOLEAN),
  minOccPC(DOUBLE),
  numGeneratedRules(INT),
  numGeneratedRulesPC(DOUBLE),
  expectedRuleLength(DOUBLE), //Ex( sum ( rule.length * rule.coverage)))
  averageRuleLength(DOUBLE),  //  sum(rule.length) / numRules


  correctPC(DOUBLE),
  coverPC(DOUBLE),

  expectedDimensionality(DOUBLE),
  expectedDimensionalityPC(DOUBLE),
  expectedErrorsPC(DOUBLE), pctCorrect(DOUBLE);

  StoryKey(KeyType keyType) {
    this.keyType = keyType;
  }

  public enum KeyType {
    DOUBLE, STRING, INT, OBJECT, BOOLEAN, ENUM
  }

  public final KeyType keyType;


  public static boolean contains(String keyName) {
    return Arrays.stream(values())
            .anyMatch(storyKey -> storyKey.name().equals(keyName));
  }

  public static String csvHeaders(StoryKey... keys) {
    return Arrays.stream(keys.length > 0 ? keys : StoryKey.values())
            .map(key -> key.toString())
            .collect(Collectors.joining(", "));
  }
}
