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
public class Given_WeightedAutomatonModel_When_SettingCharAt {

    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 5)
    public WeightedAutomatonModel argModel;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 3)
    public WeightedAutomatonModel baseModel;
    @Parameter(value = 2)
    public int expectedModelCount;
    @Parameter(value = 4)
    public int offset;
    private AutomatonModel charSetModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.setCharAt({4}, <{1} Automaton Model>) - Expected MC = {3}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create baseAutomaton models
        WeightedAutomatonModel emptyModel = getEmptyWeightedModel(alphabet);
        WeightedAutomatonModel emptyStringModel = getEmptyStringWeightedModel(alphabet);
        WeightedAutomatonModel concreteModel = getConcreteWeightedModel(alphabet,"ABC");
        WeightedAutomatonModel uniformModel = getUniformWeightedModel(alphabet, initialBoundLength);
        WeightedAutomatonModel nonUniformModel = getNonUniformWeightedModel(alphabet, initialBoundLength);

        // create arg baseAutomaton models
        WeightedAutomatonModel aModel = getConcreteWeightedModel(alphabet, "A");
        WeightedAutomatonModel bModel = getConcreteWeightedModel(alphabet, "B");
        WeightedAutomatonModel cModel = getConcreteWeightedModel(alphabet, "C");
        WeightedAutomatonModel dModel = getConcreteWeightedModel(alphabet, "D");

        return Arrays.asList(new Object[][]{
                {"Empty", "'A'", 0, emptyModel, 0, aModel},
                {"Empty", "'B'", 0, emptyModel, 0, bModel},
                {"Empty", "'C'", 0, emptyModel, 0, cModel},
                {"Empty", "'D'", 0, emptyModel, 0, dModel},
                {"Empty", "'A'", 0, emptyModel, 1, aModel},
                {"Empty", "'B'", 0, emptyModel, 1, bModel},
                {"Empty", "'C'", 0, emptyModel, 1, cModel},
                {"Empty", "'D'", 0, emptyModel, 1, dModel},
                {"Empty", "'A'", 0, emptyModel, 2, aModel},
                {"Empty", "'B'", 0, emptyModel, 2, bModel},
                {"Empty", "'C'", 0, emptyModel, 2, cModel},
                {"Empty", "'D'", 0, emptyModel, 2, dModel},
                {"Empty String", "'A'", 0, emptyStringModel, 0, aModel},
                {"Empty String", "'B'", 0, emptyStringModel, 0, bModel},
                {"Empty String", "'C'", 0, emptyStringModel, 0, cModel},
                {"Empty String", "'D'", 0, emptyStringModel, 0, dModel},
                {"Empty String", "'A'", 0, emptyStringModel, 1, aModel},
                {"Empty String", "'B'", 0, emptyStringModel, 1, bModel},
                {"Empty String", "'C'", 0, emptyStringModel, 1, cModel},
                {"Empty String", "'D'", 0, emptyStringModel, 1, dModel},
                {"Empty String", "'A'", 0, emptyStringModel, 2, aModel},
                {"Empty String", "'B'", 0, emptyStringModel, 2, bModel},
                {"Empty String", "'C'", 0, emptyStringModel, 2, cModel},
                {"Empty String", "'D'", 0, emptyStringModel, 2, dModel},
                {"Concrete", "'A'", 2, concreteModel, 0, aModel},
                {"Concrete", "'B'", 2, concreteModel, 0, bModel},
                {"Concrete", "'C'", 2, concreteModel, 0, cModel},
                {"Concrete", "'D'", 2, concreteModel, 0, dModel},
                {"Concrete", "'A'", 2, concreteModel, 1, aModel},
                {"Concrete", "'B'", 2, concreteModel, 1, bModel},
                {"Concrete", "'C'", 2, concreteModel, 1, cModel},
                {"Concrete", "'D'", 2, concreteModel, 1, dModel},
                {"Concrete", "'A'", 2, concreteModel, 2, aModel},
                {"Concrete", "'B'", 2, concreteModel, 2, bModel},
                {"Concrete", "'C'", 2, concreteModel, 2, cModel},
                {"Concrete", "'D'", 2, concreteModel, 2, dModel},
                {"Uniform", "'A'", 84, uniformModel, 0, aModel},
                {"Uniform", "'B'", 84, uniformModel, 0, bModel},
                {"Uniform", "'C'", 84, uniformModel, 0, cModel},
                {"Uniform", "'D'", 84, uniformModel, 0, dModel},
                {"Uniform", "'A'", 80, uniformModel, 1, aModel},
                {"Uniform", "'B'", 80, uniformModel, 1, bModel},
                {"Uniform", "'C'", 80, uniformModel, 1, cModel},
                {"Uniform", "'D'", 80, uniformModel, 1, dModel},
                {"Uniform", "'A'", 64, uniformModel, 2, aModel},
                {"Uniform", "'B'", 64, uniformModel, 2, bModel},
                {"Uniform", "'C'", 64, uniformModel, 2, cModel},
                {"Uniform", "'D'", 64, uniformModel, 2, dModel},
                {"Non-uniform", "'A'", 45, nonUniformModel, 0, aModel},
                {"Non-uniform", "'B'", 45, nonUniformModel, 0, bModel},
                {"Non-uniform", "'C'", 45, nonUniformModel, 0, cModel},
                {"Non-uniform", "'D'", 45, nonUniformModel, 0, dModel},
                {"Non-uniform", "'A'", 44, nonUniformModel, 1, aModel},
                {"Non-uniform", "'B'", 44, nonUniformModel, 1, bModel},
                {"Non-uniform", "'C'", 44, nonUniformModel, 1, cModel},
                {"Non-uniform", "'D'", 44, nonUniformModel, 1, dModel},
                {"Non-uniform", "'A'", 37, nonUniformModel, 2, aModel},
                {"Non-uniform", "'B'", 37, nonUniformModel, 2, bModel},
                {"Non-uniform", "'C'", 37, nonUniformModel, 2, cModel},
                {"Non-uniform", "'D'", 37, nonUniformModel, 2, dModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.charSetModel = this.baseModel.setCharAt(this.offset, this.argModel);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.charSetModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "<%s Automaton Model>.setCharAt(%d, <%s Automaton Model>)", baseDescription, offset, argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
