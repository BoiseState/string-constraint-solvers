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
public class Given_AggregateAutomataModel_When_AssertingNotEqualsIgnoringCase {

    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 4)
    public AggregateAutomataModel argModel;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 3)
    public AggregateAutomataModel baseModel;
    @Parameter(value = 2)
    public int expectedModelCount;
    private AutomatonModel notEqualModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.assertNotEqualsIgnoreCase(<{1} Automaton Model>) - Expected MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D,a-d");
        int initialBoundLength = 3;

        // create automaton models
        AggregateAutomataModel emptyModel = getEmptyAggregateModel(alphabet);
        AggregateAutomataModel emptyStringModel = getEmptyStringAggregateModel(alphabet);
        AggregateAutomataModel concreteLowerModel = getConcreteAggregateModel(alphabet,"abc");
        AggregateAutomataModel concreteUpperModel = getConcreteAggregateModel(alphabet,"ABC");
        AggregateAutomataModel uniformModel = getUniformAggregateModel(alphabet, initialBoundLength);
        AggregateAutomataModel nonUniformModel = getNonUniformAggregateModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", 0, emptyModel, emptyModel},
                {"Empty", "Empty String", 0, emptyModel, emptyStringModel},
                {"Empty", "Concrete Lower", 0, emptyModel, concreteLowerModel},
                {"Empty", "Concrete Upper", 0, emptyModel, concreteUpperModel},
                {"Empty", "Uniform", 0, emptyModel, uniformModel},
                {"Empty", "Non-Uniform", 0, emptyModel, nonUniformModel},
                {"Empty String", "Empty", 0, emptyStringModel, emptyModel},
                {"Empty String", "Empty String", 0, emptyStringModel, emptyStringModel},
                {"Empty String", "Concrete Lower", 1, emptyStringModel, concreteLowerModel},
                {"Empty String", "Concrete Upper", 1, emptyStringModel, concreteUpperModel},
                {"Empty String", "Uniform", 1, emptyStringModel, uniformModel},
                {"Empty String", "Non-Uniform", 1, emptyStringModel, nonUniformModel},
                {"Concrete Lower", "Empty", 0, concreteLowerModel, emptyModel},
                {"Concrete Lower", "Empty String", 1, concreteLowerModel, emptyStringModel},
                {"Concrete Lower", "Concrete Lower", 0, concreteLowerModel, concreteLowerModel},
                {"Concrete Lower", "Concrete Upper", 0, concreteLowerModel, concreteUpperModel},
                {"Concrete Lower", "Uniform", 1, concreteLowerModel, uniformModel},
                {"Concrete Lower", "Non-Uniform", 1, concreteLowerModel, nonUniformModel},
                {"Concrete Upper", "Empty", 0, concreteUpperModel, emptyModel},
                {"Concrete Upper", "Empty String", 1, concreteUpperModel, emptyStringModel},
                {"Concrete Upper", "Concrete Lower", 0, concreteUpperModel, concreteLowerModel},
                {"Concrete Upper", "Concrete Upper", 0, concreteUpperModel, concreteUpperModel},
                {"Concrete Upper", "Uniform", 1, concreteUpperModel, uniformModel},
                {"Concrete Upper", "Non-Uniform", 1, concreteUpperModel, nonUniformModel},
                {"Uniform", "Empty", 0, uniformModel, emptyModel},
                {"Uniform", "Empty String", 584, uniformModel, emptyStringModel},
                {"Uniform", "Concrete Lower", 577, uniformModel, concreteLowerModel},
                {"Uniform", "Concrete Upper", 577, uniformModel, concreteUpperModel},
                {"Uniform", "Uniform", 585, uniformModel, uniformModel},
                {"Uniform", "Non-Uniform", 585, uniformModel, nonUniformModel},
                {"Non-Uniform", "Empty", 0, nonUniformModel, emptyModel},
                {"Non-Uniform", "Empty String", 185, nonUniformModel, emptyStringModel},
                {"Non-Uniform", "Concrete Lower", 181, nonUniformModel, concreteLowerModel},
                {"Non-Uniform", "Concrete Upper", 181, nonUniformModel, concreteUpperModel},
                {"Non-Uniform", "Uniform", 185, nonUniformModel, uniformModel},
                {"Non-Uniform", "Non-Uniform", 185, nonUniformModel, nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.notEqualModel = this.baseModel.assertNotEqualsIgnoreCase(this.argModel);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.notEqualModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "<%s Automaton Model>.assertNotEqualsIgnoreCase(<%s Automaton Model>)", baseDescription, argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
