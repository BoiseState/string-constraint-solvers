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
public class Given_ConcreteValues_When_AssertingIsEmpty {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 2)
    public ConcreteValues values;
    @Parameter(value = 1)
    public int expectedModelCount;
    private ConcreteValues resultValues;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Values>.assertIsEmpty() - Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automaton models
        ConcreteValues emptyValues = getEmptyValues(alphabet);
        ConcreteValues emptyStringValues = getEmptyStringValues(alphabet);
        ConcreteValues concreteValues = getConcreteValues(alphabet, "ABC");
        ConcreteValues uniformValues = getUniformValues(alphabet, initialBoundLength);
        ConcreteValues nonUniformValues = getNonUniformValues(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, emptyValues},
                {"Empty String", 1, emptyStringValues},
                {"Concrete", 0, concreteValues},
                {"Uniform", 1, uniformValues},
                {"Non-Uniform", 0, nonUniformValues}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultValues = values.assertIsEmpty();
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = (int)resultValues.modelCount();

        // *** assert ***
        String reason = String.format("<%s Values>.assertIsEmpty()", description);
        assertThat(reason, modelCount, is(equalTo(expectedModelCount)));
    }
}
