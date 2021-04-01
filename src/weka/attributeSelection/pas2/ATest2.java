package weka.attributeSelection.pas2;

import weka.classifiers.rules.medri.MedriOptions;

public class ATest2 {
  public static void main(String[] args) {
    System.out.println(MedriOptions.ALGORITHMS.prism.name());

    MedriOptions.ALGORITHMS medri = MedriOptions.ALGORITHMS.valueOf("medri");
    System.out.println("medri = " + medri);

  }
}
