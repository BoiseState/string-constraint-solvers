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
public class Given_BoundedAutomatonModel_When_GettingPrefix {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 2)
    public BoundedAutomatonModel model;
    @Parameter(value = 3)
    public int end;
    private AutomatonModel prefixModel;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.prefix({3}) - Expected MC = {1}")
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
                {"Empty", 0, emptyModel, 0},
                {"Empty", 0, emptyModel, 1},
                {"Empty", 0, emptyModel, 2},
                {"Empty", 0, emptyModel, 3},
                {"Empty String", 1, emptyStringModel, 0},
                {"Empty String", 0, emptyStringModel, 1},
                {"Empty String", 0, emptyStringModel, 2},
                {"Empty String", 0, emptyStringModel, 3},
                {"Concrete", 1, concreteModel, 0},
                {"Concrete", 1, concreteModel, 1},
                {"Concrete", 1, concreteModel, 2},
                {"Concrete", 1, concreteModel, 3},
                {"Uniform", 1, uniformModel, 0},
                {"Uniform", 4, uniformModel, 1},
                {"Uniform", 16, uniformModel, 2},
                {"Uniform", 64, uniformModel, 3},
                {"Non-uniform", 1, nonUniformModel, 0},
                {"Non-uniform", 4, nonUniformModel, 1},
                {"Non-uniform", 16, nonUniformModel, 2},
                {"Non-uniform", 37, nonUniformModel, 3}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.prefixModel = this.model.prefix(this.end);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.prefixModel.modelCount().intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
