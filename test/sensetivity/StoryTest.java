package sensetivity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoryTest {

  @Test
  void of() {
    Story a = Story.get()
            .set(StoryKey.dataset, "ds1")
            .set(StoryKey.attEvalMethod, TEvaluator.IG);

    Story b = Story.get()
            .set(StoryKey.dataset, "ds2")
            .set(StoryKey.errorRate, 0);

    Story result = Story.of(a, b);
    assertEquals(3, result.size());
    assertEquals("ds2", result.get(StoryKey.dataset));
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
  }
}