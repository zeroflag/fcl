package com.vectron.fcl;

import com.vectron.fcl.types.Lst;
import com.vectron.fcl.types.Nil;
import com.vectron.fcl.types.Obj;
import com.vectron.fcl.types.Str;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Juggler {
    private final List<Obj> input;
    private final List<Obj> output;
    private final LStack stack;
    private final LStack rstack;
    private final Set<Obj> uniqueOutput;
    private final List<Integer> code;
    private final List<Word> availableWords;
    private final int maxSteps;

    private interface Code {
        boolean eval(Juggler juggler);
    }

    private enum Word {
        DUP("dup", Juggler::dup),
        DROP("drop", Juggler::drop),
        OVER("over", Juggler::over),
        SWAP("swap", Juggler::swap),
        NIP("nip", Juggler::nip),
        TUCK("tuck", Juggler::tuck),
        ROT("rot", Juggler::rot),
        MROT("-rot", Juggler::mrot),
        DUP2("2dup", Juggler::dup2),
        DROP2("2drop", Juggler::drop2),
        SWAP2("2swap", Juggler::swap2),
        OVER2("2over", Juggler::over2),
        RTO2("2rot", Juggler::rot2),
        MRTO2("-2rot", Juggler::mrot2),
        RTO(">r", Juggler::rto),
        RFROM("r>", Juggler::rfrom);

        private final String name;
        private final Code code;

        Word(String name, Code code) {
            this.name = name;
            this.code = code;
        }

        public boolean eval(Juggler juggler) {
            return code.eval(juggler);
        }
    }

    public static Obj solve(Lst input, Lst output, Lst excluded, int maxSteps) {
        List<String> words = solve(input.value(), output.value(), filter(excluded.value()), maxSteps);
        if (words == null) return Nil.INSTANCE;
        Lst result = new Lst();
        for (String each : words)
            result.append(new Str(each));
        return result;
    }

    private static Set<String> filter(List<Obj> excluded) {
        Set<String> result = new HashSet<>();
        for (Obj each : excluded) result.add(each.asStr().value());
        return result;
    }

    public static List<String> solve(List<Obj> input, List<Obj> output, Set<String> excluded, int maxSteps) {
        Juggler juggler = new Juggler(input, output, excluded, maxSteps);
        return juggler.solve();
    }

    private Juggler(List<Obj> input, List<Obj> output, Set<String> excluded, int maxSteps) {
        this.input = input;
        this.output = output;
        this.maxSteps = maxSteps;
        this.uniqueOutput = new HashSet<>(output);
        this.stack = new LStack();
        this.rstack = new LStack();
        this.code = new ArrayList<>();
        this.availableWords = populateWords(excluded);
        this.code.add(0);
    }

    private List<Word> populateWords(Set<String> excluded) {
        List<Word> result = new ArrayList<>();
        for (Word each : Word.values()) {
            if (!excluded.contains(each.name))
                result.add(each);
        }
        return result;
    }

    private List<String> solve() {
        if (input.isEmpty() && output.isEmpty() || input.equals(output))
            return Collections.emptyList();
        while (code.size() <= maxSteps) {
            if (goodCode(code))
                return result(code);
            next(code);
        }
        return null;
    }

    private void next(List<Integer> code) {
        int i = code.size() -1;
        int max = availableWords.size() - 1;
        code.set(i, code.get(i) +1);
        while (code.get(i) > max) {
            code.set(i, 0);
            if (i > 0) {
                code.set(--i, code.get(i) +1);
            } else {
                code.add(0, 0);
            }
        }
    }

    private boolean goodCode(List<Integer> code) {
        stack.clear();
        stack.addAll(input);
        rstack.clear();
        List<LStack> stackHistory = new ArrayList<>();
        List<LStack> rstackHistory = new ArrayList<>();

        for (int i = 0; i < code.size(); i++) {
            if (!nthWord(code.get(i)).eval(this) || nop() || cycle(stackHistory, rstackHistory)) {
                skip(i, code);
                return false;
            }
            stackHistory.add(new LStack(stack));
            rstackHistory.add(new LStack(rstack));
        }
        return rstack.isEmpty() && stack.equals(output);
    }

    private Word nthWord(int n) {
        return availableWords.get(n);
    }

    private void skip(int n, List<Integer> code) {
        int max = availableWords.size() -1;
        for (int i = n +1; i < code.size(); i++) {
            code.set(i, max);
        }
    }

    private boolean cycle(List<LStack> stackHistory, List<LStack> rstackHistory) {
        for (int i = 0; i < stackHistory.size(); i++) {
            if (stackHistory.get(i).equals(stack) && rstackHistory.get(i).equals(rstack))
                return true;
        }
        return false;
    }

    private boolean nop() {
        return rstack.isEmpty() && stack.equals(input);
    }

    private List<String> result(List<Integer> code) {
        List<String> result = new ArrayList<>();
        for (Integer each : code)
            result.add(nthWord(each).name);
        return result;
    }

    private Obj pick(int i) {
        return stack.get(stack.size() - i);
    }

    private boolean dup() {
        if (stack.size() == 0) return false;
        stack.push(pick(1));
        return true;
    }

    private boolean dup2() {
        if (stack.size() < 2) return false;
        stack.push(pick(2));
        stack.push(pick(2));
        return true;
    }

    private boolean drop2() {
        if (stack.size() < 2) return false;
        stack.pop();
        stack.pop();
        return !missing();
    }

    private boolean drop() {
        if (stack.size() == 0) return false;
        stack.pop();
        return !missing();
    }

    private boolean swap() {
        if (stack.size() < 2) return false;
        Obj n1 = stack.pop();
        Obj n2 = stack.pop();
        stack.push(n1);
        stack.push(n2);
        return true;
    }

    private boolean swap2() {
        if (stack.size() < 4) return false;
        Obj n1 = stack.pop();
        Obj n2 = stack.pop();
        Obj n3 = stack.pop();
        Obj n4 = stack.pop();
        stack.push(n2);
        stack.push(n1);
        stack.push(n4);
        stack.push(n3);
        return true;
    }

    private boolean over() {
        if (stack.size() < 2) return false;
        stack.push(pick(2));
        return true;
    }

    private boolean over2() {
        if (stack.size() < 4) return false;
        stack.push(pick(4));
        stack.push(pick(4));
        return true;
    }

    private boolean nip() {
        if (stack.size() < 2) return false;
        Obj n = stack.pop();
        stack.pop();
        stack.push(n);
        return !missing();
    }

    private boolean tuck() {
        if (stack.size() < 2) return false;
        Obj n1 = stack.pop();
        Obj n2 = stack.pop();
        stack.push(n1);
        stack.push(n2);
        stack.push(n1);
        return true;
    }

    private boolean rot() {
        if (stack.size() < 3) return false;
        Obj n1 = stack.pop();
        Obj n2 = stack.pop();
        Obj n3 = stack.pop();
        stack.push(n2);
        stack.push(n1);
        stack.push(n3);
        return true;
    }

    private boolean mrot() {
        if (stack.size() < 3) return false;
        Obj n1 = stack.pop();
        Obj n2 = stack.pop();
        Obj n3 = stack.pop();
        stack.push(n1);
        stack.push(n3);
        stack.push(n2);
        return true;
    }

    private boolean rot2() {
        if (stack.size() < 6) return false;
        Obj n1 = stack.pop();
        Obj n2 = stack.pop();
        Obj n3 = stack.pop();
        Obj n4 = stack.pop();
        Obj n5 = stack.pop();
        Obj n6 = stack.pop();
        stack.push(n4);
        stack.push(n3);
        stack.push(n2);
        stack.push(n1);
        stack.push(n6);
        stack.push(n5);
        return true;
    }

    private boolean mrot2() {
        if (stack.size() < 6) return false;
        Obj n1 = stack.pop();
        Obj n2 = stack.pop();
        Obj n3 = stack.pop();
        Obj n4 = stack.pop();
        Obj n5 = stack.pop();
        Obj n6 = stack.pop();
        stack.push(n2);
        stack.push(n1);
        stack.push(n6);
        stack.push(n5);
        stack.push(n4);
        stack.push(n3);
        return true;
    }

    private boolean rfrom() {
        if (rstack.size() < 1) return false;
        stack.push(rstack.pop());
        return true;
    }

    private boolean rto() {
        if (stack.size() < 1) return false;
        rstack.push(stack.pop());
        return true;
    }

    private boolean missing() {
        for (Obj each : uniqueOutput) {
            if (!stack.contains(each) && !rstack.contains(each))
                return true;
        }
        return false;
    }

    public final class LStack extends ArrayList<Obj> {
        public LStack() {
            super(10);
        }

        public LStack(final Collection<Obj> collection) {
            super(collection);
        }

        public void push(Obj item) {
            add(item);
        }

        public Obj pop() {
            Obj top = peek();
            remove(size() - 1);
            return top;
        }

        public Obj peek() {
            int size = size();
            if (size == 0) {
                throw new EmptyStackException();
            }
            return get(size - 1);
        }
    }
}

