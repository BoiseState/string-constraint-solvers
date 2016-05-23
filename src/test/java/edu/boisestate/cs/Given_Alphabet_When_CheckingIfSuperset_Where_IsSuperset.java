package edu.boisestate.cs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class Given_Alphabet_When_CheckingIfSuperset_Where_IsSuperset {

    private boolean isSuperset;

    @Before
    public void setup() {

        // *** arrange ***
        // initialize minimal alphabet declaration
        String minAlphabetDeclaration = "A-D";

        // create alphabet
        Alphabet alphabet = new Alphabet("A-D");


        // *** act ***
        this.isSuperset = alphabet.isSuperset(minAlphabetDeclaration);
    }

    @Test
    public void it_should_return_true() {

        // *** assert ***
        assertTrue(this.isSuperset);
    }
}
