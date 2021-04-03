package weka.attributeSelection.cas;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

public class LSet {

  /**
   * @param attData:   attribute data (whole data)
   * @param lines:     lines to count with
   * @param numItems:  total number of unique items in attributes
   * @param labels:    labels data (whole data)
   * @param numLabels: number of classes in dataset
   * @return array[item][label] -> count of labels
   */
  public static int[][] count(int[] attData,
                              int[] lines,
                              int numItems,
                              int[] labels,
                              int numLabels) {
    int[][] result = new int[numItems][numLabels];
    for (int line : lines)
      result[attData[line]][labels[line]]++;

    return result;
  }

  public static int[][] count(int[] attData,
                              int numItems,
                              int[] labels,
                              int numLabels) {
    int[][] result = new int[numItems][numLabels];
    for (int line = 0; line < attData.length; line++)
      result[attData[line]][labels[line]]++;

    return result;
  }


  /**
   * Count items in attData
   *
   * @param attData
   * @param numItems
   * @return int[numItems] counts
   */
  public static int[] count(int[] attData, int numItems) {
    int[] result = new int[numItems];
    for (int item : attData) result[item]++;
    return result;
  }

  /**
   * @param attData
   * @param lines
   * @param numItems
   * @return
   */
  public static int[] count(int[] attData, int[] lines, int numItems) {
    int[] result = new int[numItems];
    for (int line : lines) result[attData[line]]++;
    return result;
  }


  public static int[][] split(int[] attData, int[] lines, int[] itemCount) {
    int[][] result = new int[itemCount.length][];
    for (int i = 0; i < result.length; i++)
      result[i] = new int[itemCount[i]];
    int[] indx = new int[itemCount.length];
    for (int line : lines) {
      int item = attData[line];
      result[item][indx[item]] = line;
    }
    return result;
  }

  public static int[][] split(int[] attData, int[] lines, int numItems) {
    int[] count = count(attData, lines, numItems);
    return split(attData, lines, count);
  }


  public static int[] index(int[] attData, int[] lines) {
    int[] result = new int[lines.length];
    for (int i = 0; i < lines.length; i++)
      result[i] = attData[lines[i]];
    return result;
  }

  public static int[] retainAll(int[] s1, int[] s2) {
    int[] result = new int[Math.min(s1.length, s2.length)];
    int i1 = 0, i2 = 0, io = 0;

    while (i1 < s1.length && i2 < s2.length) {
      if (s1[i1] == s2[i2]) {
        result[io++] = s1[i1];
        i1++;
        i2++;
      } else if (s1[i1] < s2[i2])
        i1++;
      else
        i2++;
    }
    return Arrays.copyOf(result, io);
  }


  /**
   * Find the remaining set of items, s1 \ s2
   * ! IMPORTANT this method assumes unique sorted arrays
   *
   * @param s1
   * @param s2
   * @return
   */
  public static int[] removeAll(int[] s1, int[] s2) {
    //unique sorted arrays
    int[] result = new int[s1.length - s2.length];
    int i1 = 0;
    int io = 0;
    for (int line : s2) {
      while (s1[i1] != line) {
        result[io++] = s1[i1++];
      }
      i1++;
    }
    while (i1 < s1.length)
      result[io++] = s1[i1++];
    return result;
  }


  //  /**
//   * s1, s2 are sorted
//   * s1 contains s2
//   * @param s1
//   * @param s2
//   * @return
//   */
//  public static int[] removeAll(int[] s1, int[] s2) {
//    int[] result = new int[s1.length - s2.length];
//    int i1 =0, i2=0, io = 0;
//    while (i1 < s1.length && i2 < s2.length) {
//      if (s1[i1] != s2[i2]) {
//        result[io++] = s1[i1++];
//        i2++;
//      }else i1++;
//    }
//    return result;
//  }
  public static void tets(String[] args) {

    int[] s1 = {1, 2, 3, 6, 7, 9, 10};
    int[] s2 = {3, 9};
    System.out.println(Arrays.toString(removeAll(s1, s2)));

//    int[] s1 = {1, 2, 3, 6, 7, 9, 10};
//    int[] s2 = {2, 3, 9, 10, 11};
//    System.out.println(Arrays.toString(retainAll(s1, s2)));

  }

  public static void main(String[] args) {
    int n = 1000000;
    int limit = 10000000;

    Random rnd = new Random(1L);

    Set<Integer> s1 = new HashSet<>(n);
    while (s1.size() < n) {
      s1.add(Math.abs(rnd.nextInt(limit)));
    }
    int[] a1 = s1.stream().mapToInt(i -> i).toArray();
    Arrays.sort(a1);
//    System.out.println("s1 = " + s1);
//    System.out.println("a1 = " + Arrays.toString(a1));

    Set<Integer> s2 = new HashSet<>(n);
    while (s2.size() < n / 2) {
//      s2.add(a1[rnd.nextInt(a1.length)]);
      s2.add(Math.abs(rnd.nextInt(limit)));
    }
    int[] a2 = s2.stream().mapToInt(i -> i).toArray();
    Arrays.sort(a2);
//    System.out.println("s2 = " + s2);
//    System.out.println("a2 = " + Arrays.toString(a2));

    long t1 = System.nanoTime();
    int[] a3 = retainAll(a1, a2);
    System.out.println("a3.length = " + a3.length);
    s1.retainAll(s2);
    System.out.println("s1.size() = " + s1.size());
    long t2 = System.nanoTime() - t1;
    System.out.println("t2 = " + t2);

//    System.out.println("a3 = " + Arrays.toString(a3));
//    s1.removeAll(s2);
//    System.out.println("s1 = " + s1);


  }

  /**
   * @param data
   * @param lines          should be sorted
   * @param distinctCounts
   * @return
   */
  public static int[][] splitToItemLines(int[] data, //
                                         int[] lines,
                                         int[] distinctCounts) {
    int[][] result = new int[distinctCounts.length][];
    int[] indexes = new int[distinctCounts.length];

    for (int i = 0; i < distinctCounts.length; i++) {
      result[i] = new int[distinctCounts[i]];
    }

    for (int line : lines) {
      int itemIndex = data[line];
      result[itemIndex][indexes[itemIndex]++] = line;
    }
    for (int i = 0; i < result.length; i++) {
      result[i] = Arrays.copyOf(result[i], indexes[i]);
    }
    return result;
  }

  /**
   * @param a
   * @return
   */
  public static int max(int[] a) {
    assert a.length > 0;
    int result = a[0];
    for (int i = 1; i < a.length; i++)
      if (a[i] > result) result = a[i];
    return result;
  }

  public static int correct(int[][] a) {
    int result = 0;
    for (int i = 0; i < a.length; i++)
      result += max(a[i]);
    return result;
  }

  public static int sum(int[] a){
    int result = 0;
    for(int v: a)result += v;
    return result;
  }
  public static int sum(int[][] a) {
    int result = 0;
    for(int[] v: a)result += sum(v);
    return result;
  }

}
