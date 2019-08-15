package sensetivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class RStoryDriver {
  static Logger logger = LoggerFactory.getLogger(RStoryDriver.class.getName());


  public static List<Story> setRepeat(List<Story> stories, int iteration) {
    return stories.stream()
            .map(s -> s.copy()
                            .set(StoryKey.l2ClassExperimentIteration, iteration + 1)
//                        .set(StoryKey.l2ClassExperimentID, s.id)
            )
            .collect(Collectors.toList());
  }
}
