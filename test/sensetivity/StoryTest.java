package sensetivity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoryTest {

  @BeforeEach
  void setUp() {
  }

  @Test
  void copy() {
    Story s = Story.get()
            .set(StoryKey.numAttributes, 10)
            .set(StoryKey.numInstances, 100);
    assertEquals(2, s.size());
    Story s2 = s.copy();
    assertEquals(2, s2.size());

    Story s3 = s.copy(StoryKey.errorRate);
    assertEquals(Story.NONE, s3);
    assertTrue(s3.isNone());
    assertEquals(2, s2.size());
    s3 = s.copy(StoryKey.errorRate, 3.00);
    assertEquals(3, s3.size());

    System.out.println("s3.stringValues() = " + s3.stringValues());



  }
}