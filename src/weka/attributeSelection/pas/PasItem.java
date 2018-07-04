package weka.attributeSelection.pas;

import weka.classifiers.rules.medri.IRule;

public class PasItem extends IRule {

    private int[] corrects;

    public int[] getCorrects() {
        return corrects;
    }

    public PasItem(int label, int correct, int covers) {
        super(label, correct, covers);
    }

    public PasItem(int label) {
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
