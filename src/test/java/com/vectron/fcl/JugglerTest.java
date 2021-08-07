package com.vectron.fcl;

import com.vectron.fcl.types.Num;
import com.vectron.fcl.types.Obj;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.vectron.fcl.types.Num.ONE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JugglerTest {
    private final Set<String> excluded = new HashSet<>();

    @Test
    public void testEmpty() {
        assertTrue(solve(emptyList(), emptyList()).isEmpty());
    }

    @Test
    public void testSame() {
        assertTrue(solve(asList(ONE), asList(ONE)).isEmpty());
    }

    @Test
    public void testSingleDrop() {
        assertSolution("drop", "1", "");
    }

    @Test
    public void testSingleDup() {
        assertSolution("dup", "0", "0 0");
    }

    @Test
    public void testSingleSwap() {
        assertSolution("swap", "0 1", "1 0");
    }

    @Test
    public void testSingle2Swap() {
        assertSolution("2swap", "0 1 2 3", "2 3 0 1");
    }

    @Test
    public void testSingleOver() {
        assertSolution("over", "0 1", "0 1 0");
    }

    @Test
    public void testSingle2Over() {
        assertSolution("2over", "0 1 2 3", "0 1 2 3 0 1");
    }

    @Test
    public void testSingleNip() {
        assertSolution("nip", "0 1", "1");
    }

    @Test
    public void testSingleTuck() {
        assertSolution("tuck", "0 1", "1 0 1");
    }

    @Test
    public void testSingleRot() {
        assertSolution("rot", "0 1 2", "1 2 0");
    }

    @Test
    public void testSingle2Rot() {
        assertSolution("2rot", "0 1 2 3 4 5", "2 3 4 5 0 1");
    }

    @Test
    public void testSingle2mRot() {
        assertSolution("-2rot", "0 1 2 3 4 5", "4 5 0 1 2 3");
    }

    @Test
    public void testSingleMRot() {
        assertSolution("-rot", "0 1 2", "2 0 1");
    }

    @Test
    public void testSingle2dup() {
        assertSolution("2dup", "0 1", "0 1 0 1");
    }

    @Test
    public void testReverse3() {
        assertSolution("swap rot", "0 1 2", "2 1 0");
    }

    @Test
    public void testReverse4() {
        assertSolution("swap 2swap swap", "0 1 2 3", "3 2 1 0");
        excluded.add("2swap");
        assertSolution("over tuck -2rot 2drop swap", "0 1 2 3", "3 2 1 0");
        excluded.add("-2rot");
        assertSolution("swap 2over swap 2rot 2drop", "0 1 2 3", "3 2 1 0");
        excluded.add("2over");
        assertSolution("rot >r -rot r> rot", "0 1 2 3", "3 2 1 0");
    }

    @Test
    public void testReverse5() {
        assertSolution("over 2swap 2rot -rot nip", "0 1 2 3 4", "4 3 2 1 0");
    }

    @Test
    public void testOverOver() {
        excluded.add("2dup");
        assertSolution("over over", "0 1", "0 1 0 1");
    }

    @Test
    public void testSingle2drop() {
        assertSolution("2drop", "0 1", "");
    }

    @Test
    public void testComplex1() {
        assertSolution("drop over swap 2swap", "0 1 2 3", "1 2 0 1");
        excluded.add("2swap");
        assertSolution("drop over >r rot r>", "0 1 2 3", "1 2 0 1");
    }

    @Test
    public void testComplex2() {
        assertSolution("drop tuck 2swap", "0 1 2 3", "1 2 0 2");
        excluded.add("2swap");
        assertSolution("drop rot over", "0 1 2 3", "1 2 0 2");
    }

    @Test
    public void testComplex3() {
        assertSolution("drop rot dup", "0 1 2 3", "1 2 0 0");
    }

    @Test
    public void testComplex4() {
        assertSolution("swap 2over drop 2swap nip", "0 1 2 3", "0 2 0 3");
        excluded.add("2over");
        assertSolution("rot drop >r over r>", "0 1 2 3", "0 2 0 3");
    }

    @Test
    public void testComplex5() {
        assertSolution("drop rot drop -rot", "0 1 2 3 4", "3 0 2");
    }

    @Test
    public void testComplex6() {
        assertSolution("drop 2drop nip over swap", "0 1 2 3 4 5", "0 0 2");
    }

    @Test
    public void testComplex7() {
        assertSolution("drop 2drop nip 2dup rot", "0 1 2 3 4 5", "0 0 2 2");
    }

    @Test
    public void testComplex8() {
        assertSolution("drop 2drop swap -rot dup", "0 1 2 3 4 5", "1 0 2 2");
    }

    @Test
    public void testNoSolution1() {
        assertSolution(null, "0 1 2 3 4 5", "0 0 2 1");
    }

    private List<String> solve(List<Obj> input, List<Obj> output) {
        return Juggler.solve(input, output, excluded, 5);
    }

    private void assertSolution(String expected, String input, String output) {
        if (expected == null)
            assertEquals(null, solve(parse(input), parse(output)));
        else
            assertEquals(Arrays.asList(expected.split(" ")), solve(parse(input), parse(output)));
    }

    private List<Obj> parse(String str) {
        List<Obj> result = new ArrayList<>();
        if (str.equals("")) return result;
        for (String each : str.split(" "))
            result.add(Num.parse(each));
        return result;
    }
}