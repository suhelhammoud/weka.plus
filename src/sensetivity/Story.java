package sensetivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

enum StoryKey {
//  dataset, numAttributes, method, median, variables,
//  classifier, errorRate, precision, recall, fMeasure

    dataset, //relation name, or dataset filename
    numInstances,
    numAttributes,// excluding the label class attribute
    evalMethod, //PAS, L2, CHI, ..etc
    evalSupport,// for PAS attribute selector only
    evalConfidence, // for PAS attribute selector only
    numAttributesToSelect, //for attribute selection filter
    attributeEntropy, //entropy threshold for num of attributes

    classifier, // NB, MeDRI
    support, //
    confidence, //

    /* Classification results */
    errorRate,
    precision,
    recall,
    fMeasure;

    public static String csvHeaders() {
        //TODO check EnumSet.allOf(StoryKey)
        return Arrays.stream(StoryKey.values())
                .map(item -> item.toString())
                .collect(Collectors.joining(", "));
    }
}

public class Story {
    private final AtomicLong ID = new AtomicLong();

    final private Map<StoryKey, Object> data;
    public final long id = ID.getAndIncrement();

    /**
     * Later stories would set the final values in the result
     *
     * @param stories
     * @return
     */
    public static Story of(Story... stories) {
        final Story result = new Story();
        Arrays.stream(stories).forEach(s -> {
            result.update(s);
        });

        return result;
    }

    /**
     * Mutual update
     *
     * @param that
     * @return
     */
    public Story update(Story that) {
        for (Map.Entry<StoryKey, Object> ent : that.data.entrySet()) {
            this.set(ent.getKey(), ent.getValue());
        }
        return this;
    }

    /**
     * @param args
     * @return
     */
    public Story copy(Object... args) {
        Story result = new Story();
        result.data.putAll(this.data);

        if (args.length % 2 != 0)
            return result;

        for (int i = 0; i < args.length / 2; i++) {
            StoryKey key = (StoryKey) args[i];
            result.set((StoryKey) args[i], args[i + 1]);
        }
        return result;
    }

    public static Story get() {
        return new Story();
    }

    public Story() {
        data = new HashMap<>(StoryKey.values().length);
    }

    public Object get(StoryKey key) {
        return data.get(key);
    }


    public Story set(StoryKey key, Object value) {
        data.put(key, value);
        return this;
    }


    public String stringValues() {
        return Arrays.stream(StoryKey.values())
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

        Story srun = Story.get()
                .set(StoryKey.dataset, "irirs")
                .set(StoryKey.evalMethod, TEvaluator.IG);

        srun.set(StoryKey.errorRate, 77);
        System.out.println(srun.stringValues());
    }

}
