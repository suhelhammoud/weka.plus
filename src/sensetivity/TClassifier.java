package sensetivity;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.rules.MeDRI;
import weka.classifiers.rules.medri.MedriOptions;

public enum TClassifier {
  NB, MEDRI;

  public Classifier get() {
    switch (this) {
      case NB:
        return new NaiveBayes();
      case MEDRI:
        return new MeDRI();
      default:
        System.err.println(name() + " unknown Classifier");
    }
    return null;
  }

  public Classifier getWith(Object... args) {
    switch (this) {
      case MEDRI:
        double support = (double) args[0];
        double confidence = (double) args[1];
        MeDRI result = new MeDRI();
        result.setMinFrequency(support);
        result.setMinRuleStrength(confidence);
        result.setAddDefaultRule(true);
        result.setAlgorithm(MedriOptions.ALGORITHMS.medri.selectedTag());
        return result;
      default:
        return get();
    }
  }

  public String className() {
    switch (this) {
      case NB:
        return NaiveBayes.class.getName();
      case MEDRI:
        return MeDRI.class.getName();
      default:
        return "error class name for "+ this;
    }
  }
}