package edu.boisestate.cs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_Alphabet_When_DeclarationIsParsed {

    @Parameter // first data value (0) is default
    public String declaration;
    @Parameter(value = 1)
    public Character[] expectedSymbols;
    private Alphabet alphabet;

    @Parameters(name = "{index}: Alphabet Declaration \"{0}\"")
    public static Iterable<Object[]> data() {
        // index 0 is declaration
        // index 1 is array of all expected characters
        return Arrays.asList(new Object[][]{
                // normal declaration
                {"A-D", new Character[]{'A', 'B', 'C', 'D'}},
                // contains comma
                {" -\",,,0-2",
                 new Character[]{' ', '!', '"', ',', '0', '1', '2'}},
                // contains comma as from part of range
                {" -\",,-.,0-2",
                 new Character[]{' ', '!', '"', ',', '-', '.', '0', '1', '2'}},
                // contains comma as to part of range
                {" -\",*-,,0-2",
                 new Character[]{' ', '!', '"', '*', '+', ',', '0', '1', '2'}},
                // ends with comma
                {" -\",,", new Character[] { ' ', '!', '"', ',' }},
                // starts with comma
                {",,0-2", new Character[]{',', '0', '1', '2'}}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.alphabet = new Alphabet(this.declaration);
    }

    @Test
    public void it_should_have_the_correct_symbol_set() {
        // *** assert ***
        Set<Character> symbolSet = this.alphabet.getSymbolSet();
        assertThat(symbolSet, containsInAnyOrder(this.expectedSymbols));
    }
}
