package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class InstancesUtils {

  static Logger logger = LoggerFactory.getLogger(InstancesUtils.class.getName());

  public static Instances instancesOf(String fileName) {
    return instancesOf(Path.of(fileName));
  }

  public static Instances instancesOf(Path path) {
    try {
      return new Instances(new FileReader(path.toFile()));
    } catch (IOException e) {
      e.printStackTrace();
    }
    logger.error("Could not generate Instances from {}", path.toString());
    return null;
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

}
