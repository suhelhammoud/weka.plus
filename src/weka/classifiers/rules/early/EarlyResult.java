package weka.classifiers.rules.early;

import java.util.List;

/**
 * Created by suhel on 31/03/16.
 */
public class EarlyResult {

  private long scannedInstances = 0;
  private List<ERule> rules;

  public void setScannedInstances(long scannedInstances) {
    this.scannedInstances = scannedInstances;
  }

  public long getScannedInstances() {
    return scannedInstances;
  }

  public List<ERule> getRules() {
    return rules;
  }

  public void setRules(List<ERule> rules) {
    this.rules = rules;
  }
}
