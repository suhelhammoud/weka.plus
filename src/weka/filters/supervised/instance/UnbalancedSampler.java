package weka.filters.supervised.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sensetivity.FilesUtils;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 * <!-- globalinfo-start -->
 * Produces an unbalanced random subsample (with replacement)
 * The number of instances in the generated
 * The dataset must have a binary class attribute
 * dataset must be specified. If not, the generated dataset will contain the same number as the original dataset
 * attribute.
 * If the unblance ratio is not specified then the flilter will maintain the class distribution
 * <p/>
 * <!-- globalinfo-end -->
 * <p>
 * <!-- options-start --> Valid options are:
 * <p/>
 *
 * <pre>
 * -S &lt;num&gt;
 *  Specify the random number seed (default 1)
 * </pre>
 *
 * <pre>
 * -Z &lt;num&gt;
 *  The size of the output dataset, as a percentage of
 *  the input dataset (default 1.0)
 * </pre>

 *
 * <pre>
 * -no-replacement
 *  Disables replacement of instances
 *  (default: with replacement)
 * </pre>
 *
 * <pre>
 * -V
 *  Inverts the selection - only available with '-no-replacement'.
 * </pre>
 * <p>
 * <!-- options-end -->
 *
 * @author Suhel Hammoud (suhel.hammoud@gmail.com)
 * @version $Revision: 0 $
 */
public class UnbalancedSampler extends Filter implements SupervisedFilter, OptionHandler {


    static Logger logger = LoggerFactory.getLogger(UnbalancedSampler.class.getName());

    /**
     * for serialization.
     */
    static final long serialVersionUID = 7079004953548300686L;

    /**
     * The subsample size, percent of original set, default 1.0.
     */
    protected double m_SampleSizePercent = 1.0;

    /**
     * The 1st class Ratio percent, default 0.5
     */
    protected double m_classPercent = 0.5;


    /**
     * The random number generator seed.
     */
    protected int m_RandomSeed = 1;

    /**
     * The degree of bias towards uniform (nominal) class distribution.
     */
//    protected double m_BiasToUniformClass = 0;

    /**
     * Whether to perform sampling with replacement or without.
     */
    protected boolean m_NoReplacement = false;


    /**
     * Returns a string describing this filter.
     *
     * @return a description of the filter suitable for displaying in the
     * explorer/experimenter gui
     */
    public String globalInfo() {
        return "Produces an unbalanced random subsample (with replacement).\n" +
                "The number of instances in the generated " +
                "dataset must be specified. If not, the generated dataset will contain the same number as the original dataset" +
                "The dataset must have a binary class attribute" +
                "attribute. \n" +
                " If the unblance ratio is not specified then the flilter will maintain the class distribution";
    }

    /**
     * Returns an enumeration describing the available options.
     *
     * @return an enumeration of all the available options.
     */
    @Override
    public Enumeration<Option> listOptions() {

        Vector<Option> result = new Vector<Option>(5);

        result.addElement(new Option(
                "\tSpecify the random number seed (default 1)", "S", 1, "-S <num>"));

        result.addElement(new Option(
                "\tThe size of the output dataset, as a percentage of\n"
                        + "\tthe input dataset (default 1.0)", "Z", 1, "-Z <num>"));


        result.addElement(new Option(
                "\tPercentage of first class label in output generated dataset\n"
                        + "\tthe input dataset (default 0.5)", "U", 1, "-U <num>"));

        return result.elements();
    }

    /**
     * Parses a given list of options.
     * <p/>
     * <p>
     * <!-- options-start --> Valid options are:
     * <p/>
     *
     * <pre>
     * -S &lt;num&gt;
     *  Specify the random number seed (default 1)
     * </pre>
     *
     * <pre>
     * -Z &lt;num&gt;
     *  The size of the output dataset, as a percentage of
     *  the input dataset (default 1.0)
     * </pre>
     *
     * <pre>
     * -U &lt;num&gt;
     *  Percentage of first class label in output generated dataset
     *  the input dataset (default 0.5)
     * </pre>
     *
     * <p>
     * <!-- options-end -->
     *
     * @param options the list of options as an array of strings
     * @throws Exception if an option is not supported
     */
    @Override
    public void setOptions(String[] options) throws Exception {
        String tmpStr;

        tmpStr = Utils.getOption('S', options);
        if (tmpStr.length() != 0) {
            setRandomSeed(Integer.parseInt(tmpStr));
        } else {
            setRandomSeed(1);
        }

        tmpStr = Utils.getOption('Z', options);
        if (tmpStr.length() != 0) {
            setSampleSizePercent(Double.parseDouble(tmpStr));//TODO
        } else {
            setSampleSizePercent(1.0);//TODO
        }

        tmpStr = Utils.getOption('U', options);
        if (tmpStr.length() != 0) {
            setSampleSizePercent(Double.parseDouble(tmpStr));//TODO
        } else {
            setSampleSizePercent(0.5);//TODO
        }


        if (getInputFormat() != null) {
            setInputFormat(getInputFormat());
        }

        Utils.checkForRemainingOptions(options);
    }

    /**
     * Gets the current settings of the filter.
     *
     * @return an array of strings suitable for passing to setOptions
     */
    @Override
    public String[] getOptions() {

        Vector<String> result = new Vector<String>();


        result.add("-S");
        result.add("" + getRandomSeed());

        result.add("-Z");
        result.add("" + getSampleSizePercent());

        result.add("-U");
        result.add("" + getClassPercent());//TODO


        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the tip text for this property.
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String biasToUniformClassTipText() {
        return "Whether to use bias towards a uniform class. A value of 0 leaves the class "
                + "distribution as-is, a value of 1 ensures the class distribution is "
                + "uniform in the output data.";
    }


    /**
     * Returns the tip text for this property.
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String randomSeedTipText() {
        return "Sets the random number seed for subsampling.";
    }

    /**
     * Gets the random number seed.
     *
     * @return the random number seed.
     */
    public int getRandomSeed() {
        return m_RandomSeed;
    }

    /**
     * Sets the random number seed.
     *
     * @param newSeed the new random number seed.
     */
    public void setRandomSeed(int newSeed) {
        m_RandomSeed = newSeed;
    }

    /**
     * Returns the tip text for this property.
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String sampleSizePercentTipText() {
        return "The subsample size as a percentage of the original set.";
    }

    /**
     * Gets the subsample size as a percentage of the original set.
     *
     * @return the subsample size
     */
    public double getSampleSizePercent() {
        return m_SampleSizePercent;
    }

    /**
     * Sets the size of the subsample, as a percentage of the original set.
     *
     * @param newSampleSizePercent the subsample set size, between 0 and 1.0.
     */
    public void setSampleSizePercent(double newSampleSizePercent) {
        m_SampleSizePercent = newSampleSizePercent;
    }

    /**
     * Returns the tip text for this property.
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String classPercentTipText() {
        return "The subsample size as a percentage of the original set.";
    }

    /**
     * Gets the subsample size as a percentage of the original set.
     *
     * @return the subsample size
     */
    public double getClassPercent() {
        return m_classPercent;
    }

    /**
     * Sets the size of the subsample, as a percentage of the original set.
     *
     * @param classPercent the subsample set size, between 0 and 1.0.
     */
    public void setClassPercent(double classPercent) {
        m_classPercent = classPercent;
    }

    /**
     * Returns the Capabilities of this filter.
     *
     * @return the capabilities of this object
     * @see Capabilities
     */
    @Override
    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.disableAll();

        // attributes
        result.enableAllAttributes();
        result.enable(Capabilities.Capability.MISSING_VALUES);

        // class
//        result.enable(Capabilities.Capability.NOMINAL_CLASS);
        result.enable(Capabilities.Capability.BINARY_CLASS);//TODO check if this works

        return result;
    }

    /**
     * Sets the format of the input instances.
     *
     * @param instanceInfo an Instances object containing the input instance
     *                     structure (any instances contained in the object are ignored -
     *                     only the structure is required).
     * @return true if the outputFormat may be collected immediately
     * @throws Exception if the input format can't be set successfully
     */
    @Override
    public boolean setInputFormat(Instances instanceInfo) throws Exception {

        super.setInputFormat(instanceInfo);
        setOutputFormat(instanceInfo);
        return true;
    }

    /**
     * Input an instance for filtering. Filter requires all training instances be
     * read before producing output.
     *
     * @param instance the input instance
     * @return true if the filtered instance may now be collected with output().
     * @throws IllegalStateException if no input structure has been defined
     */
    @Override
    public boolean input(Instance instance) {

        if (getInputFormat() == null) {
            throw new IllegalStateException("No input instance format defined");
        }
        if (m_NewBatch) {
            resetQueue();
            m_NewBatch = false;
        }
        if (isFirstBatchDone()) {
            push(instance);
            return true;
        } else {
            bufferInput(instance);
            return false;
        }
    }

    /**
     * Signify that this batch of input to the filter is finished. If the filter
     * requires all instances prior to filtering, output() may now be called to
     * retrieve the filtered instances.
     *
     * @return true if there are instances pending output
     * @throws IllegalStateException if no input structure has been defined
     */
    @Override
    public boolean batchFinished() {

        if (getInputFormat() == null) {
            throw new IllegalStateException("No input instance format defined");
        }

        if (!isFirstBatchDone()) {
            // Do the subsample, and clear the input instances.
            createSubsample();
        }
        flushInput();

        m_NewBatch = true;
        m_FirstBatchDone = true;
        return (numPendingOutput() != 0);
    }

    /**
     * Creates a subsample of the current set of input instances. The output
     * instances are pushed onto the output queue for collection.
     */
    protected void createSubsample() {

        Instances data = getInputFormat();

        // Count num instances in each class
        int[] numInstancesPerClass = new int[2];
        for (Instance instance : data) {
            numInstancesPerClass[(int) instance.classValue()]++;
        }

        if (Arrays.stream(numInstancesPerClass).anyMatch(i -> i == 0)) {
            logger.error("Data contains less than 2 classes");
            return;
        }

        int numActualClasses = 2;//Arrays.stream(numInstancesPerClass).sum();

        // Collect data per class
//        Instance[][] instancesPerClass = new Instance[data.numClasses()][];
        Instance[][] instancesPerClass = new Instance[2][];
        instancesPerClass[0] = new Instance[numInstancesPerClass[0]];
        instancesPerClass[1] = new Instance[numInstancesPerClass[1]];


        int[] counterPerClass = new int[2];//TODO 2
        for (Instance instance : data) {
            int classValue = (int) instance.classValue();
            instancesPerClass[classValue][counterPerClass[classValue]++] = instance;
        }

        // Determine how much data we want for each class
        int numOutSamples = (int) Math.round(data.numInstances() * m_SampleSizePercent);

        int[] numInstancesToSample = new int[2];
        numInstancesToSample[0] = (int) (m_classPercent * numOutSamples);
        numInstancesToSample[1] = numOutSamples - numInstancesToSample[0];




        Random random = new Random(m_RandomSeed);
        for (int i = 0; i < data.numClasses(); i++) {
            int numEligible = numInstancesPerClass[i];
            for (int j = 0; j < numInstancesToSample[i]; j++) {
                // Sampling with replacement
                push(instancesPerClass[i][random.nextInt(numEligible)]);
            }
        }

        logger.info("numOutSample = {}, 1st = {}, 2nd = {}",
                numOutSamples,
                numInstancesToSample[0],
                numInstancesToSample[1]);

    }

    /**
     * Returns the revision string.
     *
     * @return the revision
     */
    @Override
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 11310 $");
    }

    /**
     * Main method for testing this class.
     *
     * @param argv should contain arguments to the filter: use -h for help
     */
    public static void main(String[] argv) {
        runFilter(new Resample(), argv);
    }
}
