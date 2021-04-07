package utils.experiments;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.rules.MeDRI;
import weka.classifiers.rules.ODRI;
import weka.classifiers.rules.medri.MedriOptions;

public enum TClassifier {
  NB, MEDRI, ODRI_T;

  public Classifier get() {
    switch (this) {
      case NB:
        return new NaiveBayes();
      case MEDRI:
        return new MeDRI();
      case ODRI_T:
        return new ODRI();
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
      case ODRI_T:
        int minOcc = (int) args[0];
        boolean addDefaultRule = args.length == 2 && (boolean) args[1];
        ODRI oResult = new ODRI();
        oResult.setMinOccurrence(minOcc);
        oResult.setAddDefaultRule(addDefaultRule);
        return oResult;

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
      case ODRI_T:
        return ODRI.class.getName();
      default:
        return "error class name for " + this;
    }
  }
}