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
public class Given_UnboundedAutomatonModel_When_AssertingEmpty {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 2)
    public UnboundedAutomatonModel model;
    private AutomatonModel emptyModel;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.assertEmpty() - Expected MC = {1}")
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
                {"Empty", 0, emptyModel},
                {"Empty String", 1, emptyStringModel},
                {"Concrete", 0, concreteModel},
                {"Uniform", 1, uniformModel},
                {"Non-uniform", 0, nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.emptyModel = this.model.assertEmpty();

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.emptyModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "Expected Model Count Invalid for <%s Automaton Model>.isEmpty()",
                                       description);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
