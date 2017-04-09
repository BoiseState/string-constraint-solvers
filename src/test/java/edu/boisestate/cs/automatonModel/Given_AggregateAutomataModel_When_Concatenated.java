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
public class Given_AggregateAutomataModel_When_Concatenated {

    @Parameter(value = 3)
    public AggregateAutomataModel baseModel;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 2)
    public int expectedModelCount;
    @Parameter(value = 4)
    public AggregateAutomataModel argModel;
    private AutomatonModel concatModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.concat(<{1} Automaton Model>) - Expected MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automaton models
        AggregateAutomataModel emptyModel = getEmptyAggregateModel(alphabet);
        AggregateAutomataModel emptyStringModel = getEmptyStringAggregateModel(alphabet);
        AggregateAutomataModel concreteModel = getConcreteAggregateModel(alphabet, "ABC");
        AggregateAutomataModel uniformModel = getUniformAggregateModel(alphabet, initialBoundLength);
        AggregateAutomataModel nonUniformModel = getNonUniformAggregateModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", 0, emptyModel, emptyModel},
                {"Empty", "Empty String", 0, emptyModel, emptyStringModel},
                {"Empty", "Concrete", 0, emptyModel, concreteModel},
                {"Empty", "Uniform", 0, emptyModel, uniformModel},
                {"Empty", "Non-Uniform", 0, emptyModel, nonUniformModel},
                {"Empty String", "Empty", 0, emptyStringModel, emptyModel},
                {"Empty String", "Empty String", 1, emptyStringModel, emptyStringModel},
                {"Empty String", "Concrete", 1, emptyStringModel, concreteModel},
                {"Empty String", "Uniform", 85, emptyStringModel, uniformModel},
                {"Empty String", "Non-Uniform", 45, emptyStringModel, nonUniformModel},
                {"Concrete", "Empty", 0, concreteModel, emptyModel},
                {"Concrete", "Empty String", 1, concreteModel, emptyStringModel},
                {"Concrete", "Concrete", 1, concreteModel, concreteModel},
                {"Concrete", "Uniform", 85, concreteModel, uniformModel},
                {"Concrete", "Non-Uniform", 45, concreteModel, nonUniformModel},
                {"Uniform", "Empty", 0, uniformModel, emptyModel},
                {"Uniform", "Empty String", 85, uniformModel, emptyStringModel},
                {"Uniform", "Concrete", 85, uniformModel, concreteModel},
                {"Uniform", "Uniform", 7225, uniformModel, uniformModel},
                {"Uniform", "Non-Uniform", 4122, uniformModel, nonUniformModel}, // over-approx
                {"Non-Uniform", "Empty", 0, nonUniformModel, emptyModel},
                {"Non-Uniform", "Empty String", 45, nonUniformModel, emptyStringModel},
                {"Non-Uniform", "Concrete", 45, nonUniformModel, concreteModel},
                {"Non-Uniform", "Uniform", 4122, nonUniformModel, uniformModel}, // over-approx
                {"Non-Uniform", "Non-Uniform", 2235, nonUniformModel, nonUniformModel} // over-approx
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.concatModel = this.baseModel.concatenate(this.argModel);
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.concatModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "<%s Automaton Model>.concatenate(<%s Automaton Model>)", baseDescription, argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
