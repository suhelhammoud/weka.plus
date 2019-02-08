package sensetivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.NumericToBinary;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

//        try(Files.write(path, content)){}
        try {
            Files.write(path, content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Path createOutDir(String outDir) {
        if (Paths.get(outDir).toFile().exists()) {
            for (int dirNum = 0; dirNum < 1000000; dirNum++) {
                //TODO add limit
                Path cPath = Paths.get(outDir, "" + dirNum);
                if (!cPath.toFile().exists()) {
                    cPath.toFile().mkdirs();
                    return cPath.toAbsolutePath();
                }
            }
        } else {
            Path result = Paths.get(outDir, "0");
            result.toFile().mkdirs();
            return result.toAbsolutePath();
        }

        logger.error("Could not generate outpath from {}", outDir);
        return null; //never reached TODO
    }

    public static Instances instancesOf(String path) {
        return instancesOf(Paths.get(path));
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

    public static String fileNameNoSuffix(Path path) {
        String fileName = path.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf(".");

        return fileName.substring(0, extensionIndex);
    }

    public static void discretize(String inDir, String outDir) throws Exception {

        List<Path> arffDatasets = FilesUtils.listFiles(inDir, ".arff");
        for (Path path : arffDatasets) {
            try {

                Instances data = instancesOf(path);
                data.setClassIndex(data.numAttributes() - 1);
                Discretize disTransform = new Discretize();
                disTransform.setUseBetterEncoding(true);
                disTransform.setInputFormat(data);
                Instances dataD = Filter.useFilter(data, disTransform);
                dataD.setRelationName(data.relationName());

                ArffSaver saver = new ArffSaver();
                saver.setInstances(dataD);
                saver.setFile(Paths.get(outDir, path.getFileName().toString()).toFile());
                saver.writeBatch();
            } catch (Exception e) {
                logger.error("Can not discretize " + path.toString());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        discretize("data/arff", "/tmp/a");
//        Path outDir = createOutDir("data/result");
//        List<String> content = Arrays.asList("one", "two", "three");
//        boolean written = writeToFile(outDir, "out.txt", content);
//        System.out.println("written = " + written);
    }

}
