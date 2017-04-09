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
public class Given_WeightedAutomatonModel_When_ReplacingKnownCharacters {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 4)
    public char replace;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 2)
    public WeightedAutomatonModel model;
    @Parameter(value = 3)
    public char find;
    private AutomatonModel replacedModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.replace({3}, {4}) - Expected MC = {1}")
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
                {"Empty", 0, emptyModel, 'A', 'A'},
                {"Empty", 0, emptyModel, 'A', 'B'},
                {"Empty", 0, emptyModel, 'A', 'C'},
                {"Empty", 0, emptyModel, 'A', 'D'},
                {"Empty", 0, emptyModel, 'B', 'A'},
                {"Empty", 0, emptyModel, 'B', 'B'},
                {"Empty", 0, emptyModel, 'B', 'C'},
                {"Empty", 0, emptyModel, 'B', 'D'},
                {"Empty", 0, emptyModel, 'C', 'A'},
                {"Empty", 0, emptyModel, 'C', 'B'},
                {"Empty", 0, emptyModel, 'C', 'C'},
                {"Empty", 0, emptyModel, 'C', 'D'},
                {"Empty", 0, emptyModel, 'D', 'A'},
                {"Empty", 0, emptyModel, 'D', 'B'},
                {"Empty", 0, emptyModel, 'D', 'C'},
                {"Empty", 0, emptyModel, 'D', 'D'},
                {"Empty String", 1, emptyStringModel, 'A', 'A'},
                {"Empty String", 1, emptyStringModel, 'A', 'B'},
                {"Empty String", 1, emptyStringModel, 'A', 'C'},
                {"Empty String", 1, emptyStringModel, 'A', 'D'},
                {"Empty String", 1, emptyStringModel, 'B', 'A'},
                {"Empty String", 1, emptyStringModel, 'B', 'B'},
                {"Empty String", 1, emptyStringModel, 'B', 'C'},
                {"Empty String", 1, emptyStringModel, 'B', 'D'},
                {"Empty String", 1, emptyStringModel, 'C', 'A'},
                {"Empty String", 1, emptyStringModel, 'C', 'B'},
                {"Empty String", 1, emptyStringModel, 'C', 'C'},
                {"Empty String", 1, emptyStringModel, 'C', 'D'},
                {"Empty String", 1, emptyStringModel, 'D', 'A'},
                {"Empty String", 1, emptyStringModel, 'D', 'B'},
                {"Empty String", 1, emptyStringModel, 'D', 'C'},
                {"Empty String", 1, emptyStringModel, 'D', 'D'},
                {"Concrete", 1, concreteModel, 'A', 'A'},
                {"Concrete", 1, concreteModel, 'A', 'B'},
                {"Concrete", 1, concreteModel, 'A', 'C'},
                {"Concrete", 1, concreteModel, 'A', 'D'},
                {"Concrete", 1, concreteModel, 'B', 'A'},
                {"Concrete", 1, concreteModel, 'B', 'B'},
                {"Concrete", 1, concreteModel, 'B', 'C'},
                {"Concrete", 1, concreteModel, 'B', 'D'},
                {"Concrete", 1, concreteModel, 'C', 'A'},
                {"Concrete", 1, concreteModel, 'C', 'B'},
                {"Concrete", 1, concreteModel, 'C', 'C'},
                {"Concrete", 1, concreteModel, 'C', 'D'},
                {"Concrete", 1, concreteModel, 'D', 'A'},
                {"Concrete", 1, concreteModel, 'D', 'B'},
                {"Concrete", 1, concreteModel, 'D', 'C'},
                {"Concrete", 1, concreteModel, 'D', 'D'},
                {"Uniform", 85, uniformModel, 'A', 'A'},
                {"Uniform", 85, uniformModel, 'A', 'B'},
                {"Uniform", 85, uniformModel, 'A', 'C'},
                {"Uniform", 85, uniformModel, 'A', 'D'},
                {"Uniform", 85, uniformModel, 'B', 'A'},
                {"Uniform", 85, uniformModel, 'B', 'B'},
                {"Uniform", 85, uniformModel, 'B', 'C'},
                {"Uniform", 85, uniformModel, 'B', 'D'},
                {"Uniform", 85, uniformModel, 'C', 'A'},
                {"Uniform", 85, uniformModel, 'C', 'B'},
                {"Uniform", 85, uniformModel, 'C', 'C'},
                {"Uniform", 85, uniformModel, 'C', 'D'},
                {"Uniform", 85, uniformModel, 'D', 'A'},
                {"Uniform", 85, uniformModel, 'D', 'B'},
                {"Uniform", 85, uniformModel, 'D', 'C'},
                {"Uniform", 85, uniformModel, 'D', 'D'},
                {"Non-Uniform", 45, nonUniformModel, 'A', 'A'},
                {"Non-Uniform", 45, nonUniformModel, 'A', 'B'},
                {"Non-Uniform", 45, nonUniformModel, 'A', 'C'},
                {"Non-Uniform", 45, nonUniformModel, 'A', 'D'},
                {"Non-Uniform", 45, nonUniformModel, 'B', 'A'},
                {"Non-Uniform", 45, nonUniformModel, 'B', 'B'},
                {"Non-Uniform", 45, nonUniformModel, 'B', 'C'},
                {"Non-Uniform", 45, nonUniformModel, 'B', 'D'},
                {"Non-Uniform", 45, nonUniformModel, 'C', 'A'},
                {"Non-Uniform", 45, nonUniformModel, 'C', 'B'},
                {"Non-Uniform", 45, nonUniformModel, 'C', 'C'},
                {"Non-Uniform", 45, nonUniformModel, 'C', 'D'},
                {"Non-Uniform", 45, nonUniformModel, 'D', 'A'},
                {"Non-Uniform", 45, nonUniformModel, 'D', 'B'},
                {"Non-Uniform", 45, nonUniformModel, 'D', 'C'},
                {"Non-Uniform", 45, nonUniformModel, 'D', 'D'}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.replacedModel =
                this.model.replace(this.find, this.replace);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.replacedModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "<%s Automaton Model>.replace(%c, %c)", description, find, replace);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
