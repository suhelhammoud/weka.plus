package weka.classifiers.rules.odri;

import java.util.List;

/**
 * Created by suhel on 31/03/16.
 */
public class OdriResult {

  private long scannedInstances = 0;
  private List<ORule> rules;

  public void setScannedInstances(long scannedInstances) {
    this.scannedInstances = scannedInstances;
  }

  public long getScannedInstances() {
    return scannedInstances;
  }

  public List<ORule> getRules() {
    return rules;
  }

  public void setRules(List<ORule> rules) {
    this.rules = rules;
  }
}
