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
public class Given_BoundedAutomatonModel_When_Inserting {

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
    private AutomatonModel insertedModel;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.insert({4}, <{1} Automaton Model>) - Expected MC = {2}")
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
                {"Empty", "Empty", 0, emptyModel, 0, emptyModel},
                {"Empty", "Empty", 0, emptyModel, 1, emptyModel},
                {"Empty", "Empty", 0, emptyModel, 2, emptyModel},
                {"Empty", "Empty", 0, emptyModel, 3, emptyModel},
                {"Empty", "Empty String", 0, emptyModel, 0, emptyStringModel},
                {"Empty", "Empty String", 0, emptyModel, 1, emptyStringModel},
                {"Empty", "Empty String", 0, emptyModel, 2, emptyStringModel},
                {"Empty", "Empty String", 0, emptyModel, 3, emptyStringModel},
                {"Empty", "Concrete", 0, emptyModel, 0, concreteModel},
                {"Empty", "Concrete", 0, emptyModel, 1, concreteModel},
                {"Empty", "Concrete", 0, emptyModel, 2, concreteModel},
                {"Empty", "Concrete", 0, emptyModel, 3, concreteModel},
                {"Empty", "Uniform", 0, emptyModel, 0, uniformModel},
                {"Empty", "Uniform", 0, emptyModel, 1, uniformModel},
                {"Empty", "Uniform", 0, emptyModel, 2, uniformModel},
                {"Empty", "Uniform", 0, emptyModel, 3, uniformModel},
                {"Empty", "Non-uniform", 0, emptyModel, 0, nonUniformModel},
                {"Empty", "Non-uniform", 0, emptyModel, 1, nonUniformModel},
                {"Empty", "Non-uniform", 0, emptyModel, 2, nonUniformModel},
                {"Empty", "Non-uniform", 0, emptyModel, 3, nonUniformModel},
                {"Empty String", "Empty", 0, emptyStringModel, 0, emptyModel},
                {"Empty String", "Empty", 0, emptyStringModel, 1, emptyModel},
                {"Empty String", "Empty", 0, emptyStringModel, 2, emptyModel},
                {"Empty String", "Empty", 0, emptyStringModel, 3, emptyModel},
                {"Empty String", "Empty String", 1, emptyStringModel, 0, emptyStringModel},
                {"Empty String", "Empty String", 0, emptyStringModel, 1, emptyStringModel},
                {"Empty String", "Empty String", 0, emptyStringModel, 2, emptyStringModel},
                {"Empty String", "Empty String", 0, emptyStringModel, 3, emptyStringModel},
                {"Empty String", "Concrete", 1, emptyStringModel, 0, concreteModel},
                {"Empty String", "Concrete", 0, emptyStringModel, 1, concreteModel},
                {"Empty String", "Concrete", 0, emptyStringModel, 2, concreteModel},
                {"Empty String", "Concrete", 0, emptyStringModel, 3, concreteModel},
                {"Empty String", "Uniform", 85, emptyStringModel, 0, uniformModel},
                {"Empty String", "Uniform", 0, emptyStringModel, 1, uniformModel},
                {"Empty String", "Uniform", 0, emptyStringModel, 2, uniformModel},
                {"Empty String", "Uniform", 0, emptyStringModel, 3, uniformModel},
                {"Empty String", "Non-uniform", 45, emptyStringModel, 0, nonUniformModel},
                {"Empty String", "Non-uniform", 0, emptyStringModel, 1, nonUniformModel},
                {"Empty String", "Non-uniform", 0, emptyStringModel, 2, nonUniformModel},
                {"Empty String", "Non-uniform", 0, emptyStringModel, 3, nonUniformModel},
                {"Concrete", "Empty", 0, concreteModel, 0, emptyModel},
                {"Concrete", "Empty", 0, concreteModel, 1, emptyModel},
                {"Concrete", "Empty", 0, concreteModel, 2, emptyModel},
                {"Concrete", "Empty", 0, concreteModel, 3, emptyModel},
                {"Concrete", "Empty String", 1, concreteModel, 0, emptyStringModel},
                {"Concrete", "Empty String", 1, concreteModel, 1, emptyStringModel},
                {"Concrete", "Empty String", 1, concreteModel, 2, emptyStringModel},
                {"Concrete", "Empty String", 1, concreteModel, 3, emptyStringModel},
                {"Concrete", "Concrete", 1, concreteModel, 0, concreteModel},
                {"Concrete", "Concrete", 1, concreteModel, 1, concreteModel},
                {"Concrete", "Concrete", 1, concreteModel, 2, concreteModel},
                {"Concrete", "Concrete", 1, concreteModel, 3, concreteModel},
                {"Concrete", "Uniform", 85, concreteModel, 0, uniformModel},
                {"Concrete", "Uniform", 85, concreteModel, 1, uniformModel},
                {"Concrete", "Uniform", 85, concreteModel, 2, uniformModel},
                {"Concrete", "Uniform", 85, concreteModel, 3, uniformModel},
                {"Concrete", "Non-uniform", 45, concreteModel, 0, nonUniformModel},
                {"Concrete", "Non-uniform", 45, concreteModel, 1, nonUniformModel},
                {"Concrete", "Non-uniform", 45, concreteModel, 2, nonUniformModel},
                {"Concrete", "Non-uniform", 45, concreteModel, 3, nonUniformModel},
                {"Uniform", "Empty", 0, uniformModel, 0, emptyModel},
                {"Uniform", "Empty", 0, uniformModel, 1, emptyModel},
                {"Uniform", "Empty", 0, uniformModel, 2, emptyModel},
                {"Uniform", "Empty", 0, uniformModel, 3, emptyModel},
                {"Uniform", "Empty String", 85, uniformModel, 0, emptyStringModel},
                {"Uniform", "Empty String", 84, uniformModel, 1, emptyStringModel},
                {"Uniform", "Empty String", 80, uniformModel, 2, emptyStringModel},
                {"Uniform", "Empty String", 64, uniformModel, 3, emptyStringModel},
                {"Uniform", "Concrete", 85, uniformModel, 0, concreteModel},
                {"Uniform", "Concrete", 84, uniformModel, 1, concreteModel},
                {"Uniform", "Concrete", 80, uniformModel, 2, concreteModel},
                {"Uniform", "Concrete", 64, uniformModel, 3, concreteModel},
                {"Uniform", "Uniform", 5461, uniformModel, 0, uniformModel},
                {"Uniform", "Uniform", 5460, uniformModel, 1, uniformModel},
                {"Uniform", "Uniform", 5456, uniformModel, 2, uniformModel},
                {"Uniform", "Uniform", 5440, uniformModel, 3, uniformModel},
                {"Uniform", "Non-uniform", 3825, uniformModel, 0, nonUniformModel},
                {"Uniform", "Non-uniform", 3824, uniformModel, 1, nonUniformModel},
                {"Uniform", "Non-uniform", 3820, uniformModel, 2, nonUniformModel},
                {"Uniform", "Non-uniform", 3804, uniformModel, 3, nonUniformModel},
                {"Non-uniform", "Empty", 0, nonUniformModel, 0, emptyModel},
                {"Non-uniform", "Empty", 0, nonUniformModel, 1, emptyModel},
                {"Non-uniform", "Empty", 0, nonUniformModel, 2, emptyModel},
                {"Non-uniform", "Empty", 0, nonUniformModel, 3, emptyModel},
                {"Non-uniform", "Empty String", 45, nonUniformModel, 0, emptyStringModel},
                {"Non-uniform", "Empty String", 45, nonUniformModel, 1, emptyStringModel},
                {"Non-uniform", "Empty String", 44, nonUniformModel, 2, emptyStringModel},
                {"Non-uniform", "Empty String", 37, nonUniformModel, 3, emptyStringModel},
                {"Non-uniform", "Concrete", 45, nonUniformModel, 0, concreteModel},
                {"Non-uniform", "Concrete", 45, nonUniformModel, 1, concreteModel},
                {"Non-uniform", "Concrete", 44, nonUniformModel, 2, concreteModel},
                {"Non-uniform", "Concrete", 37, nonUniformModel, 3, concreteModel},
                {"Non-uniform", "Uniform", 3825, nonUniformModel, 0, uniformModel},
                {"Non-uniform", "Uniform", 3825, nonUniformModel, 1, uniformModel},
                {"Non-uniform", "Uniform", 3825, nonUniformModel, 2, uniformModel},
                {"Non-uniform", "Uniform", 3825, nonUniformModel, 3, uniformModel},
                {"Non-uniform", "Non-uniform", 2025, nonUniformModel, 0, nonUniformModel},
                {"Non-uniform", "Non-uniform", 2025, nonUniformModel, 1, nonUniformModel},
                {"Non-uniform", "Non-uniform", 2025, nonUniformModel, 2, nonUniformModel},
                {"Non-uniform", "Non-uniform", 2025, nonUniformModel, 3, nonUniformModel},
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.insertedModel = this.baseModel.insert(this.offset, this.argModel);
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.insertedModel.modelCount().intValue();

        // *** assert ***
        String reason = String.format(
                "Expected Model Count Invalid for <%s Automaton Model>.insert" +
                "(%d, <%s Automaton Model>)",
                baseDescription,
                offset,
                argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
