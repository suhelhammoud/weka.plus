package sensetivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


public class Story {
    private final static AtomicLong ID = new AtomicLong();
    public final long id;

    final private Map<StoryKey, Object> data;

    /**
     * Later stories would set the final values in the result
     *
     * @param stories
     * @return
     */
    public static Story of(Story... stories) {
        return Arrays.stream(stories)
                .reduce(new Story(), Story::update);
    }

    /**
     * Mutual update
     *
     * @param that
     * @return
     */
    public Story update(Story that) {
        that.data.entrySet()
                .stream()
                .forEach(entry ->
                        this.set(entry.getKey(), entry.getValue()));
        return this;
    }

    /**
     * Copy and add pair of (key,value) to the resulted Story
     *
     * @param args Must be an even number in order to accept the (key, value) pairs
     * @return copy of this Story with additional (key, value) pairs set in args
     * If number of args is not even then return only copy of this story
     */
    public Story copy(Object... args) {
        Story result = new Story();
        result.data.putAll(this.data);

        if (args.length % 2 != 0)
            return result;
        for (int i = 0; i < args.length / 2; i++) {
            result.set((StoryKey) args[i], args[i + 1]);
        }
        return result;
    }

    public static Story get() {
        return new Story();
    }

    private Story() {
        id = ID.getAndIncrement();
         data = new HashMap<>(StoryKey.values().length);
    }

    public Object get(StoryKey key) {
        return data.get(key);
    }


    public Story set(StoryKey key, Object value) {
        data.put(key, value);
        return this;
    }

    public String stringValues(StoryKey... keys) {
        return Arrays.stream(
                keys.length > 0 ?
                        keys :
                        StoryKey.values())
                .map(key -> data.containsKey(key) ?
                        data.get(key).toString() :
                        "")
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return stringValues();
    }

    public static void main(String[] args) {
        System.out.println(StoryKey.csvHeaders());

        Story a = Story.get()
                .set(StoryKey.dataset, "irirs")
                .set(StoryKey.evalMethod, TEvaluator.IG);

        Story b = Story.get()
                .set(StoryKey.dataset, "irirsb")
                .set(StoryKey.errorRate, 0);

        Story result = Story.of(a, b);
        System.out.println(result.stringValues());
    }

}
