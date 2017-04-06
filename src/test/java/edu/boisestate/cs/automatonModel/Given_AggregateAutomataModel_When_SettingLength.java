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
public class Given_AggregateAutomataModel_When_SettingLength {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 2)
    public AggregateAutomataModel model;
    @Parameter(value = 3)
    public int length;
    private AutomatonModel lengthModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.setLength({3}) - Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automaton models
        AggregateAutomataModel emptyModel = getEmptyAggregateModel(alphabet);
        AggregateAutomataModel emptyStringModel = getEmptyStringAggregateModel(alphabet);
        AggregateAutomataModel concreteModel = getConcreteAggregateModel(alphabet,"ABC");
        AggregateAutomataModel uniformModel = getUniformAggregateModel(alphabet, initialBoundLength);
        AggregateAutomataModel nonUniformModel = getNonUniformAggregateModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, emptyModel, 0},
                {"Empty", 0, emptyModel, 1},
                {"Empty", 0, emptyModel, 2},
                {"Empty", 0, emptyModel, 3},
                {"Empty String", 1, emptyStringModel, 0},
                {"Empty String", 1, emptyStringModel, 1},
                {"Empty String", 1, emptyStringModel, 2},
                {"Empty String", 1, emptyStringModel, 3},
                {"Concrete", 1, concreteModel, 0},
                {"Concrete", 1, concreteModel, 1},
                {"Concrete", 1, concreteModel, 2},
                {"Concrete", 1, concreteModel, 3},
                {"Uniform", 1, uniformModel, 0},
                {"Uniform", 5, uniformModel, 1},
                {"Uniform", 21, uniformModel, 2},
                {"Uniform", 85, uniformModel, 3},
                {"Non-Uniform", 0, nonUniformModel, 0},
                {"Non-Uniform", 1, nonUniformModel, 1},
                {"Non-Uniform", 8, nonUniformModel, 2},
                {"Non-Uniform", 45, nonUniformModel, 3}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.lengthModel = this.model.setLength(this.length);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.lengthModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "<%s Automaton Model>.setLength(%d)", description, length);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
