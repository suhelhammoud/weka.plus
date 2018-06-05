package weka.attributeSelection.pas;

import weka.classifiers.rules.medri.MedriOptions;

public class ATest {
    public static void main(String[] args) {
        System.out.println(MedriOptions.ALGORITHMS.prism.name());

        MedriOptions.ALGORITHMS medri = MedriOptions.ALGORITHMS.valueOf("medri");
        System.out.println("medri = " + medri);

    }
}
