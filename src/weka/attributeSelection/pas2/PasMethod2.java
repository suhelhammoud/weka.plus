package weka.attributeSelection.pas2;

import weka.core.SelectedTag;
import weka.core.Tag;

public enum PasMethod2 {
  rules, rules1st, items;

  public static Tag[] toTags() {
    PasMethod2[] levels = values();
    Tag[] result = new Tag[levels.length];
    for (int i = 0; i < levels.length; i++) {
      result[i] = new Tag(i, levels[i].name(), levels[i].name());
    }
    return result;
  }

  public SelectedTag selectedTag() {
    return new SelectedTag(this.name(), toTags());
  }

  public static PasMethod2 of(String name) {
    return valueOf(name.toLowerCase());
  }
}