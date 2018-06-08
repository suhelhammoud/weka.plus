package sensetivity;

import weka.attributeSelection.*;

public enum TEvaluator {
    IG, CHI, L2, PAS;

    public ASEvaluation get() {
        switch (this) {
            case IG:
                return new InfoGainAttributeEval();
            case L2:
                return new L2AttributeEval();
            case CHI:
                return new ChiSquaredAttributeEval();
            case PAS:
                return new PasAttributeEval();
            default:
                System.err.println(name() + " unknown ASEvaluation");
                return null;
        }
    }

}





