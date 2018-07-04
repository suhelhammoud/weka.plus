package sensetivity;

import weka.attributeSelection.*;
import weka.attributeSelection.pas.PasMethod;

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

    /**
     * @param args case PAS:
     *             args[0]: support
     *             args[1]: confidence
     *             args[2]: PasMethod
     * @return
     */
    public ASEvaluation getWith(Object... args) {
        switch (this) {
            case PAS:
                PasAttributeEval result = new PasAttributeEval();
                result.setMinFrequency((Double) args[0]);
                result.setMinItemStrength((Double) args[1]);
                result.setPasMethod((PasMethod) args[2]);
                return result;
            default:
                return get();
        }

    }

}





