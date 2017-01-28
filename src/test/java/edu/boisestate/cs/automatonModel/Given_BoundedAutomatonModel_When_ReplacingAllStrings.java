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
public class Given_BoundedAutomatonModel_When_ReplacingAllStrings {

    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 1)
    public String findDescription;
    @Parameter(value = 2)
    public String replaceDescription;
    @Parameter(value = 6)
    public BoundedAutomatonModel replaceModel;
    @Parameter(value = 3)
    public int expectedModelCount;
    @Parameter(value = 4)
    public BoundedAutomatonModel baseModel;
    @Parameter(value = 5)
    public BoundedAutomatonModel findModel;
    private AutomatonModel replacedModel;


    @Parameters(name = "{index}: <{0} Automaton Model>.replace({1} Automaton, {2} Automaton) - " +
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
//                {"Empty", "Empty", "Empty", -1, emptyModel, emptyModel, emptyModel},
//                {"Empty", "Empty", "Empty String", -1, emptyModel, emptyModel, emptyStringModel},
//                {"Empty", "Empty", "Concrete", -1, emptyModel, emptyModel, concreteModel},
//                {"Empty", "Empty", "Uniform", -1, emptyModel, emptyModel, uniformModel},
//                {"Empty", "Empty", "Non-uniform", -1, emptyModel, emptyModel, nonUniformModel},
//                {"Empty", "Empty String", "Empty", -1, emptyModel, emptyStringModel, emptyModel},
//                {"Empty", "Empty String", "Empty String", -1, emptyModel, emptyStringModel, emptyStringModel},
//                {"Empty", "Empty String", "Concrete", -1, emptyModel, emptyStringModel, concreteModel},
//                {"Empty", "Empty String", "Uniform", -1, emptyModel, emptyStringModel, uniformModel},
//                {"Empty", "Empty String", "Non-uniform", -1, emptyModel, emptyStringModel, nonUniformModel},
//                {"Empty", "Concrete", "Empty", -1, emptyModel, concreteModel, emptyModel},
//                {"Empty", "Concrete", "Empty String", -1, emptyModel, concreteModel, emptyStringModel},
//                {"Empty", "Concrete", "Concrete", -1, emptyModel, concreteModel, concreteModel},
//                {"Empty", "Concrete", "Uniform", -1, emptyModel, concreteModel, uniformModel},
//                {"Empty", "Concrete", "Non-uniform", -1, emptyModel, concreteModel, nonUniformModel},
//                {"Empty", "Uniform", "Empty", -1, emptyModel, uniformModel, emptyModel},
//                {"Empty", "Uniform", "Empty String", -1, emptyModel, uniformModel, emptyStringModel},
//                {"Empty", "Uniform", "Concrete", -1, emptyModel, uniformModel, concreteModel},
//                {"Empty", "Uniform", "Uniform", -1, emptyModel, uniformModel, uniformModel},
//                {"Empty", "Uniform", "Non-uniform", -1, emptyModel, uniformModel, nonUniformModel},
//                {"Empty", "Non-uniform", "Empty", -1, emptyModel, nonUniformModel, emptyModel},
//                {"Empty", "Non-uniform", "Empty String", -1, emptyModel, nonUniformModel, emptyStringModel},
//                {"Empty", "Non-uniform", "Concrete", -1, emptyModel, nonUniformModel, concreteModel},
//                {"Empty", "Non-uniform", "Uniform", -1, emptyModel, nonUniformModel, uniformModel},
//                {"Empty", "Non-uniform", "Non-uniform", -1, emptyModel, nonUniformModel, nonUniformModel},
//                {"Empty String", "Non-uniform", "Non-uniform", -1, emptyStringModel, nonUniformModel, nonUniformModel},
//                {"Empty String", "Empty", "Empty", -1, emptyStringModel, emptyModel, emptyModel},
//                {"Empty String", "Empty", "Empty String", -1, emptyStringModel, emptyModel, emptyStringModel},
//                {"Empty String", "Empty", "Concrete", -1, emptyStringModel, emptyModel, concreteModel},
//                {"Empty String", "Empty", "Uniform", -1, emptyStringModel, emptyModel, uniformModel},
//                {"Empty String", "Empty", "Non-uniform", -1, emptyStringModel, emptyModel, nonUniformModel},
//                {"Empty String", "Empty String", "Empty", -1, emptyStringModel, emptyStringModel, emptyModel},
//                {"Empty String", "Empty String", "Empty String", -1, emptyStringModel, emptyStringModel, emptyStringModel},
//                {"Empty String", "Empty String", "Concrete", -1, emptyStringModel, emptyStringModel, concreteModel},
//                {"Empty String", "Empty String", "Uniform", -1, emptyStringModel, emptyStringModel, uniformModel},
//                {"Empty String", "Empty String", "Non-uniform", -1, emptyStringModel, emptyStringModel, nonUniformModel},
//                {"Empty String", "Concrete", "Empty", -1, emptyStringModel, concreteModel, emptyModel},
//                {"Empty String", "Concrete", "Empty String", -1, emptyStringModel, concreteModel, emptyStringModel},
//                {"Empty String", "Concrete", "Concrete", -1, emptyStringModel, concreteModel, concreteModel},
//                {"Empty String", "Concrete", "Uniform", -1, emptyStringModel, concreteModel, uniformModel},
//                {"Empty String", "Concrete", "Non-uniform", -1, emptyStringModel, concreteModel, nonUniformModel},
//                {"Empty String", "Uniform", "Empty", -1, emptyStringModel, uniformModel, emptyModel},
//                {"Empty String", "Uniform", "Empty String", -1, emptyStringModel, uniformModel, emptyStringModel},
//                {"Empty String", "Uniform", "Concrete", -1, emptyStringModel, uniformModel, concreteModel},
//                {"Empty String", "Uniform", "Uniform", -1, emptyStringModel, uniformModel, uniformModel},
//                {"Empty String", "Uniform", "Non-uniform", -1, emptyStringModel, uniformModel, nonUniformModel},
//                {"Empty String", "Non-uniform", "Empty", -1, emptyStringModel, nonUniformModel, emptyModel},
//                {"Empty String", "Non-uniform", "Empty String", -1, emptyStringModel, nonUniformModel, emptyStringModel},
//                {"Empty String", "Non-uniform", "Concrete", -1, emptyStringModel, nonUniformModel, concreteModel},
//                {"Empty String", "Non-uniform", "Uniform", -1, emptyStringModel, nonUniformModel, uniformModel},
//                {"Empty String", "Non-uniform", "Non-uniform", -1, emptyStringModel, nonUniformModel, nonUniformModel},
//                {"Concrete", "Empty", "Empty", -1, concreteModel, emptyModel, emptyModel},
//                {"Concrete", "Empty", "Empty String", -1, concreteModel, emptyModel, emptyStringModel},
//                {"Concrete", "Empty", "Concrete", -1, concreteModel, emptyModel, concreteModel},
//                {"Concrete", "Empty", "Uniform", -1, concreteModel, emptyModel, uniformModel},
//                {"Concrete", "Empty", "Non-uniform", -1, concreteModel, emptyModel, nonUniformModel},
//                {"Concrete", "Empty String", "Empty", -1, concreteModel, emptyStringModel, emptyModel},
//                {"Concrete", "Empty String", "Empty String", -1, concreteModel, emptyStringModel, emptyStringModel},
//                {"Concrete", "Empty String", "Concrete", -1, concreteModel, emptyStringModel, concreteModel},
//                {"Concrete", "Empty String", "Uniform", -1, concreteModel, emptyStringModel, uniformModel},
//                {"Concrete", "Empty String", "Non-uniform", -1, concreteModel, emptyStringModel, nonUniformModel},
//                {"Concrete", "Concrete", "Empty", -1, concreteModel, concreteModel, emptyModel},
//                {"Concrete", "Concrete", "Empty String", -1, concreteModel, concreteModel, emptyStringModel},
//                {"Concrete", "Concrete", "Concrete", -1, concreteModel, concreteModel, concreteModel},
//                {"Concrete", "Concrete", "Uniform", -1, concreteModel, concreteModel, uniformModel},
//                {"Concrete", "Concrete", "Non-uniform", -1, concreteModel, concreteModel, nonUniformModel},
//                {"Concrete", "Uniform", "Empty", -1, concreteModel, uniformModel, emptyModel},
//                {"Concrete", "Uniform", "Empty String", -1, concreteModel, uniformModel, emptyStringModel},
//                {"Concrete", "Uniform", "Concrete", -1, concreteModel, uniformModel, concreteModel},
//                {"Concrete", "Uniform", "Uniform", -1, concreteModel, uniformModel, uniformModel},
//                {"Concrete", "Uniform", "Non-uniform", -1, concreteModel, uniformModel, nonUniformModel},
//                {"Concrete", "Non-uniform", "Empty", -1, concreteModel, nonUniformModel, emptyModel},
//                {"Concrete", "Non-uniform", "Empty String", -1, concreteModel, nonUniformModel, emptyStringModel},
//                {"Concrete", "Non-uniform", "Concrete", -1, concreteModel, nonUniformModel, concreteModel},
//                {"Concrete", "Non-uniform", "Uniform", -1, concreteModel, nonUniformModel, uniformModel},
//                {"Concrete", "Non-uniform", "Non-uniform", -1, concreteModel, nonUniformModel, nonUniformModel},
//                {"Uniform", "Empty", "Empty", -1, uniformModel, emptyModel, emptyModel},
//                {"Uniform", "Empty", "Empty String", -1, uniformModel, emptyModel, emptyStringModel},
//                {"Uniform", "Empty", "Concrete", -1, uniformModel, emptyModel, concreteModel},
//                {"Uniform", "Empty", "Uniform", -1, uniformModel, emptyModel, uniformModel},
//                {"Uniform", "Empty", "Non-uniform", -1, uniformModel, emptyModel, nonUniformModel},
//                {"Uniform", "Empty String", "Empty", -1, uniformModel, emptyStringModel, emptyModel},
//                {"Uniform", "Empty String", "Empty String", -1, uniformModel, emptyStringModel, emptyStringModel},
//                {"Uniform", "Empty String", "Concrete", -1, uniformModel, emptyStringModel, concreteModel},
//                {"Uniform", "Empty String", "Uniform", -1, uniformModel, emptyStringModel, uniformModel},
//                {"Uniform", "Empty String", "Non-uniform", -1, uniformModel, emptyStringModel, nonUniformModel},
//                {"Uniform", "Concrete", "Empty", -1, uniformModel, concreteModel, emptyModel},
//                {"Uniform", "Concrete", "Empty String", -1, uniformModel, concreteModel, emptyStringModel},
//                {"Uniform", "Concrete", "Concrete", -1, uniformModel, concreteModel, concreteModel},
//                {"Uniform", "Concrete", "Uniform", -1, uniformModel, concreteModel, uniformModel},
//                {"Uniform", "Concrete", "Non-uniform", -1, uniformModel, concreteModel, nonUniformModel},
//                {"Uniform", "Uniform", "Empty", -1, uniformModel, uniformModel, emptyModel},
//                {"Uniform", "Uniform", "Empty String", -1, uniformModel, uniformModel, emptyStringModel},
//                {"Uniform", "Uniform", "Concrete", -1, uniformModel, uniformModel, concreteModel},
//                {"Uniform", "Uniform", "Uniform", -1, uniformModel, uniformModel, uniformModel},
//                {"Uniform", "Uniform", "Non-uniform", -1, uniformModel, uniformModel, nonUniformModel},
//                {"Uniform", "Non-uniform", "Empty", -1, uniformModel, nonUniformModel, emptyModel},
//                {"Uniform", "Non-uniform", "Empty String", -1, uniformModel, nonUniformModel, emptyStringModel},
//                {"Uniform", "Non-uniform", "Concrete", -1, uniformModel, nonUniformModel, concreteModel},
//                {"Uniform", "Non-uniform", "Uniform", -1, uniformModel, nonUniformModel, uniformModel},
//                {"Uniform", "Non-uniform", "Non-uniform", -1, uniformModel, nonUniformModel, nonUniformModel},
//                {"Non-uniform", "Empty", "Empty", -1, nonUniformModel, emptyModel, emptyModel},
//                {"Non-uniform", "Empty", "Empty String", -1, nonUniformModel, emptyModel, emptyStringModel},
//                {"Non-uniform", "Empty", "Concrete", -1, nonUniformModel, emptyModel, concreteModel},
//                {"Non-uniform", "Empty", "Uniform", -1, nonUniformModel, emptyModel, uniformModel},
//                {"Non-uniform", "Empty", "Non-uniform", -1, nonUniformModel, emptyModel, nonUniformModel},
//                {"Non-uniform", "Empty String", "Empty", -1, nonUniformModel, emptyStringModel, emptyModel},
//                {"Non-uniform", "Empty String", "Empty String", -1, nonUniformModel, emptyStringModel, emptyStringModel},
//                {"Non-uniform", "Empty String", "Concrete", -1, nonUniformModel, emptyStringModel, concreteModel},
//                {"Non-uniform", "Empty String", "Uniform", -1, nonUniformModel, emptyStringModel, uniformModel},
//                {"Non-uniform", "Empty String", "Non-uniform", -1, nonUniformModel, emptyStringModel, nonUniformModel},
//                {"Non-uniform", "Concrete", "Empty", -1, nonUniformModel, concreteModel, emptyModel},
//                {"Non-uniform", "Concrete", "Empty String", -1, nonUniformModel, concreteModel, emptyStringModel},
//                {"Non-uniform", "Concrete", "Concrete", -1, nonUniformModel, concreteModel, concreteModel},
//                {"Non-uniform", "Concrete", "Uniform", -1, nonUniformModel, concreteModel, uniformModel},
//                {"Non-uniform", "Concrete", "Non-uniform", -1, nonUniformModel, concreteModel, nonUniformModel},
//                {"Non-uniform", "Uniform", "Empty", -1, nonUniformModel, uniformModel, emptyModel},
//                {"Non-uniform", "Uniform", "Empty String", -1, nonUniformModel, uniformModel, emptyStringModel},
//                {"Non-uniform", "Uniform", "Concrete", -1, nonUniformModel, uniformModel, concreteModel},
//                {"Non-uniform", "Uniform", "Uniform", -1, nonUniformModel, uniformModel, uniformModel},
//                {"Non-uniform", "Uniform", "Non-uniform", -1, nonUniformModel, uniformModel, nonUniformModel},
//                {"Non-uniform", "Non-uniform", "Empty", -1, nonUniformModel, nonUniformModel, emptyModel},
//                {"Non-uniform", "Non-uniform", "Empty String", -1, nonUniformModel, nonUniformModel, emptyStringModel},
//                {"Non-uniform", "Non-uniform", "Concrete", -1, nonUniformModel, nonUniformModel, concreteModel},
//                {"Non-uniform", "Non-uniform", "Uniform", -1, nonUniformModel, nonUniformModel, uniformModel},
//                {"Non-uniform", "Non-uniform", "Non-uniform", -1, nonUniformModel, nonUniformModel, nonUniformModel},
        });
    }

    @Before
    public void setup() {
        // *** act ***
//        this.replacedModel = this.baseModel.replace(this.findModel, this.replaceModel);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
//        int modelCount = this.replacedModel.modelCount().intValue();
//
//        // *** assert ***
//        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
