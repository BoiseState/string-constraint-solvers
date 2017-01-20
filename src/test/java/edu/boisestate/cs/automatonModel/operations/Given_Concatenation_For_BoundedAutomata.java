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

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_Concatenation_For_BoundedAutomata {

    @Parameter(value = 4)
    public Automaton argAutomaton;
    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 3)
    public Automaton baseAutomaton;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 2)
    public int expectedModelCount;
    private Automaton concatAutomaton;

    @Parameters(name = "{index}: <{0} Automaton>.concat(<{1}>) - Expected" +
                       " MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet 
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 2;

        // create automata
        Automaton concrete = BasicAutomata.makeString("AB");
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                         .repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A'))
                                     .concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);

        // bound automata
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                          .repeat(0, initialBoundLength);
        concrete = concrete.intersection(bounding);
        uniform = uniform.intersection(bounding);
        nonUniform = nonUniform.intersection(bounding);
        concrete.determinize();
        concrete.minimize();
        uniform.determinize();
        uniform.minimize();
        nonUniform.determinize();
        nonUniform.minimize();

        return Arrays.asList(new Object[][]{
                {"Concrete base", "Concrete arg", 1, concrete, concrete},
                {"Concrete base", "Uniform arg", 21, concrete, uniform},
                {"Concrete base", "Non-uniform arg", 8, concrete, nonUniform},
                {"Uniform base", "Concrete arg", 21, uniform, concrete},
                {"Uniform base", "Uniform arg", 341, uniform, uniform},
                {"Uniform base", "Non-uniform arg", 148, uniform, nonUniform},
                {"Non-uniform base", "Concrete arg", 8, nonUniform, concrete},
                {"Non-uniform base", "Uniform arg", 148, nonUniform, uniform},
                {"Non-uniform base",
                 "Non-uniform arg",
                 60,
                 nonUniform,
                 nonUniform}
        });
    }

    @Before
    public void setup() {
        // *** arrange ***
        Automaton.setMinimizeAlways(true);

        // *** act ***
        this.concatAutomaton =
                this.baseAutomaton.concatenate(this.argAutomaton);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.concatAutomaton)
                                           .intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
