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
public class Given_UnboundedAutomatonModel_When_Subtracting {

    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 4)
    public UnboundedAutomatonModel argModel;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 3)
    public UnboundedAutomatonModel baseModel;
    @Parameter(value = 2)
    public int expectedModelCount;
    private AutomatonModel subtractedModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.minus(<{1} Automaton Model>) - Expected MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automaton models
        UnboundedAutomatonModel emptyModel = getEmptyUnboundedModel(alphabet);
        UnboundedAutomatonModel emptyStringModel = getEmptyStringUnboundedModel(alphabet);
        UnboundedAutomatonModel concreteModel = getConcreteUnboundedModel(alphabet,"ABC");
        UnboundedAutomatonModel uniformModel = getUniformUnboundedModel(alphabet, initialBoundLength);
        UnboundedAutomatonModel nonUniformModel = getNonUniformUnboundedModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", 0, emptyModel, emptyModel},
                {"Empty", "Empty String", 0, emptyModel, emptyStringModel},
                {"Empty", "Concrete", 0, emptyModel, concreteModel},
                {"Empty", "Uniform", 0, emptyModel, uniformModel},
                {"Empty", "Non-uniform", 0, emptyModel, nonUniformModel},
                {"Empty String", "Empty", 1, emptyStringModel, emptyModel},
                {"Empty String", "Empty String", 0, emptyStringModel, emptyStringModel},
                {"Empty String", "Concrete", 1, emptyStringModel, concreteModel},
                {"Empty String", "Uniform", 0, emptyStringModel, uniformModel},
                {"Empty String", "Non-uniform", 1, emptyStringModel, nonUniformModel},
                {"Concrete", "Empty", 1, concreteModel, emptyModel},
                {"Concrete", "Empty String", 1, concreteModel, emptyStringModel},
                {"Concrete", "Concrete", 0, concreteModel, concreteModel},
                {"Concrete", "Uniform", 0, concreteModel, uniformModel},
                {"Concrete", "Non-uniform", 0, concreteModel, nonUniformModel},
                {"Uniform", "Empty", 85, uniformModel, emptyModel},
                {"Uniform", "Empty String", 84, uniformModel, emptyStringModel},
                {"Uniform", "Concrete", 84, uniformModel, concreteModel},
                {"Uniform", "Uniform", 0, uniformModel, uniformModel},
                {"Uniform", "Non-uniform", 40, uniformModel, nonUniformModel},
                {"Non-uniform", "Empty", 45, nonUniformModel, emptyModel},
                {"Non-uniform", "Empty String", 45, nonUniformModel, emptyStringModel},
                {"Non-uniform", "Concrete", 44, nonUniformModel, concreteModel},
                {"Non-uniform", "Uniform", 0, nonUniformModel, uniformModel},
                {"Non-uniform", "Non-uniform", 0, nonUniformModel, nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.subtractedModel = this.baseModel.minus(this.argModel);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.subtractedModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "Expected Model Count Invalid for <%s Automaton Model>.minus(<%s Automaton Model>)",
                                       baseDescription,
                                       argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
