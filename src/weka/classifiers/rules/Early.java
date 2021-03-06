package weka.classifiers.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.rules.early.*;
import weka.core.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by suhel on 23/03/16.
 */
public class Early implements Classifier, OptionHandler,
        CapabilitiesHandler, TechnicalInformationHandler, Serializable {

  static Logger logger = LoggerFactory.getLogger(Early.class.getName());
  static final long serialVersionUID = 1210258885525901107L;

  private String[] labels;

  /**
   * Returns the revision string.
   *
   * @return the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 001 $");
  }

  /**
   * Returns a string describing classifier
   *
   * @return a description suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Class for building and using a medri rule set for classification. "
            + "Can only deal with nominal attributes. Can't deal with missing values. "
            + "For more information, see \n\n"
            + getTechnicalInformation().toString();
  }

  /**
   * Returns an instance of a TechnicalInformation object, containing
   * detailed information about the technical background of this class,
   * e.g., paper reference or book this class is based on.
   *
   * @return the technical information about this class
   */
  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation result;

    result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "F. Thabtah, S. Hammoud");
    result.setValue(TechnicalInformation.Field.YEAR, "2020");
    result.setValue(TechnicalInformation.Field.TITLE, "early: An algorithm for inducing modular rules");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Journal");
    result.setValue(TechnicalInformation.Field.VOLUME, "vol");
    result.setValue(TechnicalInformation.Field.NUMBER, "number");
    result.setValue(TechnicalInformation.Field.PAGES, "p_start-p_end");
    return result;
  }

  /**
   * Holds algorithm configurations and OptionHandler parameters
   */
  private EarlyOptions moptions = new EarlyOptions();

  /**
   * Holds algorithm configurations and MedriOption parameters
   */
  private List<ERule> m_rules = new ArrayList<>();

  /**
   * Classifies a given instance.
   *
   * @param inst the instance to be classified
   * @return the classification
   */
  public double classifyInstance(Instance inst) {

    for (ERule rule : m_rules) {
      int cls = rule.classify(EarlyUtils.toIntArray(inst));
      if (cls != ERule.EMPTY)
        return cls;
    }
//        return Instance.missingValue();
    // TODO how to return missing values? old weka is not working no more!
    return -1;
  }

  //TODO use instead of EMPTY (-1) returned values, later...
  public boolean canClassifyInstance(Instance inst) {
    return m_rules.stream()
            .anyMatch(r -> r.canCoverInstance(EarlyUtils.toIntArray(inst)));
  }

  @Override
  public double[] distributionForInstance(Instance instance) throws Exception {
    //TODO bug fix, exceptions in classification are not considered mis-classification
    int predication = (int) classifyInstance(instance);
    assert predication > -1;
    double[] result = new double[labels.length];
    if (predication > -1) //TODO search weka to find how to report missing values
      result[predication] = 1.0;

    return result;
  }


  @Override
  public Enumeration listOptions() {
    return moptions.listOptions();
  }

  @Override
  public void setOptions(String[] options) throws Exception {
    moptions.setOptions(options);
  }

  @Override
  public String[] getOptions() {
    return moptions.getOptions();
  }

  public boolean getAddDefaultRule() {
    return moptions.getAddDefaultRule();
  }

  public void setAddDefaultRule(boolean b) {
    moptions.setAddDefaultRule(b);
  }


  public double getMinFrequency() {
    return moptions.getMinFrequency();
  }

  public void setMinFrequency(double support) {
    moptions.setMinFrequency(support);
  }

  public double getMinRuleStrength() {
    return moptions.getMinRuleStrength();
  }

  public void setMinRuleStrength(double confidence) {
    moptions.setMinRuleStrength(confidence);
  }


  public void setAlgorithm(SelectedTag newMethod) {
    moptions.setAlgorithm(newMethod);
  }

  public void setAlgorithm(String algorithm) {
    EarlyOptions.ALGORITHMS.valueOf(algorithm);
  }

  public SelectedTag getAlgorithm() {
    return moptions.getAlgorithm();
  }

  public String algorithmTipText() {
    return "Which algorithm to use, prism, edri, or medri ?";
  }

  /**
   * Returns default capabilities of the classifier.
   *
   * @return the capabilities of this classifier
   */
  public Capabilities getCapabilities() {
//        Capabilities result = super.getCapabilities();
    Capabilities result = new Capabilities(this);

    result.disableAll();

    // attributes
    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);

    // class
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);

    return result;
  }


  /**
   * Generates the classifier.
   *
   * @param data the data to be used
   * @throws Exception if the classifier can't built successfully
   */
  public void buildClassifier(Instances data) throws Exception {
    logger.info("build classifier with data ={} of size={}", data.relationName(), data.numInstances());

    assert data.classIndex() == data.numAttributes() - 1;

    data.setClassIndex(data.numAttributes() - 1);
    this.labels = EarlyUtils.attributeValues(data.attribute(data.classIndex()));

    moptions.setMaxNumInstances(data.numInstances());
    moptions.setInstancesCopy(data);

    moptions.resetScannedInstances(0);

    EarlyOptions.ALGORITHMS algo = EarlyOptions.ALGORITHMS.of(
            moptions.getAlgorithm().toString());
    switch (algo) {
      case early: {
        double minSupport = moptions.getMinFrequency();
        double minConfidence = moptions.getMinRuleStrength();
        int minFreq = (int) Math.ceil(minSupport * data.numInstances() + 1.e-6);
        logger.debug("minFreq used = {}", minFreq);
        buildClassifierEarly(data, minFreq, minConfidence, moptions.getAddDefaultRule());

      }
      break;
      case prism:
        buildClassifierPrism(data, moptions.getAddDefaultRule());
        break;

      case edri: {
        double minSupport = moptions.getMinFrequency();
        double minConfidence = moptions.getMinRuleStrength();
        int minFreq = (int) Math.ceil(minSupport * data.numInstances() + 1.e-6);
        logger.debug("minFreq used = {}", minFreq);
        buildClassifierEDRI(data, minFreq, minConfidence, moptions.getAddDefaultRule());
      }
      break;

      case medri: {
        double minSupport = moptions.getMinFrequency();
        double minConfidence = moptions.getMinRuleStrength();
        int minFreq = (int) Math.ceil(minSupport * data.numInstances() + 1.e-6);
        logger.debug("minFreq used = {}", minFreq);
        buildClassifierMeDRI(data, minFreq, minConfidence, moptions.getAddDefaultRule());

      }
      break;

      default:
        System.err.println("Algorithm is no listed before");

    }

  }


  public EarlyResult buildClassifierMeDRI(Instances data, int minSupport, double minConfidence, boolean addDefaultRule) {
    logger.debug("buildClassifierMeDRI");

    EPair<Collection<int[]>, int[]> linesLabels = EarlyUtils.mapIdataAndLabels(data);
    Collection<int[]> lineData = linesLabels.key;
    int[] labelsCount = linesLabels.value;
//
    logger.trace("original lines size ={}", lineData.size());

    int[] numItems = EarlyUtils.countItemsInAttributes(data);
    EarlyResult result = EarlyUtils.buildClassifierMeDRI(numItems, labelsCount,
            lineData, minSupport, minConfidence, addDefaultRule);

    m_rules.clear();
    m_rules.addAll(result.getRules());
    moptions.resetScannedInstances(result.getScannedInstances());
    return result;
  }


  public EarlyResult buildClassifierEarly(
          Instances data,
          int minSupport,
          double minConfidence,
          boolean addDefaultRule) {
    logger.debug("buildClassifierMeDRI");

    EPair<Collection<int[]>, int[]> linesLabels = EarlyUtils.mapIdataAndLabels(data);
    Collection<int[]> lineData = linesLabels.key;
    int[] labelsCount = linesLabels.value;
//
    logger.trace("original lines size ={}", lineData.size());

    int[] numItems = EarlyUtils.countItemsInAttributes(data);
    EarlyResult result = EarlyUtils.buildClassifierEarly(
            numItems,
            labelsCount,
            lineData,
            minSupport,
            minConfidence,
            addDefaultRule);

    m_rules.clear();
    m_rules.addAll(result.getRules());
    moptions.resetScannedInstances(result.getScannedInstances());
    return result;
  }

  public EarlyResult buildClassifierEDRI(Instances data, int minSupport, double minConfidence, boolean addDefaultRule) {
    int[] iattrs = EarlyUtils.countItemsInAttributes(data);

    EPair<Collection<int[]>, int[]> linesLabels = EarlyUtils.mapIdataAndLabels(data);
    Collection<int[]> lineData = linesLabels.key;
    int[] labelsCount = linesLabels.value;

    logger.trace("original lines size ={}", lineData.size());
    EarlyResult result = EarlyUtils.buildClassifierEDRI(iattrs, labelsCount,
            lineData, minSupport, minConfidence, addDefaultRule);

    m_rules.clear();
    m_rules.addAll(result.getRules());
    moptions.resetScannedInstances(result.getScannedInstances());
    return result;
  }


  public EarlyResult buildClassifierPrism(Instances data, boolean addDefaultRule) {

    int[] iattrs = EarlyUtils.countItemsInAttributes(data);

    EPair<Collection<int[]>, int[]> linesLabels = EarlyUtils.mapIdataAndLabels(data);
    Collection<int[]> lineData = linesLabels.key;
    int[] labelsCount = linesLabels.value;

    logger.trace("original lines size ={}", lineData.size());
    EarlyResult result = EarlyUtils.buildClassifierPrism(iattrs, labelsCount, lineData, addDefaultRule);

    m_rules.clear();
    m_rules.addAll(result.getRules());
    moptions.resetScannedInstances(result.getScannedInstances());
    return result;
  }

  public String toString(Instances data, int maxDigit) {
    if (m_rules == null) {
      return getAlgorithm() + "No model built yet.";
    }

    StringBuilder sb = new StringBuilder();

    sb.append(String.format("Classifier = %s , add default rule = %s", getAlgorithm().toString(), String.valueOf(getAddDefaultRule())));
    if (!getAlgorithm().toString().toUpperCase().equals("PRISM")) {
      sb.append(String.format(" min freq = %.4f, min strength = %.2f", getMinFrequency(), getMinRuleStrength()));
    }
    sb.append("\nNumber of rules generated = " + m_rules.size());
    String intPattern = EarlyUtils.formatIntPattern(m_rules.size());
    sb.append("\n" + getAlgorithm() + " rules ( frequency, strength ) \n----------\n");
    for (int i = 0; i < m_rules.size(); i++) {
      ERule rule = m_rules.get(i);
      sb.append(String.format(intPattern + " - ", (i + 1)) + rule.toString(data, maxDigit) + "\n");
    }

    sb.append(String.format("\nClassifier = %s , add default rule = %s", getAlgorithm().toString(), String.valueOf(getAddDefaultRule())));
    if (!getAlgorithm().toString().toUpperCase().equals("PRISM")) {
      sb.append(String.format(" min freq = %.4f, min strenght = %.2f\n ", getMinFrequency(), getMinRuleStrength()));
    } else sb.append("\n");

    sb.append(String.format("Avg. Weighted Rule Length = %2.2f", getAvgWeightedRuleLength(m_rules)) + "\n");
    sb.append(String.format("Avg. Rule Length = %2.2f", getAvgRuleLength(m_rules)) + "\n");

    int numInstances = moptions.getMaxNumInstances();
    sb.append(String.format("Num of Instances of training dataset = %,d \n", numInstances));
    EPair<Integer, Integer> pnp = getInstancesOfPerfectRules(m_rules);
    sb.append(String.format("# Instances covered with perfect rules = %,d instances  ( %.2f %% )  \n", (int) pnp.key, (double) pnp.key / pnp.value));

    EPair<Integer, Integer> pnprules = perfectRules(m_rules);
    sb.append(String.format("# perfect rules = %,d , # not perfect rules = %,d \n", pnprules.key, pnprules.value));

    long scannedInstances = moptions.getScannedInstances();
    double scannedInstancesPercent = (double) scannedInstances / (double) numInstances;


    sb.append(String.format("Instances scanned to find all rules = %,d  (= %,d * %,3.2f ) \n", scannedInstances, numInstances, scannedInstancesPercent));
    return sb.toString();
  }

  private static EPair<Integer, Integer> perfectRules(List<ERule> rules) {
    int perfectRules = 0;
    int notPerfectRules = 0;

    for (ERule rule : rules) {
      if (1 - rule.getConfidence() < 1e-6) {
        perfectRules++;
      } else notPerfectRules++;
    }
    return new EPair(perfectRules, notPerfectRules);
  }

  private static EPair<Integer, Integer> getInstancesOfPerfectRules(List<ERule> rules) {
    int perfectRules = 0;
    int totals = 0;

    for (ERule rule : rules) {
      int correct = rule.getCorrect();
      totals += correct;
      if (1 - rule.getConfidence() < 1e-6) {
        perfectRules += correct;
      }
    }
//        perfectRules = perfectRules/totals;
//        notPerfectRules = notPerfectRules/totals;
    return new EPair(perfectRules, totals);
  }


  /**
   * Prints a description of the classifier.
   *
   * @return a description of the classifier as a string
   */
  public String toString() {
    int maxDigits = moptions.getMaxNumInstances();
    Instances data = moptions.getInstances();
    return toString(data, maxDigits);
  }

  //all based of all number of instances, remaining default rule length = 0
  private double getAvgWeightedRuleLength(List<ERule> rules) {
    double result = 0;
    for (ERule rule : rules) {
      //TODO accumulate rule rule.m_correct instead of final maxNumInstances
      result += rule.getLenghtWeighted();
    }
    return result / (double) moptions.getMaxNumInstances();
  }

  private double getAvgRuleLength(List<ERule> rules) {
    double result = 0;
    for (ERule rule : rules) {
      result += rule.getLength();
    }
    return result / (double) rules.size();
  }


}
