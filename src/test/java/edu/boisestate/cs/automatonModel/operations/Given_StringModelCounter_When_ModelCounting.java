package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
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
public class Given_StringModelCounter_When_ModelCounting {

    @Parameter(value = 2)
    public Automaton automaton;
    @Parameter(value = 1)
    public int boundingLength;
    @Parameter(value = 0) // first data value (0) is default
    public int expectedModelCount;
    private BigInteger modelCount;

    @Parameters(name = "{index}: Expected MC {0}, Bounding Length {1}, Automaton {2}")
    public static Iterable<Object[]> data() {

        // index 0 is automaton
        // index 1 is the expected model count
        // index 2 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {0, -1, BasicAutomata.makeEmpty()},
                {0, 0, BasicAutomata.makeEmpty()},
                {1, -1, BasicAutomata.makeEmptyString()},
                {1, 0, BasicAutomata.makeEmptyString()},
                {4, -1, BasicAutomata.makeCharRange('A', 'D')},
                {4, 1, BasicAutomata.makeCharRange('A', 'D')},
                {21, -1, BasicAutomata.makeCharRange('A', 'D').repeat(0, 2)},
                {21, 2, BasicAutomata.makeCharRange('A', 'D').repeat()}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        if(this.boundingLength < 0) {
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
