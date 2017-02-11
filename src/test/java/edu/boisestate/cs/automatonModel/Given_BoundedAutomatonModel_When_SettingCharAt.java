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
public class Given_BoundedAutomatonModel_When_SettingCharAt {

    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 5)
    public BoundedAutomatonModel argModel;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 3)
    public BoundedAutomatonModel baseModel;
    @Parameter(value = 2)
    public int expectedModelCount;
    @Parameter(value = 4)
    public int offset;
    private AutomatonModel charSetModel;

    @Parameters(name = "{index}: <{0} Automaton Model>.setCharAt({4}, <{1} Automaton Model>) - Expected MC = {3}")
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

        // create arg automaton models
        BoundedAutomatonModel aModel = getConcreteBoundedModel(alphabet, "A");
        BoundedAutomatonModel bModel = getConcreteBoundedModel(alphabet, "B");
        BoundedAutomatonModel cModel = getConcreteBoundedModel(alphabet, "C");
        BoundedAutomatonModel dModel = getConcreteBoundedModel(alphabet, "D");

        return Arrays.asList(new Object[][]{
                {"Empty", "'A'", -1, emptyModel, 0, aModel},
                {"Empty", "'B'", -1, emptyModel, 0, bModel},
                {"Empty", "'C'", -1, emptyModel, 0, cModel},
                {"Empty", "'D'", -1, emptyModel, 0, dModel},
                {"Empty", "'A'", -1, emptyModel, 1, aModel},
                {"Empty", "'B'", -1, emptyModel, 1, bModel},
                {"Empty", "'C'", -1, emptyModel, 1, cModel},
                {"Empty", "'D'", -1, emptyModel, 1, dModel},
                {"Empty", "'A'", -1, emptyModel, 2, aModel},
                {"Empty", "'B'", -1, emptyModel, 2, bModel},
                {"Empty", "'C'", -1, emptyModel, 2, cModel},
                {"Empty", "'D'", -1, emptyModel, 2, dModel},
                {"Empty String", "'A'", -1, emptyStringModel, 0, aModel},
                {"Empty String", "'B'", -1, emptyStringModel, 0, bModel},
                {"Empty String", "'C'", -1, emptyStringModel, 0, cModel},
                {"Empty String", "'D'", -1, emptyStringModel, 0, dModel},
                {"Empty String", "'A'", -1, emptyStringModel, 1, aModel},
                {"Empty String", "'B'", -1, emptyStringModel, 1, bModel},
                {"Empty String", "'C'", -1, emptyStringModel, 1, cModel},
                {"Empty String", "'D'", -1, emptyStringModel, 1, dModel},
                {"Empty String", "'A'", -1, emptyStringModel, 2, aModel},
                {"Empty String", "'B'", -1, emptyStringModel, 2, bModel},
                {"Empty String", "'C'", -1, emptyStringModel, 2, cModel},
                {"Empty String", "'D'", -1, emptyStringModel, 2, dModel},
                {"Concrete", "'A'", -1, concreteModel, 0, aModel},
                {"Concrete", "'B'", -1, concreteModel, 0, bModel},
                {"Concrete", "'C'", -1, concreteModel, 0, cModel},
                {"Concrete", "'D'", -1, concreteModel, 0, dModel},
                {"Concrete", "'A'", -1, concreteModel, 1, aModel},
                {"Concrete", "'B'", -1, concreteModel, 1, bModel},
                {"Concrete", "'C'", -1, concreteModel, 1, cModel},
                {"Concrete", "'D'", -1, concreteModel, 1, dModel},
                {"Concrete", "'A'", -1, concreteModel, 2, aModel},
                {"Concrete", "'B'", -1, concreteModel, 2, bModel},
                {"Concrete", "'C'", -1, concreteModel, 2, cModel},
                {"Concrete", "'D'", -1, concreteModel, 2, dModel},
                {"Uniform", "'A'", -1, uniformModel, 0, aModel},
                {"Uniform", "'B'", -1, uniformModel, 0, bModel},
                {"Uniform", "'C'", -1, uniformModel, 0, cModel},
                {"Uniform", "'D'", -1, uniformModel, 0, dModel},
                {"Uniform", "'A'", -1, uniformModel, 1, aModel},
                {"Uniform", "'B'", -1, uniformModel, 1, bModel},
                {"Uniform", "'C'", -1, uniformModel, 1, cModel},
                {"Uniform", "'D'", -1, uniformModel, 1, dModel},
                {"Uniform", "'A'", -1, uniformModel, 2, aModel},
                {"Uniform", "'B'", -1, uniformModel, 2, bModel},
                {"Uniform", "'C'", -1, uniformModel, 2, cModel},
                {"Uniform", "'D'", -1, uniformModel, 2, dModel},
                {"Non-uniform", "'A'", -1, nonUniformModel, 0, aModel},
                {"Non-uniform", "'B'", -1, nonUniformModel, 0, bModel},
                {"Non-uniform", "'C'", -1, nonUniformModel, 0, cModel},
                {"Non-uniform", "'D'", -1, nonUniformModel, 0, dModel},
                {"Non-uniform", "'A'", -1, nonUniformModel, 1, aModel},
                {"Non-uniform", "'B'", -1, nonUniformModel, 1, bModel},
                {"Non-uniform", "'C'", -1, nonUniformModel, 1, cModel},
                {"Non-uniform", "'D'", -1, nonUniformModel, 1, dModel},
                {"Non-uniform", "'A'", -1, nonUniformModel, 2, aModel},
                {"Non-uniform", "'B'", -1, nonUniformModel, 2, bModel},
                {"Non-uniform", "'C'", -1, nonUniformModel, 2, cModel},
                {"Non-uniform", "'D'", -1, nonUniformModel, 2, dModel},
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
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
