package sensetivity.l2;

import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.classifiers.bayes.NaiveBayes;

enum L2AttributeEvalMethod {
  IG, CHI, L2;

  public String className() {
    switch (this) {
      case L2:
        return InfoGainAttributeEval.class.getName();
      case IG:
        return ChiSquaredAttributeEval.class.getName();
      case CHI:
        return ChiSquaredAttributeEval.class.getName();
    }
    return "error";
  }

  public static void main(String[] args) {
    System.out.println(L2RClassifier.NB.className());
  }
}

enum L2RClassifier {
  NB;

  public String className() {
    switch (this) {
      case NB:
        return NaiveBayes.class.getName();
    }
    return "error";
  }
}

enum L2KEYS {
  dataset, numAttributes, att_method, median, variables,
  classifier, errorRate, precision, recall, fMeasure
}

