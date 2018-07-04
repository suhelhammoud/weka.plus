package weka.attributeSelection.pas;

import weka.core.SelectedTag;
import weka.core.Tag;

public enum PasAlgorithm {
    rules, rules1st, items;

    public static Tag[] toTags() {
        PasAlgorithm[] levels = values();
        Tag[] result = new Tag[levels.length];
        for (int i = 0; i < levels.length; i++) {
            result[i] = new Tag(i, levels[i].name(), levels[i].name());
        }
        return result;
    }

    public SelectedTag selectedTag() {
        return new SelectedTag(this.name(), toTags());
    }

    public static PasAlgorithm of(String name) {
        return valueOf(name.toLowerCase());
    }
}