package edu.boisestate.cs.automaton;

import edu.boisestate.cs.Alphabet;
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
public class Given_WeightedAutomaton_When_Subtracting {

    @Parameter(value = 4)
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
    @Parameters(name = "{index}: <{0} Automaton>.minus(<{1} Automaton>) ->" +
                       " Expected MC = {2}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automata
        WeightedAutomaton empty = makeEmpty();
        WeightedAutomaton emptyString = makeEmptyString();
        WeightedAutomaton concrete = getConcreteWeightedAutomaton(alphabet, "ABC");
        WeightedAutomaton uniformBounded = getUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton nonUniformBounded = getNonUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton uniformUnbounded = getUniformUnboundedWeightedAutomaton(alphabet);
        WeightedAutomaton nonUniformUnbounded = getNonUniformUnboundedWeightedAutomaton(alphabet);

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", 0, empty, empty},
                {"Empty", "Empty String", 0, empty, emptyString},
                {"Empty", "Concrete", 0, empty, concrete},
                {"Empty", "Uniform Unbounded", 0, empty, uniformUnbounded},
                {"Empty", "Non-Uniform Unbounded", 0, empty, nonUniformUnbounded},
                {"Empty", "Uniform Bounded", 0, empty, uniformBounded},
                {"Empty", "Non-Uniform Bounded", 0, empty, nonUniformBounded},
                {"Empty String", "Empty", 1, emptyString, empty},
                {"Empty String", "Empty String", 0, emptyString, emptyString},
                {"Empty String", "Concrete", 1, emptyString, concrete},
                {"Empty String", "Uniform Unbounded", 0, emptyString, uniformUnbounded},
                {"Empty String", "Non-Uniform Unbounded", 1, emptyString, nonUniformUnbounded},
                {"Empty String", "Uniform Bounded", 0, emptyString, uniformBounded},
                {"Empty String", "Non-Uniform Bounded", 1, emptyString, nonUniformBounded},
                {"Concrete", "Empty", 1, concrete, empty},
                {"Concrete", "Empty String", 1, concrete, emptyString},
                {"Concrete", "Concrete", 0, concrete, concrete},
                {"Concrete", "Uniform Unbounded", 0, concrete, uniformUnbounded},
                {"Concrete", "Non-Uniform Unbounded", 0, concrete, nonUniformUnbounded},
                {"Concrete", "Uniform Bounded", 0, concrete, uniformBounded},
                {"Concrete", "Non-Uniform Bounded", 0, concrete, nonUniformBounded},
                {"Uniform Unbounded", "Empty", 85, uniformUnbounded, empty},
                {"Uniform Unbounded", "Empty String", 84, uniformUnbounded, emptyString},
                {"Uniform Unbounded", "Concrete", 84, uniformUnbounded, concrete},
                {"Uniform Unbounded", "Uniform Unbounded", 0, uniformUnbounded, uniformUnbounded},
                {"Uniform Unbounded", "Non-Uniform Unbounded", 40, uniformUnbounded, nonUniformUnbounded},
                {"Uniform Unbounded", "Uniform Bounded", 0, uniformUnbounded, uniformBounded},
                {"Uniform Unbounded", "Non-Uniform Bounded", 40, uniformUnbounded, nonUniformBounded},
                {"Non-Uniform Unbounded", "Empty", 45, nonUniformUnbounded, empty},
                {"Non-Uniform Unbounded", "Empty String", 45, nonUniformUnbounded, emptyString},
                {"Non-Uniform Unbounded", "Concrete", 44, nonUniformUnbounded, concrete},
                {"Non-Uniform Unbounded", "Uniform Unbounded", 0, nonUniformUnbounded, uniformUnbounded},
                {"Non-Uniform Unbounded", "Non-Uniform Unbounded", 0, nonUniformUnbounded, nonUniformUnbounded},
                {"Non-Uniform Unbounded", "Uniform Bounded", 0, nonUniformUnbounded, uniformBounded},
                {"Non-Uniform Unbounded", "Non-Uniform Bounded", 0, nonUniformUnbounded, nonUniformBounded},
                {"Uniform Bounded", "Empty", 85, uniformBounded, empty},
                {"Uniform Bounded", "Empty String", 84, uniformBounded, emptyString},
                {"Uniform Bounded", "Concrete", 84, uniformBounded, concrete},
                {"Uniform Bounded", "Uniform Unbounded", 0, uniformBounded, uniformUnbounded},
                {"Uniform Bounded", "Non-Uniform Unbounded", 40, uniformBounded, nonUniformUnbounded},
                {"Uniform Bounded", "Uniform Bounded", 0, uniformBounded, uniformBounded},
                {"Uniform Bounded", "Non-Uniform Bounded", 40, uniformBounded, nonUniformBounded},
                {"Non-Uniform Bounded", "Empty", 45, nonUniformBounded, empty},
                {"Non-Uniform Bounded", "Empty String", 45, nonUniformBounded, emptyString},
                {"Non-Uniform Bounded", "Concrete", 44, nonUniformBounded, concrete},
                {"Non-Uniform Bounded", "Uniform Unbounded", 0, nonUniformBounded, uniformUnbounded},
                {"Non-Uniform Bounded", "Non-Uniform Unbounded", 0, nonUniformBounded, nonUniformUnbounded},
                {"Non-Uniform Bounded", "Uniform Bounded", 0, nonUniformBounded, uniformBounded},
                {"Non-Uniform Bounded", "Non-Uniform Bounded", 0, nonUniformBounded, nonUniformBounded}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultAutomaton = baseAutomaton.minus(argAutomaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton, 3)
                                           .intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
