package weka.classifiers.rules.odri;

import weka.core.Attribute;
import weka.core.Instances;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class OData {

  final int[] labels;
  final Map<Integer, int[]> iData;
  final Map<Integer, double[]> nData;
  final int[] numItems;

  public OData(Instances instances) {
    this.iData = new LinkedHashMap<>();
    this.nData = new LinkedHashMap<>();
    this.labels = mapIAttribute(instances, instances.numAttributes() - 1);
    this.numItems = countItemsInAttributes(instances);

    for (int attIndex = 0; attIndex < instances.numAttributes() - 1; attIndex++) {
      if (instances.attribute(attIndex).type() == Attribute.NOMINAL) {
        iData.put(attIndex, mapIAttribute(instances, attIndex));
      }
      if (instances.attribute(attIndex).type() == Attribute.NUMERIC) {
        nData.put(attIndex, mapDAttribute(instances, attIndex));
      }
    }
  }

  public int numLabels() {
    return numItems[numItems.length - 1];
  }

  public int numInstances() {
    return labels.length;
  }

  public int[] allLines(){
    return IntStream.range(0, numInstances()).toArray();
  }

  /**
   * Return array containing number of items in each corresponding attribute
   *
   * @param data
   * @return number of distinct items in each attributes
   */
  public static int[] countItemsInAttributes(Instances data) {
    int[] result = new int[data.numAttributes()];
    for (int i = 0; i < result.length; i++) {
      result[i] = data.attribute(i).numValues();
    }
    return result;
  }


  public static double[] mapDAttribute(Instances instances, int attIndex) {
    double[] result = new double[instances.numInstances()];
    for (int i = 0; i < instances.numInstances(); i++) {
      result[i] = instances.instance(i).value(attIndex);
    }
    return result;
  }

  public static int[] mapIAttribute(Instances instances, int attIndex) {
    int[] result = new int[instances.numInstances()];
    for (int i = 0; i < instances.numInstances(); i++) {
      result[i] = (int) instances.instance(i).value(attIndex);
    }
    return result;
  }


  public static OData of(Instances instances) {
    return new OData(instances);
  }

  public static void main(String[] args) throws IOException {
    String inFile = "data/arff/ecoli.arff";

    Instances instances = new Instances(OdriUtils.readDataFile(inFile));

    OData oData = OData.of(instances);
    System.out.println("labels = " + Arrays.toString(oData.labels));

    System.out.println("======================= Integers");
    for (Map.Entry<Integer, int[]> e : oData.iData.entrySet()) {
      System.out.println(e.getKey() + "= "+ Arrays.toString(e.getValue()));
    }
    System.out.println("======================= Doubles");
    for (Map.Entry<Integer, double[]> e : oData.nData.entrySet()) {
      System.out.println(e.getKey() + "= "+ Arrays.toString(e.getValue()));
    }
  }
}
