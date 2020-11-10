package weka.classifiers.rules.odri;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.rules.eDRI;
import weka.classifiers.rules.medri.MedriOptions;
import weka.core.*;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by suhel on 10/11/2020.
 */
public class OdriOptions implements OptionHandler, Serializable {

  static final long serialVersionUID = 1010258885025902127L;
  static Logger logger = LoggerFactory.getLogger(OdriOptions.class);

  private boolean addDefaultRule = false;

  public boolean getAddDefaultRule() {
    return addDefaultRule;
  }

  public void setAddDefaultRule(boolean addDefaultRule) {
    this.addDefaultRule = addDefaultRule;
  }


  public enum LEVELS {
    off, trace, debug, info, warn, error, fatal;

    public static Tag[] toTags() {
      LEVELS[] levels = values();
      Tag[] result = new Tag[levels.length];
      for (int i = 0; i < levels.length; i++) {
        result[i] = new Tag(i, levels[i].name(), levels[i].name());
      }
      return result;
    }

  }



  protected String m_debugLevel = LEVELS.info.name();


  public String debugLevel() {
    return m_debugLevel;
  }

  protected int minOcc = 1;


  protected int maxNumInstances = 0;

  public int getMaxNumInstances() {
    return maxNumInstances;
  }

  public void setMaxNumInstances(int maxNumInstances) {
    this.maxNumInstances = maxNumInstances;
  }

  public SelectedTag getDebugLevel() {
    return new SelectedTag(m_debugLevel, LEVELS.toTags());
  }



  public int getMinOcc() {
    return minOcc;
  }

  public void setMinOcc(int minOcc) {
    this.minOcc = minOcc;
  }


  Instances instances;

  public void setInstancesCopy(Instances data) {
    instances = new Instances(data, 0);
  }

  public Instances getInstances() {
    return instances;
  }


  @Override
  public Enumeration listOptions() {
    Vector<Option> result = new Vector<>(1);
    result.addElement(new Option("Add Default Rule?", "R", 0, "-R"));
    result.addElement(new Option("minimum m_support", "S", 1, "-S <lower bound for minimum minOcc >"));
    result.addElement(new Option("description", "D", 1, "-D < off | trace | debug | info | warn | error | fatal >"));
    return result.elements();

  }

  @Override
  public void setOptions(String[] options) throws Exception {
    String optionString = Utils.getOption('D', options);
    m_debugLevel = LEVELS.valueOf(optionString).name();

    String minOccString = Utils.getOption('S', options);
    minOcc = Integer.parseInt(minOccString);

    addDefaultRule = Utils.getFlag('R', options);
  }

  @Override
  public String[] getOptions() {
    String[] result = new String[5];
    int currentIndex = 0;
    result[currentIndex++] = "-D";
    result[currentIndex++] = m_debugLevel;

    result[currentIndex++] = "-S";
    result[currentIndex++] = "" + minOcc;

    result[currentIndex++] = addDefaultRule? "-R": "";

    return result;
  }

  public void setDebugLevel(SelectedTag newMethod) {
    m_debugLevel = newMethod.getSelectedTag().getIDStr();
  }


  public void changeLogLevelRunTime() {
    changeLogLevelRunTime(m_debugLevel);
  }


  public static void changeLogLevelRunTime(String logLevel) {
//        Logger lg = (Logger) LoggerFactory.getLogger(edri.class);
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(eDRI.class))
            .setLevel(Level.toLevel(logLevel));

  }

}
