package sensetivity;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum StoryKey {

  dataset, //relation name, or dataset filename
  numInstances,
  numAttributes,// excluding the label class attribute
  evalMethod, //PAS, L2, CHI, ..etc
  pasMethod, //rules, rules1st, and items,
  evalSupport,// for PAS attribute selector only
  evalConfidence, // for PAS attribute selector only
  numAttributesToSelect, //for attribute selection filter
  entropy, //entropy threshold for num of attributes
  huffman, //huffman threshold for num of attributes
  cutoffThreshold, // threshold in
  variablesThreshold, //threshold out
  classifier, // NB, MeDRI
  support, //
  confidence, //
  l2ResampleSizeRatio, // compared to original numInstances
  l2ClassRatio, // for binary class labels only
  l2ClassRepeat,
  l2ClassExperimentID,
  l2ClassExperimentIteration,

  /* Classification results */
  errorRate,
  precision,
  recall,
  fMeasure;


  public static String csvHeaders(StoryKey... keys) {
    return Arrays.stream(keys.length > 0 ?
            keys : StoryKey.values())
            .map(key -> key.toString())
            .collect(Collectors.joining(", "));
  }
}
