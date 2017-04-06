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
public class Given_WeightedPreciseInsert_For_WeightedAutomata {

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
    @Parameters(name = "{index}: <{0} Automaton>.insert({4}, <{1} Automaton>) -> Expected MC = {1}")
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

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
//                {"Empty", "Empty", 0, empty, 0, empty},
//                {"Empty", "Empty", 0, empty, 1, empty},
//                {"Empty", "Empty", 0, empty, 2, empty},
//                {"Empty", "Empty", 0, empty, 3, empty},
//                {"Empty", "Empty String", 0, empty, 0, emptyString},
//                {"Empty", "Empty String", 0, empty, 1, emptyString},
//                {"Empty", "Empty String", 0, empty, 2, emptyString},
//                {"Empty", "Empty String", 0, empty, 3, emptyString},
//                {"Empty", "Concrete", 0, empty, 0, concrete},
//                {"Empty", "Concrete", 0, empty, 1, concrete},
//                {"Empty", "Concrete", 0, empty, 2, concrete},
//                {"Empty", "Concrete", 0, empty, 3, concrete},
//                {"Empty", "Uniform", 0, empty, 0, uniform},
//                {"Empty", "Uniform", 0, empty, 1, uniform},
//                {"Empty", "Uniform", 0, empty, 2, uniform},
//                {"Empty", "Uniform", 0, empty, 3, uniform},
//                {"Empty", "Non-Uniform", 0, empty, 0, nonUniform},
//                {"Empty", "Non-Uniform", 0, empty, 1, nonUniform},
//                {"Empty", "Non-Uniform", 0, empty, 2, nonUniform},
//                {"Empty", "Non-Uniform", 0, empty, 3, nonUniform},
//                {"Empty String", "Empty", 0, emptyString, 0, empty},
//                {"Empty String", "Empty", 0, emptyString, 1, empty},
//                {"Empty String", "Empty", 0, emptyString, 2, empty},
//                {"Empty String", "Empty", 0, emptyString, 3, empty},
//                {"Empty String", "Empty String", 1, emptyString, 0, emptyString},
//                {"Empty String", "Empty String", 0, emptyString, 1, emptyString},
//                {"Empty String", "Empty String", 0, emptyString, 2, emptyString},
//                {"Empty String", "Empty String", 0, emptyString, 3, emptyString},
//                {"Empty String", "Concrete", 2, emptyString, 0, concrete},
//                {"Empty String", "Concrete", 0, emptyString, 1, concrete},
//                {"Empty String", "Concrete", 0, emptyString, 2, concrete},
//                {"Empty String", "Concrete", 0, emptyString, 3, concrete},
//                {"Empty String", "Uniform", 85, emptyString, 0, uniform},
//                {"Empty String", "Uniform", 0, emptyString, 1, uniform},
//                {"Empty String", "Uniform", 0, emptyString, 2, uniform},
//                {"Empty String", "Uniform", 0, emptyString, 3, uniform},
//                {"Empty String", "Non-Uniform", 45, emptyString, 0, nonUniform},
//                {"Empty String", "Non-Uniform", 0, emptyString, 1, nonUniform},
//                {"Empty String", "Non-Uniform", 0, emptyString, 2, nonUniform},
//                {"Empty String", "Non-Uniform", 0, emptyString, 3, nonUniform},
//                {"Concrete", "Empty", 0, concrete, 0, empty},
//                {"Concrete", "Empty", 0, concrete, 1, empty},
//                {"Concrete", "Empty", 0, concrete, 2, empty},
//                {"Concrete", "Empty", 0, concrete, 3, empty},
//                {"Concrete", "Empty String", 2, concrete, 0, emptyString},
//                {"Concrete", "Empty String", 2, concrete, 1, emptyString},
//                {"Concrete", "Empty String", 2, concrete, 2, emptyString},
//                {"Concrete", "Empty String", 2, concrete, 3, emptyString},
//                {"Concrete", "Concrete", 4, concrete, 0, concrete},
//                {"Concrete", "Concrete", 4, concrete, 1, concrete},
//                {"Concrete", "Concrete", 4, concrete, 2, concrete},
//                {"Concrete", "Concrete", 4, concrete, 3, concrete},
//                {"Concrete", "Uniform", 170, concrete, 0, uniform},
//                {"Concrete", "Uniform", 170, concrete, 1, uniform},
//                {"Concrete", "Uniform", 170, concrete, 2, uniform},
//                {"Concrete", "Uniform", 170, concrete, 3, uniform},
//                {"Concrete", "Non-Uniform", 90, concrete, 0, nonUniform},
//                {"Concrete", "Non-Uniform", 90, concrete, 1, nonUniform},
//                {"Concrete", "Non-Uniform", 90, concrete, 2, nonUniform},
//                {"Concrete", "Non-Uniform", 90, concrete, 3, nonUniform},
//                {"Uniform", "Empty", 0, uniform, 0, empty},
//                {"Uniform", "Empty", 0, uniform, 1, empty},
//                {"Uniform", "Empty", 0, uniform, 2, empty},
//                {"Uniform", "Empty", 0, uniform, 3, empty},
//                {"Uniform", "Empty String", 85, uniform, 0, emptyString},
//                {"Uniform", "Empty String", 84, uniform, 1, emptyString},
//                {"Uniform", "Empty String", 80, uniform, 2, emptyString},
//                {"Uniform", "Empty String", 64, uniform, 3, emptyString},
//                {"Uniform", "Concrete", 170, uniform, 0, concrete},
//                {"Uniform", "Concrete", 168, uniform, 1, concrete},
//                {"Uniform", "Concrete", 160, uniform, 2, concrete},
//                {"Uniform", "Concrete", 128, uniform, 3, concrete},
//                {"Uniform", "Uniform", 7225, uniform, 0, uniform},
//                {"Uniform", "Uniform", 7140, uniform, 1, uniform},
//                {"Uniform", "Uniform", 6800, uniform, 2, uniform},
//                {"Uniform", "Uniform", 5440, uniform, 3, uniform},
//                {"Uniform", "Non-Uniform", 3825, uniform, 0, nonUniform},
//                {"Uniform", "Non-Uniform", 3780, uniform, 1, nonUniform},
//                {"Uniform", "Non-Uniform", 3600, uniform, 2, nonUniform},
//                {"Uniform", "Non-Uniform", 2880, uniform, 3, nonUniform},
//                {"Non-Uniform", "Empty", 0, nonUniform, 0, empty},
//                {"Non-Uniform", "Empty", 0, nonUniform, 1, empty},
//                {"Non-Uniform", "Empty", 0, nonUniform, 2, empty},
//                {"Non-Uniform", "Empty", 0, nonUniform, 3, empty},
//                {"Non-Uniform", "Empty String", 45, nonUniform, 0, emptyString},
//                {"Non-Uniform", "Empty String", 45, nonUniform, 1, emptyString},
//                {"Non-Uniform", "Empty String", 44, nonUniform, 2, emptyString},
//                {"Non-Uniform", "Empty String", 37, nonUniform, 3, emptyString},
//                {"Non-Uniform", "Concrete", 90, nonUniform, 0, concrete},
//                {"Non-Uniform", "Concrete", 90, nonUniform, 1, concrete},
//                {"Non-Uniform", "Concrete", 88, nonUniform, 2, concrete},
//                {"Non-Uniform", "Concrete", 74, nonUniform, 3, concrete},
//                {"Non-Uniform", "Uniform", 3825, nonUniform, 0, uniform},
//                {"Non-Uniform", "Uniform", 3825, nonUniform, 1, uniform},
//                {"Non-Uniform", "Uniform", 3740, nonUniform, 2, uniform},
//                {"Non-Uniform", "Uniform", 3145, nonUniform, 3, uniform},
//                {"Non-Uniform", "Non-Uniform", 2025, nonUniform, 0, nonUniform},
//                {"Non-Uniform", "Non-Uniform", 2025, nonUniform, 1, nonUniform},
//                {"Non-Uniform", "Non-Uniform", 1980, nonUniform, 2, nonUniform},
//                {"Non-Uniform", "Non-Uniform", 1665, nonUniform, 3, nonUniform}
        });
    }

    @Before
    public void setup() {
        // *** arrange ***
         WeightedPreciseInsert operation = new WeightedPreciseInsert(offset);

        // *** act ***
        resultAutomaton = operation.op(baseAutomaton, argAutomaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton)
                                           .intValue();

        // *** assert ***
        String reason = String.format("<%s Automaton>.insert(%d, <%s Automaton>)", baseDescription, offset, argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
