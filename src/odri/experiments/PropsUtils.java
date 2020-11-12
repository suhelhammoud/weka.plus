package odri.experiments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.pas.PasMethod;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PropsUtils extends Properties {
  static Logger logger = LoggerFactory.getLogger(PropsUtils.class.getName());

  public static PropsUtils of(String fileName) throws IOException {
    PropsUtils result = new PropsUtils();
    result.load(new FileReader(fileName));
    result.init();
    return result;
  }

  private List<TClassifier> classifiers;

  private String arffDir;
  private List<String> datasets;
  private String outDir;


  public List<String> getDatasets() {
    return datasets;
  }

  public String getArffDir() {
    return arffDir;
  }


  public List<TClassifier> getClassifiers() {
    return classifiers;
  }


  public String getOutDir() {
    return outDir;
  }

  private Stream<String> getStream(String property) {
    return getStream(property, "");
  }

  private Stream<String> getStream(String property, String defaultValue) {
    return Arrays.stream(getProperty(property, defaultValue)
            .trim().split("\\s+"))
            .map(s -> s.trim())
            .filter(s -> s.length() > 0);
  }


  private void init() {


    classifiers = getStream("classifiers")
            .map(s -> TClassifier.valueOf(s.toUpperCase()))
            .collect(Collectors.toList());

    logger.debug("classifiers : {}", classifiers);


    arffDir = getProperty("arff.dir").trim();

    datasets = Arrays.asList(getProperty("datasets", "")
            .trim().split("\\s+"));

    outDir = getProperty("out.dir", "data/results");

  }

  public static void main(String[] args)
          throws IOException {
    PropsUtils params = PropsUtils.of("data/conf_l2_unbalanced.properties");

    System.out.println("arff.dir = " + params.getArffDir());
    System.out.println("datasets = " + params.getDatasets());

    System.out.println("params.getClassifiers() = "
            + params.getClassifiers());
  }
}
