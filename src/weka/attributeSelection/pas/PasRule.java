package weka.attributeSelection.pas;

import weka.classifiers.rules.medri.IRule;

public class PasRule extends IRule {

    private int[] corrects;

    public int[] getCorrects() {
        return corrects;
    }

    public PasRule(int label, int correct, int covers) {
        super(label, correct, covers);
    }

    public PasRule(int label) {
        super(label);
        this.corrects = new int[0];
    }

    public boolean addTest(int att, int val, int correct) {
        if (contains(attIndexes, att))
            return false;
        attIndexes = addElement(attIndexes, att);
        attValues = addElement(attValues, val);
        corrects = addElement(corrects, correct);
        return true;
    }

}
