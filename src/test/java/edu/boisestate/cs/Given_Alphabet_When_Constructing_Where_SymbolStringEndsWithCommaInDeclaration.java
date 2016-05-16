package edu.boisestate.cs;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class Given_Alphabet_When_Constructing_Where_SymbolStringEndsWithCommaInDeclaration {

    private Alphabet alphabet;

    @Before
    public void setup() {

        // *** arrange ***
        String symbolString = " -\",,";

        // *** act ***
        this.alphabet = new Alphabet(symbolString);
    }

    @Test
    public void it_should_contain_the_correct_symbol_set() {

        // *** assert ***
        Set<Character> symbolSet = this.alphabet.getSymbolSet();
        Character[] symbols = new Character[] { ' ', '!', '"', ',' };
        assertThat(symbolSet, containsInAnyOrder(symbols));
    }
}
