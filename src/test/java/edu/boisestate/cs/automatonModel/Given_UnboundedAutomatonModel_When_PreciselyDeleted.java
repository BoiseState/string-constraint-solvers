package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
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
public class Given_UnboundedAutomatonModel_When_PreciselyDeleted {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 4)
    public int end;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 2)
    public UnboundedAutomatonModel model;
    @Parameter(value = 3)
    public int start;
    private AutomatonModel deleteModel;


    @Parameters(name = "{index}: <{0} Automaton Model>.delete({3}, {4}) - " +
                       "Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automaton models
        UnboundedAutomatonModel emptyModel = getEmptyUnboundedModel(alphabet);
        UnboundedAutomatonModel emptyStringModel = getEmptyStringUnboundedModel(alphabet);
        UnboundedAutomatonModel concreteModel = getConcreteUnboundedModel(alphabet, "ABC");
        UnboundedAutomatonModel uniformModel = getUniformUnboundedModel(alphabet, initialBoundLength);
        UnboundedAutomatonModel nonUniformModel = getNonUniformUnboundedModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, emptyModel, 0, 0},
                {"Empty", 0, emptyModel, 0, 1},
                {"Empty", 0, emptyModel, 0, 2},
                {"Empty", 0, emptyModel, 0, 3},
                {"Empty", 0, emptyModel, 1, 1},
                {"Empty", 0, emptyModel, 1, 2},
                {"Empty", 0, emptyModel, 1, 3},
                {"Empty", 0, emptyModel, 2, 2},
                {"Empty", 0, emptyModel, 2, 3},
                {"Empty", 0, emptyModel, 3, 3},
                {"Empty String", 1, emptyStringModel, 0, 0},
                {"Empty String", 1, emptyStringModel, 0, 1},
                {"Empty String", 1, emptyStringModel, 0, 2},
                {"Empty String", 1, emptyStringModel, 0, 3},
                {"Empty String", 0, emptyStringModel, 1, 1},
                {"Empty String", 0, emptyStringModel, 1, 2},
                {"Empty String", 0, emptyStringModel, 1, 3},
                {"Empty String", 0, emptyStringModel, 2, 2},
                {"Empty String", 0, emptyStringModel, 2, 3},
                {"Empty String", 0, emptyStringModel, 3, 3},
                {"Concrete", 1, concreteModel, 0, 0},
                {"Concrete", 1, concreteModel, 0, 1},
                {"Concrete", 1, concreteModel, 0, 2},
                {"Concrete", 1, concreteModel, 0, 3},
                {"Concrete", 1, concreteModel, 1, 1},
                {"Concrete", 1, concreteModel, 1, 2},
                {"Concrete", 1, concreteModel, 1, 3},
                {"Concrete", 1, concreteModel, 2, 2},
                {"Concrete", 1, concreteModel, 2, 3},
                {"Concrete", 1, concreteModel, 3, 3},
                {"Uniform", 85, uniformModel, 0, 0},
                {"Uniform", 21, uniformModel, 0, 1},
                {"Uniform", 5, uniformModel, 0, 2},
                {"Uniform", 1, uniformModel, 0, 3},
                {"Uniform", 84, uniformModel, 1, 1},
                {"Uniform", 20, uniformModel, 1, 2},
                {"Uniform", 4, uniformModel, 1, 3},
                {"Uniform", 80, uniformModel, 2, 2},
                {"Uniform", 16, uniformModel, 2, 3},
                {"Uniform", 64, uniformModel, 3, 3},
                {"Non-uniform", 45, nonUniformModel, 0, 0},
                {"Non-uniform", 21, nonUniformModel, 0, 1},
                {"Non-uniform", 5, nonUniformModel, 0, 2},
                {"Non-uniform", 1, nonUniformModel, 0, 3},
                {"Non-uniform", 45, nonUniformModel, 1, 1},
                {"Non-uniform", 20, nonUniformModel, 1, 2},
                {"Non-uniform", 4, nonUniformModel, 1, 3},
                {"Non-uniform", 44, nonUniformModel, 2, 2},
                {"Non-uniform", 16, nonUniformModel, 2, 3},
                {"Non-uniform", 37, nonUniformModel, 3, 3},
                });
    }

    @Before
    public void setup() {
        // *** act ***
        this.deleteModel = this.model.delete(this.start, this.end);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.deleteModel.modelCount().intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
