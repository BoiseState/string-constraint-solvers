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
import static edu.boisestate.cs.automatonModel.AutomatonTestUtilities
        .getNonUniformBoundedModel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_BoundedAutomatonModel_When_Complemented {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 2)
    public BoundedAutomatonModel model;
    @Parameter(value = 1)
    public int expectedModelCount;
    private AutomatonModel complementModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.complement() - Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automaton models
        BoundedAutomatonModel emptyModel = getEmptyBoundedModel(alphabet);
        BoundedAutomatonModel emptyStringModel = getEmptyStringBoundedModel(alphabet);
        BoundedAutomatonModel concreteModel = getConcreteBoundedModel(alphabet,"ABC");
        BoundedAutomatonModel uniformModel = getUniformBoundedModel(alphabet, initialBoundLength);
        BoundedAutomatonModel nonUniformModel = getNonUniformBoundedModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", 85, emptyModel},
                {"Empty String", 84, emptyStringModel},
                {"Concrete", 84, concreteModel},
                {"Uniform", 0, uniformModel},
                {"Non-uniform", 40, nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.complementModel = this.model.complement(3);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.complementModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "Expected Model Count Invalid for <%s Automaton Model>.complement()",
                                       description);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
