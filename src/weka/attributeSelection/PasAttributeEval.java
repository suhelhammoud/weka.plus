package weka.attributeSelection;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.pas.*;
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


public class PasAttributeEval extends ASEvaluation implements
        AttributeEvaluator, OptionHandler {

    static Logger logger = LoggerFactory.getLogger(PasAttributeEval.class.getName());

    PasOptions pasOptions = new PasOptions();

    public PasOptions getPasOptions() {
        return pasOptions;
    }

    /**
     * for serialization
     */
    static final long serialVersionUID = -3049819495125894189L;

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
    public PasAttributeEval() {
        resetOptions();
    }

    /**
     * Returns an enumeration describing the available options.
     *
     * @return an enumeration of all the available options.
     **/
    @Override
    public Enumeration<Option> listOptions() {
        return pasOptions.listOptions();
    }


    @Override
    public void setOptions(String[] options) throws Exception {
        pasOptions.setOptions(options);
    }

    /**
     * @return an array of strings suitable for passing to setOptions()
     */
    @Override
    public String[] getOptions() {
        return pasOptions.getOptions();
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
        result.enable(Capabilities.Capability.MISSING_VALUES); // TODO check if applicable

        // class
        result.enable(Capabilities.Capability.NOMINAL_CLASS);
        result.enable(Capabilities.Capability.MISSING_CLASS_VALUES); //TODO check if applicable

        return result;
    }

    /**
     * PAS attribute evaluator
     *
     * @param initialData set of instances serving as training dataset
     * @throws Exception if the evaluator has not been generated successfully
     */
    @Override
    public void buildEvaluator(Instances initialData) throws Exception {
        // can evaluator handle dataset?
        getCapabilities().testWithFail(initialData);

        Instances data = null;

        if (!pasOptions.getBinarizeNumericAttributes()) {
            Discretize disTransform = new Discretize();
            disTransform.setUseBetterEncoding(true);
            disTransform.setInputFormat(initialData);
            data = Filter.useFilter(initialData, disTransform);
        } else {
            NumericToBinary binTransform = new NumericToBinary();
            binTransform.setInputFormat(initialData);
            data = Filter.useFilter(initialData, binTransform);
        }

        data.setRelationName(initialData.relationName());

        //TODO look into Chi implementation of contingency tables
        logger.debug("build classifier with data ={} of size={}", data.relationName(), data.numInstances());

        assert data.classIndex() == data.numAttributes() - 1;

        data.setClassIndex(data.numAttributes() - 1);
        logger.debug("build pas evaluator");

        Tuple<Collection<int[]>, int[]> linesLabels = PasUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.k;
        int[] labelsCount = linesLabels.v;
//
        logger.trace("original lines size ={}", lineData.size());

        int[] numItems = PasUtils.countItemsInAttributes(data);

        int minFreq = (int) Math.ceil(pasOptions.getMinFrequency()
                * data.numInstances() + 1.e-6);
        logger.debug("minFreq used = {}", minFreq);

        List<PasItem> items = new ArrayList<>();

        final PasMethod pasMethod = pasOptions.getPasMethodEnum();

        switch (pasMethod) {
            case rules:
                items = PasUtils.evaluateAttributesRules(numItems,
                        labelsCount,
                        lineData,
                        minFreq,
                        pasOptions.getMinItemStrength(),
                        false);
//                logger.info("run att eval with algorithm rules = {}", items.size());

                break;
            case rules1st:

                items = PasUtils.evaluateAttributesRules1st(numItems,
                        labelsCount,
                        lineData,
                        minFreq,
                        pasOptions.getMinItemStrength(),
                        false);

//                logger.info("run att eval with algorithm rules1st = {}", items.size());

                break;
            case items:
                items = PasUtils.evaluateAttributesItems(numItems,
                        labelsCount,
                        lineData,
                        minFreq,
                        pasOptions.getMinItemStrength(),
                        false);
//                logger.info("run att eval with algorithm items = {}", items.size());
                break;
        }


        double[] rawRanks = PasUtils.rankAttributes(
                items,
                data.numAttributes() - 1,
                pasMethod
        );//exclude label class attribute


        m_pas = PasUtils.normalizeVector(rawRanks);

        if (pasOptions.getShowDebugMessages()) {
            String msg = PasUtils.printResult(items,
                    data,
                    Arrays.stream(rawRanks).sum(),
                    data.numAttributes() - 1);
            logger.info(msg);
        }
    }


    /**
     * Reset options to their default values
     */
    protected void resetOptions() {
        pasOptions.resetOptions();
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


            if (!getMissingMerge()) {
                text.append("\n\tMissing values treated as separate"); //TODO check
            }
            if (getBinarizeNumericAttributes()) {
                text.append("\n\tNumeric attributes are just binarized");
            }
        }

        text.append("\n\t Minimum Support: " + getMinFrequency());
        text.append("\n\t Minimum Confidence: " + getMinItemStrength());

        text.append("\n");
        text.append("\n");
        text.append(PasUtils.printRanks(m_pas));
        text.append("\n\n");
        text.append(PasUtils.printCutOffPoint(m_pas));


        text.append("\n");

        return text.toString();
    }

    public SelectedTag getPasMethod() {
        return pasOptions.getPashMethod();
    }

    public void setPasMethod(SelectedTag tag) {
        pasOptions.setPasMethod(tag);
    }

    public void setPasMethod(PasMethod pm) {
        pasOptions.setPasMethod(pm);
    }

    public double getMinFrequency() {
        return pasOptions.getMinFrequency();
    }

    public void setMinFrequency(double minFrequency) {
        pasOptions.setMinFrequency(minFrequency);
    }

    public double getMinItemStrength() {
        return pasOptions.getMinItemStrength();
    }

    public void setMinItemStrength(double strength) {
        pasOptions.setMinItemStrength(strength);
    }

    public void setMissingMerge(boolean b) {
        pasOptions.setMissingMerge(b);
    }

    public boolean getMissingMerge() {
        return pasOptions.getMissingMerge();
    }

    public void setBinarizeNumericAttributes(boolean b) {
        pasOptions.setBinarizeNumericAttributes(b);
    }

    public boolean getBinarizeNumericAttributes() {
        return pasOptions.getBinarizeNumericAttributes();
    }

    public boolean getShowDebugMessages() {
        return pasOptions.getShowDebugMessages();
    }

    public void setShowDebugMessages(boolean b) {
        pasOptions.setShowDebugMessages(b);
    }

    public boolean getShowCutOffPoint() {
        return pasOptions.getShowCuttOffPoint();
    }

    public void setShowCutOffPoint(boolean b) {
        pasOptions.setShowCutOffPoint(b);
    }

    ;

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
        runEvaluator(new weka.attributeSelection.PasAttributeEval(), args);

    }

}
