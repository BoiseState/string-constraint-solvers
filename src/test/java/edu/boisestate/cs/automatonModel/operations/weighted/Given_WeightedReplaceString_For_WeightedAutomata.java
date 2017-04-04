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
public class Given_WeightedReplaceString_For_WeightedAutomata {

    @Parameter(value = 4)
    public String replace;
    @Parameter(value = 3)
    public String find;
    @Parameter(value = 2)
    public WeightedAutomaton automaton;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 0) // first data value (0) is default
    public String description;

    private WeightedAutomaton resultAutomaton;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.replace('{3}', '{4}') -> Expected MC = {1}")
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
//                {"Empty", 0, empty, "A", "A"},
//                {"Empty", 0, empty, "A", "B"},
//                {"Empty", 0, empty, "A", "C"},
//                {"Empty", 0, empty, "A", "D"},
//                {"Empty", 0, empty, "B", "A"},
//                {"Empty", 0, empty, "B", "B"},
//                {"Empty", 0, empty, "B", "C"},
//                {"Empty", 0, empty, "B", "D"},
//                {"Empty", 0, empty, "C", "A"},
//                {"Empty", 0, empty, "C", "B"},
//                {"Empty", 0, empty, "C", "C"},
//                {"Empty", 0, empty, "C", "D"},
//                {"Empty", 0, empty, "D", "A"},
//                {"Empty", 0, empty, "D", "B"},
//                {"Empty", 0, empty, "D", "C"},
//                {"Empty", 0, empty, "D", "D"},
//                {"Empty String", 1, emptyString, "A", "A"},
//                {"Empty String", 1, emptyString, "A", "B"},
//                {"Empty String", 1, emptyString, "A", "C"},
//                {"Empty String", 1, emptyString, "A", "D"},
//                {"Empty String", 1, emptyString, "B", "A"},
//                {"Empty String", 1, emptyString, "B", "B"},
//                {"Empty String", 1, emptyString, "B", "C"},
//                {"Empty String", 1, emptyString, "B", "D"},
//                {"Empty String", 1, emptyString, "C", "A"},
//                {"Empty String", 1, emptyString, "C", "B"},
//                {"Empty String", 1, emptyString, "C", "C"},
//                {"Empty String", 1, emptyString, "C", "D"},
//                {"Empty String", 1, emptyString, "D", "A"},
//                {"Empty String", 1, emptyString, "D", "B"},
//                {"Empty String", 1, emptyString, "D", "C"},
//                {"Empty String", 1, emptyString, "D", "D"},
//                {"Concrete", 2, concrete, "A", "A"},
//                {"Concrete", 2, concrete, "A", "B"},
//                {"Concrete", 2, concrete, "A", "C"},
//                {"Concrete", 2, concrete, "A", "D"},
//                {"Concrete", 2, concrete, "B", "A"},
//                {"Concrete", 2, concrete, "B", "B"},
//                {"Concrete", 2, concrete, "B", "C"},
//                {"Concrete", 2, concrete, "B", "D"},
//                {"Concrete", 2, concrete, "C", "A"},
//                {"Concrete", 2, concrete, "C", "B"},
//                {"Concrete", 2, concrete, "C", "C"},
//                {"Concrete", 2, concrete, "C", "D"},
//                {"Concrete", 2, concrete, "D", "A"},
//                {"Concrete", 2, concrete, "D", "B"},
//                {"Concrete", 2, concrete, "D", "C"},
//                {"Concrete", 2, concrete, "D", "D"},
//                {"Uniform", 85, uniform, "A", "A"},
//                {"Uniform", 85, uniform, "A", "B"},
//                {"Uniform", 85, uniform, "A", "C"},
//                {"Uniform", 85, uniform, "A", "D"},
//                {"Uniform", 85, uniform, "B", "A"},
//                {"Uniform", 85, uniform, "B", "B"},
//                {"Uniform", 85, uniform, "B", "C"},
//                {"Uniform", 85, uniform, "B", "D"},
//                {"Uniform", 85, uniform, "C", "A"},
//                {"Uniform", 85, uniform, "C", "B"},
//                {"Uniform", 85, uniform, "C", "C"},
//                {"Uniform", 85, uniform, "C", "D"},
//                {"Uniform", 85, uniform, "D", "A"},
//                {"Uniform", 85, uniform, "D", "B"},
//                {"Uniform", 85, uniform, "D", "C"},
//                {"Uniform", 85, uniform, "D", "D"},
//                {"Non-uniform", 45, nonUniform, "A", "A"},
//                {"Non-uniform", 45, nonUniform, "A", "B"},
//                {"Non-uniform", 45, nonUniform, "A", "C"},
//                {"Non-uniform", 45, nonUniform, "A", "D"},
//                {"Non-uniform", 45, nonUniform, "B", "A"},
//                {"Non-uniform", 45, nonUniform, "B", "B"},
//                {"Non-uniform", 45, nonUniform, "B", "C"},
//                {"Non-uniform", 45, nonUniform, "B", "D"},
//                {"Non-uniform", 45, nonUniform, "C", "A"},
//                {"Non-uniform", 45, nonUniform, "C", "B"},
//                {"Non-uniform", 45, nonUniform, "C", "C"},
//                {"Non-uniform", 45, nonUniform, "C", "D"},
//                {"Non-uniform", 45, nonUniform, "D", "A"},
//                {"Non-uniform", 45, nonUniform, "D", "B"},
//                {"Non-uniform", 45, nonUniform, "D", "C"},
//                {"Non-uniform", 45, nonUniform, "D", "D"}
        });
    }

    @Before
    public void setup() {
        // *** arrange ***
         WeightedReplaceString operation = new WeightedReplaceString(find, replace);

        // *** act ***
        resultAutomaton = operation.op(automaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton)
                                           .intValue();

        // *** assert ***
        String reason = String.format("<%s Automaton>.replace(\"%s\", \"%s\")", description, find, replace);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
