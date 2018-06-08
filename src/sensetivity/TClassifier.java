package sensetivity;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.rules.MeDRI;

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
}