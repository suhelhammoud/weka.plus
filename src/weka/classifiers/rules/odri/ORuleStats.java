package weka.classifiers.rules.odri;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ORuleStats {
  private final List<ORule> rules;
  private final int numInstances;
  private final int numAttributes;

  public ORuleStats(List<ORule> rules,
                    int numInstances,
                    int numAttributes) {
//    this.rules = rules.stream()
//            .map(r -> r.copy())
//            .collect(Collectors.toList());
    this.rules = Collections.unmodifiableList(rules);
    this.numInstances = numInstances;
    this.numAttributes = numAttributes;
  }

  public int numRules() {
    return rules.size();
  }

  public double numRulesPC() {
    return (double) rules.size() / numInstances;
  }

  public double correctPC() {
    return rules.stream()
            .mapToInt(ORule::getCorrect)
            .sum() / (double) numInstances;
  }

  public double coversPC() {
    return rules.stream()
            .mapToInt(ORule::getCovers)
            .sum() / (double) numInstances;
  }


  public double lengthWeightebyCover() {
    return rules.stream()
            .mapToDouble(ORule::getLenghtCoverWeighted)
            .sum() / numInstances;
  }

  public double expectedErrorsPercent() {
    return rules.stream()
            .mapToDouble(ORule::getErrors)
            .sum() / numInstances;
  }

  public double expectedDimensionality() {
    int totalCorrect = rules.stream()
            .mapToInt(r -> r.correct)
            .sum();
    int totalErrors = numInstances - totalCorrect;
    double aCorrect = rules.stream()
            .mapToDouble(r -> r.correct * r.getLength())
            .sum();
    return (aCorrect + totalErrors * numAttributes) / numInstances;
  }

  public double expectedDimensionalityPC() {
    return expectedDimensionality() / numAttributes;
  }

}
