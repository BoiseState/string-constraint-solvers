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
public class Given_AggregateAutomataModel_When_SettingCharAt {

    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 5)
    public AggregateAutomataModel argModel;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 3)
    public AggregateAutomataModel baseModel;
    @Parameter(value = 2)
    public int expectedModelCount;
    @Parameter(value = 4)
    public int offset;
    private AutomatonModel charSetModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.setCharAt({4}, <{1} Automaton Model>) - Expected MC = {3}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create baseAutomaton models
        AggregateAutomataModel emptyModel = getEmptyAggregateModel(alphabet);
        AggregateAutomataModel emptyStringModel = getEmptyStringAggregateModel(alphabet);
        AggregateAutomataModel concreteModel = getConcreteAggregateModel(alphabet,"ABC");
        AggregateAutomataModel uniformModel = getUniformAggregateModel(alphabet, initialBoundLength);
        AggregateAutomataModel nonUniformModel = getNonUniformAggregateModel(alphabet, initialBoundLength);

        // create arg baseAutomaton models
        AggregateAutomataModel aModel = getConcreteAggregateModel(alphabet, "A");
        AggregateAutomataModel bModel = getConcreteAggregateModel(alphabet, "B");
        AggregateAutomataModel cModel = getConcreteAggregateModel(alphabet, "C");
        AggregateAutomataModel dModel = getConcreteAggregateModel(alphabet, "D");

        return Arrays.asList(new Object[][]{
                {"Empty", "A", 0, emptyModel, 0, aModel},
                {"Empty", "B", 0, emptyModel, 0, bModel},
                {"Empty", "C", 0, emptyModel, 0, cModel},
                {"Empty", "D", 0, emptyModel, 0, dModel},
                {"Empty", "A", 0, emptyModel, 1, aModel},
                {"Empty", "B", 0, emptyModel, 1, bModel},
                {"Empty", "C", 0, emptyModel, 1, cModel},
                {"Empty", "D", 0, emptyModel, 1, dModel},
                {"Empty", "A", 0, emptyModel, 2, aModel},
                {"Empty", "B", 0, emptyModel, 2, bModel},
                {"Empty", "C", 0, emptyModel, 2, cModel},
                {"Empty", "D", 0, emptyModel, 2, dModel},
                {"Empty String", "A", 0, emptyStringModel, 0, aModel},
                {"Empty String", "B", 0, emptyStringModel, 0, bModel},
                {"Empty String", "C", 0, emptyStringModel, 0, cModel},
                {"Empty String", "D", 0, emptyStringModel, 0, dModel},
                {"Empty String", "A", 0, emptyStringModel, 1, aModel},
                {"Empty String", "B", 0, emptyStringModel, 1, bModel},
                {"Empty String", "C", 0, emptyStringModel, 1, cModel},
                {"Empty String", "D", 0, emptyStringModel, 1, dModel},
                {"Empty String", "A", 0, emptyStringModel, 2, aModel},
                {"Empty String", "B", 0, emptyStringModel, 2, bModel},
                {"Empty String", "C", 0, emptyStringModel, 2, cModel},
                {"Empty String", "D", 0, emptyStringModel, 2, dModel},
                {"Concrete", "A", 1, concreteModel, 0, aModel},
                {"Concrete", "B", 1, concreteModel, 0, bModel},
                {"Concrete", "C", 1, concreteModel, 0, cModel},
                {"Concrete", "D", 1, concreteModel, 0, dModel},
                {"Concrete", "A", 1, concreteModel, 1, aModel},
                {"Concrete", "B", 1, concreteModel, 1, bModel},
                {"Concrete", "C", 1, concreteModel, 1, cModel},
                {"Concrete", "D", 1, concreteModel, 1, dModel},
                {"Concrete", "A", 1, concreteModel, 2, aModel},
                {"Concrete", "B", 1, concreteModel, 2, bModel},
                {"Concrete", "C", 1, concreteModel, 2, cModel},
                {"Concrete", "D", 1, concreteModel, 2, dModel},
                {"Uniform", "A", 21, uniformModel, 0, aModel},
                {"Uniform", "B", 21, uniformModel, 0, bModel},
                {"Uniform", "C", 21, uniformModel, 0, cModel},
                {"Uniform", "D", 21, uniformModel, 0, dModel},
                {"Uniform", "A", 20, uniformModel, 1, aModel},
                {"Uniform", "B", 20, uniformModel, 1, bModel},
                {"Uniform", "C", 20, uniformModel, 1, cModel},
                {"Uniform", "D", 20, uniformModel, 1, dModel},
                {"Uniform", "A", 16, uniformModel, 2, aModel},
                {"Uniform", "B", 16, uniformModel, 2, bModel},
                {"Uniform", "C", 16, uniformModel, 2, cModel},
                {"Uniform", "D", 16, uniformModel, 2, dModel},
                {"Non-Uniform", "A", 21, nonUniformModel, 0, aModel},
                {"Non-Uniform", "B", 21, nonUniformModel, 0, bModel},
                {"Non-Uniform", "C", 21, nonUniformModel, 0, cModel},
                {"Non-Uniform", "D", 21, nonUniformModel, 0, dModel},
                {"Non-Uniform", "A", 20, nonUniformModel, 1, aModel},
                {"Non-Uniform", "B", 20, nonUniformModel, 1, bModel},
                {"Non-Uniform", "C", 20, nonUniformModel, 1, cModel},
                {"Non-Uniform", "D", 20, nonUniformModel, 1, dModel},
                {"Non-Uniform", "A", 16, nonUniformModel, 2, aModel},
                {"Non-Uniform", "B", 16, nonUniformModel, 2, bModel},
                {"Non-Uniform", "C", 16, nonUniformModel, 2, cModel},
                {"Non-Uniform", "D", 16, nonUniformModel, 2, dModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.charSetModel = this.baseModel.setCharAt(this.offset, this.argModel);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.charSetModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "<%s Automaton Model>.setCharAt(%d, <%s Automaton Model>)", baseDescription, offset, argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}