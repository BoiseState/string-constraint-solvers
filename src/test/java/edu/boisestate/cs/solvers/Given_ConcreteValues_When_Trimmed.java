package edu.boisestate.cs.solvers;

import edu.boisestate.cs.Alphabet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static edu.boisestate.cs.solvers.SolverTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_ConcreteValues_When_Trimmed {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 2)
    public ConcreteValues values;
    @Parameter(value = 1)
    public int expectedModelCount;
    private ConcreteValues resultValues;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Values>.trim() - Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet(" ,A-D");
        int initialBoundLength = 3;

        // create automaton models
        ConcreteValues emptyValues = getEmptyValues(alphabet);
        ConcreteValues emptyStringValues = getEmptyStringValues(alphabet);
        ConcreteValues nonWhitespaceConcreteValues = getConcreteValues(alphabet, "ABC");
        ConcreteValues whitespaceConcreteValues = getConcreteValues(alphabet, " B ");
        ConcreteValues uniformValues = getUniformValues(alphabet, initialBoundLength);
        ConcreteValues nonUniformValues = getNonUniformValues(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, emptyValues},
                {"Empty String", 1, emptyStringValues},
                {"Non-Whitespace Concrete", 1, nonWhitespaceConcreteValues},
                {"Whitespace Concrete", 1, whitespaceConcreteValues},
                {"Uniform", 156, uniformValues},
                {"Non-Uniform", 71, nonUniformValues}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultValues = values.trim();
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = resultValues.getValues().size();

        // *** assert ***
        String reason = String.format("<%s Values>.trim()", description);
        assertThat(reason, modelCount, is(equalTo(expectedModelCount)));
    }
}
