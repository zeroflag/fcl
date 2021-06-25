package com.vectron.fcl;

import com.vectron.fcl.Fcl;
import com.vectron.fcl.FclStack;
import com.vectron.fcl.RamTranscript;
import com.vectron.fcl.types.Obj;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FclDasmTest { // TODO
    private Fcl fcl;
    private RamTranscript transcript = new RamTranscript();

    @Before
    public void setUp() throws Exception {
        resetForth();
        assertEquals(0, fcl.stackSize());
        assertEquals(0, fcl.rStackSize());
        assertEquals(0, evalPop("psp @").longValue());
    }

    private void resetForth() throws IOException {
        fcl = new Fcl(new FclStack(), 524288, transcript);
        load("core.forth");
        load("ops.forth");
        load("locals.forth");
        load("quotations.forth");
        load("collections.forth");
        load("http.forth");
    }

    private void load(final String fileName) throws IOException {
        FileReader reader = null;
        try {
            reader = new FileReader(System.getProperty("user.dir") + "/src/main/res/raw/" + fileName);
            fcl.eval(reader);
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    @Test
    public void name() {
        eval(": tst1 -> a -> b -> c -> d 12 ;");
        dasm("tst1");
    }

    private void dasm(String word) {
        eval("'" + word + "' dasm");
        System.err.println(transcript.content());
    }

    @Test
    public void testLocalDisasm() {
        eval(": sqe -> a -> b -> c\n" +
                "    b neg b b * 4 a * c * - sqrt - 2 a * /\n" +
                "    b neg b b * 4 a * c * - sqrt + 2 a * /\n" +
                ";\n");
        dasm("sqe");
    }

    @Test
    public void test() {
        eval(": find ( n -- n )\n" +
                "0 => count -> s\n" +
                "1001 1 do\n" +
                "   i s /mod -> quotient -> remainder \n" +
                "   remainder 0 = if\n" +
                "       count inc\n" +
                "   then\n" +
                "loop\n" +
                "count @ ;");
        dasm("find");
    }

    @Test
    public void testLocalEarlyDisasm() {
        eval(": tst -> x -> y\n" +
                "  x y < if 1 exit then -1 ;\n" +
                ";\n");
        dasm("tst");
    }

    @Test
    public void testNoLocal() {
        eval(": tst\n" +
                " < if 1 exit then -1 ;\n");
        dasm("tst");
    }

    @After
    public void tearDown() throws Exception {
        assertEquals(0, fcl.stackSize());
        assertEquals(0, fcl.rStackSize());
        assertEquals(0, evalPop("psp @").longValue());
    }

    private void eval(String script) {
        fcl.eval(script);
    }

    private Obj evalPop(String script) {
        eval(script);
        return fcl.pop();
    }
}

