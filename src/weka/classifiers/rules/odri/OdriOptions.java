package weka.classifiers.rules.odri;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.rules.eDRI;
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


  protected int minOcc = 1;


  protected int maxNumInstances = 0;

  public int getMaxNumInstances() {
    return maxNumInstances;
  }

  public void setMaxNumInstances(int maxNumInstances) {
    this.maxNumInstances = maxNumInstances;
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
    return result.elements();

  }

  @Override
  public void setOptions(String[] options) throws Exception {
    String minOccString = Utils.getOption('S', options);
    minOcc = Integer.parseInt(minOccString);

    addDefaultRule = Utils.getFlag('R', options);
  }

  @Override
  public String[] getOptions() {
    String[] result = new String[3];
    int currentIndex = 0;

    result[currentIndex++] = "-S";
    result[currentIndex++] = "" + minOcc;

    result[currentIndex++] = addDefaultRule? "-R": "";

    return result;
  }



}
