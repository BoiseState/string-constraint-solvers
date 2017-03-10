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
public class Given_WeightedAutomatonModel_When_AssertingNotEqualsIgnoringCase {

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
    private AutomatonModel notEqualModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.assertNotEqualsIgnoreCase(<{1} Automaton Model>) - Expected MC = {2}")
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
                {"Empty String", "Empty String", 0, emptyStringModel, emptyStringModel},
                {"Empty String", "Concrete Lower", 1, emptyStringModel, concreteLowerModel},
                {"Empty String", "Concrete Upper", 1, emptyStringModel, concreteUpperModel},
                {"Empty String", "Uniform", 584, emptyStringModel, uniformModel},
                {"Empty String", "Non-uniform", 185, emptyStringModel, nonUniformModel},
                {"Concrete Lower", "Empty", 0, concreteLowerModel, emptyModel},
                {"Concrete Lower", "Empty String", 1, concreteLowerModel, emptyStringModel},
                {"Concrete Lower", "Concrete Lower", 0, concreteLowerModel, concreteLowerModel},
                {"Concrete Lower", "Concrete Upper", 0, concreteLowerModel, concreteUpperModel},
                {"Concrete Lower", "Uniform", 577, concreteLowerModel, uniformModel},
                {"Concrete Lower", "Non-uniform", 181, concreteLowerModel, nonUniformModel},
                {"Concrete Upper", "Empty", 0, concreteUpperModel, emptyModel},
                {"Concrete Upper", "Empty String", 1, concreteUpperModel, emptyStringModel},
                {"Concrete Upper", "Concrete Lower", 0, concreteUpperModel, concreteLowerModel},
                {"Concrete Upper", "Concrete Upper", 0, concreteUpperModel, concreteUpperModel},
                {"Concrete Upper", "Uniform", 577, concreteUpperModel, uniformModel},
                {"Concrete Upper", "Non-uniform", 181, concreteUpperModel, nonUniformModel},
                {"Uniform", "Empty", 0, uniformModel, emptyModel},
                {"Uniform", "Empty String", 584, uniformModel, emptyStringModel},
                {"Uniform", "Concrete Lower", 577, uniformModel, concreteLowerModel},
                {"Uniform", "Concrete Upper", 577, uniformModel, concreteUpperModel},
                {"Uniform", "Uniform", 337856, uniformModel, uniformModel},
                {"Uniform", "Non-uniform", 106811, uniformModel, nonUniformModel},
                {"Non-uniform", "Empty", 0, nonUniformModel, emptyModel},
                {"Non-uniform", "Empty String", 185, nonUniformModel, emptyStringModel},
                {"Non-uniform", "Concrete Lower", 181, nonUniformModel, concreteLowerModel},
                {"Non-uniform", "Concrete Upper", 181, nonUniformModel, concreteUpperModel},
                {"Non-uniform", "Uniform", 106811, nonUniformModel, uniformModel},
                {"Non-uniform", "Non-uniform", 33386, nonUniformModel, nonUniformModel}
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
        String reason = String.format( "Expected Model Count Invalid for <%s Automaton Model>.assertNotEqualsIgnoreCase(<%s Automaton Model>)",
                                       baseDescription,
                                       argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}