package weka.classifiers.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.rules.medri.*;
import weka.classifiers.rules.odri.ORule;
import weka.classifiers.rules.odri.OdriOptions;
import weka.classifiers.rules.odri.OdriUtils;
import weka.core.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by suhel on 10/11/2020.
 */
public class ODRI implements Classifier, OptionHandler,
        CapabilitiesHandler, TechnicalInformationHandler, Serializable {

  static Logger logger = LoggerFactory.getLogger(ODRI.class.getName());
  static final long serialVersionUID = 1310258425525902107L;

  private String[] labels;

  /**
   * Returns the revision string.
   *
   * @return the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 002 $");
  }

  /**
   * Returns a string describing classifier
   *
   * @return a description suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Class for building and using a ordi rule set for classification. "
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
    result.setValue(TechnicalInformation.Field.AUTHOR, "S. Hammoud,F. Thabtah");
    result.setValue(TechnicalInformation.Field.YEAR, "2020");
    result.setValue(TechnicalInformation.Field.TITLE, "odri: An algorithm for ordered induced rules");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Journal");
    result.setValue(TechnicalInformation.Field.VOLUME, "vol");
    result.setValue(TechnicalInformation.Field.NUMBER, "number");
    result.setValue(TechnicalInformation.Field.PAGES, "p_start-p_end");
    return result;
  }

  /**
   * Holds algorithm configurations and OptionHandler parameters
   */
  private OdriOptions options = new OdriOptions();

  /**
   * Holds algorithm configurations and MedriOption parameters
   */
  private List<ORule> m_rules = new ArrayList<>();

  public List<ORule> resultORules() {
    return m_rules.stream()
            .map(r -> r.copy())
            .collect(Collectors.toList());
  }

  /**
   * Classifies a given instance.
   *
   * @param inst the instance to be classified
   * @return the classification
   */
  public double classifyInstance(Instance inst) {

    for (ORule rule : m_rules) {
      int cls = rule.classify(OdriUtils.toIntArray(inst));
      if (cls != IRule.EMPTY)
        return cls;
    }
//        return Instance.missingValue();
    // TODO how to return missing values? old weka is not working no more!
    return -1;
  }

  //TODO use instead of EMPTY (-1) returned values, later...
  public boolean canClassifyInstance(Instance inst) {
    return m_rules.stream()
            .anyMatch(r -> r.canCoverInstance(OdriUtils.toIntArray(inst)));
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
    return options.listOptions();
  }

  @Override
  public void setOptions(String[] options) throws Exception {
    this.options.setOptions(options);
  }

  @Override
  public String[] getOptions() {
    return options.getOptions();
  }

  public boolean getAddDefaultRule() {
    return options.getAddDefaultRule();
  }

  public void setAddDefaultRule(boolean b) {
    options.setAddDefaultRule(b);
  }


  public int getMinOccurrence() {
    return options.getMinOcc();
  }

  public void setMinOccurrence(int minOcc) {
    options.setMinOcc(minOcc);
  }

  public void setDebugLevel(SelectedTag newMethod) {
    options.setDebugLevel(newMethod);
  }

  public SelectedTag getDebugLevel() {
    return options.getDebugLevel();
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

    logger.info("build classifier with data ={} of size={}",
            data.relationName(), data.numInstances());

    assert data.classIndex() == data.numAttributes() - 1;

    data.setClassIndex(data.numAttributes() - 1);
    this.labels = OdriUtils.attributeValues(data.attribute(data.classIndex()));

    options.setMaxNumInstances(data.numInstances());
    options.setInstancesCopy(data);


    m_rules = buildClassifierOdri(data,
            options.getMinOcc(),
            options.getAddDefaultRule());


  }


  public List<ORule> buildClassifierOdri(Instances instances,
                                         int minOcc,
                                         boolean addDefaultRule) {
    options.changeLogLevelRunTime();

    logger.debug("buildClassifierOdri with minOcc={}, addDefaultRule={}", minOcc, addDefaultRule);


    instances.setClassIndex(instances.numAttributes() - 1);
    logger.debug("relation= {}, num instances = {}",
            instances.relationName(),
            instances.numInstances());
    final int[] numberOfItems = OdriUtils.countItemsInAttributes(instances);
    int[][] data = OdriUtils.mapIdataAndLabelsToArrays(instances);

    return OdriUtils.buildClassifierOdri(data,
            numberOfItems,
            minOcc,
            addDefaultRule);
  }

  public String toString(Instances data, int maxDigit) {
    if (m_rules == null) return "No model built yet.";

    StringBuilder sb = new StringBuilder();

    sb.append(String.format("Classifier = odri , add default rule = %s",
            getAddDefaultRule()));
    sb.append(String.format(" min occ = %,d", getMinOccurrence()));
    sb.append("\nNumber of rules generated = " + m_rules.size());
    String intPattern = OdriUtils.formatIntPattern(m_rules.size());
    sb.append("\n ORDI rules ( frequency, strength ) \n----------\n");
    for (int i = 0; i < m_rules.size(); i++) {
      ORule rule = m_rules.get(i);
      sb.append(String.format(intPattern + " - ",
              (i + 1)) + rule.toString(data, maxDigit) + "\n");
    }

    sb.append(String.format("\nClassifier = ODRI , add default rule = %s",
            getAddDefaultRule()));

    sb.append("\n");
    sb.append(String.format("Avg. Weighted Rule Length = %2.2f", getAvgWeightedRuleLength(m_rules)) + "\n");
    sb.append(String.format("Avg. Rule Length = %2.2f", getAvgRuleLength(m_rules)) + "\n");

    int numInstances = options.getMaxNumInstances();
    sb.append(String.format("Num of Instances of training dataset = %,d \n", numInstances));
    Pair<Integer, Integer> pnp = getInstancesOfPerfectRules(m_rules);
    sb.append(String.format("# Instances covered with perfect rules = %,d instances  ( %.2f %% )  \n",
            (int) pnp.key,
            (double) pnp.key / pnp.value));

    Pair<Integer, Integer> pnprules = perfectRules(m_rules);
    sb.append(String.format("# perfect rules = %,d , # not perfect rules = %,d \n",
            pnprules.key, pnprules.value));

    return sb.toString();
  }

  private static Pair<Integer, Integer> perfectRules(List<ORule> rules) {
    int perfectRules = 0;
    int notPerfectRules = 0;

    for (ORule rule : rules) {
      if (1 - rule.getConfidence() < 1e-6) {
        perfectRules++;
      } else notPerfectRules++;
    }
    return new Pair(perfectRules, notPerfectRules);
  }

  private static Pair<Integer, Integer> getInstancesOfPerfectRules(List<ORule> rules) {
    int perfectRules = 0;
    int totals = 0;

    for (ORule rule : rules) {
      int correct = rule.getCorrect();
      totals += correct;
      if (1 - rule.getConfidence() < 1e-6) {
        perfectRules += correct;
      }
    }
//        perfectRules = perfectRules/totals;
//        notPerfectRules = notPerfectRules/totals;
    return new Pair(perfectRules, totals);
  }


  /**
   * Prints a description of the classifier.
   *
   * @return a description of the classifier as a string
   */
  public String toString() {
    int maxDigits = options.getMaxNumInstances();
    Instances data = options.getInstances();
    return toString(data, maxDigits);
  }

  //all based of all number of instances, remaining default rule length = 0
  private double getAvgWeightedRuleLength(List<ORule> rules) {
    double result = 0;
    for (ORule rule : rules) {
      //TODO accumulate rule rule.m_correct instead of final maxNumInstances
      result += rule.getLenghtCorrectWeighted();
    }
    return result / (double) options.getMaxNumInstances();
  }

  private double getAvgRuleLength(List<ORule> rules) {
    return rules.stream()
            .mapToInt(rule -> rule.getLength())
            .sum() / (double) rules.size();
  }
}
