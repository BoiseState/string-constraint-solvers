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
public class Given_ConcreteValues_When_ConvertingToUpperCase {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 2)
    public ConcreteValues values;
    @Parameter(value = 1)
    public int expectedModelCount;
    private ConcreteValues resultValues;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Values>.toUpperCase() - Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D,a-d");
        int initialBoundLength = 3;

        // create automaton models
        ConcreteValues emptyValues = getEmptyValues(alphabet);
        ConcreteValues emptyStringValues = getEmptyStringValues(alphabet);
        ConcreteValues lowerConcreteValues = getConcreteValues(alphabet, "abc");
        ConcreteValues upperConcreteValues = getConcreteValues(alphabet, "ABC");
        ConcreteValues uniformValues = getUniformValues(alphabet, initialBoundLength);
        ConcreteValues nonUniformValues = getNonUniformValues(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, emptyValues},
                {"Empty String", 1, emptyStringValues},
                {"Lower Concrete", 1, lowerConcreteValues},
                {"Upper Concrete", 1, upperConcreteValues},
                {"Uniform", 585, uniformValues},
                {"Non-Uniform", 185, nonUniformValues}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultValues = values.toUpperCase();
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = resultValues.getValues().size();

        // *** assert ***
        String reason = String.format("<%s Values>.toUpperCase()", description);
        assertThat(reason, modelCount, is(equalTo(expectedModelCount)));
    }
}
