package weka.attributeSelection.pas;

import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.SelectedTag;
import weka.core.Utils;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

public class PasOptions implements OptionHandler, Serializable {
    static final long serialVersionUID = 3110258885025902127L;

    protected String m_algorithm = PasAlgorithm.medri.name();
    protected double m_minFrequency = 0.01;
    protected double m_minItemStrength = 0.5;

    /**
     * To show more debug results to the output
     */
    private boolean m_debug; //TODO might delete it later

    /**
     * Treat missing values as a separate value
     */
    private boolean m_missing_merge;

    /**
     * Just binarize numeric attributes
     */
    private boolean m_Binarize;



    public String binarizeTipText(){
        return "binarize tip text";
    }


    public PasAlgorithm getAlgorithmEnum(){
        return PasAlgorithm.of(m_algorithm);
    }

    public SelectedTag getAlgorithm(){
        return PasAlgorithm.of(m_algorithm).selectedTag();
    }

    public void setAlgorithm(SelectedTag algorithm){
        m_algorithm = algorithm.getSelectedTag().getIDStr();
    }

    public String algorithmTipText(){
        return "choose algorithm";
    }

    public void setMinFrequency(double minFrequency){
        m_minFrequency = minFrequency;
    }

    public double getMinFrequency(){
        return m_minFrequency;
    }

    public String minFrequencyTipText(){
        return "min frequency tip text";
    }

    public void setMinItemStrength(double strength){
        m_minItemStrength = strength;
    }

    public double getMinItemStrength(){
        return m_minItemStrength;
    }

    public String minItemStrengthTipText(){
        return "min items strength tip text";
    }


    /**
     * Reset options to their default values
     */
    public void resetOptions() {
        m_missing_merge = true;
        m_Binarize = false;
        m_debug = false;
        m_minFrequency = 0.001;
        m_minItemStrength = .1;
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

        setMinFrequency(Double.parseDouble(Utils.getOption('S', options)));
        setMinItemStrength(Double.parseDouble(Utils.getOption('C', options)));

//        final String fIndex = Utils.getOption('F', options); //TODO what is F ?
        Utils.checkForRemainingOptions(options); //only in chi, TODO: check this later

        // exclude M, B, F
        setMissingMerge(!(Utils.getFlag('M', options)));
        setBinarizeNumericAttributes(Utils.getFlag('B', options));
        setShowDebugMessages(Utils.getFlag('D', options));

        setMinFrequency(Double.parseDouble(Utils.getOption('S', options)));
        setMinItemStrength(Double.parseDouble(Utils.getOption('C', options)));

        final String fIndex = Utils.getOption('F', options); //TODO what is F ?
        Utils.checkForRemainingOptions(options); //only in chi, TODO: check this later

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
        options.add(String.valueOf(m_minFrequency));

        options.add("-C");
        options.add(String.valueOf(m_minItemStrength));

        return options.toArray(new String[0]);
    }
}
