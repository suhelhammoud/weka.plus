package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

enum HeaderType {
  nominal, numeric, string;

  public static HeaderType of(String h) {
    switch (h.toLowerCase()) {
      case "d":
      case "f":
      case "numeric":
        return numeric;
      case "s":
      case "string":
        return string;
      default:
        return nominal;
    }
  }
}

public class CsvToArff {
  public final static String missingValue = "?";

  final String relationName;
  final String directory;
  final String inFile;
  final String outFile;

  final List<String> headerNames;
  final List<HeaderType> headerTypes;

  int[] indices;
  List<String> csvHeaders;
  List<List<String>> values;

  private CsvToArff(String relationName,
                    String directory,
                    String inFile,
                    String outFile,
                    List<String> headerNames,
                    List<HeaderType> headerTypes) {
    this.relationName = relationName;
    this.directory = directory;
    this.headerNames = headerNames;
    this.headerTypes = headerTypes;
    this.inFile = inFile;
    this.outFile = outFile;
  }

  @Override
  public String toString() {
    return "ArffMeta{" +
            "\n\trelationName='" + relationName + '\'' +
            "\n\tdirectory='" + directory + '\'' +
            "\n\tinFile='" + inFile + '\'' +
            "\n\toutFile='" + outFile + '\'' +
            "\n\theaderNames=" + headerNames +
            "\n\theaderTypes=" + headerTypes +
            "\n}";
  }

  public static List<String> readLines(String metaFile) throws Exception {
    List<String> lines = Files.readAllLines(Path.of(metaFile));
    return lines.stream()
            .map(line -> line.trim())
            .map(line -> line.replaceAll("#.*", ""))
            .filter(line -> line.length() != 0)
            .filter(line -> !line.startsWith("#"))
            .collect(Collectors.toList());
  }

  private static String extractProp(List<String> lines,
                                    String key,
                                    String defaultValue) {

    return lines.stream()
            .filter(line -> line.startsWith(key))
            .findFirst()
            .orElse(defaultValue)
            .replaceAll(key, "")
            .trim();
  }

  public static CsvToArff of(String metaFile) throws Exception {

    List<String> lines = readLines(metaFile);

    String relationName = extractProp(lines, "@relation", "relation_name");
    String metaFileLocation = Path.of(metaFile).getParent().toString();

    String directory = extractProp(lines, "@directory", metaFileLocation);
    directory = Path.of(metaFileLocation, directory).toString();
    String inFile = extractProp(lines, "@in_file", "no_in_file_found");
    String outFile = extractProp(lines, "@out_file", "no_out_file_found");

    /* Remaining lines are only for headers */
    lines = lines.stream()
            .filter(line -> !line.startsWith("@"))
            .collect(Collectors.toList());

    List<String> headerNames = new ArrayList<>(lines.size());
    List<HeaderType> headerTypes = new ArrayList<>(lines.size());

    for (String line : lines) {
      List<String> items = Arrays.stream(line.split("\\s*:\\s*"))
              .map(item -> item.trim())
              .collect(Collectors.toList());

      headerNames.add(items.get(0));
      if (items.size() == 1) {
        headerTypes.add(HeaderType.nominal);
      } else {
        headerTypes.add(HeaderType.of(items.get(1)));
      }
    }
    return new CsvToArff(relationName,
            directory,
            inFile,
            outFile,
            headerNames,
            headerTypes);
  }

  public void loadLines() throws Exception {
    Path inPath = Path.of(directory, inFile);
    System.out.println("Loading data from " + inPath.toAbsolutePath());
    if (!Files.exists(inPath)) {
      throw new IOException(inPath + " not found");
    }

    List<String> allLines = Files.readAllLines(inPath).stream()
            .map(line -> cleanLine(line))
            .filter(line -> line.length() > 0)
            .collect(Collectors.toList());

    final int numHeaders = (int) allLines.get(0).chars()
            .filter(ch -> ch == ',')
            .count() + 1;

    System.out.println("numHeaders = " + numHeaders);
    //TODO empty header should not be allowed
    final List<String> csvHeaders = cleanLineItems(allLines.get(0), numHeaders);

    allLines = allLines.subList(1, allLines.size());

    List<List<String>> values = new ArrayList<>(allLines.size());
    for (String line : allLines) {
      values.add(cleanLineItems(line, numHeaders));
    }


    int numMissMatch = (int) headerNames.stream()
            .filter(h -> !csvHeaders.contains(h))
            .count();

    if (numMissMatch > 0) {
      System.err.println("There are " + numMissMatch + " mismatched headers ");
      headerNames.stream()
              .filter(h -> !csvHeaders.contains(h.toUpperCase()))
              .forEach(h -> System.err.println("Error in header " + h));

      throw new Exception("Error in headers");
    }

    int[] indices = headerNames.stream()
            .mapToInt(h -> csvHeaders.indexOf(h))
            .toArray();

    this.csvHeaders = csvHeaders;
    this.values = values;
    this.indices = indices;

    System.out.println("Total Headers = " + csvHeaders.size());
    System.out.println("Total Values = " + values.size());
    System.out.println("Indices = " + Arrays.toString(indices));
  }

  public static String cleanLine(String line) {
    return line.trim()
//            .toLowerCase() //TODO ignore case
            .replaceAll("\\s", "_");
  }

  private static List<String> cleanLineItems(String line, int length) {
    String[] lineValues = new String[length];
    Arrays.fill(lineValues, missingValue);
    String[] tmpItems = line.split("\\s*,\\s*");

    for (int i = 0; i < tmpItems.length; i++) {
      String s = tmpItems[i].replaceAll("\\s", "_");
      lineValues[i] = s.equals("") ? missingValue : s;
    }
    return Arrays.asList(lineValues);
  }

  String writeHeader() {
    StringBuilder sb = new StringBuilder("@relation " + relationName + "\n\n");
    for (int i = 0; i < headerNames.size(); i++) {
      sb.append(writeOneAttribute(i));
    }
    return sb.append("\n").toString();
  }

  private String writeOneAttribute(int index) {
    StringBuilder sb = new StringBuilder("@attribute " + headerNames.get(index));
    switch (headerTypes.get(index)) {
      case numeric:
        sb.append(" numeric\n"); //TODO check REAL
        break;
      case nominal:
        sb.append(distinct(indices[index]));
        break;
      case string:
        sb.append("string\n");
        break;
    }
    return sb.toString();
  }


  private String distinct(int i) {
    StringBuilder sb = new StringBuilder(" {");

    List<String> items = values.stream()
            .map(l -> l.get(i))
            .filter(item -> !item.equals(missingValue))
            .distinct()
            .collect(Collectors.toList());
    sb.append(String.join(", ", items))
            .append("}\n");
    return sb.toString();
  }

  String writeData() {
    StringBuilder sb = new StringBuilder("@data\n");
    for (List<String> lineValues : values) {
      String lineS = Arrays.stream(indices)
              .mapToObj(i -> lineValues.get(i))
              .collect(Collectors.joining(","));
      sb.append(lineS);
      sb.append("\n");
    }
    return sb.toString();
  }

  public void toArff() throws Exception {

    StringBuilder sb = new StringBuilder();
    sb.append(writeHeader());
    sb.append(writeData());

    Path outPath = Path.of(directory, outFile);
    if (Files.exists(outPath)) {
      System.out.println("Warn Overwriting existing file: " +
              outPath.toAbsolutePath());
    }
    Files.writeString(outPath, sb.toString());
  }

  public static void main(String[] args) throws Exception {
    String metaFile = args.length > 0 ?
            args[0] :
            "data/manual/data/new/Subset_12.meta";

    CsvToArff meta = CsvToArff.of(metaFile);
    System.out.println(meta);
    meta.loadLines();
    meta.toArff();
  }
}
