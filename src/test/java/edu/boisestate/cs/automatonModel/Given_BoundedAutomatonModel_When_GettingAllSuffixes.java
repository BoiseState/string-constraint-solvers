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
public class Given_BoundedAutomatonModel_When_GettingAllSuffixes {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 2)
    public BoundedAutomatonModel model;
    @Parameter(value = 1)
    public int expectedModelCount;
    private AutomatonModel deleteModel;

    @Parameters(name = "{index}: <{0} Automaton Model>.allSuffixes() - Expected MC = {1}")
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
                {"Empty", 0, emptyModel},
                {"Empty String", 1, emptyStringModel},
                {"Concrete", 4, concreteModel},
                {"Uniform", 85, uniformModel},
                {"Non-uniform", 58, nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.deleteModel = this.model.allSuffixes();

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.deleteModel.modelCount().intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
