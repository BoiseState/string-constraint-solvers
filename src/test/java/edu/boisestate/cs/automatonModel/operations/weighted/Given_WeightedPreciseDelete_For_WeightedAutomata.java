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
public class Given_WeightedPreciseDelete_For_WeightedAutomata {

    @Parameter(value = 3)
    public int start;
    @Parameter(value = 4)
    public int end;
    @Parameter(value = 2)
    public WeightedAutomaton automaton;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 0) // first data value (0) is default
    public String description;

    private WeightedAutomaton resultAutomaton;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.delete({3}, {4}) ->" +
                       " Expected MC = {1}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automata
        WeightedAutomaton empty = makeEmpty();
        WeightedAutomaton emptyString = makeEmptyString();
        WeightedAutomaton concrete = getConcreteWeightedAutomaton(alphabet, "ABC");
        WeightedAutomaton balancedUniform0 = balanced_Uniform_WeightedAutomaton(alphabet, 0);
        WeightedAutomaton balancedUniform1 = balanced_Uniform_WeightedAutomaton(alphabet, 1);
        WeightedAutomaton balancedUniform2 = balanced_Uniform_WeightedAutomaton(alphabet, 2);
        WeightedAutomaton balancedUniform3 = balanced_Uniform_WeightedAutomaton(alphabet, 3);
        WeightedAutomaton balancedNonUniform0 = balanced_NonUniform_WeightedAutomaton(alphabet, 0);
        WeightedAutomaton balancedNonUniform1 = balanced_NonUniform_WeightedAutomaton(alphabet, 1);
        WeightedAutomaton balancedNonUniform2 = balanced_NonUniform_WeightedAutomaton(alphabet, 2);
        WeightedAutomaton balancedNonUniform3 = balanced_NonUniform_WeightedAutomaton(alphabet, 3);

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty, 0, 0},
                {"Empty", 0, empty, 0, 1},
                {"Empty", 0, empty, 0, 2},
                {"Empty", 0, empty, 0, 3},
                {"Empty", 0, empty, 1, 1},
                {"Empty", 0, empty, 1, 2},
                {"Empty", 0, empty, 1, 3},
                {"Empty", 0, empty, 2, 2},
                {"Empty", 0, empty, 2, 3},
                {"Empty", 0, empty, 3, 3},
                {"Empty String", 1, emptyString, 0, 0},
                {"Empty String", 1, emptyString, 0, 1},
                {"Empty String", 1, emptyString, 0, 2},
                {"Empty String", 1, emptyString, 0, 3},
                {"Empty String", 0, emptyString, 1, 1},
                {"Empty String", 0, emptyString, 1, 2},
                {"Empty String", 0, emptyString, 1, 3},
                {"Empty String", 0, emptyString, 2, 2},
                {"Empty String", 0, emptyString, 2, 3},
                {"Empty String", 0, emptyString, 3, 3},
                {"Concrete", 1, concrete, 0, 0},
                {"Concrete", 1, concrete, 0, 1},
                {"Concrete", 1, concrete, 0, 2},
                {"Concrete", 1, concrete, 0, 3},
                {"Concrete", 1, concrete, 1, 1},
                {"Concrete", 1, concrete, 1, 2},
                {"Concrete", 1, concrete, 1, 3},
                {"Concrete", 1, concrete, 2, 2},
                {"Concrete", 1, concrete, 2, 3},
                {"Concrete", 1, concrete, 3, 3},
                {"Balanced Uniform 0", 1, balancedUniform0, 0, 0},
                {"Balanced Uniform 0", 1, balancedUniform0, 0, 1},
                {"Balanced Uniform 0", 1, balancedUniform0, 0, 2},
                {"Balanced Uniform 0", 1, balancedUniform0, 0, 3},
                {"Balanced Uniform 0", 0, balancedUniform0, 1, 1},
                {"Balanced Uniform 0", 0, balancedUniform0, 1, 2},
                {"Balanced Uniform 0", 0, balancedUniform0, 1, 3},
                {"Balanced Uniform 0", 0, balancedUniform0, 2, 2},
                {"Balanced Uniform 0", 0, balancedUniform0, 2, 3},
                {"Balanced Uniform 0", 0, balancedUniform0, 3, 3},
                {"Balanced Uniform 1", 4, balancedUniform1, 0, 0},
                {"Balanced Uniform 1", 4, balancedUniform1, 0, 1},
                {"Balanced Uniform 1", 4, balancedUniform1, 0, 2},
                {"Balanced Uniform 1", 4, balancedUniform1, 0, 3},
                {"Balanced Uniform 1", 4, balancedUniform1, 1, 1},
                {"Balanced Uniform 1", 4, balancedUniform1, 1, 2},
                {"Balanced Uniform 1", 4, balancedUniform1, 1, 3},
                {"Balanced Uniform 1", 0, balancedUniform1, 2, 2},
                {"Balanced Uniform 1", 0, balancedUniform1, 2, 3},
                {"Balanced Uniform 1", 0, balancedUniform1, 3, 3},
                {"Balanced Uniform 2", 16, balancedUniform2, 0, 0},
                {"Balanced Uniform 2", 16, balancedUniform2, 0, 1},
                {"Balanced Uniform 2", 16, balancedUniform2, 0, 2},
                {"Balanced Uniform 2", 16, balancedUniform2, 0, 3},
                {"Balanced Uniform 2", 16, balancedUniform2, 1, 1},
                {"Balanced Uniform 2", 16, balancedUniform2, 1, 2},
                {"Balanced Uniform 2", 16, balancedUniform2, 1, 3},
                {"Balanced Uniform 2", 16, balancedUniform2, 2, 2},
                {"Balanced Uniform 2", 16, balancedUniform2, 2, 3},
                {"Balanced Uniform 2", 0, balancedUniform2, 3, 3},
                {"Balanced Uniform 3", 64, balancedUniform3, 0, 0},
                {"Balanced Uniform 3", 64, balancedUniform3, 0, 1},
                {"Balanced Uniform 3", 64, balancedUniform3, 0, 2},
                {"Balanced Uniform 3", 64, balancedUniform3, 0, 3},
                {"Balanced Uniform 3", 64, balancedUniform3, 1, 1},
                {"Balanced Uniform 3", 64, balancedUniform3, 1, 2},
                {"Balanced Uniform 3", 64, balancedUniform3, 1, 3},
                {"Balanced Uniform 3", 64, balancedUniform3, 2, 2},
                {"Balanced Uniform 3", 64, balancedUniform3, 2, 3},
                {"Balanced Uniform 3", 64, balancedUniform3, 3, 3},
                {"Balanced Non-Uniform 0", 0, balancedNonUniform0, 0, 0},
                {"Balanced Non-Uniform 0", 0, balancedNonUniform0, 0, 1},
                {"Balanced Non-Uniform 0", 0, balancedNonUniform0, 0, 2},
                {"Balanced Non-Uniform 0", 0, balancedNonUniform0, 0, 3},
                {"Balanced Non-Uniform 0", 0, balancedNonUniform0, 1, 1},
                {"Balanced Non-Uniform 0", 0, balancedNonUniform0, 1, 2},
                {"Balanced Non-Uniform 0", 0, balancedNonUniform0, 1, 3},
                {"Balanced Non-Uniform 0", 0, balancedNonUniform0, 2, 2},
                {"Balanced Non-Uniform 0", 0, balancedNonUniform0, 2, 3},
                {"Balanced Non-Uniform 0", 0, balancedNonUniform0, 3, 3},
                {"Balanced Non-Uniform 1", 1, balancedNonUniform1, 0, 0},
                {"Balanced Non-Uniform 1", 1, balancedNonUniform1, 0, 1},
                {"Balanced Non-Uniform 1", 1, balancedNonUniform1, 0, 2},
                {"Balanced Non-Uniform 1", 1, balancedNonUniform1, 0, 3},
                {"Balanced Non-Uniform 1", 1, balancedNonUniform1, 1, 1},
                {"Balanced Non-Uniform 1", 1, balancedNonUniform1, 1, 2},
                {"Balanced Non-Uniform 1", 1, balancedNonUniform1, 1, 3},
                {"Balanced Non-Uniform 1", 0, balancedNonUniform1, 2, 2},
                {"Balanced Non-Uniform 1", 0, balancedNonUniform1, 2, 3},
                {"Balanced Non-Uniform 1", 0, balancedNonUniform1, 3, 3},
                {"Balanced Non-Uniform 2", 7, balancedNonUniform2, 0, 0},
                {"Balanced Non-Uniform 2", 7, balancedNonUniform2, 0, 1},
                {"Balanced Non-Uniform 2", 7, balancedNonUniform2, 0, 2},
                {"Balanced Non-Uniform 2", 7, balancedNonUniform2, 0, 3},
                {"Balanced Non-Uniform 2", 7, balancedNonUniform2, 1, 1},
                {"Balanced Non-Uniform 2", 7, balancedNonUniform2, 1, 2},
                {"Balanced Non-Uniform 2", 7, balancedNonUniform2, 1, 3},
                {"Balanced Non-Uniform 2", 7, balancedNonUniform2, 2, 2},
                {"Balanced Non-Uniform 2", 7, balancedNonUniform2, 2, 3},
                {"Balanced Non-Uniform 2", 0, balancedNonUniform2, 3, 3},
                {"Balanced Non-Uniform 3", 37, balancedNonUniform3, 0, 0},
                {"Balanced Non-Uniform 3", 37, balancedNonUniform3, 0, 1},
                {"Balanced Non-Uniform 3", 37, balancedNonUniform3, 0, 2},
                {"Balanced Non-Uniform 3", 37, balancedNonUniform3, 0, 3},
                {"Balanced Non-Uniform 3", 37, balancedNonUniform3, 1, 1},
                {"Balanced Non-Uniform 3", 37, balancedNonUniform3, 1, 2},
                {"Balanced Non-Uniform 3", 37, balancedNonUniform3, 1, 3},
                {"Balanced Non-Uniform 3", 37, balancedNonUniform3, 2, 2},
                {"Balanced Non-Uniform 3", 37, balancedNonUniform3, 2, 3},
                {"Balanced Non-Uniform 3", 37, balancedNonUniform3, 3, 3}
        });
    }

    @Before
    public void setup() {
        // *** arrange ***
         WeightedPreciseDelete operation = new WeightedPreciseDelete(start, end);

        // *** act ***
        resultAutomaton = operation.op(automaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton)
                                           .intValue();

        // *** assert ***
        String reason = String.format("<%s Automaton>.delete(%d, %d)", description, start, end);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
