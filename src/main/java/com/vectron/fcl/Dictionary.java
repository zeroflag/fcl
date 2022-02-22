package com.vectron.fcl;

import com.vectron.fcl.types.Word;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dictionary {
    public interface Filter {
        boolean shouldInclude(String name);
        Filter ALL = new Filter() {
            public boolean shouldInclude(String name) {
                return true;
            }
        };
        Filter NO_IMPLS = new Filter() {
            public boolean shouldInclude(String name) {
                return !name.endsWith("-impl");
            }
        };
    }
    private final List<Word> dict = new ArrayList<>();
    private final Fcl fcl;

    public Dictionary(Fcl fcl) {
        this.fcl = fcl;
    }

    public void add(Word word) {
        dict.add(word);
    }

    public Word at(String name) {
        for (int i = dict.size() - 1; i >= 0; i--) {
            Word each = dict.get(i);
            if (each.match(name, fcl))
                return each;
        }
        return null;
    }

    public void remove(String name) {
        Word existing = at(name);
        if (existing != null)
            dict.remove(existing);
    }

    public Set<String> wordList(Filter filter) {
        Set<String> result = new HashSet<>();
        for (Word word : dict) {
            if (filter.shouldInclude(word.name()))
                result.add(word.name());
        }
        return result;
    }

    public Word lastWord() {
        return dict.get(dict.size() -1);
    }
}
