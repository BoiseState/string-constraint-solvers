package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static edu.boisestate.cs.automatonModel.operations
        .AutomatonOperationTestUtilities.getConcreteAutomaton;
import static edu.boisestate.cs.automatonModel.operations
        .AutomatonOperationTestUtilities.getNonUniformBoundedAutomaton;
import static edu.boisestate.cs.automatonModel.operations
        .AutomatonOperationTestUtilities.getUniformBoundedAutomaton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_AutomatonModel_When_GettingRequiredCharAutomaton {

    @Parameter(value = 2)
    public Automaton automaton;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 0) // first data value (0) is default
    public String description;

    private Automaton resultAutomaton;

    private static int initialBoundLength;
    private static Alphabet alphabet;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.getRequiredCharAutomaton() - Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        alphabet = new Alphabet("A-D");
        initialBoundLength = 3;

        // create automata
        Automaton empty = BasicAutomata.makeEmpty();
        Automaton emptyString = BasicAutomata.makeEmptyString();
        Automaton concrete = getConcreteAutomaton(alphabet, "ABC");
        Automaton concreteA = getConcreteAutomaton(alphabet, "A");
        Automaton uniform = getUniformBoundedAutomaton(alphabet, initialBoundLength);
        Automaton nonUniform = getNonUniformBoundedAutomaton(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty},
                {"Empty String", 1, emptyString},
                {"Concrete", 1, concrete},
                {"Concrete A", 1, concreteA},
                {"Uniform", 0, uniform},
                {"Non-Uniform", 0, nonUniform}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultAutomaton = AutomatonModel.getRequiredCharAutomaton(automaton, alphabet, initialBoundLength);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton)
                                           .intValue();

        // *** assert ***
        String reason = String.format("<%s Automaton>.getRequiredCharAutomaton()", description);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
