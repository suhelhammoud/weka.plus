package sensetivity;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FilesUtilsTest {

  @Test
  void createOutDir() {
    String testDir = "/tmp/" + System.nanoTime();
    Paths.get(testDir).toFile().mkdirs();
    int totalDirs = 10;
    for (int i = 0; i < totalDirs; i++) {
      FilesUtils.createOutDir(testDir);
    }
    int numDirsCreated = Paths.get(testDir).toFile().list().length;
    assertEquals(totalDirs, numDirsCreated);
  }
}