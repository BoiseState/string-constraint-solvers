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
public class Given_BoundedAutomatonModel_When_AssertingEqualsIgnoreCase {

    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 4)
    public BoundedAutomatonModel argModel;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 3)
    public BoundedAutomatonModel baseModel;
    @Parameter(value = 2)
    public int expectedModelCount;
    private AutomatonModel equalModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.assertEqualsIgnoreCase(<{1} Automaton Model>) - Expected MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D,a-d");
        int initialBoundLength = 3;

        // create automaton models
        BoundedAutomatonModel emptyModel = getEmptyBoundedModel(alphabet);
        BoundedAutomatonModel emptyStringModel = getEmptyStringBoundedModel(alphabet);
        BoundedAutomatonModel concreteLowerModel = getConcreteBoundedModel(alphabet,"abc");
        BoundedAutomatonModel concreteUpperModel = getConcreteBoundedModel(alphabet,"ABC");
        BoundedAutomatonModel uniformModel = getUniformBoundedModel(alphabet, initialBoundLength);
        BoundedAutomatonModel nonUniformModel = getNonUniformBoundedModel(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", 0, emptyModel, emptyModel},
                {"Empty", "Empty String", 0, emptyModel, emptyStringModel},
                {"Empty", "Concrete Lower", 0, emptyModel, concreteLowerModel},
                {"Empty", "Concrete Upper", 0, emptyModel, concreteUpperModel},
                {"Empty", "Uniform", 0, emptyModel, uniformModel},
                {"Empty", "Non-uniform", 0, emptyModel, nonUniformModel},
                {"Empty String", "Empty", 0, emptyStringModel, emptyModel},
                {"Empty String", "Empty String", 1, emptyStringModel, emptyStringModel},
                {"Empty String", "Concrete Lower", 0, emptyStringModel, concreteLowerModel},
                {"Empty String", "Concrete Upper", 0, emptyStringModel, concreteUpperModel},
                {"Empty String", "Uniform", 1, emptyStringModel, uniformModel},
                {"Empty String", "Non-uniform", 0, emptyStringModel, nonUniformModel},
                {"Concrete Lower", "Empty", 0, concreteLowerModel, emptyModel},
                {"Concrete Lower", "Empty String", 0, concreteLowerModel, emptyStringModel},
                {"Concrete Lower", "Concrete Lower", 1, concreteLowerModel, concreteLowerModel},
                {"Concrete Lower", "Concrete Upper", 1, concreteLowerModel, concreteUpperModel},
                {"Concrete Lower", "Uniform", 1, concreteLowerModel, uniformModel},
                {"Concrete Lower", "Non-uniform", 1, concreteLowerModel, nonUniformModel},
                {"Concrete Upper", "Empty", 0, concreteUpperModel, emptyModel},
                {"Concrete Upper", "Empty String", 0, concreteUpperModel, emptyStringModel},
                {"Concrete Upper", "Concrete Lower", 1, concreteUpperModel, concreteLowerModel},
                {"Concrete Upper", "Concrete Upper", 1, concreteUpperModel, concreteUpperModel},
                {"Concrete Upper", "Uniform", 1, concreteUpperModel, uniformModel},
                {"Concrete Upper", "Non-uniform", 1, concreteUpperModel, nonUniformModel},
                {"Uniform", "Empty", 0, uniformModel, emptyModel},
                {"Uniform", "Empty String", 1, uniformModel, emptyStringModel},
                {"Uniform", "Concrete Lower", 8, uniformModel, concreteLowerModel},
                {"Uniform", "Concrete Upper", 8, uniformModel, concreteUpperModel},
                {"Uniform", "Uniform", 585, uniformModel, uniformModel},
                {"Uniform", "Non-uniform", 326, uniformModel, nonUniformModel},
                {"Non-uniform", "Empty", 0, nonUniformModel, emptyModel},
                {"Non-uniform", "Empty String", 0, nonUniformModel, emptyStringModel},
                {"Non-uniform", "Concrete Lower", 4, nonUniformModel, concreteLowerModel},
                {"Non-uniform", "Concrete Upper", 4, nonUniformModel, concreteUpperModel},
                {"Non-uniform", "Uniform", 185, nonUniformModel, uniformModel},
                {"Non-uniform", "Non-uniform", 185, nonUniformModel, nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.equalModel = this.baseModel.assertEqualsIgnoreCase(this.argModel);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.equalModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format( "<%s Automaton Model>.assertEqualsIgnoreCase(<%s Automaton Model>)", baseDescription, argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
