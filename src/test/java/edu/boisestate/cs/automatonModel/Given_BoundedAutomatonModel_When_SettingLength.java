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
public class Given_BoundedAutomatonModel_When_SettingLength {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 2)
    public BoundedAutomatonModel model;
    @Parameter(value = 3)
    public int length;
    private AutomatonModel lengthModel;


    @Parameters(name = "{index}: <{0} Automaton Model>.setLength({3}) - Expected MC = {1}")
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
                {"Empty", -1, emptyModel, 0},
                {"Empty", -1, emptyModel, 1},
                {"Empty", -1, emptyModel, 2},
                {"Empty", -1, emptyModel, 3},
                {"Empty String", -1, emptyStringModel, 0},
                {"Empty String", -1, emptyStringModel, 1},
                {"Empty String", -1, emptyStringModel, 2},
                {"Empty String", -1, emptyStringModel, 3},
                {"Concrete", -1, concreteModel, 0},
                {"Concrete", -1, concreteModel, 1},
                {"Concrete", -1, concreteModel, 2},
                {"Concrete", -1, concreteModel, 3},
                {"Uniform", -1, uniformModel, 0},
                {"Uniform", -1, uniformModel, 1},
                {"Uniform", -1, uniformModel, 2},
                {"Uniform", -1, uniformModel, 3},
                {"Non-uniform", -1, nonUniformModel, 0},
                {"Non-uniform", -1, nonUniformModel, 1},
                {"Non-uniform", -1, nonUniformModel, 2},
                {"Non-uniform", -1, nonUniformModel, 3},
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.lengthModel = this.model.setLength(this.length);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.lengthModel.modelCount().intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}