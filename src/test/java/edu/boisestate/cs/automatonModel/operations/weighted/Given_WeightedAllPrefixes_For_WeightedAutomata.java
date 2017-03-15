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
public class Given_WeightedAllPrefixes_For_WeightedAutomata {

    @Parameter(value = 2)
    public WeightedAutomaton automaton;
    @Parameter(value = 0) // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    private WeightedAutomaton resultAutomaton;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.allPrefixes() - Expected MC = {1}")
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

        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty},
                {"Empty String", 1, emptyString},
                {"Concrete", 4, concrete},
                {"Uniform Bounded", 85, uniformBounded},
                {"Non-uniform Bounded", 58, nonUniformBounded},
                {"Uniform Unbounded", 85, uniformBounded},
                {"Non-uniform Unbounded", 58, nonUniformBounded}
        });
    }

    @Before
    public void setup() {
        // *** arrange ***
        WeightedAllPrefixes substring = new WeightedAllPrefixes();

        // *** act ***
        resultAutomaton = substring.op(automaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton)
                                           .intValue();

        // *** assert ***
        String message = String.format("<%s Automaton>.allPrefixes()", description);
        assertThat(message, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
