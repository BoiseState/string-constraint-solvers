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
public class Given_WeightedAutomatonModel_When_Trimmed {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 2)
    public WeightedAutomatonModel model;
    @Parameter(value = 1)
    public int expectedModelCount;
    private AutomatonModel trimModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.trim() - Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet(" ,A-D");
        int initialBoundLength = 3;

        // create automaton models
        WeightedAutomatonModel emptyModel = getEmptyWeightedModel(alphabet);
        WeightedAutomatonModel emptyStringModel = getEmptyStringWeightedModel(alphabet);
        WeightedAutomatonModel whitespaceConcreteModel = getConcreteWeightedModel(alphabet," A ");
        WeightedAutomatonModel noWhitespaceConcreteModel = getConcreteWeightedModel(alphabet,"ABC");
        WeightedAutomatonModel uniformModel = getUniformWeightedModel(alphabet, initialBoundLength);
        WeightedAutomatonModel nonUniformModel = getNonUniformWeightedModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
//                {"Empty", 0, emptyModel},
//                {"Empty String", 1, emptyStringModel},
//                {"Concrete Whitespace", 1, whitespaceConcreteModel},
//                {"Concrete No Whitespace", 1, noWhitespaceConcreteModel},
//                {"Uniform", 156, uniformModel},
//                {"Non-Uniform", 71, nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.trimModel = this.model.trim();
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.trimModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "<%s Automaton Model>.trim()", description);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
