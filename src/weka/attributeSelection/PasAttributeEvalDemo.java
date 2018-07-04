package weka.attributeSelection;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.pas.PasItem;
import weka.attributeSelection.pas.PasUtils;
import weka.classifiers.rules.medri.IRule;
import weka.classifiers.rules.medri.MeDRIResult;
import weka.classifiers.rules.medri.MedriUtils;
import weka.classifiers.rules.medri.Pair;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.NumericToBinary;

import java.util.*;

/**
 * <!-- globalinfo-start --> Practical Attribute Selection :<br/>
 * <br/>
 * Evaluates the worth of an attribute by measuring the practical initial coverage.<br/>
 * capability of it
 * <br/>
 * <p/>
 * <!-- globalinfo-end -->
 * <p>
 * <!-- options-start --> Valid options are:
 * <p/>
 * <p>
 * <pre>
 * -M
 *  treat missing values as a seperate value.
 * </pre>
 * <p>
 * <pre>
 * -B
 *  just binarize numeric attributes instead
 *  of properly discretizing them.
 * </pre>
 * <p>
 * <!-- technical-bibtex-start --> BibTeX:
 *
 * <pre>
 * &#64;inproceedings{todo,
 *    address = {todo},
 *    author = {Suhel, Fadi},
 *    journal title = { todo},
 *    pages = {xxx-xxx},
 *    publisher = {todo},
 *    title = {TODO},
 *    year = {2018}
 * }
 * </pre>
 * <p/>
 * <!-- technical-bibtex-end -->
 * <!-- options-end -->
 *
 * @author Fadi  (f.thabtah@gmail.com)
 * @author Suhel (suhel.hammoud@gmail.com)
 * @version $Revision: 00007778 $
 * @see Discretize
 * @see NumericToBinary
 */


public class PasAttributeEvalDemo extends ASEvaluation implements
        AttributeEvaluator, OptionHandler {

    static Logger logger = LoggerFactory.getLogger(PasAttributeEvalDemo.class.getName());

    /**
     * for serialization
     */
    static final long serialVersionUID = -3049819495125894189L;

    /**
     * Treat missing values as a separate value
     */
    private boolean m_missing_merge;

    /**
     * Just binarize numeric attributes
     */
    private boolean m_Binarize;

    /**
     * To show more debug results to the output
     */
    private boolean m_debug; //TODO might delete it later


    /**
     * Optional initial searching measures
     */
    protected double m_support = 0.05;

    protected double m_confidence = 0.40;

    /**
     * The Pas ranking value for each attribute
     */
    private double[] m_pas;


    public double[] getAttributesRanks() {
        return Arrays.copyOf(m_pas, m_pas.length);
    }

    /**
     * evaluates an individual attribute by measuring Its Va values
     *
     * @param attribute the index of the attribute to be evaluated
     * @return the Va value
     * @throws Exception if the attribute could not be evaluated
     */
    @Override
    public double evaluateAttribute(int attribute) throws Exception {
        return m_pas[attribute];
    }

    /**
     * Returns a string describing this attribute evaluator
     *
     * @return a description of the evaluator suitable for displaying in the
     * explorer/experimenter gui
     */
    public String globalInfo() {
        return "Pas AttributeEval :\n\nEvaluates the worth of an attribute "
                + "\n more info on :\nhttps://gitlab.com/suhel.hammoud/weka.plus/tree/master/src/weka/attributeSelection/PasAttributeEval.java";
    }

    /**
     * Constructor
     */
    public PasAttributeEvalDemo() {
        resetOptions();
    }

    /**
     * Returns an enumeration describing the available options.
     *
     * @return an enumeration of all the available options.
     **/
    @Override
    public Enumeration<Option> listOptions() {
        Vector<Option> newVector = new Vector<Option>(5);

        newVector.addElement(new Option("\ttreat missing values as a separate value.",
                "M", 0, "-M"));


        newVector.addElement(new Option("\tminimum support value "
                , "S", 0, "-S"));

        newVector.addElement(new Option("\tminimum confidence value "
                , "C", 0, "-C"));

        newVector.addElement(new Option(
                "\tjust binarize numeric attributes instead \n"
                        + "\tof properly discretizing them.", "B", 0, "-B"));

        newVector.addElement(new Option("\tshow debug messages.",
                "D", 0, "-D"));

        return newVector.elements();
    }

    /**
     * Parses a given list of options.
     * <p/>
     * <p>
     * <!-- options-start --> Valid options are:
     * <p/>
     * <p>
     * <pre>
     * -S
     *  Minimums support threshold.
     * </pre><pre>
     * -C
     *  Minimum confidence threshold.
     * </pre><pre>
     * -M
     *  treat missing values as a separate value.
     * </pre>
     * <p>
     * <pre>
     * -B
     *  just binarize numeric attributes instead
     *  of properly discretizing them.
     * </pre>
     * <p>
     * <!-- options-end -->
     *
     * @param options the list of options as an array of strings
     * @throws Exception if an option is not supported
     */
    @Override
    public void setOptions(String[] options) throws Exception {
        resetOptions();
        setMissingMerge(!(Utils.getFlag('M', options)));
        setBinarizeNumericAttributes(Utils.getFlag('B', options));
        setShowDebugMessages(Utils.getFlag('D', options));

        setSupport(Double.parseDouble(Utils.getOption('S', options)));
        setConfidence(Double.parseDouble(Utils.getOption('C', options)));

        final String fIndex = Utils.getOption('F', options); //TODO what is F ?
        Utils.checkForRemainingOptions(options); //only in chi, TODO: check this later
    }

    /**
     * Va Gets the current settings.
     *
     * @return an array of strings suitable for passing to setOptions()
     */
    @Override
    public String[] getOptions() {
        Vector<String> options = new Vector<String>();

        if (!getMissingMerge()) {
            options.add("-M");
        }

        if (!getShowDebugMessages()) {
            options.add("-D");
        }

        if (getBinarizeNumericAttributes()) {
            options.add("-B");
        }

        options.add("-S");
        options.add(String.valueOf(m_support));

        options.add("-C");
        options.add(String.valueOf(m_confidence));

        return options.toArray(new String[0]);
    }

    public boolean getShowDebugMessages() {
        return m_debug;
    }

    public void setShowDebugMessages(boolean b) {
        this.m_debug = b;
    }

    public String showDebugMessagesTipText() {
        return "include debug messages in loggers";
    }


    /**
     * Returns the tip text for this property
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String binarizeNumericAttributesTipText() {
        return "Just binarize numeric attributes instead of properly discretizing them.";
    }


    /**
     * Binarize numeric attributes.
     *
     * @param b true=binarize numeric attributes
     */
    public void setBinarizeNumericAttributes(boolean b) {
        m_Binarize = b;
    }

    /**
     * get whether numeric attributes are just being binarized.
     *
     * @return true if missing values are being distributed.
     */
    public boolean getBinarizeNumericAttributes() {
        return m_Binarize;
    }

    /**
     * Returns the tip text for this property
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String missingMergeTipText() {
        return "Distribute counts for missing values. Counts are distributed "
                + "across other values in proportion to their frequency. Otherwise, "
                + "missing is treated as a separate value.";
    }

    public void setSupport(double support) {
        m_support = support;
    }

    //TODO double support or minimum frequency ?
    public double getSupport() {
        return m_support;
    }

    public String supportTipText() {
        return "Minimum support value, default = 0.05";
    }

    public void setConfidence(double confidence) {
        m_confidence = confidence;
    }

    public double getConfidence() {
        return m_confidence;
    }

    public String confidenceTipText() {
        return "Minimum confidence valu, default = 0.40";
    }

    /**
     * distribute the counts for missing values across observed values
     *
     * @param b true=distribute missing values.
     */
    public void setMissingMerge(boolean b) {
        m_missing_merge = b;
    }

    /**
     * get whether missing values are being distributed or not
     *
     * @return true if missing values are being distributed.
     */
    public boolean getMissingMerge() {
        return m_missing_merge;
    }

    /**
     * Returns the capabilities of this evaluator.
     *
     * @return the capabilities of this evaluator
     * @see Capabilities
     */
    @Override
    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.disableAll();

        // attributes
        result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
        result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
//        result.enable(Capabilities.Capability.DATE_ATTRIBUTES); //TODO check if applicable
        result.enable(Capabilities.Capability.MISSING_VALUES); // TODO check if applicable

        // class
        result.enable(Capabilities.Capability.NOMINAL_CLASS);
        result.enable(Capabilities.Capability.MISSING_CLASS_VALUES); //TODO check if applicable
//        result.enable(Capabilities.Capability.NUMERIC_CLASS);
//        result.enable(Capabilities.Capability.DATE_CLASS);

        return result;
    }

    /**
     * PAS attribute evaluator
     *
     * @param data set of instances serving as training dataset
     * @throws Exception if the evaluator has not been generated successfully
     */
    @Override
    public void buildEvaluator(Instances data) throws Exception {
        // can evaluator handle dataset?
        getCapabilities().testWithFail(data);

        int classIndex = data.classIndex();
        int numInstances = data.numInstances();

        if (!m_Binarize) {
            Discretize disTransform = new Discretize();
            disTransform.setUseBetterEncoding(true);
            disTransform.setInputFormat(data);
            data = Filter.useFilter(data, disTransform);
        } else {
            NumericToBinary binTransform = new NumericToBinary();
            binTransform.setInputFormat(data);
            data = Filter.useFilter(data, binTransform);
        }
        int numClasses = data.attribute(classIndex).numValues();

        //TODO look into Chi implementation of contingency tables
        logger.debug("build classifier with data ={} of size={}", data.relationName(), data.numInstances());

        assert data.classIndex() == data.numAttributes() - 1;

        data.setClassIndex(data.numAttributes() - 1);
        logger.debug("build pas evaluator");

        Pair<Collection<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.key;

        int[] labelsCount = linesLabels.value;
//
        logger.trace("original lines size ={}", lineData.size());

        int[] numItems = MedriUtils.countItemsInAttributes(data);

        int minFreq = (int) Math.ceil(getSupport() * data.numInstances() + 1.e-6);
        logger.debug("minFreq used = {}", minFreq);

        List<PasItem> items = PasUtils.evaluateAttributesDemo(numItems,
                labelsCount,
                lineData,
                minFreq,
                getConfidence(),
                true);

        double[] rawRanks = PasUtils.rankAttributes(
                items,
                data.numAttributes() - 1);//exclude label class attribute


        m_pas = PasUtils.normalizeVector(rawRanks);

        if (m_debug) {
            String msg = printResult(items,
                    data,
                    Arrays.stream(rawRanks).sum(),
                    data.numAttributes() - 1);
            logger.info(msg);
        }
    }

    //TODO delete later
    private String printResult(List<PasItem> rules,
                               Instances data,
                               double sumWeights,
                               int numAttributes) {
        StringBuilder result = new StringBuilder();

        StringJoiner sj = new StringJoiner("\n\t",
                "\n" + data.relationName() + "\n\t",
                "\n\n");
//        double sumWeights = Arrays.stream(rawRanks).sum();

        final int totalLines = rules.stream()
                .mapToInt(rule -> rule.getCovers())
                .sum();
        int availableLines = totalLines;

        double[] rawRanks = new double[numAttributes];
        for (IRule rule : rules) {
//            sj.add("rule: " + rule.toString(data, 3));
            sj.add("rule: " + rule.toString());
            final double linesRatio = (double) availableLines / totalLines;
            final double weight = rule.getCorrect()
                    * availableLines;
            sj.add(String.format("weight = %06.1f , lines = %04d, lines ratio = %3.3f",
                    weight,
                    availableLines,
                    linesRatio));
            availableLines -= rule.getCovers();
            for (int attIndex : rule.getAttIndexes()) {
                rawRanks[attIndex] += weight;
            }
        }

        sj.add("Result attributes weights");
        sj.add(Arrays.toString(rawRanks));

        sj.add("Normalized Attributes weights");

        sj.add(PasUtils.arrayToString(PasUtils.normalizeVector(rawRanks),
                "%1.3f"));

        return sj.toString();
    }


    /**
     * Reset options to their default values
     */
    protected void resetOptions() {
        m_pas = null;          //Va
        m_missing_merge = true;
        m_Binarize = false;
        m_debug = false;
        m_support = 0.05;
        m_confidence = .4;
    }


    /**
     * Describe the attribute evaluator
     *
     * @return a description of the attribute evaluator as a string
     */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        if (m_pas == null) {  //Va
            text.append("Pas attribute evaluator has not been built");
        } else { //All OK
            text.append("\tPas Ranking Filter");


            if (!m_missing_merge) {
                text.append("\n\tMissing values treated as separate"); //TODO check
            }
            if (m_Binarize) {
                text.append("\n\tNumeric attributes are just binarized");
            }
        }

        text.append("\n\t Minimum Support: " + m_support);
        text.append("\n\t Minimum Confidence: " + m_confidence);

        text.append("\n");
        text.append("\n");
        text.append(printRanks(m_pas));

        text.append("\n");

        return text.toString();
    }

    private String printRanks(double[] ranks) {
        StringJoiner sj = new StringJoiner("\n\t\t");
        sj.add("Attributes Ranks:");
        sj.add("att\t\tweight");
        sj.add("-------------------------");
        for (int i = 0; i < m_pas.length; i++) {
            sj.add(String.format("%02d\t\t%1.3f", i + 1, m_pas[i]));
        }
        return sj.toString();
    }

    /**
     * Returns the revision string.
     *
     * @return the revision
     */
    @Override
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 00007778 $"); //arbitrary number
    }

    // ============
    // Test method.
    // ============

    /**
     * Main method for testing this class.
     *
     * @param args the options
     */
    public static void main(String[] args) {
        runEvaluator(new PasAttributeEvalDemo(), args);

    }

}
