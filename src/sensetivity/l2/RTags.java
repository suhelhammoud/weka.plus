package sensetivity.l2;

import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.L2AttributeEval;
import weka.classifiers.bayes.NaiveBayes;

enum L2AttributeEvalMethod {
  IG, CHI, L2;

  public String className() {
    switch (this) {
      case L2:
        return L2AttributeEval.class.getName();
      case CHI:
        return ChiSquaredAttributeEval.class.getName();
      default: // case IG:
        return InfoGainAttributeEval.class.getName();
    }
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
  dataset, numAttributes, att_method, median,
  variables, classifier, errorRate, precision,
  recall, fMeasure, weightedAreaUnderROC,
  areaUnderROC0, areaUnderROC1
}

