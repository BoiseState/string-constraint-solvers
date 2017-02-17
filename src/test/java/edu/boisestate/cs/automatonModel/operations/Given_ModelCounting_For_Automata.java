package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigInteger;
import java.util.Arrays;

import static edu.boisestate.cs.automatonModel.operations.StringModelCounter
        .ModelCount;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_ModelCounting_For_Automata {

    @Parameter(value = 3)
    public Automaton automaton;
    @Parameter(value = 1)
    public int boundingLength;
    @Parameter(value = 2)
    public String description;
    @Parameter(value = 0) // first data value (0) is default
    public int expectedModelCount;
    private BigInteger modelCount;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: Expected MC {0}, Bounding Length {1}, " +
                       "Automaton <{2}>")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automata
        Automaton empty = BasicAutomata.makeEmpty();
        Automaton emptyString = BasicAutomata.makeEmptyString();
        Automaton concrete = BasicAutomata.makeString("ABC");
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                         .repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A'))
                                     .concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);

        // determinize and minimize automata
        concrete.determinize();
        concrete.minimize();
        uniform.determinize();
        uniform.minimize();
        nonUniform.determinize();
        nonUniform.minimize();

        // bound automata
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                          .repeat(0, initialBoundLength);
        Automaton boundUniform = uniform.intersection(bounding);
        Automaton boundNonUniform = nonUniform.intersection(bounding);
        boundUniform.determinize();
        boundUniform.minimize();
        boundNonUniform.determinize();
        boundNonUniform.minimize();

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {0, initialBoundLength, "Empty", empty},
                {0, -1, "Empty", empty},
                {1, initialBoundLength, "Empty String", emptyString},
                {1, -1, "Empty String", emptyString},
                {1, initialBoundLength, "Concrete", concrete},
                {1, -1, "Concrete", concrete},
                {85, initialBoundLength, "Uniform", uniform},
                {85, -1, "Uniform", boundUniform},
                {45, initialBoundLength, "Non-uniform", nonUniform},
                {45, -1, "Non-uniform", boundNonUniform}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        if (this.boundingLength < 0) {
            this.modelCount = ModelCount(this.automaton);
        } else {
            this.modelCount = ModelCount(this.automaton,
                                         this.boundingLength);
        }
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** assert ***
        assertThat(this.modelCount.intValue(),
                   is(equalTo(this.expectedModelCount)));
    }
}
