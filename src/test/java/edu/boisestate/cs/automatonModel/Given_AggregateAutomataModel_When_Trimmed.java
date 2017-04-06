package edu.boisestate.cs.automatonModel;

import edu.boisestate.cs.Alphabet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static edu.boisestate.cs.automatonModel.AutomatonTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_AggregateAutomataModel_When_Trimmed {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 2)
    public AggregateAutomataModel model;
    @Parameter(value = 1)
    public int expectedModelCount;
    private AutomatonModel trimModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.trim() - Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet(" ,A-D");
        int initialBoundLength = 3;

        // create automaton models
        AggregateAutomataModel emptyModel = getEmptyAggregateModel(alphabet);
        AggregateAutomataModel emptyStringModel = getEmptyStringAggregateModel(alphabet);
        AggregateAutomataModel whiteSpaceConcreteModel = getConcreteAggregateModel(alphabet," A ");
        AggregateAutomataModel noWhiteSpaceConcreteModel = getConcreteAggregateModel(alphabet,"ABC");
        AggregateAutomataModel uniformModel = getUniformAggregateModel(alphabet, initialBoundLength);
        AggregateAutomataModel nonUniformModel = getNonUniformAggregateModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, emptyModel},
                {"Empty String", 1, emptyStringModel},
                {"Concrete Whitespace", 1, whiteSpaceConcreteModel},
                {"Concrete No Whitespace", 1, noWhiteSpaceConcreteModel},
                {"Uniform", 109, uniformModel},
                {"Non-Uniform", 52, nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.trimModel = this.model.trim();
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.trimModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "<%s Automaton Model>.trim()", description);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
