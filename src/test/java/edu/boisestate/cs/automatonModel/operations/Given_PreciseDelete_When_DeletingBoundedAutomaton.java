package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.util.DotToGraph;
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
public class Given_PreciseDelete_When_DeletingBoundedAutomaton {

    @Parameter(value = 2)
    public Automaton automaton;
    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 4)
    public int end;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 3)
    public int start;
    private Automaton deletedAutomaton;


    @Parameters(name = "{index}: [{0} Automaton].delete({3}, {4}) - Expected" +
                       " MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automata
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                          .repeat(0, initialBoundLength);
        Automaton known = BasicAutomata.makeString("ABC");
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                         .repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A'))
                                     .concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);

        // bound automata
        known = known.intersection(bounding);
        uniform = uniform.intersection(bounding);
        nonUniform = nonUniform.intersection(bounding);
        known.determinize();
        known.minimize();
        uniform.determinize();
        uniform.minimize();
        nonUniform.determinize();
        nonUniform.minimize();

        return Arrays.asList(new Object[][]{
                {"Known", 1, known, 0, 0},
                {"Known", 1, known, 0, 1},
                {"Known", 1, known, 0, 2},
                {"Known", 1, known, 0, 3},
                {"Known", 1, known, 1, 1},
                {"Known", 1, known, 1, 2},
                {"Known", 1, known, 1, 3},
                {"Known", 1, known, 2, 2},
                {"Known", 1, known, 2, 3},
                {"Known", 1, known, 3, 3},
                {"Unknown Uniform", 85, uniform, 0, 0},
                {"Unknown Uniform", 21, uniform, 0, 1},
                {"Unknown Uniform", 5, uniform, 0, 2},
                {"Unknown Uniform", 1, uniform, 0, 3},
                {"Unknown Uniform", 84, uniform, 1, 1},
                {"Unknown Uniform", 20, uniform, 1, 2},
                {"Unknown Uniform", 4, uniform, 1, 3},
                {"Unknown Uniform", 80, uniform, 2, 2},
                {"Unknown Uniform", 16, uniform, 2, 3},
                {"Unknown Uniform", 64, uniform, 3, 3},
                {"Unknown Non-uniform", 45, nonUniform, 0, 0},
                {"Unknown Non-uniform", 45, nonUniform, 0, 1},
                {"Unknown Non-uniform", 5, nonUniform, 0, 2},
                {"Unknown Non-uniform", 1, nonUniform, 0, 3},
                {"Unknown Non-uniform", 45, nonUniform, 1, 1},
                {"Unknown Non-uniform", 20, nonUniform, 1, 2},
                {"Unknown Non-uniform", 4, nonUniform, 1, 3},
                {"Unknown Non-uniform", 44, nonUniform, 2, 2},
                {"Unknown Non-uniform", 7, nonUniform, 2, 3},
                {"Unknown Non-uniform", 37, nonUniform, 3, 3}
        });
    }

    @Before
    public void setup() {

        // *** arrange ***
        PreciseDelete delete = new PreciseDelete(this.start, this.end);

        // *** act ***
        this.deletedAutomaton = delete.op(this.automaton);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.deletedAutomaton)
                                           .intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
