package edu.boisestate.cs.automatonModel;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.AutomatonModel;
import edu.boisestate.cs.automatonModel.WeightedAutomatonModel;
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
public class Given_WeightedAutomatonModel_When_GettingSuffix {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 2)
    public WeightedAutomatonModel model;
    @Parameter(value = 3)
    public int start;
    private AutomatonModel deleteModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.suffix({3}) - " +
                       "Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create baseAutomaton models
        WeightedAutomatonModel emptyModel = getEmptyWeightedModel(alphabet);
        WeightedAutomatonModel emptyStringModel = getEmptyStringWeightedModel(alphabet);
        WeightedAutomatonModel concreteModel = getConcreteWeightedModel(alphabet,"ABC");
        WeightedAutomatonModel uniformModel = getUniformWeightedModel(alphabet, initialBoundLength);
        WeightedAutomatonModel nonUniformModel = getNonUniformWeightedModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", -1, emptyModel, 0},
                {"Empty", -1, emptyModel, 1},
                {"Empty", -1, emptyModel, 2},
                {"Empty", -1, emptyModel, 3},
                {"Empty String", -1, emptyStringModel, 0},
                {"Empty String", -1, emptyStringModel, 1},
                {"Empty String", -1, emptyStringModel, 2},
                {"Empty String", -1, emptyStringModel, 3},
                {"Concrete", -1, concreteModel, 0},
                {"Concrete", -1, concreteModel, 1},
                {"Concrete", -1, concreteModel, 2},
                {"Concrete", -1, concreteModel, 3},
                {"Uniform", -1, uniformModel, 0},
                {"Uniform", -1, uniformModel, 1},
                {"Uniform", -1, uniformModel, 2},
                {"Uniform", -1, uniformModel, 3},
                {"Non-uniform", -1, nonUniformModel, 0},
                {"Non-uniform", -1, nonUniformModel, 1},
                {"Non-uniform", -1, nonUniformModel, 2},
                {"Non-uniform", -1, nonUniformModel, 3},
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.deleteModel = this.model.suffix(this.start);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.deleteModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "Expected Model Count Invalid for <%s Automaton Model>.suffix(%d)",
                                       description,
                                       start);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
