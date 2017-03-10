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
public class Given_WeightedAutomatonModel_When_AssertingEqualsIgnoreCase {

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
    private AutomatonModel equalModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.assertEqualsIgnoreCase(<{1} Automaton Model>) - Expected MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D,a-d");
        int initialBoundLength = 3;

        // create automaton models
        WeightedAutomatonModel emptyModel = getEmptyWeightedModel(alphabet);
        WeightedAutomatonModel emptyStringModel = getEmptyStringWeightedModel(alphabet);
        WeightedAutomatonModel concreteLowerModel = getConcreteWeightedModel(alphabet,"abc");
        WeightedAutomatonModel concreteUpperModel = getConcreteWeightedModel(alphabet,"ABC");
        WeightedAutomatonModel uniformModel = getUniformWeightedModel(alphabet, initialBoundLength);
        WeightedAutomatonModel nonUniformModel = getNonUniformWeightedModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", 0, emptyModel, emptyModel},
                {"Empty", "Empty String", 0, emptyModel, emptyStringModel},
                {"Empty", "Concrete Lower", 0, emptyModel, concreteLowerModel},
                {"Empty", "Concrete Upper", 0, emptyModel, concreteUpperModel},
                {"Empty", "Uniform", 0, emptyModel, uniformModel},
                {"Empty", "Non-uniform", 0, emptyModel, nonUniformModel},
                {"Empty String", "Empty", 0, emptyStringModel, emptyModel},
                {"Empty String", "Empty String", 1, emptyStringModel, emptyStringModel},
                {"Empty String", "Concrete Lower", 0, emptyStringModel, concreteLowerModel},
                {"Empty String", "Concrete Upper", 0, emptyStringModel, concreteUpperModel},
                {"Empty String", "Uniform", 1, emptyStringModel, uniformModel},
                {"Empty String", "Non-uniform", 0, emptyStringModel, nonUniformModel},
                {"Concrete Lower", "Empty", 0, concreteLowerModel, emptyModel},
                {"Concrete Lower", "Empty String", 0, concreteLowerModel, emptyStringModel},
                {"Concrete Lower", "Concrete Lower", 1, concreteLowerModel, concreteLowerModel},
                {"Concrete Lower", "Concrete Upper", 1, concreteLowerModel, concreteUpperModel},
                {"Concrete Lower", "Uniform", 8, concreteLowerModel, uniformModel},
                {"Concrete Lower", "Non-uniform", 4, concreteLowerModel, nonUniformModel},
                {"Concrete Upper", "Empty", 0, concreteUpperModel, emptyModel},
                {"Concrete Upper", "Empty String", 0, concreteUpperModel, emptyStringModel},
                {"Concrete Upper", "Concrete Lower", 1, concreteUpperModel, concreteLowerModel},
                {"Concrete Upper", "Concrete Upper", 1, concreteUpperModel, concreteUpperModel},
                {"Concrete Upper", "Uniform", 8, concreteUpperModel, uniformModel},
                {"Concrete Upper", "Non-uniform", 4, concreteUpperModel, nonUniformModel},
                {"Uniform", "Empty", 0, uniformModel, emptyModel},
                {"Uniform", "Empty String", 1, uniformModel, emptyStringModel},
                {"Uniform", "Concrete Lower", 8, uniformModel, concreteLowerModel},
                {"Uniform", "Concrete Upper", 8, uniformModel, concreteUpperModel},
                {"Uniform", "Uniform", 4369, uniformModel, uniformModel},
                {"Uniform", "Non-uniform", 1414, uniformModel, nonUniformModel},
                {"Non-uniform", "Empty", 0, nonUniformModel, emptyModel},
                {"Non-uniform", "Empty String", 0, nonUniformModel, emptyStringModel},
                {"Non-uniform", "Concrete Lower", 4, nonUniformModel, concreteLowerModel},
                {"Non-uniform", "Concrete Upper", 4, nonUniformModel, concreteUpperModel},
                {"Non-uniform", "Uniform", 1414, nonUniformModel, uniformModel},
                {"Non-uniform", "Non-uniform", 839, nonUniformModel, nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.equalModel = this.baseModel.assertEqualsIgnoreCase(this.argModel);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.equalModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "Expected Model Count Invalid for <%s Automaton Model>.assertEqualsIgnoreCase(<%s Automaton Model>)",
                                       baseDescription,
                                       argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}