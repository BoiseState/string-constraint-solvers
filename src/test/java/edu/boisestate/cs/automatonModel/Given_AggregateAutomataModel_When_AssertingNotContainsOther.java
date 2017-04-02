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
public class Given_AggregateAutomataModel_When_AssertingNotContainsOther {

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
    private AutomatonModel notContainsModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.assertNotContainsOther(<{1} Automaton Model>) - Expected MC = {2}")
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
                {"Empty", "Empty", 0, emptyModel, emptyModel},
                {"Empty", "Empty String", 0, emptyModel, emptyStringModel},
                {"Empty", "Concrete", 0, emptyModel, concreteModel},
                {"Empty", "Uniform", 0, emptyModel, uniformModel},
                {"Empty", "Non-uniform", 0, emptyModel, nonUniformModel},
                {"Empty String", "Empty", 1, emptyStringModel, emptyModel},
                {"Empty String", "Empty String", 0, emptyStringModel, emptyStringModel},
                {"Empty String", "Concrete", 1, emptyStringModel, concreteModel},
                {"Empty String", "Uniform", 1, emptyStringModel, uniformModel},
                {"Empty String", "Non-uniform", 1, emptyStringModel, nonUniformModel},
                {"Concrete", "Empty", 1, concreteModel, emptyModel},
                {"Concrete", "Empty String", 0, concreteModel, emptyStringModel},
                {"Concrete", "Concrete", 0, concreteModel, concreteModel},
                {"Concrete", "Uniform", 1, concreteModel, uniformModel},
                {"Concrete", "Non-uniform", 1, concreteModel, nonUniformModel},
                {"Uniform", "Empty", 85, uniformModel, emptyModel},
                {"Uniform", "Empty String", 0, uniformModel, emptyStringModel},
                {"Uniform", "Concrete", 84, uniformModel, concreteModel},
                {"Uniform", "Uniform", 85, uniformModel, uniformModel},
                {"Uniform", "Non-uniform", 85, uniformModel, nonUniformModel},
                {"Non-uniform", "Empty", 45, nonUniformModel, emptyModel},
                {"Non-uniform", "Empty String", 0, nonUniformModel, emptyStringModel},
                {"Non-uniform", "Concrete", 44, nonUniformModel, concreteModel},
                {"Non-uniform", "Uniform", 45, nonUniformModel, uniformModel},
                {"Non-uniform", "Non-uniform", 45, nonUniformModel, nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.notContainsModel = this.baseModel.assertNotContainsOther(this.argModel);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.notContainsModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "<%s Automaton Model>.assertNotContainsOther(<%s Automaton Model>)",
                                       baseDescription,
                                       argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
