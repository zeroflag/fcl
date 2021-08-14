package com.vectron.fcl;

import com.vectron.fcl.exceptions.Aborted;
import com.vectron.fcl.exceptions.NotUnderstood;
import com.vectron.fcl.exceptions.TypeMismatched;
import com.vectron.fcl.types.Nil;
import com.vectron.fcl.types.Num;
import com.vectron.fcl.types.Obj;
import com.vectron.fcl.types.Str;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.vectron.fcl.types.Bool.FALSE;
import static com.vectron.fcl.types.Bool.TRUE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FclTest {
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
        fcl.addPrimitive("exchange", () -> {}, false);
        fcl.addPrimitive("aux>", () -> {}, false);
        fcl.addPrimitive(">aux", () -> {}, false);
        load("core.forth");
        load("ops.forth");
        load("locals.forth");
        load("quotations.forth");
        load("collections.forth");
        load("http.forth");
        load("misc.forth");
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

    @After
    public void tearDown() throws Exception {
        System.out.println("Transcript: " + transcript.content());
        assertEquals(0, fcl.stackSize());
        assertEquals(0, fcl.rStackSize());
        assertEquals(0, evalPop("psp @").longValue());
    }

    @Test
    public void testArithmetic() {
        assertEquals(3, evalPop("1 2 +").longValue());
        assertEquals(12, evalPop("3 4 *").longValue());
        assertEquals(4, evalPop("6 2 -").longValue());
        assertEquals(-3, evalPop("7 10 -").longValue());
        assertEquals(2.0, evalPop("100 50 /").doubleValue(), 0.01);
        assertEquals(101, evalPop("100 1+").longValue());
        assertEquals(256, evalPop("2 8 pow").doubleValue(), 0.01);
        assertEquals(65536, evalPop("2 16 pow").doubleValue(), 0.01);
        assertEquals(1157.625, evalPop("10.5 3 pow").doubleValue(), 0.01);
        assertEquals(asList(1l, 3l), evalGetStack("10 3 /mod"));
        assertEquals(asList(0l, 2l), evalGetStack("10 5 /mod"));
        assertEquals(1, evalPop("10 3 mod").intValue());
        assertEquals(3, evalPop("10 3 div").intValue());
        assertEquals(0, evalPop("10 5 mod").intValue());
        assertEquals(2, evalPop("10 5 div").intValue());
        assertEquals(7, evalPop("7 round").longValue());
        assertEquals(8, evalPop("7.5 round").longValue());
        assertEquals(7, evalPop("7.4 round").longValue());
        assertEquals(asList(1l, 85l), evalGetStack("2 8 pow 3 /mod"));
        assertEquals(3, evalPop("10 3 /mod nip").intValue());
        assertEquals(1, evalPop("10 3 /mod drop").intValue());
    }

    @Test
    public void testNumberLiterals() {
        assertEquals(12345, evalPop("12345").longValue());
        assertEquals(255, evalPop("0xFF").longValue());
        assertEquals(131, evalPop("0b10000011").longValue());
        assertEquals(-1.44, evalPop("-1.44").doubleValue(), 0.01);
    }

    @Test
    public void testLogic() {
        assertEquals(TRUE, evalPop("true true and"));
        assertEquals(FALSE, evalPop("true false and"));
        assertEquals(FALSE, evalPop("false true and"));
        assertEquals(FALSE, evalPop("false false and"));

        assertEquals(TRUE, evalPop("true true or"));
        assertEquals(TRUE, evalPop("true false or"));
        assertEquals(TRUE, evalPop("false true or"));
        assertEquals(FALSE, evalPop("false false or"));
    }

    @Test
    public void testIntLogic() {
        assertEquals(255, evalPop("255 0 or").longValue());
        assertEquals(7, evalPop("4 3 or").longValue());
        assertEquals(30, evalPop("12 26 or").longValue());
        assertEquals(0, evalPop("100 0 and").longValue());
        assertEquals(8, evalPop("26 12 and").longValue());
        assertEquals(0, evalPop("255 0 and").longValue());
        assertEquals(-1, evalPop("0 not").longValue());
    }

    @Test
    public void testJuggling() { // http://sovietov.com/app/forthwiz.html
        assertEquals(asList(2l, 1l), evalGetStack("1 2 swap"));
        assertEquals(asList(1l, 2l, 1l), evalGetStack("1 2 over"));
        assertEquals(asList(1l, 2l, 3l, 4l, 1l, 2l), evalGetStack("1 2 3 4 2over"));
        assertEquals(asList(3l, 3l), evalGetStack("3 dup"));
        assertEquals(asList(3l, 4l, 3l, 4l), evalGetStack("3 4 2dup"));
        assertEquals(asList(6l), evalGetStack("5 6 nip"));
        assertEquals(asList(7l), evalGetStack("7 8 drop"));
        assertEquals(emptyList(), evalGetStack("6 5 2drop"));
        assertEquals(asList(2l, 3l, 1l), evalGetStack("1 2 3 rot"));
        assertEquals(asList(3l, 1l, 2l), evalGetStack("1 2 3 -rot"));
        assertEquals(asList(2l, 1l, 2l), evalGetStack("1 2 tuck"));
        assertEquals(asList(3l, 4l, 1l, 2l), evalGetStack("1 2 3 4 2swap"));
        assertEquals(asList(3l, 4l, 5l, 6l, 1l, 2l), evalGetStack("1 2 3 4 5 6 2rot"));
    }

    @Test
    public void testCmp() {
        assertEquals(FALSE, evalPop("1 2 ="));
        assertEquals(TRUE, evalPop("2 2 ="));
        assertEquals(TRUE, evalPop("1 2 !="));
        assertEquals(FALSE, evalPop("1 1 !="));

        assertEquals(TRUE, evalPop("10 20 <"));
        assertEquals(FALSE, evalPop("10 10 <"));
        assertEquals(FALSE, evalPop("13 10 <"));

        assertEquals(TRUE, evalPop("30 20 >"));
        assertEquals(FALSE, evalPop("10 10 >"));
        assertEquals(FALSE, evalPop("4 10 >"));

        assertEquals(TRUE, evalPop("10 20 <="));
        assertEquals(TRUE, evalPop("10 10 <="));
        assertEquals(FALSE, evalPop("13 10 <="));

        assertEquals(TRUE, evalPop("30 20 >="));
        assertEquals(TRUE, evalPop("10 10 >="));
        assertEquals(FALSE, evalPop("4 10 >="));
    }

    @Test
    public void testUtils() {
        assertEquals(123, evalPop("123 456 min").longValue());
        assertEquals(12, evalPop("654 12 min").longValue());
        assertEquals(456, evalPop("123 456 max").longValue());
        assertEquals(654, evalPop("654 12 max").longValue());
        assertEquals(5, evalPop("1 2 3 4 5 depth").longValue());
        eval("clean");
        assertEquals(100, evalPop("10 20 30 40 sum*").longValue());
        assertEquals(120, evalPop("2 3 4 5 prod*").longValue());
        assertEquals(42, evalPop("42 sum*").longValue());
        assertEquals(55, evalPop("55 prod*").longValue());

        assertEquals(-3, evalPop("3 -1 0 9 12 -3 49 6 min*").longValue());
        assertEquals(3, evalPop("3 min*").longValue());

        assertEquals(49, evalPop("3 -1 0 9 12 -3 49 6 max*").longValue());
        assertEquals(3, evalPop("3 max*").longValue());

        eval("sum*");
        eval("prod*");
        eval("min*");
        eval("max*");
    }

    @Test
    public void testSorts() {
        assertEquals(
                asList(-65l, -3l, -1l, 0l, 3l, 3l, 6l, 7l, 9.0, 12l, 49.5, 123l),
                evalGetStack("3 3 -1 0 9.0 12 -3 49.5 6 7 123 -65 dsc*"));
        assertEquals(
                asList(123l, 49.5, 12l, 9.0, 7l, 6l, 3l, 3l, 0l, -1l, -3l, -65l),
                evalGetStack("3 3 -1 0 9.0 12 -3 49.5 6 7 123 -65 asc*"));
        assertEquals(
                asList(5l, 4l, 3l, 2l, 1l),
                evalGetStack("1 2 3 4 5 rev*"));
        eval("clean");
        eval("asc*");
        eval("dsc*");
        eval("rev*");
    }

    @Test
    public void testFinance() {
        assertEquals(1216.65, evalPop("1000 4 5 cin1").doubleValue(), 0.01);
        assertEquals(821.93, evalPop("1000 4 5 dis").doubleValue(), 0.01);
        assertEquals(558.39, evalPop("1000 6 10 dis").doubleValue(), 0.01);
        evalDoubles("1000.0 4 5 100 cin2", asList(1000.0, 2240.0, 3529.6, 4870.784, 6265.61536, 7716.2399744));
        assertEquals(15, evalPop("100 tip1").doubleValue(), 0.01);
        evalDoubles("100 1 tip2", asList(115.0, 15.0));
        evalDoubles("100 3 tip2", asList(38.33, 5));
        evalDoubles("3421 5 tip2", asList(786.83, 102.63));
        assertEquals(-371.68, evalPop("[ -1000 50 100 150 200 250 ] 5 npv").doubleValue(), 0.01);
        assertEquals(-371.68, evalPop("-1000 50 100 150 200 250 5 npv*").doubleValue(), 0.01);
        assertEquals(-250, evalPop("[ -1000 50 100 150 200 250 ] 0 npv").doubleValue(), 0.01);
        assertEquals(12.006, evalPop("[ -500 50 100 150 200 250 ] irr").doubleValue(), 0.01);
        assertEquals(-7.431, evalPop("[ -1000 50 100 150 200 250 ] irr").doubleValue(), 0.01);
        assertEquals(-28.482, evalPop("-10 irr-guess ! -5000 200 230 400 202 450 irr*").doubleValue(), 0.01);
        assertEquals(120000000, evalPop("400000 4 fire").doubleValue(), 0.01);
        assertEquals(1.96, evalPop("4 2 ri").doubleValue(), 0.01);
    }

    private void evalDoubles(String script, List<Number> expected) {
        List<Object> result = evalGetStack(script);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < result.size(); i++)
            assertEquals((Double) expected.get(0), (Double) result.get(0), 0.01);
    }

    @Test
    public void testWhile() {
        eval(": ctn-while " +
                "begin " +
                "    dup 0 >= " +
                "while " +
                "    dup . 1 - " +
                "repeat " +
                "drop ; " +
                "5 ctn-while");
        assertEquals("5\n4\n3\n2\n1\n0\n", transcript());
    }

    @Test
    public void testUntil() {
        eval(": ctn-until " +
                " begin " +
                "    dup . " +
                "    1 - dup " +
                "  0 < until " +
                "  drop ; " +
                "5 ctn-until");
        assertEquals("5\n4\n3\n2\n1\n0\n", transcript());
    }

    @Test
    public void testCase() {
        eval(": day ( n -- s )\n" +
                "  case\n" +
                "    1 of 'Monday' endof\n" +
                "    2 of 'Tuesday' endof\n" +
                "    3 of 'Wednesday' endof\n" +
                "    4 of 'Thursday' endof\n" +
                "    5 of 'Friday' endof\n" +
                "    6 of 'Saturday' endof\n" +
                "    7 of 'Sunday' endof\n" +
                "    drop 'Unknown day'\n" +
                "  endcase ;"
        );
        assertEquals("Monday", evalPop("1 day").asStr().value());
        assertEquals("Tuesday", evalPop("2 day").asStr().value());
        assertEquals("Wednesday", evalPop("3 day").asStr().value());
        assertEquals("Thursday", evalPop("4 day").asStr().value());
        assertEquals("Friday", evalPop("5 day").asStr().value());
        assertEquals("Saturday", evalPop("6 day").asStr().value());
        assertEquals("Sunday", evalPop("7 day").asStr().value());
        assertEquals("Unknown day", evalPop("8 day").asStr().value());
    }

    @Test
    public void testRStack() {
        eval("12 >r 14 >r r> r>");
        assertEquals(12, fcl.pop().longValue());
        assertEquals(14, fcl.pop().longValue());
        eval("42 >r i r>");
        assertEquals(42, fcl.pop().longValue());
        assertEquals(42, fcl.pop().longValue());
        eval("7 >r 8 >r i j r> r> 2drop");
        assertEquals(7, fcl.pop().longValue());
        assertEquals(8, fcl.pop().longValue());
        assertEquals(asList(1l, 2l), evalGetStack("1 >r 2 >r rswap r> r>"));
    }

    @Test
    public void testMemory() {
        eval("42 800 !");
        assertEquals(42, evalPop("800 @").longValue());
        eval("true 900 !");
        assertEquals(TRUE, evalPop("900 @"));
        eval("false 900 !");
        assertEquals(FALSE, evalPop("900 @"));
    }

    @Test
    public void testDo() {
        eval(": ctn-do 0 do i . loop ; 10 ctn-do");
        assertEquals("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n", transcript());
    }

    @Test
    public void testDo0() {
        eval(": ctn-do 0 do i . loop ; 0 ctn-do");
        assertEquals("", transcript());
    }

    @Test
    public void testDo1() {
        eval(": ctn-do 0 do i . loop ; 1 ctn-do");
        assertEquals("0\n", transcript());
    }

    @Test
    public void testStr() {
        assertEquals(new Str("hello"), evalPop("'hello'"));
        assertEquals(new Str("  "), evalPop("'  '"));
        assertEquals(new Str("hello world"), evalPop("'hello world'"));
        assertEquals(11, evalPop("'hello world' size").intValue());
        assertEquals(new Str("hello"), evalPop("'hello world' 0 5 substr"));
        assertEquals(new Str("h"), evalPop("'hello world' 0 1 substr"));
        assertEquals(new Str("e"), evalPop("'hello world' 1 2 substr"));
        assertEquals(new Str(""), evalPop("'hello world' 0 0 substr"));
        assertEquals(new Str("hello world"), evalPop("'hello world' 0 11 substr"));
        assertEquals(new Str("e"), evalPop("'hello world' 1 at"));
        assertEquals(new Str("olleh"), evalPop("'hello' reverse"));
        assertEquals(new Str("HELLO"), evalPop("'hello' upper"));
        assertEquals(new Str("hello"), evalPop("'HELLO' lower"));
        assertEquals(new Str("xx"), evalPop("'  xx  ' trim"));
        assertEquals(1, evalPop("'abcd' 'bc' index-of").intValue());
        assertEquals(-1, evalPop("'abcd' 'ba' index-of").intValue());
        assertEquals(new Str("abcyyydepkcyyyk"), evalPop("'abcxxdepkcxxk' 'xx' 'yyy' replace"));
        assertEquals(new Str("af123"), evalPop("'af' '123' concat"));
        assertEquals("[ 'hello' 'world' ]", evalPop("'hello world' ' ' split").toString());
        assertEquals("[ 'a' 'd' ]", evalPop("'abcd' 'bc' split").toString());
        assertEquals(new Str("hello world 1  2   3 !"), evalPop("'hello world 1  2   3 !'"));
        assertEquals(new Str("123"), evalPop("123 >str"));
        eval("1 2 + drop 'a b c   test' .");
        assertEquals("a b c   test\n", transcript());
    }

    @Test
    public void testFormatString() {
        assertEquals(new Str("a=1 b=xx"), evalPop("[ 1 'xx' ] 'a=%d b=%s' format"));
        assertEquals(new Str("a=ff b=false"), evalPop("[ 255 false ] 'a=%x b=%b' format"));
    }

    @Test
    public void testCompilingUndefined() throws Exception {
        try {
            eval(": tst asdf123 ;");
            fail("expected not understood");
        } catch (NotUnderstood e) {
            resetForth();
        }
    }

    @Test
    public void testQuotationTypeMismatch() throws Exception {
        if (!Fcl.STRICT) return;
        try {
            eval(": tst { + } ; tst 3 *");
        } catch (TypeMismatched e) {
            resetForth();
        }
    }

    @Test
    public void testMath() {
        assertEquals(0, evalPop("100 0 percent").doubleValue(), 0.001);
        assertEquals(10, evalPop("100 10 percent").doubleValue(), 0.001);
        assertEquals(100, evalPop("100 100 percent").doubleValue(), 0.001);
        assertEquals(130.686, evalPop("4356.2 3 percent").doubleValue(), 0.001);
        assertEquals(0.16482, evalPop("1.23 13.4 percent").doubleValue(), 0.001);
    }

    @Test
    public void testInterOp() {
        assertEquals(1, evalPop("0 cos").doubleValue(), 0.001);
        assertEquals(1, evalPop("0.0 cos").doubleValue(), 0.001);
        assertEquals(1, evalPop("pi 2 * cos").doubleValue(), 0.001);
        assertEquals(1, evalPop("pi 2 / sin").doubleValue(), 0.001);
        assertEquals(1, evalPop("pi 4 / tan").doubleValue(), 0.001);
        assertEquals(3, evalPop("1000.0 10log").doubleValue(), 0.001);
        assertEquals(8, evalPop("256 2log").doubleValue(), 0.001);
        assertEquals(5, evalPop("253 3 nlog").doubleValue(), 0.1);
        assertEquals(3.433, evalPop("251 5 nlog").doubleValue(), 0.1);
        assertEquals(4.0, evalPop("16 sqrt").doubleValue(), 0.001);
        assertEquals(1.0, evalPop("e elog").doubleValue(), 0.001);
        assertEquals(Math.PI / 2, evalPop("1 asin").doubleValue(), 0.001);
        assertEquals(0, evalPop("1 acos").doubleValue(), 0.001);
        assertEquals(Math.PI / 4, evalPop("1 atan").doubleValue(), 0.001);
        assertEquals(1.1752, evalPop("1 sinh").doubleValue(), 0.001);
        assertEquals(1.5430, evalPop("1 cosh").doubleValue(), 0.001);
        assertEquals(0.7616, evalPop("1 tanh").doubleValue(), 0.001);
        assertEquals(1, evalPop("1.3 :intValue jvm-call-method").doubleValue(), 0.01);
        assertFalse(evalPop("1.3 'nosuch' jvm-has-method").boolValue());
        assertTrue(evalPop("1.3 'round' jvm-has-method").boolValue());
        assertTrue(evalPop("[ 1 2 ] 'iterator' jvm-has-method").boolValue());
        assertTrue(evalPop("[ 1 2 ] 'append/O' jvm-has-method").boolValue());
        assertFalse(evalPop("[ 1 2 ] :append/i jvm-has-method").boolValue());
        assertFalse(evalPop("[ 1 2 ] 'append/OO' jvm-has-method").boolValue());
    }

    @Test
    public void testFactorial() {
        assertEquals(1, evalPop("-1 n!").intValue());
        assertEquals(1, evalPop("0 n!").intValue());
        assertEquals(1, evalPop("1 n!").intValue());
        assertEquals(2, evalPop("2 n!").intValue());
        assertEquals(6, evalPop("3 n!").intValue());
        assertEquals(120, evalPop("5 n!").intValue());
        assertEquals(3628800, evalPop("10 n!").intValue());
        assertEquals(479001600, evalPop("12 n!").intValue());
        assertEquals(6, evalPop("3.4 n!").intValue()); // rounding
    }

    @Test
    public void testAvg() {
        assertEquals(0, evalGetStack("avg*").size());
        assertEquals(2, evalPop("2 avg*").doubleValue(), 0.01);
        assertEquals(1.5, evalPop("1 2 avg*").doubleValue(), 0.01);
        assertEquals(17.2875, evalPop("2.4 0 -3 10.7 -12 50 89 1.2 avg*").doubleValue(), 0.01);
    }

    @Test
    public void testUnitConversions() {
        assertEquals(0, evalPop("0 ft>m").doubleValue(), 0.01);
        assertEquals(0.3048, evalPop("1 ft>m").doubleValue(), 0.01);
        assertEquals(0.6096, evalPop("2 ft>m").doubleValue(), 0.01);
        assertEquals(3048, evalPop("10000 ft>m").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 m>ft").doubleValue(), 0.01);
        assertEquals(3.28084, evalPop("1 m>ft").doubleValue(), 0.01);
        assertEquals(190.289, evalPop("58 m>ft").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 in>cm").doubleValue(), 0.01);
        assertEquals(2.54, evalPop("1 in>cm").doubleValue(), 0.01);
        assertEquals(254, evalPop("100 in>cm").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 cm>in").doubleValue(), 0.01);
        assertEquals(0.393701, evalPop("1 cm>in").doubleValue(), 0.01);
        assertEquals(39.3701, evalPop("100 cm>in").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 mi>km").doubleValue(), 0.01);
        assertEquals(1.60934, evalPop("1 mi>km").doubleValue(), 0.01);
        assertEquals(141.622, evalPop("88 mi>km").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 km>mi").doubleValue(), 0.01);
        assertEquals(0.621371, evalPop("1 km>mi").doubleValue(), 0.01);
        assertEquals(54.6807, evalPop("88 km>mi").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 nm>mi").doubleValue(), 0.01);
        assertEquals(1.15078, evalPop("1 nm>mi").doubleValue(), 0.01);
        assertEquals(9.20624, evalPop("8 nm>mi").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 mi>nm").doubleValue(), 0.01);
        assertEquals(0.868976, evalPop("1 mi>nm").doubleValue(), 0.01);
        assertEquals(6.95181, evalPop("8 mi>nm").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 nm>km").doubleValue(), 0.01);
        assertEquals(1.852, evalPop("1 nm>km").doubleValue(), 0.01);
        assertEquals(14.816, evalPop("8 nm>km").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 km>nm").doubleValue(), 0.01);
        assertEquals(0.539957, evalPop("1 km>nm").doubleValue(), 0.01);
        assertEquals(4.31965, evalPop("8 km>nm").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 yd>m").doubleValue(), 0.01);
        assertEquals(0.9144, evalPop("1 yd>m").doubleValue(), 0.01);
        assertEquals(109.728, evalPop("120 yd>m").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 m>yd").doubleValue(), 0.01);
        assertEquals(1.09361, evalPop("1 m>yd").doubleValue(), 0.01);
        assertEquals(131.234, evalPop("120 m>yd").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 lb>kg").doubleValue(), 0.01);
        assertEquals(0.453592, evalPop("1 lb>kg").doubleValue(), 0.01);
        assertEquals(68.0389, evalPop("150 lb>kg").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 kg>lb").doubleValue(), 0.01);
        assertEquals(2.20462, evalPop("1 kg>lb").doubleValue(), 0.01);
        assertEquals(154.324, evalPop("70 kg>lb").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 oz>g").doubleValue(), 0.01);
        assertEquals(28.3495, evalPop("1 oz>g").doubleValue(), 0.01);
        assertEquals(368.544, evalPop("13 oz>g").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 g>oz").doubleValue(), 0.01);
        assertEquals(0.035274, evalPop("1 g>oz").doubleValue(), 0.01);
        assertEquals(3.31575, evalPop("94 g>oz").doubleValue(), 0.01);


        assertEquals(0, evalPop("0 dg>rd").doubleValue(), 0.01);
        assertEquals(0.0174533, evalPop("1 dg>rd").doubleValue(), 0.01);
        assertEquals(Math.PI * 2, evalPop("360 dg>rd").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 rd>dg").doubleValue(), 0.01);
        assertEquals(57.2958, evalPop("1 rd>dg").doubleValue(), 0.01);
        assertEquals(360, evalPop("2 pi * rd>dg").doubleValue(), 0.01);


        assertEquals(32, evalPop("0 c>f").doubleValue(), 0.01);
        assertEquals(33.8, evalPop("1 c>f").doubleValue(), 0.01);
        assertEquals(100.4, evalPop("38 c>f").doubleValue(), 0.01);
        assertEquals(14, evalPop("-10 c>f").doubleValue(), 0.01);

        assertEquals(-17.7778, evalPop("0 f>c").doubleValue(), 0.01);
        assertEquals(-17.2222, evalPop("1 f>c").doubleValue(), 0.01);
        assertEquals(54.4444, evalPop("130 f>c").doubleValue(), 0.01);
        assertEquals(-67.7778, evalPop("-90 f>c").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 w>hp").doubleValue(), 0.01);
        assertEquals(0.00134102, evalPop("1 w>hp").doubleValue(), 0.01);
        assertEquals(18.103798, evalPop("13500 w>hp").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 hp>w").doubleValue(), 0.01);
        assertEquals(745.7, evalPop("1 hp>w").doubleValue(), 0.01);
        assertEquals(89484, evalPop("120 hp>w").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 kc>j").doubleValue(), 0.01);
        assertEquals(4184, evalPop("1 kc>j").doubleValue(), 0.01);
        assertEquals(560656, evalPop("134 kc>j").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 j>kc").doubleValue(), 0.01);
        assertEquals(0.000239006, evalPop("1 j>kc").doubleValue(), 0.01);
        assertEquals(26.8479924, evalPop("112332 j>kc").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 b>pa").doubleValue(), 0.01);
        assertEquals(100000, evalPop("1 b>pa").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 pa>b").doubleValue(), 0.01);
        assertEquals(1e-5, evalPop("1 pa>b").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 t>pa").doubleValue(), 0.01);
        assertEquals(133.322, evalPop("1 t>pa").doubleValue(), 0.01);

        assertEquals(0, evalPop("0 pa>t").doubleValue(), 0.01);
        assertEquals(0.00750062, evalPop("1 pa>t").doubleValue(), 0.01);
    }

    @Test
    public void testFreemem() {
        int free = evalPop("freemem").intValue();
        evalPop("123 allot");
        evalPop("456 allot");
        evalPop("0 allot");
        assertEquals(free - 123 - 456, evalPop("freemem").intValue());
    }

    @Test
    public void testToNum() {
        assertEquals(123, evalPop("'123' >num").intValue());
        assertEquals(0, evalPop("'0' >num").intValue());
        assertEquals(-1.2, evalPop("'-1.2' >num").doubleValue(), 0.01);
    }

    @Test
    public void testAllot() {
        int here = evalPop("here").intValue();
        assertEquals(here, evalPop("787 allot").intValue());
        assertEquals(here + 787, evalPop("3 allot").intValue());
    }

    @Test
    public void testVar() {
        eval("var: a0");
        assertEquals(0, evalPop("a0 @").intValue());
        eval("var: a1");
        eval("var: a2");
        eval("42 a1 !");
        eval("5 a2 !");
        assertEquals(42 + 5, evalPop("a1 @ a2 @ +").intValue());
        eval("var: a1");
        eval("a1 inc");
        eval("a1 inc");
        assertEquals(2, evalPop("a1 @").intValue());
    }

    @Test
    public void testConst() {
        eval("12 val: a0");
        eval("13 val: a1");
        assertEquals(12, evalPop("a0").intValue());
        assertEquals(13, evalPop("a1").intValue());
    }

    @Test
    public void testOverride() {
        eval(": t1 1+ ;");
        eval(": t1 override t1 2 * ;");
        assertEquals(8, evalPop("3 t1").intValue());
    }

    @Test
    public void testPstackOverflow() throws Exception {
        for (int i = 0; i < 127; i++) {
            eval("frame.alloc");
        }
        try {
            eval("frame.alloc");
        } catch (Aborted e) {
            resetForth();
            assertEquals(e.getMessage(), "pstack overflow");
        }
    }

    @Test
    public void testPstackUnderflow() throws Exception {
        try {
            eval("frame.drop");
        } catch (Aborted e) {
            resetForth();
            assertEquals("pstack underflow", e.getMessage());
        }
    }

    @Test
    public void testCreate() {
        eval("'my-square' create ` dup , ` * , ` exit ,");
        assertEquals(9, evalPop("3 my-square").intValue());
    }

    @Test
    public void testLocalOrder() {
        eval(": tst1 -> a -> b a ;");
        eval(": tst2 -> a -> b b ;");
        assertEquals(8, evalPop("6 8 tst1").intValue());
        assertEquals(6, evalPop("6 8 tst2").intValue());
    }

    @Test
    public void testLocalVals() {
        eval("var: a 42 a !");
        eval("var: f 43 f !");
        assertEquals(42, evalPop("a @").intValue());
        assertEquals(43, evalPop("f @").intValue());
        eval(": add-loc -> a -> b a b + ;");
        assertEquals(15, evalPop("13 2 add-loc").intValue());
        eval(": mul-loc -> c -> d c d * ;");
        assertEquals(12, evalPop("3 4 mul-loc").intValue());
        eval(": sub-loc -> e -> f f e - ;");
        assertEquals(13, evalPop("17 4 sub-loc").intValue());
        eval(": comp -> f f 3 add-loc f mul-loc a @ sub-loc ;");
        assertEquals(130 - 42, evalPop("10 comp").intValue());
        assertEquals(42, evalPop("a @").intValue());
        assertEquals(43, evalPop("f @").intValue());
        assertEquals(Math.E, evalPop("e").doubleValue(), 0.01);
        assertNull(fcl.get("b"));
        assertNull(fcl.get("c"));
        assertNull(fcl.get("d"));
    }

    @Test
    public void testLocalBasic() {
        assertEquals(12, evalPop(": tst -> a a ; 12 tst").intValue());
    }

    @Test
    public void testLocalCleanup() {
        assertEquals(-95.625,
            evalPop(": tst -> a8 -> a7 -> a6 -> a5 -> a4 -> a3 -> a2 -> a1 " +
                    "a1 a2 + a3 a4 - a5 a6 * a7 a8 / + - * ; " +
                    "1 2 3 4 5 6 7 8 tst").doubleValue(), 0.01);
        assertNull(fcl.get("a1"));
        assertNull(fcl.get("a2"));
        assertNull(fcl.get("a3"));
        assertNull(fcl.get("a4"));
        assertNull(fcl.get("a5"));
        assertNull(fcl.get("a6"));
        assertNull(fcl.get("a7"));
        assertNull(fcl.get("a8"));
    }

    @Test
    public void testLocalVars() {
        assertEquals(12, evalPop(": tst => a a @ ; 12 tst").intValue());
        eval(": find ( n -- n )\n" +
                "0 => count -> s\n" +
                "1001 1 do\n" +
                "   i s /mod -> quotient -> remainder \n" +
                "   remainder 0 = if\n" +
                "       count inc\n" +
                "   then\n" +
                "loop\n" +
                "count @ ;");
        assertEquals(500, evalPop("2 find").intValue());
        assertEquals(33, evalPop("\n" +
                ": tst0\n" +
                "    ( 10 ) -> c\n" +
                "    3 -> a\n" +
                "    0 => d\n" +
                "    a 0 do\n" +
                "        i -> b ( 0, 1, 2 )\n" +
                "        b c * d @ + d !\n" +
                "    loop \n" +
                "    d @ ;\n" +
                "\n" +
                ": tst\n" +
                "    -> a ( 10 ) => b ( 3 ) \n" +
                "    a tst0 b @ + -> c c ;\n" +
                "\n" +
                "3 10 tst").intValue());
    }

    @Test
    public void testMaxNumberOfLocals() throws Exception {
        try {
            eval(": tst -> a1 -> a2 -> a3 -> a4 -> a5 -> a6 -> a7 -> a8 -> a9 ; ");
            fail("Expected too many local");
        } catch (Aborted e) {
            resetForth();
            assertEquals("Too many local variables", e.getMessage());
        }
    }

    @Test
    public void testIndependentLocals() throws Exception {
        for (int i = 0; i < 1024; i++) {
            eval(": tst" + i + " -> a ; ");
        }
    }

    @Test
    public void testLdpOverflow() throws IOException {
        eval(": tst0 -> a a ; ");
        for (int i = 1; i < 128; i++) {
            eval(": tst" + i + " -> a a tst" + (i-1) + " ;");
        }
        assertEquals(567, evalPop("567 tst126").intValue());
        try {
            eval(": tst-last -> a a tst126 ; 12 tst-last drop");
            fail("expected pstack overflow");
        } catch (Aborted e) {
            assertEquals("pstack overflow", e.getMessage());
            resetForth();
        }
    }

    @Test
    public void testLocalWithSameNames() throws Exception {
        try {
            evalGetStack(": tst -> a -> a a a ; 1 2");
            fail("expected abort");
        } catch (Aborted e) {
            assertEquals("local already exists", e.getMessage());
            resetForth();
        }
    }

    @Test
    public void testEarlyReturn() {
        eval(": sum1 0 10 0 do \n" +
                "i 9 = if unloop exit then\n" +
                "i +\n" +
                "loop ;");
        assertEquals(36, evalPop("sum1").intValue());
    }

    @Test
    public void testLocalsWithEarlyReturn() {
        eval(": tst -> a -> b " +
                "a b < if 42 exit -1 else 43 then ;");
        eval(": tst2 -> a " +
                "5 3 tst a + exit drop -100 ;");
        assertEquals(42, evalPop("5 3 tst").intValue());
        assertEquals(43, evalPop("3 5 tst").intValue());
        assertEquals(52, evalPop("10 tst2").intValue());
    }

    @Test
    public void testLotsOfLocals() {
        eval(": tst -> a1 -> a2 -> a3 -> a4 -> a5 -> a6 -> a7 -> a8 ;");
    }

    @Test
    public void testMaps() {
        assertEquals(0, evalPop("<map> size").intValue());
        assertEquals(1, evalPop("<map> dup 'key1' 'value1' put size").intValue());
        eval("<map> val: m");
        eval("m 'a' 1 put " +
                "m 2 'b' put " +
                "m true false put");
        assertEquals(3, evalPop("m size").intValue());
        assertEquals(1, evalPop("m 'a' at").intValue());
        assertEquals("b", evalPop("m 2 at").asStr().value());
        assertEquals("#[ 'x' 'y' ]#", evalPop("<map> dup [ 'x' 'y' ] add").toString());
        assertEquals(false, evalPop("m true at").boolValue());
        assertEquals(Nil.INSTANCE, evalPop("m 'nosuch' at"));
        assertEquals(0, evalPop("m clear m size").intValue());
    }

    @Test
    public void testList() {
        assertEquals(0, evalPop("<list> size").intValue());
        assertEquals(1, evalPop("<list> dup 'val1' add size").intValue());
        eval("<list> val: m");
        eval("m 'a' add " +
                "m 2 add " +
                "m true add");
        assertEquals(3, evalPop("m size").intValue());
        assertEquals("a", evalPop("m 0 at").asStr().value());
        assertEquals(2, evalPop("m 1 at").intValue());
        assertEquals(true, evalPop("m 2 at").boolValue());
        assertEquals(1, evalPop("m 2 index-of").intValue());
        assertEquals(0, evalPop("m clear m size").intValue());
        assertEquals(6, evalPop("[ 4 -1 3 ] sum").intValue());
        assertEquals(1+2+3+4+5, evalPop("1 5 .. sum").intValue());
    }

    @Test
    public void testListMinMax() {
        assertEquals(17, evalPop("[ 1 -2 3.5 -3 17 3 -1 ] maxl").intValue());
        assertEquals(-3, evalPop("[ 1 -2 3.5 -3 17 3 -1 ] minl").intValue());
        assertEquals(Nil.INSTANCE, evalPop("[ ] minl"));
        assertEquals(Nil.INSTANCE, evalPop("[ ] maxl"));
    }

    @Test
    public void testRanges() {
        assertEquals("[ 1 2 3 4 5 ]", evalPop(": tst 1 5 .. { } map ; tst").toString());
        assertEquals("[ 1 3 5 7 9 ]", evalPop(": tst 1 10 2 ... { } map ; tst").toString());
        assertEquals("[ 1 4 7 10 ]", evalPop(": tst 1 10 3 ... { } map ; tst").toString());
        assertEquals("[ 5 4 3 2 1 ]", evalPop(": tst 5 1 -1 ... { } map ; tst").toString());
        assertEquals("[ 10 6 2 ]", evalPop(": tst 10 1 -4 ... { } map ; tst").toString());
        assertEquals("[ 1 ]", evalPop(": tst 1 1 .. { } map ; tst").toString());
        assertEquals("[  ]", evalPop(": tst 1 0 .. { } map ; tst").toString());
        assertEquals("[  ]", evalPop(": tst 0 -1 .. { } map ; tst").toString());
        assertEquals("[ 0 1 ]", evalPop(": tst 0 1 .. { } map ; tst").toString());
        assertEquals("[ -1 0 ]", evalPop(": tst -1 0 .. { } map ; tst").toString());
        assertEquals("[ 1 4 9 16 25 ]", evalPop(": tst 1 5 .. { dup * } map ; tst").toString());
        assertEquals(55, evalPop(": tst 0 1 10 .. { + } each ; tst").intValue());
        assertEquals("[ 1 3 5 7 9 ]", evalPop(": tst 1 10 .. { odd? } filter ; tst").toString());
        assertEquals("[ 100 102 104 ]", evalPop(": tst 100 105 .. { even? } filter ; tst").toString());
    }

    @Test
    public void testTimes() {
        eval(": tst { 'a' . } 5 times ; tst");
        assertEquals("a\na\na\na\na\n", transcript());
        assertEquals("[ 1 1 1 1 1 ]", evalPop(": tst <list> { dup 1 add } 5 times ; tst").toString());
    }

    @Test
    public void testListLiteral() {
        assertEquals(0, evalPop("[ ] size").intValue());
        assertEquals(asList(new Num(1), new Num(2), new Num(3)), evalPop("[ 1 2 3 ]").value());
        assertEquals(asList(new Num(1), new Num(2), new Num(3)), evalPop(": tst [ 1 2 3 ] ; tst").value());
        assertEquals("[ 1 2 [ 3 ] [ 4 5 ] ]", evalPop("[ 1 2 [ 3 ] [ 4 5 ] ]").toString());
        assertEquals("[ 1 2 [ 3 ] [ 4 5 ] ]", evalPop(": tst [ 1 2 [ 3 ] [ 4 5 ] ] ; tst").toString());
        assertEquals("[ 1 2 [ #[ 'a' 1 ]# ] ]", evalPop("[ 1 2 [ #[ 'a' 1 ]# ] ]").toString());
        assertEquals("[ 1 2 3 4 ]", evalPop("[ 1 2 ] [ 3 4 ] concat").toString());
        assertEquals("[ 2 3 ]", evalPop("[ 1 2 3 4 ] 1 3 sublst").toString());
        assertEquals("[ 'a' 'c' ]", evalPop("[ 'a' 'b' 'c' ] dup 1 remove-at").toString());
        assertEquals("[ 'a' 'c' ]", evalPop("[ 'a' 'b' 'c' ] dup 'b' remove").toString());
        assertEquals(asList(1l, 2l, 3l, 4l), evalGetStack("[ 1 2 3 4 ] peel"));
        assertEquals(asList(1l, 2l, 3l, 4l), evalGetStack(": tst [ 1 2 3 4 ] peel ; tst"));
        assertEquals("[ 1 2 3 4 ]", evalPop("[ 1 2 3 4 ] peel list*").toString());
    }

    @Test
    public void testMapLiteral() throws Exception {
        assertEquals(0, evalPop("#[ ]# size").intValue());
        Map<Object,Object> expected = new LinkedHashMap<>();
        expected.put(new Str("b"), new Num(2));
        expected.put(new Str("a"), new Num(1));
        assertEquals(
                expected,
                (Map)evalPop("#[ 'a' 1 'b' 2 ]#").value());
        assertEquals(
                expected,
                (Map)evalPop(": tst #[ 'a' 1 'b' 2 ]# ; tst").value());
        assertEquals("#[ 'a' [ 1 2 ] 'b' #[ 'x' 'y' ]# ]#",
                evalPop("#[ 'b' #[ 'x' 'y' ]# 'a' [ 1 2 ] ]#").toString());
        assertEquals("#[ 'a' 1 ]#",
                evalPop("#[ 'a' 1 'b' 2 ]# dup 'b' remove").toString());
        assertEquals("[ 'b' 'a' ]",
                evalPop("#[ 'a' 1 'b' 2 ]# keys").toString());
        assertEquals("[ 2 1 ]",
                evalPop("#[ 'a' 1 'b' 2 ]# values").toString());
        assertEquals(asList("b", 2l, "a", 1l),
                evalGetStack("#[ 'a' 1 'b' 2 ]# peel#"));
        assertEquals("#[ 'b' 2 'a' 1 ]#",
                evalPop("'a' 1 'b' 2 map*").toString());
        assertEquals("#[ 'a' 1 'b' 2 ]#",
                evalPop("#[ 'a' 1 'b' 2 ]# peel# map*").toString());
        assertEquals("[ 'a' 1 ]",
                evalPop("#[ 'a' 1 'b' 2 ]# peel nip").toString());
        assertEquals("[ 'b' 2 ]",
                evalPop("#[ 'a' 1 'b' 2 ]# peel drop").toString());
    }

    @Test
    public void testNestedEach() {
        eval(": tst 'abc' { . 'def' { . } each } each ; tst");
        assertEquals(
                "a\n" +
                "d\n" +
                "e\n" +
                "f\n" +
                "b\n" +
                "d\n" +
                "e\n" +
                "f\n" +
                "c\n" +
                "d\n" +
                "e\n" +
                "f\n", transcript());
    }

    @Test
    public void testNestedEachWithLocals1() {
        eval(": tst [ [ 'a' 1 ] [ 'b' 2 ] [ 'c' 3 ] ] {  -> row row { . } each } each ; tst");
        assertEquals(
            "a\n" +
            "1\n" +
            "b\n" +
            "2\n" +
            "c\n" +
            "3\n", transcript());
    }

    @Test
    public void testNestedEachWithLocals2() {
        eval(": tst [ [ 'a' 1 ] [ 'b' 2 ] [ 'c' 3 ] ] { { -> col col . } each } each ; tst");
        assertEquals(
                "a\n" +
                        "1\n" +
                        "b\n" +
                        "2\n" +
                        "c\n" +
                        "3\n", transcript());
    }

    @Test
    public void testNestedEachWithLocals3() {
        eval(": tst [ [ 'a' 1 ] [ 'b' 2 ] [ 'c' 3 ] ] -> lst lst { -> row row { -> col col . } each } each 0 -> x 1 -> y ; tst");
        assertEquals(
                "a\n" +
                        "1\n" +
                        "b\n" +
                        "2\n" +
                        "c\n" +
                        "3\n", transcript());
    }

    @Test
    public void testMapValidations() throws Exception {
        try {
            eval("#[ 1 ]#");
            fail("expected abort");
        } catch (Aborted e) {
            assertEquals("expected even number of items for a map", e.getMessage());
            resetForth();
        }
        try {
            eval("1 map*");
            fail("expected abort");
        } catch (Aborted e) {
            assertEquals("expected even number of items for a map*", e.getMessage());
            resetForth();
        }
    }

    @Test
    public void testListArithmetic() {
        try {
            assertEquals("[ 3 6 9 ]", evalPop("[ 1 2 3 ] [ 1 2 3 ] +").toString());
            fail();
        } catch (TypeMismatched e) { }
        try {
            assertEquals("[ 3 6 9 ]", evalPop("[ 1 2 3 ] [ 1 2 3 ] -").toString());
            fail();
        } catch (TypeMismatched e) { }
        try {
            assertEquals("[ 3 6 9 ]", evalPop("[ 1 2 3 ] [ 1 2 3 ] *").toString());
            fail();
        } catch (TypeMismatched e) { }
        try {
            assertEquals("[ 3 6 9 ]", evalPop("[ 1 2 3 ] [ 1 2 3 ] /").toString());
            fail();
        } catch (TypeMismatched e) { }

        assertEquals("[ 3 6 9 ]", evalPop("3 [ 1 2 3 ] *").toString());
        assertEquals("[ 3 6 9 ]", evalPop("[ 1 2 3 ] 3 *").toString());
        assertEquals("[ 1 2 3 ]", evalPop("[ 3 6 9 ] 3 /").toString());
        assertEquals("[ 4 5 6 ]", evalPop("3 [ 1 2 3 ] +").toString());
        assertEquals("[ 4 5 6 ]", evalPop("[ 1 2 3 ] 3 +").toString());
        assertEquals("[ 1 2 3 ]", evalPop("[ 4 5 6 ] 3 -").toString());
        try {
            assertEquals("[ 2 1 0 ]", evalPop("3 [ 1 2 3 ] -").toString());
            fail();
        } catch (TypeMismatched e) { }
        try {
            assertEquals("[ 5 2 10 ]", evalPop("10 [ 2 5 1 ] /").toString());
            fail();
        } catch (TypeMismatched e) { }
    }

    @Test
    public void testStrArithmetic() {
        assertEquals("'ababab'", evalPop("3 'ab' *").toString());
        assertEquals("'ababab'", evalPop("'ab' 3 *").toString());
    }

    @Test
    public void testStrIter() {
        eval(": tst 'abcd' { . } each ; tst");
        assertEquals("a\nb\nc\nd\n", transcript());
        assertEquals(45, evalPop(": tst 0 '123456789' { >num + } each ; tst").intValue());
    }

    @Test
    public void testExec() {
        assertEquals(2, evalPop("1 ` 1+ exec").intValue());
        assertEquals(3, evalPop(": tst ['] 1+ exec ; 2 tst").intValue());
        assertEquals(6, evalPop(": tst ['] 1+ exec 3 + ; 2 tst").intValue());
    }

    @Test
    public void testQuotations() {
        assertEquals(2, evalPop(": tst 1 { 1+ } yield ; tst ").intValue());
        assertEquals(asList(101l, 100l), evalGetStack(": tst 100 { 1+ } keep ; tst"));
        assertEquals(asList(11l, 100l), evalGetStack(": tst 10 100 { 1+ } dip ; tst"));
        assertEquals(asList(11l, 9l), evalGetStack(": tst 10 { 1+ } { 1- } bi ; tst"));
        assertEquals(asList(21l, 29l), evalGetStack(": tst 20 30 { 1+ } { 1- } bi* ; tst"));
        assertEquals(asList(41l, 51l), evalGetStack(": tst 40 50 { 1+ } bi@ ; tst"));
    }

    @Test
    public void testQuotationsWithLocals() {
        eval(": tst\n" +
                " -> a -> b\n" +
                "b a { 1+ } { 1- } bi* \n" +
                ";\n"
        );
        assertEquals(asList(11l, 19l), evalGetStack("10 20 tst"));
        eval(": tst\n" +
                " -> a\n" +
                "{ a 1+ } yield\n" +
                ";\n"
        );
        assertEquals(31, evalPop("30 tst").intValue());
        eval(": tst\n" +
                " => a\n" +
                "{ a @ 1+ a ! } yield a @\n" +
                ";\n"
        );
        assertEquals(41, evalPop("40 tst").intValue());
        eval(": tst\n" +
                " => a\n" +
                "{ 3 -> b a @ 3 + a ! } yield a @\n" +
                ";\n"
        );
        assertEquals(43, evalPop("40 tst").intValue());

        eval(": tst\n" +
                "  -> q\n" +
                "nil => a\n" +
                "nil => b\n" +
                "1 10 1 ... { -> x x q yield -> y x y b ! a ! } each ;\n" +
                " : tst2 { dup * } tst ; tst2 \n"
        );

        assertEquals(1, evalPop(": wl 0 -> wx ;\n" +
                ": qt-calls-word-with-local\n" +
                "    1 -> x 0 => y\n" +
                "    { wl 1 y ! } 10 times y @ ; qt-calls-word-with-local\n").intValue());

        assertEquals(1, evalPop(": wl2 1 -> a { 2 -> b } ; : wl1 0 -> wx wl2 ;\n" +
                ": qt-calls-word-with-local\n" +
                "    1 -> x 0 => y\n" +
                "    { wl 1 y ! } 10 times y @ ; qt-calls-word-with-local\n").intValue());
    }

    @Test
    public void testQuotationsWithCollections() {
        eval("<list> dup 1 add dup 2 add dup 3 add dup 4 add dup 5 add val: ls");
        eval(": tst ls { . } each ; tst");
        assertEquals("1\n2\n3\n4\n5\n", transcript());
        assertEquals(15, evalPop(": tst ls 0 => sum { -> n sum @ n + sum ! } each sum @ ; tst").intValue());
        assertEquals(
                asList(new Num(1), new Num(4), new Num(9), new Num(16), new Num(25)),
                (List)evalPop(": tst ls { dup * } map ; tst").value());
        assertEquals(
                asList(new Num(2), new Num(4)),
                (List)evalPop(": tst ls { 2 /mod drop 0 = } filter ; tst").value());
        assertEquals(
                asList(new Num(2), new Num(4)),
                (List)evalPop(": tst ls { 2 /mod -> q -> r r 0 = } filter ; tst").value());

        eval("<map> val: m " +
                "m 'a' 1 put " +
                "m 'b' 2 put " +
                "m 'c' 3 put " +
                "m 'd' 4 put "
        );
        assertEquals(10, evalPop(": tst 0 => sum m { -> e sum @ e 2nd + sum ! } each sum @ ; tst").intValue());
        assertEquals(
                "[ [ 'b' 2 ] [ 'd' 4 ] ]",
                evalPop(": tst m { -> e e 1st 'b' = e 1st 'd' = or } filter ; tst").toString());
    }

    @Test(expected = Aborted.class)
    public void testAbort() {
        eval("'test' abort");
    }

    @Test
    public void testRnd() {
        double rnd1 = evalPop("rnd").doubleValue();
        double rnd2 = evalPop("rnd").doubleValue();
        assertTrue(rnd1 >= 0 && rnd1 <= 1);
        assertTrue(rnd2 >= 0 && rnd2 <= 1);
        assertTrue(rnd1 != rnd2);
    }

    @Test
    public void testSqe() {
        eval(": sqe -> c -> b -> a\n" +
                "    b neg b b * 4 a * c * - sqrt - 2 a * /\n" +
                "    b neg b b * 4 a * c * - sqrt + 2 a * /\n" +
                ";\n");
        assertEquals(asList(-1.0, 1.5), evalGetStack("2 -1 -3 sqe"));
        assertEquals(asList(-3.0, 0.5), evalGetStack("2 5 -3 sqe"));
        assertEquals(asList(Double.NaN, Double.NaN), evalGetStack("2 -9 43 sqe")); // complex
    }

    @Test
    public void testCalendar() {
        Calendar cal = Calendar.getInstance();
        assertEquals(cal.get(Calendar.YEAR), evalPop("year").intValue());
        assertEquals(cal.get(Calendar.MONTH) + 1, evalPop("month").intValue());
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), evalPop("day").intValue());
    }

    @Test
    public void testCompileOnlyWords() {
        fcl.compileTmpAndEval("1");
        assertEquals(1, fcl.pop().intValue());
        fcl.compileTmpAndEval("'hello'");
        assertEquals(new Str("hello"), fcl.pop().asStr());
        fcl.compileTmpAndEval("3 2 +");
        assertEquals(5, fcl.pop().intValue());
        fcl.compileTmpAndEval("1 2 < if true else false then");
        assertEquals(true, fcl.pop().boolValue());
        fcl.compileTmpAndEval("1 2 > if true else false then");
        assertEquals(false, fcl.pop().boolValue());
        fcl.compileTmpAndEval("0 11 0 do i + loop");
        assertEquals(55, fcl.pop().intValue());
        fcl.compileTmpAndEval("10 begin 1- dup 0 <= until");
        assertEquals(0, fcl.pop().intValue());
        fcl.compileTmpAndEval("{ 1 2 + } yield");
        assertEquals(3, fcl.pop().intValue());
        fcl.compileTmpAndEval("0 1 10 .. { + } each");
        assertEquals(55, fcl.pop().intValue());
        fcl.compileTmpAndEval("0 1 10 .. { dup * } map { + } each");
        assertEquals(385, fcl.pop().intValue());
        fcl.compileTmpAndEval("0 => sum 1 10 .. { -> n n n * } map { -> m sum @ m + sum ! } each sum @");
        assertEquals(385, fcl.pop().intValue());
        fcl.compileTmpAndEval("0 1 10 ..  { 1 and 0 != if false else true then } filter { + } each");
        assertEquals(30, fcl.pop().intValue());
    }

    @Test
    public void testHist() {
        assertEquals("#[ 'a' 2 'b' 3 'c' 1 ]#", evalPop("'ababbc' hist").toString());
        assertEquals("#[  ]#", evalPop("'' hist").toString());
        assertEquals("#[  ]#", evalPop("12 hist").toString());
    }

    @Test
    public void testEval() {
        assertEquals(3, evalPop(" '1 2 +' eval").intValue());
        assertEquals(42, evalPop(" ': tst 42 ; tst' eval").intValue());
    }

    @Test
    public void testUnsetDefer() throws Exception {
        eval("defer: xx");
        try {
            eval("xx");
            fail("Expected to fail");
        } catch (Aborted e) {
            assertEquals("Uninitialized deferred word", e.getMessage());
            resetForth();
        }
        eval("defer: xx");
        try {
            eval(": tst xx ; tst");
            fail("Expected to fail");
        } catch (Aborted e) {
            assertEquals("Uninitialized deferred word", e.getMessage());
            resetForth();
        }
    }

    @Test
    public void testDefer() { // only works for colon defs and not for primitives
        eval("defer: d1");
        eval("defer: d2");
        eval(": tst d1 d2 + ;");
        eval(": i1 11 ;");
        eval(": i2 22 ;");
        eval(": i3 42 ;");
        eval("` i1 is: d1");
        eval("` i2 is: d2");
        assertEquals(11, evalPop("d1").intValue());
        assertEquals(22, evalPop("d2").intValue());
        assertEquals(33, evalPop("tst").intValue());
        eval("` i3 is: d1");
        assertEquals(42, evalPop("d1").intValue());
        assertEquals(42+22, evalPop("tst").intValue());
    }

    @Test
    public void testHttpHeaders() {
        assertEquals(
                "#[ 'headers' #[ 'Content-Type' 'application/json' ]# 'content' #[ 'a' 1 ]# ]#",
                evalPop("#[ 'a' 1 ]# +json-type").toString());
        assertEquals(
                "#[ 'content' #[ 'a' 1 ]# 'headers' #[ 'Content-Type' 'application/json' ]# ]#",
                evalPop("#[ 'headers' #[ 'Content-Type' 'text/plain' ]# 'content' #[ 'a' 1 ]# ]# +json-type").toString());
    }

    @Test
    public void testSymbols() {
        assertEquals(":my-symbol", evalPop(":my-symbol").toString());
        assertEquals(true, evalPop(":my-symbol :my-symbol =").boolValue());
        assertEquals(false, evalPop(":my-symbol :my-symbol !=").boolValue());
        assertEquals(false, evalPop(":my-symbol :my-symbol2 =").boolValue());
        assertEquals(true, evalPop(":my-symbol :my-symbol2 !=").boolValue());
        assertEquals(true, evalPop(": tst :my-symbol :my-symbol = ; tst").boolValue());
    }

    @Test
    public void testPredicateWords() {
        eval(": octal? ( token -- ) 0 at '&' = ;\n" +
                ": lit-oct ( token -- n ) immediate match: octal?\n" +
                "   1 over size substr 8 swap :java.lang.Long/parseLong/si jvm-call-static\n" +
                "   interpret? not if ['] lit , , then ;\n");
        assertEquals(177, evalPop("&261").intValue());
        assertEquals(169, evalPop(": tst &261 &10 - ; tst ").intValue());
    }

    private String transcript() {
        return transcript.content();
    }

    private List<Object> evalGetStack(String script) {
        fcl.eval(script);
        List<Object> result = new ArrayList<>();
        int size = fcl.stackSize();
        for (int i = 0; i < size; i++) {
            result.add(fcl.pop().value());
        }
        Collections.reverse(result);
        fcl.eval("clean");
        return result;
    }

    private Obj evalPop(String script) {
        eval(script);
        return fcl.pop();
    }

    private void eval(String script) {
        fcl.eval(script);
    }
}
