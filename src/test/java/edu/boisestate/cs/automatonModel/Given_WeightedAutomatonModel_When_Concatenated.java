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
public class Given_WeightedAutomatonModel_When_Concatenated {

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
    private AutomatonModel concatModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.concat(<{1} Automaton " +
                       "Model>) - Expected MC = {2}")
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
                {"Uniform", "Non-Uniform", 3825, uniformModel, nonUniformModel},
                {"Non-Uniform", "Empty", 0, nonUniformModel, emptyModel},
                {"Non-Uniform", "Empty String", 45, nonUniformModel, emptyStringModel},
                {"Non-Uniform", "Concrete", 45, nonUniformModel, concreteModel},
                {"Non-Uniform", "Uniform", 3825, nonUniformModel, uniformModel},
                {"Non-Uniform", "Non-Uniform", 2025, nonUniformModel, nonUniformModel}
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
