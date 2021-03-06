package com.vectron.fcl;

import com.vectron.fcl.exceptions.Aborted;
import com.vectron.fcl.exceptions.TypeMismatched;
import com.vectron.fcl.interop.JvmInterOp;
import com.vectron.fcl.types.ArithmeticOperand;
import com.vectron.fcl.types.Bool;
import com.vectron.fcl.types.LogicOperand;
import com.vectron.fcl.types.Nil;
import com.vectron.fcl.types.Num;
import com.vectron.fcl.types.Obj;
import com.vectron.fcl.types.Primitive;
import com.vectron.fcl.types.Str;
import com.vectron.fcl.types.Symbol;
import com.vectron.fcl.types.Word;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Fcl {
    public static final boolean STRICT = true;
    private static final String EXIT = "exit";
    private final int SCRATCH_SIZE = 1024;
    private enum Mode { COMPILE, INTERPRET }
    private final Dictionary dict;
    private final FclStack rstack = new FclStack();
    private final JvmInterOp interOp;
    private final FclStack stack;
    private final Transcript transcript;
    private Reader reader;
    private Mode mode = Mode.INTERPRET;
    private final Object[] heap;
    private final Map<String, Symbol> symbols = new HashMap<>();
    private int dp = SCRATCH_SIZE;
    private int ip = 0;
    private boolean trace = false;

    class ColonDef implements Word {
        private final int address;
        private final String name;
        private boolean visible = true;
        private boolean immediate = false;
        private Word predicate;

        public ColonDef(int address, String name) {
            this.address = address;
            this.name = name;
        }

        @Override
        public void enter() {
            rstack.push(new Num(ip));
            innerLoop(address);
            ip = rstack.pop().intValue();
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public void visible(boolean isVisible) {
            this.visible = isVisible;
        }

        @Override
        public boolean immediate() {
            return immediate;
        }

        @Override
        public void immediate(boolean isImmediate) {
            immediate = isImmediate;
        }

        @Override
        public void predicate(Word word) {
            this.predicate = word;
        }

        @Override
        public boolean match(String name, Fcl fcl) {
            return visible && (this.name.equals(name) || predicateMatch(name, fcl));
        }

        private boolean predicateMatch(String name, Fcl fcl) {
            if (predicate == null) return false;
            fcl.push(new Str(name));
            predicate.enter();
            boolean match = fcl.pop().boolValue();
            if (match)
                fcl.push(new Str(name));
            return match;
        }

        @Override
        public String toString() {
            return "xt_" + name + " (" + address + ")";
        }

        @Override
        public long longValue() {
            return address;
        }

        @Override
        public int intValue() {
            return address;
        }

        @Override
        public double doubleValue() {
            throw new TypeMismatched(this, "double");
        }

        @Override
        public boolean boolValue() {
            throw new TypeMismatched(this, "bool");
        }

        @Override
        public Num asNum() {
            if (STRICT) throw new TypeMismatched(this, "num");
            return Num.NAN;
        }

        @Override
        public Str asStr() {
            return new Str(toString());
        }

        @Override
        public Object value() {
            return address;
        }

        @Override
        public Object unwrap() {
            return value();
        }

        @Override
        public int compareTo(Obj other) {
            return other instanceof ColonDef
                    ? name.compareTo(((ColonDef) other).name)
                    : -1;
        }

        @Override
        public Bool iterable() {
            return Bool.FALSE;
        }
    }

    public class Var implements Word {
        private final int address;
        private final String name;
        private boolean visible = true;

        public Var(int address, String name) {
            this.address = address;
            this.name = name;
            heap[address] = new Num(0);
        }

        @Override
        public void visible(boolean isVisible) {
            this.visible = isVisible;
        }

        @Override
        public boolean immediate() {
            return false;
        }

        @Override
        public void immediate(boolean isImmediate) {
            throw new Aborted("Cannot make immediate val");
        }

        @Override
        public void predicate(Word word) {
            throw new Aborted("var does not support predicate");
        }

        @Override
        public boolean match(String name, Fcl fcl) {
            return visible && this.name.equals(name);
        }

        @Override
        public void enter() {
            stack.push(new Num(address));
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return "var_" + name + " (" + address + ")";
        }

        @Override
        public long longValue() {
            return address;
        }

        @Override
        public int intValue() {
            return address;
        }

        @Override
        public double doubleValue() {
            return address;
        }

        @Override
        public boolean boolValue() {
            throw new TypeMismatched(this, "bool");
        }

        @Override
        public Num asNum() {
            if (STRICT) throw new TypeMismatched(this, "num");
            return Num.NAN;
        }

        @Override
        public Str asStr() {
            return new Str(toString());
        }

        @Override
        public Object value() {
            return address;
        }

        @Override
        public Object unwrap() {
            return value();
        }

        @Override
        public int compareTo(Obj other) {
            return other instanceof Var
                    ? name.compareTo(((Var) other).name)
                    : -1;
        }

        @Override
        public Bool iterable() {
            return Bool.FALSE;
        }
    }

    public class Val implements Word {
        private final String name;
        private final Obj value;
        private boolean visible = true;

        public Val(String name, Obj value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public void visible(boolean isVisible) {
            this.visible = isVisible;
        }

        @Override
        public boolean immediate() {
            return false;
        }

        @Override
        public void immediate(boolean isImmediate) {
            throw new Aborted("Cannot make immediate val");
        }

        @Override
        public void predicate(Word word) {
            throw new Aborted("val does not support predicate");
        }

        @Override
        public boolean match(String name, Fcl fcl) {
            return visible && this.name.equals(name);
        }

        @Override
        public void enter() {
            stack.push(value);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return "val_" + name + " (" + value + ")";
        }

        @Override
        public long longValue() {
            return value.longValue();
        }

        @Override
        public int intValue() {
            return value.intValue();
        }

        @Override
        public double doubleValue() {
            return value.doubleValue();
        }

        @Override
        public boolean boolValue() {
            return value.boolValue();
        }

        @Override
        public Num asNum() {
            if (STRICT) throw new TypeMismatched(this, "num");
            return Num.NAN;
        }

        @Override
        public Str asStr() {
            return new Str(toString());
        }

        @Override
        public Object value() {
            return value;
        }

        @Override
        public Object unwrap() {
            return value();
        }

        @Override
        public int compareTo(Obj other) {
            return other instanceof Val
                    ? name.compareTo(((Val) other).name)
                    : -1;
        }

        @Override
        public Bool iterable() {
            return Bool.FALSE;
        }
    }

    public Fcl(FclStack stack, int heapSize, Transcript transcript) {
        this.dict = new Dictionary(this);
        this.stack = stack;
        this.heap = new Object[heapSize];
        this.interOp = new JvmInterOp(stack);
        this.transcript = transcript;
        initPrimitives();
    }

    private void initPrimitives() {
        addPrimitive("+", () -> stack.push((aOp(stack.pop())).add(stack.pop())));
        addPrimitive("-", () -> {
            Obj top = stack.pop();
            stack.push(aOp(stack.pop()).sub(top));
        });
        addPrimitive("*", () -> stack.push((aOp(stack.pop())).mul(stack.pop())));
        addPrimitive("/", () -> {
            Obj top = stack.pop();
            stack.push((aOp(stack.pop())).div(top));
        });
        addPrimitive("/mod", () -> {
            Num b = stack.pop().asNum();
            Num a = stack.pop().asNum();
            stack.push(a.mod(b));
            stack.push(a.intDiv(b));
        });
        addPrimitive("pow", () -> {
            Obj exponent = stack.pop();
            Obj base = stack.pop();
            stack.push(aOp(base).pow(exponent));
        });
        addPrimitive("and", () -> stack.push(lOp(stack.pop()).and(stack.pop())));
        addPrimitive("or", () -> stack.push((lOp(stack.pop())).or(stack.pop())));
        addPrimitive("not", () -> stack.push((lOp(stack.pop())).not()));
        addPrimitive("drop", stack::pop);
        addPrimitive("dup", () -> stack.push(stack.peek()));
        addPrimitive("swap", () -> {
            Obj a = stack.pop();
            Obj b = stack.pop();
            stack.push(a);
            stack.push(b);
        });
        addPrimitive("rswap", () -> {
            Obj a = rstack.pop();
            Obj b = rstack.pop();
            rstack.push(a);
            rstack.push(b);
        });
        addPrimitive(EXIT, () -> {});
        addPrimitive("clean", stack::clean);
        addPrimitive("depth", () -> stack.push(new Num(stack.size())));
        addPrimitive("=", () -> stack.push(stack.pop().equals(stack.pop()) ? Bool.TRUE : Bool.FALSE));
        addPrimitive("<", () -> stack.push(stack.pop().asNum().greater(stack.pop().asNum())));
        addPrimitive("true", () -> stack.push(Bool.TRUE));
        addPrimitive("false", () -> stack.push(Bool.FALSE));
        addPrimitive("nil", () -> stack.push(Nil.INSTANCE));
        addPrimitive("here", () -> stack.push(new Num(dp)));
        addPrimitive("dp!", () -> dp = stack.pop().intValue() );
        addPrimitive("interpret", () -> mode = Mode.INTERPRET);
        addPrimitive("interpret?", () -> stack.push(mode == Mode.INTERPRET ? Bool.TRUE : Bool.FALSE));
        addPrimitive("trace", () -> trace = stack.pop().boolValue());
        addPrimitive("lit", () -> stack.push((Obj)heap[ip++]));
        addPrimitive(">r", () -> rstack.push(stack.pop()));
        addPrimitive("r>", () -> stack.push(rstack.pop()));
        addPrimitive("i", () -> stack.push(rstack.peek()));
        addPrimitive("j", () -> stack.push(rstack.at(1)));
        addPrimitive(",", () -> heap[dp++] = stack.pop());
        addPrimitive("!", () -> heap[stack.pop().intValue()] = stack.pop());
        addPrimitive("@", () -> stack.push((Obj) heap[stack.pop().intValue()]));
        addPrimitive("[']", () -> stack.push((Word)heap[ip++]));
        addPrimitive("`", () -> { Word word = dict.at(word()); stack.push(word == null ? Nil.INSTANCE : word); });
        addPrimitive("immediate", () -> dict.lastWord().immediate(true), true);
        addPrimitive("lastword", () -> stack.push(dict.lastWord()));
        addPrimitive("set-predicate", () -> ((Word)stack.pop()).predicate((Word)stack.pop()));
        addPrimitive(".", () -> show(stack.pop()));
        addPrimitive("jvm-call-static", interOp::jvmCallStatic);
        addPrimitive("jvm-call-method", interOp::jvmCallMethod);
        addPrimitive("jvm-has-method", interOp::jvmHasMethod);
        addPrimitive("jvm-static-var", interOp::jvmStaticVar);
        addPrimitive("jvm-null", () -> stack.push(null));
        addPrimitive("asc*", this::sortAsc);
        addPrimitive("dsc*", this::sortDsc);
        addPrimitive("rev*", this::reverse);
        addPrimitive("key", () -> stack.push(new Num(key())));
        addPrimitive("word", () -> stack.push(new Str(word())));
        addPrimitive("override", () -> dict.lastWord().visible(false), true);
        addPrimitive("reveal", () -> dict.lastWord().visible(true));
        addPrimitive("delword", () -> dict.remove((String)stack.pop().value()));
        addPrimitive("jmp#f", () -> ip += stack.pop().boolValue() ? 1 : ((Num) heap[ip]).longValue());
        addPrimitive("jmp", () -> ip += ((Num) heap[ip]).longValue());
        addPrimitive("allot", () -> { int p = dp; dp += stack.pop().longValue(); stack.push(new Num(p)); });
        addPrimitive("freemem", () -> stack.push(new Num(heap.length - dp)));
        addPrimitive("var:", () -> { String name = word(); dict.add(new Var(dp, name)); dp++; });
        addPrimitive("val:", () -> { String name = word(); dict.add(new Val(name, stack.pop())); });
        addPrimitive("abort", () -> { throw new Aborted(stack.pop().asStr().value()); });
        addPrimitive("eval", () -> eval(stack.pop().asStr().value()));
        addPrimitive("words", () -> {
            List<String> words = new ArrayList<>(wordList(Dictionary.Filter.ALL));
            Collections.sort(words);
            for (String each : words) {
                transcript.show(each);
                transcript.cr();
        }});
        addPrimitive("exec", () -> {
            rstack.push(new Num(ip));
            innerLoop(pop().intValue());
            ip = rstack.pop().intValue();
        });
        addPrimitive("create", () -> dict.add(new ColonDef(dp, (String)stack.pop().value())));
        addPrimitive("dasm", this::disassemble);
        addPrimitive(":", () -> {
            dict.add(new ColonDef(dp, word()));
            mode = Mode.COMPILE;
        });
        addPrimitive(";", () -> {
            heap[dp++] = dict.at(EXIT);
            heap[dp++] = Nil.INSTANCE;
            mode = Mode.INTERPRET;
            dict.lastWord().visible(true);
        }, true);
    }

    private LogicOperand lOp(Obj obj) {
        try {
            return (LogicOperand) obj;
        } catch (ClassCastException e) {
            throw new TypeMismatched(obj + " cannot do logic operators on " + obj);
        }
    }

    public static ArithmeticOperand aOp(Obj obj) {
        try {
            return (ArithmeticOperand) obj;
        } catch (ClassCastException e) {
            throw new TypeMismatched(obj + " cannot do arithmetic");
        }
    }

    private void show(Obj item) {
        transcript.show(item == null ? "null" : item.asStr().value());
        transcript.cr();
    }

    private void disassemble() {
        String name = (String) stack.pop().value();
        Word word = dict.at(name);
        if (word instanceof ColonDef) {
            int address = ((ColonDef)word).address;
            while (true) {
                transcript.show(String.format("[%08X] %s", address, heap[address]));
                transcript.cr();
                if (heap[address] instanceof Word && ((Word)heap[address]).name().equals(EXIT)
                    && heap[address+1] == Nil.INSTANCE) {
                    break;
                }
                address++;
            }
        } else {
            System.err.println("Not colon def: " + word);
        }
    }

    private void sortDsc() {
        stack.sortDsc();
    }

    private void sortAsc() {
        stack.sortAsc();
    }

    private void reverse() {
        stack.reverse();
    }

    private void addPrimitive(String name, Runnable code) {
        addPrimitive(name, code, false);
    }

    public void addPrimitive(String name, Runnable code, boolean immediate) {
        Primitive word = new Primitive(name, code);
        word.immediate(immediate);
        dict.add(word);
    }

    public void eval(String source) {
        eval(new StringReader(source));
    }

    public void eval(Reader reader) {
        this.reader = reader;
        String token = word();
        while (!token.isEmpty()) {
            onTokenFound(token);
            token = word();
        }
    }

    /**
     * Compile a temporary word into the scratch area and call it.
     * This is useful for evaluating words without interpretation semantics.
     * Like: if else then, loops, quotations
     */
    public void compileTmpAndEval(String script) {
        // XXX ; make locals work, ; drops the frame, so we need to alloc first, normally this is done by the overridden colon
        Word frameAlloc = dict.at("frame.alloc");
        if (frameAlloc != null)
            frameAlloc.enter();
        int savedDp = dp;
        Mode savedMode = mode;
        try {
            dp = heap.length - SCRATCH_SIZE;
            mode = Mode.COMPILE;
            eval(script);
            eval(";");
            mode = Mode.INTERPRET;
            innerLoop(heap.length - SCRATCH_SIZE);
        } finally {
            dp = savedDp;
            mode = savedMode;
        }
    }

    private String word() {
        StringBuilder token = new StringBuilder();
        int key = key();
        while (key != -1) {
            char chr = (char) key;
            if (Character.isWhitespace(chr)) {
                if (token.length() > 0)
                    return token.toString();
                token.setLength(0);
            } else {
                token.append(chr);
            }
            key = key();
        }
        return token.toString();
    }

    private int key() {
        try {
            return reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onTokenFound(String name) {
        Word word = dict.at(name);
        switch (mode) {
            case INTERPRET:
                if (word != null) {
                    trace("exec " + word.name());
                    word.enter();
                } else {
                    trace("push " + name);
                    stack.push(recognize(name));
                }
                break;
            case COMPILE:
                if (word != null) {
                    if (word.immediate()) {
                        trace("exec " + word.name());
                        word.enter();
                    } else {
                        trace(word.name() + " ,");
                        heap[dp++] = word;
                    }
                } else {
                    trace("lit " + name + " ,");
                    heap[dp++] = dict.at("lit");
                    heap[dp++] = recognize(name);
                }
                break;
        }
    }

    private void trace(String str) {
        if (trace) {
            transcript.show("T/" + mode.toString().charAt(0) + " " + str);
            transcript.cr();
        }
    }

    private Obj recognize(String token) {
        Obj str = recognizeStr(token);
        if (str != null) return str;
        Obj symbol = recognizeSymbol(token);
        if (symbol != null) return symbol;
        return Num.parse(token);
    }

    private Obj recognizeStr(String token) {
        if (!token.startsWith("'")) return null;
        StringBuilder str = new StringBuilder(token.substring(1));
        if (token.endsWith("'") && token.length() > 1) {
            str.setLength(str.length() - 1);
        } else {
            str.append(" ");
            int k = key();
            while (k != -1 && (char) k != '\'') {
                str.append((char) k);
                k = key();
            }
        }
        return new Str(str.toString());
    }

    private Obj recognizeSymbol(String token) {
        return token.startsWith(":") && token.length() > 1
                ? symbol(token.substring(1))
                : null;
    }

    private Symbol symbol(String name) {
        if (!symbols.containsKey(name))
            symbols.put(name, new Symbol(name));
        return symbols.get(name);
    }

    private void innerLoop(int address) {
        ip = address;
        Word word = (Word) heap[ip++];
        while (!EXIT.equals(word.name())) {
            word.enter();
            word = (Word) heap[ip++];
        }
    }

    public Word get(String name) {
        return dict.at(name);
    }

    public Obj pop() {
        return stack.pop();
    }

    public void push(Obj obj) {
        stack.push(obj);
    }

    public int stackSize() {
        return stack.size();
    }

    public int rStackSize() {
        return rstack.size();
    }

    public void switchStack(FclStack stack) {
        this.stack.switchStack(stack);
    }

    public void reset() {
        mode = Mode.INTERPRET;
        stack.clean();
        rstack.clean();
    }

    public Set<String> wordList(Dictionary.Filter filter) {
        return dict.wordList(filter);
    }
}