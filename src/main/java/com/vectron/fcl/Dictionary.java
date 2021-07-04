package com.vectron.fcl;

import com.vectron.fcl.types.Word;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dictionary {
    private final List<Word> dict = new ArrayList<>();

    public Dictionary() {
    }

    public void add(Word word) {
        dict.add(word);
    }

    public Word at(String name) {
        for (int i = dict.size() - 1; i >= 0; i--) {
            Word each = dict.get(i);
            if (each.visible() && name.equals(each.name()))
                return each;
        }
        return null;
    }

    public void remove(String name) {
        Word exiting = at(name);
        if (exiting != null)
            dict.remove(exiting);
    }

    public Set<String> wordList() {
        Set<String> result = new HashSet<>();
        for (Word word : dict) {
            result.add(word.name());
        }
        return result;
    }

    public Word lastWord() {
        return dict.get(dict.size() -1);
    }
}
