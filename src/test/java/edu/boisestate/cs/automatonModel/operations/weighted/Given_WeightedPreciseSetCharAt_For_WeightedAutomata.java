package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmpty;
import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmptyString;
import static edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedAutomatonOperationTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_WeightedPreciseSetCharAt_For_WeightedAutomata {

    @Parameter(value = 4)
    public int offset;
    @Parameter(value = 5)
    public WeightedAutomaton argAutomaton;
    @Parameter(value = 3)
    public WeightedAutomaton baseAutomaton;
    @Parameter(value = 2)
    public int expectedModelCount;
    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 0) // first data value (0) is default
    public String baseDescription;

    private WeightedAutomaton resultAutomaton;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.setCharAt({4}, <{1} Automaton>) -> Expected MC = {1}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automata
        WeightedAutomaton empty = makeEmpty();
        WeightedAutomaton emptyString = makeEmptyString();
        WeightedAutomaton concrete = getConcreteWeightedAutomaton(alphabet, "ABC");
        WeightedAutomaton uniform = getUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton nonUniform = getNonUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);

        // create arg automata
        WeightedAutomaton a = getConcreteWeightedAutomaton(alphabet, "A");
        WeightedAutomaton b = getConcreteWeightedAutomaton(alphabet, "B");
        WeightedAutomaton c = getConcreteWeightedAutomaton(alphabet, "C");
        WeightedAutomaton d = getConcreteWeightedAutomaton(alphabet, "D");

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
//                {"Empty", "'A'", 0, empty, 0, a},
//                {"Empty", "'B'", 0, empty, 0, b},
//                {"Empty", "'C'", 0, empty, 0, c},
//                {"Empty", "'D'", 0, empty, 0, d},
//                {"Empty", "'A'", 0, empty, 1, a},
//                {"Empty", "'B'", 0, empty, 1, b},
//                {"Empty", "'C'", 0, empty, 1, c},
//                {"Empty", "'D'", 0, empty, 1, d},
//                {"Empty", "'A'", 0, empty, 2, a},
//                {"Empty", "'B'", 0, empty, 2, b},
//                {"Empty", "'C'", 0, empty, 2, c},
//                {"Empty", "'D'", 0, empty, 2, d},
//                {"Empty String", "'A'", 0, emptyString, 0, a},
//                {"Empty String", "'B'", 0, emptyString, 0, b},
//                {"Empty String", "'C'", 0, emptyString, 0, c},
//                {"Empty String", "'D'", 0, emptyString, 0, d},
//                {"Empty String", "'A'", 0, emptyString, 1, a},
//                {"Empty String", "'B'", 0, emptyString, 1, b},
//                {"Empty String", "'C'", 0, emptyString, 1, c},
//                {"Empty String", "'D'", 0, emptyString, 1, d},
//                {"Empty String", "'A'", 0, emptyString, 2, a},
//                {"Empty String", "'B'", 0, emptyString, 2, b},
//                {"Empty String", "'C'", 0, emptyString, 2, c},
//                {"Empty String", "'D'", 0, emptyString, 2, d},
//                {"Concrete", "'A'", 2, concrete, 0, a},
//                {"Concrete", "'B'", 2, concrete, 0, b},
//                {"Concrete", "'C'", 2, concrete, 0, c},
//                {"Concrete", "'D'", 2, concrete, 0, d},
//                {"Concrete", "'A'", 2, concrete, 1, a},
//                {"Concrete", "'B'", 2, concrete, 1, b},
//                {"Concrete", "'C'", 2, concrete, 1, c},
//                {"Concrete", "'D'", 2, concrete, 1, d},
//                {"Concrete", "'A'", 2, concrete, 2, a},
//                {"Concrete", "'B'", 2, concrete, 2, b},
//                {"Concrete", "'C'", 2, concrete, 2, c},
//                {"Concrete", "'D'", 2, concrete, 2, d},
//                {"Uniform", "'A'", 84, uniform, 0, a},
//                {"Uniform", "'B'", 84, uniform, 0, b},
//                {"Uniform", "'C'", 84, uniform, 0, c},
//                {"Uniform", "'D'", 84, uniform, 0, d},
//                {"Uniform", "'A'", 80, uniform, 1, a},
//                {"Uniform", "'B'", 80, uniform, 1, b},
//                {"Uniform", "'C'", 80, uniform, 1, c},
//                {"Uniform", "'D'", 80, uniform, 1, d},
//                {"Uniform", "'A'", 64, uniform, 2, a},
//                {"Uniform", "'B'", 64, uniform, 2, b},
//                {"Uniform", "'C'", 64, uniform, 2, c},
//                {"Uniform", "'D'", 64, uniform, 2, d},
//                {"Non-Uniform", "'A'", 45, nonUniform, 0, a},
//                {"Non-Uniform", "'B'", 45, nonUniform, 0, b},
//                {"Non-Uniform", "'C'", 45, nonUniform, 0, c},
//                {"Non-Uniform", "'D'", 45, nonUniform, 0, d},
//                {"Non-Uniform", "'A'", 44, nonUniform, 1, a},
//                {"Non-Uniform", "'B'", 44, nonUniform, 1, b},
//                {"Non-Uniform", "'C'", 44, nonUniform, 1, c},
//                {"Non-Uniform", "'D'", 44, nonUniform, 1, d},
//                {"Non-Uniform", "'A'", 37, nonUniform, 2, a},
//                {"Non-Uniform", "'B'", 37, nonUniform, 2, b},
//                {"Non-Uniform", "'C'", 37, nonUniform, 2, c},
//                {"Non-Uniform", "'D'", 37, nonUniform, 2, d}
        });
    }

    @Before
    public void setup() {
        // *** arrange ***
         WeightedPreciseSetCharAt operation = new WeightedPreciseSetCharAt(offset);

        // *** act ***
        resultAutomaton = operation.op(baseAutomaton, argAutomaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton)
                                           .intValue();

        // *** assert ***
        String reason = String.format("<%s Automaton>.setCharAt(%d, <%s Automaton>)", baseDescription, offset, argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
