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
public class Given_PreciseSetCharAt_For_BoundedAutomata {

    @Parameter(value = 3)
    public Automaton baseAutomaton;
    @Parameter(value = 5)
    public Automaton argAutomaton;
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
    @Parameters(name = "{index}: <{0} Automaton>.setCharAt({4}, <{1} Automaton>) - Expected" +
                       " MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // get Automata
        Automaton empty = BasicAutomata.makeEmpty();
        Automaton emptyString = BasicAutomata.makeEmptyString();
        Automaton concrete = getConcreteAutomaton(alphabet, "ABC");
        Automaton uniform = getUniformBoundedAutomaton(alphabet, initialBoundLength);
        Automaton nonUniform = getNonUniformBoundedAutomaton(alphabet, initialBoundLength);

        // character automata
        Automaton a = BasicAutomata.makeChar('A');
        Automaton b = BasicAutomata.makeChar('B');
        Automaton c = BasicAutomata.makeChar('C');
        Automaton d = BasicAutomata.makeChar('D');

        return Arrays.asList(new Object[][]{
                {"Empty", "A", 0, empty, 0, a},
                {"Empty", "B", 0, empty, 0, b},
                {"Empty", "C", 0, empty, 0, c},
                {"Empty", "D", 0, empty, 0, d},
                {"Empty", "A", 0, empty, 1, a},
                {"Empty", "B", 0, empty, 1, b},
                {"Empty", "C", 0, empty, 1, c},
                {"Empty", "D", 0, empty, 1, d},
                {"Empty", "A", 0, empty, 2, a},
                {"Empty", "B", 0, empty, 2, b},
                {"Empty", "C", 0, empty, 2, c},
                {"Empty", "D", 0, empty, 2, d},
                {"Empty String", "A", 0, emptyString, 0, a},
                {"Empty String", "B", 0, emptyString, 0, b},
                {"Empty String", "C", 0, emptyString, 0, c},
                {"Empty String", "D", 0, emptyString, 0, d},
                {"Empty String", "A", 0, emptyString, 1, a},
                {"Empty String", "B", 0, emptyString, 1, b},
                {"Empty String", "C", 0, emptyString, 1, c},
                {"Empty String", "D", 0, emptyString, 1, d},
                {"Empty String", "A", 0, emptyString, 2, a},
                {"Empty String", "B", 0, emptyString, 2, b},
                {"Empty String", "C", 0, emptyString, 2, c},
                {"Empty String", "D", 0, emptyString, 2, d},
                {"Concrete", "A", 1, concrete, 0, a},
                {"Concrete", "B", 1, concrete, 0, b},
                {"Concrete", "C", 1, concrete, 0, c},
                {"Concrete", "D", 1, concrete, 0, d},
                {"Concrete", "A", 1, concrete, 1, a},
                {"Concrete", "B", 1, concrete, 1, b},
                {"Concrete", "C", 1, concrete, 1, c},
                {"Concrete", "D", 1, concrete, 1, d},
                {"Concrete", "A", 1, concrete, 2, a},
                {"Concrete", "B", 1, concrete, 2, b},
                {"Concrete", "C", 1, concrete, 2, c},
                {"Concrete", "D", 1, concrete, 2, d},
                {"Uniform", "A", 21, uniform, 0, a},
                {"Uniform", "B", 21, uniform, 0, b},
                {"Uniform", "C", 21, uniform, 0, c},
                {"Uniform", "D", 21, uniform, 0, d},
                {"Uniform", "A", 20, uniform, 1, a},
                {"Uniform", "B", 20, uniform, 1, b},
                {"Uniform", "C", 20, uniform, 1, c},
                {"Uniform", "D", 20, uniform, 1, d},
                {"Uniform", "A", 16, uniform, 2, a},
                {"Uniform", "B", 16, uniform, 2, b},
                {"Uniform", "C", 16, uniform, 2, c},
                {"Uniform", "D", 16, uniform, 2, d},
                {"Non-Uniform", "A", 21, nonUniform, 0, a},
                {"Non-Uniform", "B", 21, nonUniform, 0, b},
                {"Non-Uniform", "C", 21, nonUniform, 0, c},
                {"Non-Uniform", "D", 21, nonUniform, 0, d},
                {"Non-Uniform", "A", 20, nonUniform, 1, a},
                {"Non-Uniform", "B", 20, nonUniform, 1, b},
                {"Non-Uniform", "C", 20, nonUniform, 1, c},
                {"Non-Uniform", "D", 20, nonUniform, 1, d},
                {"Non-Uniform", "A", 16, nonUniform, 2, a},
                {"Non-Uniform", "B", 16, nonUniform, 2, b},
                {"Non-Uniform", "C", 16, nonUniform, 2, c},
                {"Non-Uniform", "D", 16, nonUniform, 2, d}
        });
    }

    @Before
    public void setup() {
        // *** arrange ***
        PreciseSetCharAt operation = new PreciseSetCharAt(this.offset);

        // *** act ***
        this.resultAutomaton = operation.op(baseAutomaton, argAutomaton);
        this.resultAutomaton.minimize();
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton)
                                           .intValue();

        // *** assert ***
        String reason = String.format("<%s Automaton>.setCharAt(%d, <%s Automaton>)", baseDescription, offset, argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
