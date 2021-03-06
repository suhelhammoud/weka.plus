package odri.experiments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilesUtils {
  static Logger logger = LoggerFactory.getLogger(FilesUtils.class.getName());

  public static boolean writeStoriesToFile(Path outDir,
                                           String filename,
                                           List<Story> stories,
                                           StoryKey... keys) {

    List<String> content = new ArrayList<>(stories.size() + 2);
    content.add(keys.length > 0 ?
            StoryKey.csvHeaders(keys) :
            StoryKey.csvHeaders());

    content.addAll(stories.stream()
            .map(s -> s.stringValues(keys))
            .collect(Collectors.toList()));
    return writeToFile(outDir, filename, content);
  }

  public static boolean writeToFile(Path outDir,
                                    String filename,
                                    List<String> content) {
    Path path = Paths.get(outDir.toString(), filename);
    try {
      Files.write(path, content);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private static String suggestName(String outDir) {
    for (int i = 0; i < 1000000; i++) {
      Path path = Paths.get(outDir, String.valueOf(i));
      if (!path.toFile().exists()) {
        return path.toAbsolutePath().toString();
      }
    }
    logger.error("Could not generate outpath from {}", outDir);
    return "/tmp/" + outDir + System.nanoTime(); //never reached TODO
  }

  public static Path createOutDir(String outDir) {
    Path outPath = Paths.get(suggestName(outDir));
    outPath.toFile().mkdirs();
    return outPath.toAbsolutePath();
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

  public static List<Path> listFiles(String directory, String extension) {
    // extension ".arff"
    List<Path> result = new ArrayList<>();
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
      for (Path path : directoryStream) {
        if (path.getFileName().endsWith(extension)) ;
        result.add(path.toAbsolutePath());
      }
    } catch (IOException ex) {
    }
    return result;
  }

  public static void main(String[] args) throws Exception {
//        Path outDir = createOutDir("data/result");
//        List<String> content = Arrays.asList("one", "two", "three");
//        boolean written = writeToFile(outDir, "out.txt", content);
//        System.out.println("written = " + written);
  }

}
