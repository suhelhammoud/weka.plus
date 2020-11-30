package weka.classifiers.rules.odri;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OdriNumeric {


  public static int[] sortedIndices(double[] data) {
    return IntStream.range(0, data.length)
            .boxed()
            .sorted((i, j) -> (int) Math.signum(data[i] - data[j]))
            .mapToInt(e -> e)
            .toArray();
  }

  public static double[] fancyIndex(double[] all, int[] indices) {
    double[] result = new double[indices.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = all[indices[i]];
    }
    return result;
  }

  /**
   * @param data sorted double array
   * @return
   */
  static public LinkedHashSet<Double> dbl(double[] data) {
    assert data.length > 0;
    LinkedHashSet<Double> result = new LinkedHashSet<>();
    for (int i = 1; i < data.length; i++) {
      if (data[i] == data[i - 1]) {
        result.add(data[i]);
      }
    }
    return result;

  }

  static public LinkedHashMap<Double, Integer> countExtra(double[] data) {
    LinkedHashMap<Double, Integer> result = new LinkedHashMap<>(data.length);
    for (int i = 0; i < data.length; i++) {
      Integer c = result.get(data[i]);
      c = c == null ? 1 : c + 1;
      result.put(data[i], c);
    }
    return result;
  }

  /**
   * data and labels are sorted
   *
   * @param data
   * @param labels
   */
  public static Pair<List<Integer>, List<int[]>> scan(
          double[] data,
          int[] labels,
          int numLabels) {

//    LinkedHashMap<Integer, int[]> result = new LinkedHashMap<>();
    List<Integer> splits = new ArrayList<>();
    List<int[]>   splitLabels = new ArrayList<>();
    //First value
    int[] tl = new int[numLabels];
    splitLabels.add(tl);
    tl[labels[0]]++;
//    result.put(0, tl);

    for (int i = 1; i < data.length; i++) {
      //for items of the same label or of the same value
      while (labels[i] == labels[i - 1]
              || data[i] == data[i - 1]) {
        tl[labels[i]]++;
        i++;
        if (i >= data.length) { //reach end of data
          break;
        }
      }
      if (i >= data.length) break; //no tailing data
      System.out.println("i =" + i + ", " + Arrays.toString(tl));
      tl = new int[numLabels];
      splitLabels.add(tl);
      tl[labels[i]]++;
      splits.add(i);


    }
    System.out.println("splits.size() = " + splits.size());
    System.out.println("splitLabels.size() = " + splitLabels.size());

    return new Pair<>(splits, splitLabels);
    //get unique items
    // save labels of unique items
  }

  public static List<ORange> scanToRanges(
          double[] data,
          int[] labels,
          int numLabels) {

//    LinkedHashMap<Integer, int[]> result = new LinkedHashMap<>();
    List<ORange> result = new ArrayList<>();

    int[] tl = new int[numLabels];
    ORange range = new ORange(tl);
    result.add(range);
    range.labels[labels[0]]++;

    for (int i = 1; i < data.length; i++) {
      //for items of the same label or of the same value
      while (labels[i] == labels[i - 1]
              || data[i] == data[i - 1]) {
        range.labels[labels[i]]++;
        i++;
        if (i >= data.length) { //reach end of data
          break;
        }
      }
      if (i >= data.length) break; //no tailing data
      System.out.println("i =" + i + ", " + Arrays.toString(tl));
      double splitValue = (data[i] + data[i-1]) / 2.0;
      range.upper = splitValue;

      tl = new int[numLabels];
      range = new ORange(tl);
      range.lower = splitValue;
      result.add(range);
      range.labels[labels[i]]++;

    }
    return result;
  }

  public static void main(String[] args) {
    double[] data = new double[]{64, 65, 68, 69, 70, 71, 72, 72, 75, 75, 80, 81, 83, 85, 86};
    int[] labels = new int[]{1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 1, 0, 1};
    List<ORange> result = scanToRanges(data, labels, 2);
    for (ORange range : result) {
      System.out.println(range.toString());
    }

//    for (Map.Entry<Integer, int[]> e : sR.entrySet()) {
//      System.out.println(e.getKey() + "\t" + Arrays.toString(e.getValue()));
//    }
//    LinkedHashSet<Double> splitted = dbl(data);
//    System.out.println("splitted = " + splitted);
  }
}
