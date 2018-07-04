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

    /**
     * @param args case PAS:
     *             args[0]: support
     *             args[1]: confidence
     * @return
     */
    public ASEvaluation getWith(Object... args) {
        switch (this) {
            case PAS:
                PasAttributeEval result = new PasAttributeEval();
                result.getPasOptions().setMinFrequency((Double) args[0]);
                result.getPasOptions().setMinItemStrength((Double) args[1]);
                return result;
            default:
                return get();
        }

    }

}





