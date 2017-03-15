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

import static edu.boisestate.cs.automatonModel.operations
        .AutomatonOperationTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_PreciseInsertion_For_UnboundedAutomata {

    @Parameter(value = 3)
    public Automaton baseAutomaton;
    @Parameter(value = 5)
    public Automaton argAutomaton;
    @Parameter(value = 6)
    public int baseLength;
    @Parameter(value = 7)
    public int argLength;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 2)
    public int expectedModelCount;
    @Parameter(value = 4)
    public int offset;
    private Automaton resultAutomaton;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.insert({4}, <{1} Automaton>) - Expected" +
                       " MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");

        // get Automata
        Automaton empty = BasicAutomata.makeEmpty();
        Automaton emptyString = BasicAutomata.makeEmptyString();
        Automaton concrete = getConcreteAutomaton(alphabet, "ABC");
        Automaton uniform = getUniformUnboundedAutomaton(alphabet);
        Automaton nonUniform = getNonUniformUnboundedAutomaton(alphabet);

        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", 0, empty, 0, empty, 0, 0},
                {"Empty", "Empty", 0, empty, 1, empty, 0, 0},
                {"Empty", "Empty", 0, empty, 2, empty, 0, 0},
                {"Empty", "Empty", 0, empty, 3, empty, 0, 0},
                {"Empty", "Empty String", 0, empty, 0, emptyString, 0, 0},
                {"Empty", "Empty String", 0, empty, 1, emptyString, 0, 0},
                {"Empty", "Empty String", 0, empty, 2, emptyString, 0, 0},
                {"Empty", "Empty String", 0, empty, 3, emptyString, 0, 0},
                {"Empty", "Concrete", 0, empty, 0, concrete, 0, 3},
                {"Empty", "Concrete", 0, empty, 1, concrete, 0, 3},
                {"Empty", "Concrete", 0, empty, 2, concrete, 0, 3},
                {"Empty", "Concrete", 0, empty, 3, concrete, 0, 3},
                {"Empty", "Uniform", 0, empty, 0, uniform, 0, 3},
                {"Empty", "Uniform", 0, empty, 1, uniform, 0, 3},
                {"Empty", "Uniform", 0, empty, 2, uniform, 0, 3},
                {"Empty", "Uniform", 0, empty, 3, uniform, 0, 3},
                {"Empty", "Non-Uniform", 0, empty, 1, nonUniform, 0, 3},
                {"Empty", "Non-Uniform", 0, empty, 2, nonUniform, 0, 3},
                {"Empty", "Non-Uniform", 0, empty, 0, nonUniform, 0, 3},
                {"Empty", "Non-Uniform", 0, empty, 3, nonUniform, 0, 3},
                {"Empty String", "Empty", 0, emptyString, 0, empty, 0, 0},
                {"Empty String", "Empty", 0, emptyString, 1, empty, 0, 0},
                {"Empty String", "Empty", 0, emptyString, 2, empty, 0, 0},
                {"Empty String", "Empty", 0, emptyString, 3, empty, 0, 0},
                {"Empty String", "Empty String", 1, emptyString, 0, emptyString, 0, 0},
                {"Empty String", "Empty String", 0, emptyString, 1, emptyString, 0, 0},
                {"Empty String", "Empty String", 0, emptyString, 2, emptyString, 0, 0},
                {"Empty String", "Empty String", 0, emptyString, 3, emptyString, 0, 0},
                {"Empty String", "Concrete", 1, emptyString, 0, concrete, 0, 3},
                {"Empty String", "Concrete", 0, emptyString, 1, concrete, 0, 3},
                {"Empty String", "Concrete", 0, emptyString, 2, concrete, 0, 3},
                {"Empty String", "Concrete", 0, emptyString, 3, concrete, 0, 3},
                {"Empty String", "Uniform", 85, emptyString, 0, uniform, 0, 3},
                {"Empty String", "Uniform", 0, emptyString, 1, uniform, 0, 3},
                {"Empty String", "Uniform", 0, emptyString, 2, uniform, 0, 3},
                {"Empty String", "Uniform", 0, emptyString, 3, uniform, 0, 3},
                {"Empty String", "Non-Uniform", 45, emptyString, 0, nonUniform, 0, 3},
                {"Empty String", "Non-Uniform", 0, emptyString, 1, nonUniform, 0, 3},
                {"Empty String", "Non-Uniform", 0, emptyString, 2, nonUniform, 0, 3},
                {"Empty String", "Non-Uniform", 0, emptyString, 3, nonUniform, 0, 3},
                {"Concrete", "Empty", 0, concrete, 0, empty, 3, 0},
                {"Concrete", "Empty", 0, concrete, 1, empty, 3, 0},
                {"Concrete", "Empty", 0, concrete, 2, empty, 3, 0},
                {"Concrete", "Empty", 0, concrete, 3, empty, 3, 0},
                {"Concrete", "Empty String", 1, concrete, 0, emptyString, 3, 0},
                {"Concrete", "Empty String", 1, concrete, 1, emptyString, 3, 0},
                {"Concrete", "Empty String", 1, concrete, 2, emptyString, 3, 0},
                {"Concrete", "Empty String", 1, concrete, 3, emptyString, 3, 0},
                {"Concrete", "Concrete", 1, concrete, 0, concrete, 3, 3},
                {"Concrete", "Concrete", 1, concrete, 1, concrete, 3, 3},
                {"Concrete", "Concrete", 1, concrete, 2, concrete, 3, 3},
                {"Concrete", "Concrete", 1, concrete, 3, concrete, 3, 3},
                {"Concrete", "Uniform", 85, concrete, 0, uniform, 3, 3},
                {"Concrete", "Uniform", 85, concrete, 1, uniform, 3, 3},
                {"Concrete", "Uniform", 85, concrete, 2, uniform, 3, 3},
                {"Concrete", "Uniform", 85, concrete, 3, uniform, 3, 3},
                {"Concrete", "Non-Uniform", 45, concrete, 0, nonUniform, 3, 3},
                {"Concrete", "Non-Uniform", 45, concrete, 1, nonUniform, 3, 3},
                {"Concrete", "Non-Uniform", 45, concrete, 2, nonUniform, 3, 3},
                {"Concrete", "Non-Uniform", 45, concrete, 3, nonUniform, 3, 3},
                {"Uniform", "Empty", 0, uniform, 0, empty, 3, 0},
                {"Uniform", "Empty", 0, uniform, 1, empty, 3, 0},
                {"Uniform", "Empty", 0, uniform, 2, empty, 3, 0},
                {"Uniform", "Empty", 0, uniform, 3, empty, 3, 0},
                {"Uniform", "Empty String", 85, uniform, 0, emptyString, 3, 0},
                {"Uniform", "Empty String", 84, uniform, 1, emptyString, 3, 0},
                {"Uniform", "Empty String", 80, uniform, 2, emptyString, 3, 0},
                {"Uniform", "Empty String", 64, uniform, 3, emptyString, 3, 0},
                {"Uniform", "Concrete", 85, uniform, 0, concrete, 3, 3},
                {"Uniform", "Concrete", 84, uniform, 1, concrete, 3, 3},
                {"Uniform", "Concrete", 80, uniform, 2, concrete, 3, 3},
                {"Uniform", "Concrete", 64, uniform, 3, concrete, 3, 3},
                {"Uniform", "Uniform", 5461, uniform, 0, uniform, 3, 3},
                {"Uniform", "Uniform", 5460, uniform, 1, uniform, 3, 3},
                {"Uniform", "Uniform", 5456, uniform, 2, uniform, 3, 3},
                {"Uniform", "Uniform", 5440, uniform, 3, uniform, 3, 3},
                {"Uniform", "Non-Uniform", 4368, uniform, 0, nonUniform, 3, 3},
                {"Uniform", "Non-Uniform", 4004, uniform, 1, nonUniform, 3, 3},
                {"Uniform", "Non-Uniform", 3520, uniform, 2, nonUniform, 3, 3},
                {"Uniform", "Non-Uniform", 2880, uniform, 3, nonUniform, 3, 3},
                {"Non-Uniform", "Empty", 0, nonUniform, 0, empty, 3, 0},
                {"Non-Uniform", "Empty", 0, nonUniform, 1, empty, 3, 0},
                {"Non-Uniform", "Empty", 0, nonUniform, 2, empty, 3, 0},
                {"Non-Uniform", "Empty", 0, nonUniform, 3, empty, 3, 0},
                {"Non-Uniform", "Empty String", 45, nonUniform, 0, emptyString, 3, 0},
                {"Non-Uniform", "Empty String", 45, nonUniform, 1, emptyString, 3, 0},
                {"Non-Uniform", "Empty String", 44, nonUniform, 2, emptyString, 3, 0},
                {"Non-Uniform", "Empty String", 37, nonUniform, 3, emptyString, 3, 0},
                {"Non-Uniform", "Concrete", 45, nonUniform, 0, concrete, 3, 3},
                {"Non-Uniform", "Concrete", 45, nonUniform, 1, concrete, 3, 3},
                {"Non-Uniform", "Concrete", 44, nonUniform, 2, concrete, 3, 3},
                {"Non-Uniform", "Concrete", 37, nonUniform, 3, concrete, 3, 3},
                {"Non-Uniform", "Uniform", 4368, nonUniform, 0, uniform, 3, 3},
                {"Non-Uniform", "Uniform", 4368, nonUniform, 1, uniform, 3, 3},
                {"Non-Uniform", "Uniform", 4367, nonUniform, 2, uniform, 3, 3},
                {"Non-Uniform", "Uniform", 4360, nonUniform, 3, uniform, 3, 3},
                {"Non-Uniform", "Non-Uniform", 2363, nonUniform, 0, nonUniform, 3, 3},
                {"Non-Uniform", "Non-Uniform", 2363, nonUniform, 1, nonUniform, 3, 3},
                {"Non-Uniform", "Non-Uniform", 2242, nonUniform, 2, nonUniform, 3, 3},
                {"Non-Uniform", "Non-Uniform", 1962, nonUniform, 3, nonUniform, 3, 3}
        });
    }

    @Before
    public void setup() {

        // *** arrange ***
        PreciseInsert operation = new PreciseInsert(this.offset);

        // *** act ***
        this.resultAutomaton = operation.op(baseAutomaton, argAutomaton);
        this.resultAutomaton.minimize();

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int boundLength = baseLength + argLength;
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton, boundLength)
                                           .intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
