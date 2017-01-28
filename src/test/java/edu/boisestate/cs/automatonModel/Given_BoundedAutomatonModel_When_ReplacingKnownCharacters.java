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
public class Given_BoundedAutomatonModel_When_ReplacingKnownCharacters {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 4)
    public char replace;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 2)
    public BoundedAutomatonModel model;
    @Parameter(value = 3)
    public char find;
    private AutomatonModel replacedModel;


    @Parameters(name = "{index}: <{0} Automaton Model>.replace('{3}', '{4}') - " +
                       "Expected MC = {1}")
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
                {"Empty", -1, emptyModel, 'A', 'A'},
                {"Empty", -1, emptyModel, 'A', 'B'},
                {"Empty", -1, emptyModel, 'A', 'C'},
                {"Empty", -1, emptyModel, 'A', 'D'},
                {"Empty", -1, emptyModel, 'B', 'A'},
                {"Empty", -1, emptyModel, 'B', 'B'},
                {"Empty", -1, emptyModel, 'B', 'C'},
                {"Empty", -1, emptyModel, 'B', 'D'},
                {"Empty", -1, emptyModel, 'C', 'A'},
                {"Empty", -1, emptyModel, 'C', 'B'},
                {"Empty", -1, emptyModel, 'C', 'C'},
                {"Empty", -1, emptyModel, 'C', 'D'},
                {"Empty", -1, emptyModel, 'D', 'A'},
                {"Empty", -1, emptyModel, 'D', 'B'},
                {"Empty", -1, emptyModel, 'D', 'C'},
                {"Empty", -1, emptyModel, 'D', 'D'},
                {"Empty String", -1, emptyStringModel, 'A', 'A'},
                {"Empty String", -1, emptyStringModel, 'A', 'B'},
                {"Empty String", -1, emptyStringModel, 'A', 'C'},
                {"Empty String", -1, emptyStringModel, 'A', 'D'},
                {"Empty String", -1, emptyStringModel, 'B', 'A'},
                {"Empty String", -1, emptyStringModel, 'B', 'B'},
                {"Empty String", -1, emptyStringModel, 'B', 'C'},
                {"Empty String", -1, emptyStringModel, 'B', 'D'},
                {"Empty String", -1, emptyStringModel, 'C', 'A'},
                {"Empty String", -1, emptyStringModel, 'C', 'B'},
                {"Empty String", -1, emptyStringModel, 'C', 'C'},
                {"Empty String", -1, emptyStringModel, 'C', 'D'},
                {"Empty String", -1, emptyStringModel, 'D', 'A'},
                {"Empty String", -1, emptyStringModel, 'D', 'B'},
                {"Empty String", -1, emptyStringModel, 'D', 'C'},
                {"Empty String", -1, emptyStringModel, 'D', 'D'},
                {"Concrete", -1, concreteModel, 'A', 'A'},
                {"Concrete", -1, concreteModel, 'A', 'B'},
                {"Concrete", -1, concreteModel, 'A', 'C'},
                {"Concrete", -1, concreteModel, 'A', 'D'},
                {"Concrete", -1, concreteModel, 'B', 'A'},
                {"Concrete", -1, concreteModel, 'B', 'B'},
                {"Concrete", -1, concreteModel, 'B', 'C'},
                {"Concrete", -1, concreteModel, 'B', 'D'},
                {"Concrete", -1, concreteModel, 'C', 'A'},
                {"Concrete", -1, concreteModel, 'C', 'B'},
                {"Concrete", -1, concreteModel, 'C', 'C'},
                {"Concrete", -1, concreteModel, 'C', 'D'},
                {"Concrete", -1, concreteModel, 'D', 'A'},
                {"Concrete", -1, concreteModel, 'D', 'B'},
                {"Concrete", -1, concreteModel, 'D', 'C'},
                {"Concrete", -1, concreteModel, 'D', 'D'},
                {"Uniform", -1, uniformModel, 'A', 'A'},
                {"Uniform", -1, uniformModel, 'A', 'B'},
                {"Uniform", -1, uniformModel, 'A', 'C'},
                {"Uniform", -1, uniformModel, 'A', 'D'},
                {"Uniform", -1, uniformModel, 'B', 'A'},
                {"Uniform", -1, uniformModel, 'B', 'B'},
                {"Uniform", -1, uniformModel, 'B', 'C'},
                {"Uniform", -1, uniformModel, 'B', 'D'},
                {"Uniform", -1, uniformModel, 'C', 'A'},
                {"Uniform", -1, uniformModel, 'C', 'B'},
                {"Uniform", -1, uniformModel, 'C', 'C'},
                {"Uniform", -1, uniformModel, 'C', 'D'},
                {"Uniform", -1, uniformModel, 'D', 'A'},
                {"Uniform", -1, uniformModel, 'D', 'B'},
                {"Uniform", -1, uniformModel, 'D', 'C'},
                {"Uniform", -1, uniformModel, 'D', 'D'},
                {"Non-uniform", -1, nonUniformModel, 'A', 'A'},
                {"Non-uniform", -1, nonUniformModel, 'A', 'B'},
                {"Non-uniform", -1, nonUniformModel, 'A', 'C'},
                {"Non-uniform", -1, nonUniformModel, 'A', 'D'},
                {"Non-uniform", -1, nonUniformModel, 'B', 'A'},
                {"Non-uniform", -1, nonUniformModel, 'B', 'B'},
                {"Non-uniform", -1, nonUniformModel, 'B', 'C'},
                {"Non-uniform", -1, nonUniformModel, 'B', 'D'},
                {"Non-uniform", -1, nonUniformModel, 'C', 'A'},
                {"Non-uniform", -1, nonUniformModel, 'C', 'B'},
                {"Non-uniform", -1, nonUniformModel, 'C', 'C'},
                {"Non-uniform", -1, nonUniformModel, 'C', 'D'},
                {"Non-uniform", -1, nonUniformModel, 'D', 'A'},
                {"Non-uniform", -1, nonUniformModel, 'D', 'B'},
                {"Non-uniform", -1, nonUniformModel, 'D', 'C'},
                {"Non-uniform", -1, nonUniformModel, 'D', 'D'}
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
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
