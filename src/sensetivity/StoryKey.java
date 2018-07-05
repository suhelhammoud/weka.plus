package sensetivity;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum StoryKey {
//  dataset, numAttributes, method, median, variables,
//  classifier, errorRate, precision, recall, fMeasure

    dataset, //relation name, or dataset filename
    numInstances,
    numAttributes,// excluding the label class attribute
    evalMethod, //PAS, L2, CHI, ..etc
    pasMethod, //items,
    evalSupport,// for PAS attribute selector only
    evalConfidence, // for PAS attribute selector only
    numAttributesToSelect, //for attribute selection filter
    entropy, //entropy threshold for num of attributes
    huffman, //huffman threshold for num of attributes

    classifier, // NB, MeDRI
    support, //
    confidence, //

    /* Classification results */
    errorRate,
    precision,
    recall,
    fMeasure;

    public static String csvHeaders() {
        //TODO check EnumSet.allOf(StoryKey)
        return Arrays.stream(StoryKey.values())
                .map(item -> item.toString())
                .collect(Collectors.joining(", "));
    }
}
