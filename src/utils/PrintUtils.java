package utils;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class PrintUtils {


  public static String IntsJoin(String delimeter, int[] arr) {
    StringJoiner result = new StringJoiner(", ");
    for (int i : arr) {
      result.add(String.valueOf(i));
    }
    return result.toString();
  }

  public static StringBuilder print(int[][] arr) {
    StringBuilder sb = new StringBuilder();
    if (arr == null || arr.length == 0) return sb;
    for (int i = 0; i < arr.length; i++) {
      sb.append(IntsJoin(", ", arr[i]));
      sb.append("\n");
    }
    return sb;
  }

  public static StringBuilder print(int[][][] d) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < d.length; i++) {
      sb.append("********** " + i + " *********\n");
      sb.append(print(d[i]));
    }
    return sb;
  }

  public static String arrayToString(double[] arr, String format) {
    return Arrays.stream(arr)
            .boxed()
            .map(d -> String.format(format, d))
            .collect(Collectors.joining(", ", "[", "]"));
  }

  public static String formatIntPattern(int maxDigit) {
    int digits = (int) (Math.ceil(Math.log10(maxDigit)));
    return "%0" + digits + "d";
  }

}
