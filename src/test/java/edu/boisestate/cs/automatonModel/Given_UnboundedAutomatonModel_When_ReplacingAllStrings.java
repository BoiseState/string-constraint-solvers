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

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_UnboundedAutomatonModel_When_ReplacingAllStrings {

    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 1)
    public String findDescription;
    @Parameter(value = 2)
    public String replaceDescription;
    @Parameter(value = 6)
    public UnboundedAutomatonModel replaceModel;
    @Parameter(value = 3)
    public int expectedModelCount;
    @Parameter(value = 4)
    public UnboundedAutomatonModel baseModel;
    @Parameter(value = 5)
    public UnboundedAutomatonModel findModel;
    private AutomatonModel replacedModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.replace({1} Automaton, {2} Automaton) - " +
                       "Expected MC = {1}")
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
//                {"Empty", "Empty", "Empty", -1, emptyModel, emptyModel, emptyModel},
//                {"Empty", "Empty", "Empty String", -1, emptyModel, emptyModel, emptyStringModel},
//                {"Empty", "Empty", "Concrete", -1, emptyModel, emptyModel, concreteModel},
//                {"Empty", "Empty", "Uniform", -1, emptyModel, emptyModel, uniformModel},
//                {"Empty", "Empty", "Non-Uniform", -1, emptyModel, emptyModel, nonUniformModel},
//                {"Empty", "Empty String", "Empty", -1, emptyModel, emptyStringModel, emptyModel},
//                {"Empty", "Empty String", "Empty String", -1, emptyModel, emptyStringModel, emptyStringModel},
//                {"Empty", "Empty String", "Concrete", -1, emptyModel, emptyStringModel, concreteModel},
//                {"Empty", "Empty String", "Uniform", -1, emptyModel, emptyStringModel, uniformModel},
//                {"Empty", "Empty String", "Non-Uniform", -1, emptyModel, emptyStringModel, nonUniformModel},
//                {"Empty", "Concrete", "Empty", -1, emptyModel, concreteModel, emptyModel},
//                {"Empty", "Concrete", "Empty String", -1, emptyModel, concreteModel, emptyStringModel},
//                {"Empty", "Concrete", "Concrete", -1, emptyModel, concreteModel, concreteModel},
//                {"Empty", "Concrete", "Uniform", -1, emptyModel, concreteModel, uniformModel},
//                {"Empty", "Concrete", "Non-Uniform", -1, emptyModel, concreteModel, nonUniformModel},
//                {"Empty", "Uniform", "Empty", -1, emptyModel, uniformModel, emptyModel},
//                {"Empty", "Uniform", "Empty String", -1, emptyModel, uniformModel, emptyStringModel},
//                {"Empty", "Uniform", "Concrete", -1, emptyModel, uniformModel, concreteModel},
//                {"Empty", "Uniform", "Uniform", -1, emptyModel, uniformModel, uniformModel},
//                {"Empty", "Uniform", "Non-Uniform", -1, emptyModel, uniformModel, nonUniformModel},
//                {"Empty", "Non-Uniform", "Empty", -1, emptyModel, nonUniformModel, emptyModel},
//                {"Empty", "Non-Uniform", "Empty String", -1, emptyModel, nonUniformModel, emptyStringModel},
//                {"Empty", "Non-Uniform", "Concrete", -1, emptyModel, nonUniformModel, concreteModel},
//                {"Empty", "Non-Uniform", "Uniform", -1, emptyModel, nonUniformModel, uniformModel},
//                {"Empty", "Non-Uniform", "Non-Uniform", -1, emptyModel, nonUniformModel, nonUniformModel},
//                {"Empty String", "Non-Uniform", "Non-Uniform", -1, emptyStringModel, nonUniformModel, nonUniformModel},
//                {"Empty String", "Empty", "Empty", -1, emptyStringModel, emptyModel, emptyModel},
//                {"Empty String", "Empty", "Empty String", -1, emptyStringModel, emptyModel, emptyStringModel},
//                {"Empty String", "Empty", "Concrete", -1, emptyStringModel, emptyModel, concreteModel},
//                {"Empty String", "Empty", "Uniform", -1, emptyStringModel, emptyModel, uniformModel},
//                {"Empty String", "Empty", "Non-Uniform", -1, emptyStringModel, emptyModel, nonUniformModel},
//                {"Empty String", "Empty String", "Empty", -1, emptyStringModel, emptyStringModel, emptyModel},
//                {"Empty String", "Empty String", "Empty String", -1, emptyStringModel, emptyStringModel, emptyStringModel},
//                {"Empty String", "Empty String", "Concrete", -1, emptyStringModel, emptyStringModel, concreteModel},
//                {"Empty String", "Empty String", "Uniform", -1, emptyStringModel, emptyStringModel, uniformModel},
//                {"Empty String", "Empty String", "Non-Uniform", -1, emptyStringModel, emptyStringModel, nonUniformModel},
//                {"Empty String", "Concrete", "Empty", -1, emptyStringModel, concreteModel, emptyModel},
//                {"Empty String", "Concrete", "Empty String", -1, emptyStringModel, concreteModel, emptyStringModel},
//                {"Empty String", "Concrete", "Concrete", -1, emptyStringModel, concreteModel, concreteModel},
//                {"Empty String", "Concrete", "Uniform", -1, emptyStringModel, concreteModel, uniformModel},
//                {"Empty String", "Concrete", "Non-Uniform", -1, emptyStringModel, concreteModel, nonUniformModel},
//                {"Empty String", "Uniform", "Empty", -1, emptyStringModel, uniformModel, emptyModel},
//                {"Empty String", "Uniform", "Empty String", -1, emptyStringModel, uniformModel, emptyStringModel},
//                {"Empty String", "Uniform", "Concrete", -1, emptyStringModel, uniformModel, concreteModel},
//                {"Empty String", "Uniform", "Uniform", -1, emptyStringModel, uniformModel, uniformModel},
//                {"Empty String", "Uniform", "Non-Uniform", -1, emptyStringModel, uniformModel, nonUniformModel},
//                {"Empty String", "Non-Uniform", "Empty", -1, emptyStringModel, nonUniformModel, emptyModel},
//                {"Empty String", "Non-Uniform", "Empty String", -1, emptyStringModel, nonUniformModel, emptyStringModel},
//                {"Empty String", "Non-Uniform", "Concrete", -1, emptyStringModel, nonUniformModel, concreteModel},
//                {"Empty String", "Non-Uniform", "Uniform", -1, emptyStringModel, nonUniformModel, uniformModel},
//                {"Empty String", "Non-Uniform", "Non-Uniform", -1, emptyStringModel, nonUniformModel, nonUniformModel},
//                {"Concrete", "Empty", "Empty", -1, concreteModel, emptyModel, emptyModel},
//                {"Concrete", "Empty", "Empty String", -1, concreteModel, emptyModel, emptyStringModel},
//                {"Concrete", "Empty", "Concrete", -1, concreteModel, emptyModel, concreteModel},
//                {"Concrete", "Empty", "Uniform", -1, concreteModel, emptyModel, uniformModel},
//                {"Concrete", "Empty", "Non-Uniform", -1, concreteModel, emptyModel, nonUniformModel},
//                {"Concrete", "Empty String", "Empty", -1, concreteModel, emptyStringModel, emptyModel},
//                {"Concrete", "Empty String", "Empty String", -1, concreteModel, emptyStringModel, emptyStringModel},
//                {"Concrete", "Empty String", "Concrete", -1, concreteModel, emptyStringModel, concreteModel},
//                {"Concrete", "Empty String", "Uniform", -1, concreteModel, emptyStringModel, uniformModel},
//                {"Concrete", "Empty String", "Non-Uniform", -1, concreteModel, emptyStringModel, nonUniformModel},
//                {"Concrete", "Concrete", "Empty", -1, concreteModel, concreteModel, emptyModel},
//                {"Concrete", "Concrete", "Empty String", -1, concreteModel, concreteModel, emptyStringModel},
//                {"Concrete", "Concrete", "Concrete", -1, concreteModel, concreteModel, concreteModel},
//                {"Concrete", "Concrete", "Uniform", -1, concreteModel, concreteModel, uniformModel},
//                {"Concrete", "Concrete", "Non-Uniform", -1, concreteModel, concreteModel, nonUniformModel},
//                {"Concrete", "Uniform", "Empty", -1, concreteModel, uniformModel, emptyModel},
//                {"Concrete", "Uniform", "Empty String", -1, concreteModel, uniformModel, emptyStringModel},
//                {"Concrete", "Uniform", "Concrete", -1, concreteModel, uniformModel, concreteModel},
//                {"Concrete", "Uniform", "Uniform", -1, concreteModel, uniformModel, uniformModel},
//                {"Concrete", "Uniform", "Non-Uniform", -1, concreteModel, uniformModel, nonUniformModel},
//                {"Concrete", "Non-Uniform", "Empty", -1, concreteModel, nonUniformModel, emptyModel},
//                {"Concrete", "Non-Uniform", "Empty String", -1, concreteModel, nonUniformModel, emptyStringModel},
//                {"Concrete", "Non-Uniform", "Concrete", -1, concreteModel, nonUniformModel, concreteModel},
//                {"Concrete", "Non-Uniform", "Uniform", -1, concreteModel, nonUniformModel, uniformModel},
//                {"Concrete", "Non-Uniform", "Non-Uniform", -1, concreteModel, nonUniformModel, nonUniformModel},
//                {"Uniform", "Empty", "Empty", -1, uniformModel, emptyModel, emptyModel},
//                {"Uniform", "Empty", "Empty String", -1, uniformModel, emptyModel, emptyStringModel},
//                {"Uniform", "Empty", "Concrete", -1, uniformModel, emptyModel, concreteModel},
//                {"Uniform", "Empty", "Uniform", -1, uniformModel, emptyModel, uniformModel},
//                {"Uniform", "Empty", "Non-Uniform", -1, uniformModel, emptyModel, nonUniformModel},
//                {"Uniform", "Empty String", "Empty", -1, uniformModel, emptyStringModel, emptyModel},
//                {"Uniform", "Empty String", "Empty String", -1, uniformModel, emptyStringModel, emptyStringModel},
//                {"Uniform", "Empty String", "Concrete", -1, uniformModel, emptyStringModel, concreteModel},
//                {"Uniform", "Empty String", "Uniform", -1, uniformModel, emptyStringModel, uniformModel},
//                {"Uniform", "Empty String", "Non-Uniform", -1, uniformModel, emptyStringModel, nonUniformModel},
//                {"Uniform", "Concrete", "Empty", -1, uniformModel, concreteModel, emptyModel},
//                {"Uniform", "Concrete", "Empty String", -1, uniformModel, concreteModel, emptyStringModel},
//                {"Uniform", "Concrete", "Concrete", -1, uniformModel, concreteModel, concreteModel},
//                {"Uniform", "Concrete", "Uniform", -1, uniformModel, concreteModel, uniformModel},
//                {"Uniform", "Concrete", "Non-Uniform", -1, uniformModel, concreteModel, nonUniformModel},
//                {"Uniform", "Uniform", "Empty", -1, uniformModel, uniformModel, emptyModel},
//                {"Uniform", "Uniform", "Empty String", -1, uniformModel, uniformModel, emptyStringModel},
//                {"Uniform", "Uniform", "Concrete", -1, uniformModel, uniformModel, concreteModel},
//                {"Uniform", "Uniform", "Uniform", -1, uniformModel, uniformModel, uniformModel},
//                {"Uniform", "Uniform", "Non-Uniform", -1, uniformModel, uniformModel, nonUniformModel},
//                {"Uniform", "Non-Uniform", "Empty", -1, uniformModel, nonUniformModel, emptyModel},
//                {"Uniform", "Non-Uniform", "Empty String", -1, uniformModel, nonUniformModel, emptyStringModel},
//                {"Uniform", "Non-Uniform", "Concrete", -1, uniformModel, nonUniformModel, concreteModel},
//                {"Uniform", "Non-Uniform", "Uniform", -1, uniformModel, nonUniformModel, uniformModel},
//                {"Uniform", "Non-Uniform", "Non-Uniform", -1, uniformModel, nonUniformModel, nonUniformModel},
//                {"Non-Uniform", "Empty", "Empty", -1, nonUniformModel, emptyModel, emptyModel},
//                {"Non-Uniform", "Empty", "Empty String", -1, nonUniformModel, emptyModel, emptyStringModel},
//                {"Non-Uniform", "Empty", "Concrete", -1, nonUniformModel, emptyModel, concreteModel},
//                {"Non-Uniform", "Empty", "Uniform", -1, nonUniformModel, emptyModel, uniformModel},
//                {"Non-Uniform", "Empty", "Non-Uniform", -1, nonUniformModel, emptyModel, nonUniformModel},
//                {"Non-Uniform", "Empty String", "Empty", -1, nonUniformModel, emptyStringModel, emptyModel},
//                {"Non-Uniform", "Empty String", "Empty String", -1, nonUniformModel, emptyStringModel, emptyStringModel},
//                {"Non-Uniform", "Empty String", "Concrete", -1, nonUniformModel, emptyStringModel, concreteModel},
//                {"Non-Uniform", "Empty String", "Uniform", -1, nonUniformModel, emptyStringModel, uniformModel},
//                {"Non-Uniform", "Empty String", "Non-Uniform", -1, nonUniformModel, emptyStringModel, nonUniformModel},
//                {"Non-Uniform", "Concrete", "Empty", -1, nonUniformModel, concreteModel, emptyModel},
//                {"Non-Uniform", "Concrete", "Empty String", -1, nonUniformModel, concreteModel, emptyStringModel},
//                {"Non-Uniform", "Concrete", "Concrete", -1, nonUniformModel, concreteModel, concreteModel},
//                {"Non-Uniform", "Concrete", "Uniform", -1, nonUniformModel, concreteModel, uniformModel},
//                {"Non-Uniform", "Concrete", "Non-Uniform", -1, nonUniformModel, concreteModel, nonUniformModel},
//                {"Non-Uniform", "Uniform", "Empty", -1, nonUniformModel, uniformModel, emptyModel},
//                {"Non-Uniform", "Uniform", "Empty String", -1, nonUniformModel, uniformModel, emptyStringModel},
//                {"Non-Uniform", "Uniform", "Concrete", -1, nonUniformModel, uniformModel, concreteModel},
//                {"Non-Uniform", "Uniform", "Uniform", -1, nonUniformModel, uniformModel, uniformModel},
//                {"Non-Uniform", "Uniform", "Non-Uniform", -1, nonUniformModel, uniformModel, nonUniformModel},
//                {"Non-Uniform", "Non-Uniform", "Empty", -1, nonUniformModel, nonUniformModel, emptyModel},
//                {"Non-Uniform", "Non-Uniform", "Empty String", -1, nonUniformModel, nonUniformModel, emptyStringModel},
//                {"Non-Uniform", "Non-Uniform", "Concrete", -1, nonUniformModel, nonUniformModel, concreteModel},
//                {"Non-Uniform", "Non-Uniform", "Uniform", -1, nonUniformModel, nonUniformModel, uniformModel},
//                {"Non-Uniform", "Non-Uniform", "Non-Uniform", -1, nonUniformModel, nonUniformModel, nonUniformModel},
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
//        String reason = String.format( "<%s Automaton Model>.replace(<%s Automaton Model>, <%s Automaton Model>)", baseDescription, findDescription, replaceDescription);
//        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
