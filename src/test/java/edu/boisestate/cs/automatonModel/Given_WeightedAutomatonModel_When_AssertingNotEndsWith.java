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
public class Given_WeightedAutomatonModel_When_AssertingNotEndsWith {

    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 4)
    public WeightedAutomatonModel argModel;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 3)
    public WeightedAutomatonModel baseModel;
    @Parameter(value = 2)
    public int expectedModelCount;
    private AutomatonModel notEndsWithModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.assertNotEndsWith(<{1} Automaton Model>) - Expected MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automaton models
        WeightedAutomatonModel emptyModel = getEmptyWeightedModel(alphabet);
        WeightedAutomatonModel emptyStringModel = getEmptyStringWeightedModel(alphabet);
        WeightedAutomatonModel concreteModel = getConcreteWeightedModel(alphabet,"ABC");
        WeightedAutomatonModel uniformModel = getUniformWeightedModel(alphabet, initialBoundLength);
        WeightedAutomatonModel nonUniformModel = getNonUniformWeightedModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", -1, emptyModel, emptyModel},
                {"Empty", "Empty String", -1, emptyModel, emptyStringModel},
                {"Empty", "Concrete", -1, emptyModel, concreteModel},
                {"Empty", "Uniform", -1, emptyModel, uniformModel},
                {"Empty", "Non-uniform", -1, emptyModel, nonUniformModel},
                {"Empty String", "Empty", -1, emptyStringModel, emptyModel},
                {"Empty String", "Empty String", -1, emptyStringModel, emptyStringModel},
                {"Empty String", "Concrete", -1, emptyStringModel, concreteModel},
                {"Empty String", "Uniform", -1, emptyStringModel, uniformModel},
                {"Empty String", "Non-uniform", -1, emptyStringModel, nonUniformModel},
                {"Concrete", "Empty", -1, concreteModel, emptyModel},
                {"Concrete", "Empty String", -1, concreteModel, emptyStringModel},
                {"Concrete", "Concrete", -1, concreteModel, concreteModel},
                {"Concrete", "Uniform", -1, concreteModel, uniformModel},
                {"Concrete", "Non-uniform", -1, concreteModel, nonUniformModel},
                {"Uniform", "Empty", -1, uniformModel, emptyModel},
                {"Uniform", "Empty String", -1, uniformModel, emptyStringModel},
                {"Uniform", "Concrete", -1, uniformModel, concreteModel},
                {"Uniform", "Uniform", -1, uniformModel, uniformModel},
                {"Uniform", "Non-uniform", -1, uniformModel, nonUniformModel},
                {"Non-uniform", "Empty", -1, nonUniformModel, emptyModel},
                {"Non-uniform", "Empty String", -1, nonUniformModel, emptyStringModel},
                {"Non-uniform", "Concrete", -1, nonUniformModel, concreteModel},
                {"Non-uniform", "Uniform", -1, nonUniformModel, uniformModel},
                {"Non-uniform", "Non-uniform", -1, nonUniformModel, nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.notEndsWithModel = this.baseModel.assertNotEndsWith(this.argModel);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.notEndsWithModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "Expected Model Count Invalid for <%s Automaton Model>.assertNotEndsWith(<%s Automaton Model>)",
                                       baseDescription,
                                       argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
