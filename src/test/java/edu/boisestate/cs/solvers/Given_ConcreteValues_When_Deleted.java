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
public class Given_ConcreteValues_When_Deleted {

    @Parameter(value = 3)
    public int start;
    @Parameter(value = 4)
    public int end;
    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 2)
    public ConcreteValues values;
    @Parameter(value = 1)
    public int expectedModelCount;
    private ConcreteValues resultValues;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Values>.delete({3},{4}) - Expected MC = {1}")
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
                {"Empty", 0, emptyValues, 0, 0},
                {"Empty", 0, emptyValues, 0, 1},
                {"Empty", 0, emptyValues, 0, 2},
                {"Empty", 0, emptyValues, 0, 3},
                {"Empty", 0, emptyValues, 1, 1},
                {"Empty", 0, emptyValues, 1, 2},
                {"Empty", 0, emptyValues, 1, 3},
                {"Empty", 0, emptyValues, 2, 2},
                {"Empty", 0, emptyValues, 2, 3},
                {"Empty", 0, emptyValues, 3, 3},
                {"Empty String", 1, emptyStringValues, 0, 0},
                {"Empty String", 1, emptyStringValues, 0, 1},
                {"Empty String", 1, emptyStringValues, 0, 2},
                {"Empty String", 1, emptyStringValues, 0, 3},
                {"Empty String", 0, emptyStringValues, 1, 1},
                {"Empty String", 0, emptyStringValues, 1, 2},
                {"Empty String", 0, emptyStringValues, 1, 3},
                {"Empty String", 0, emptyStringValues, 2, 2},
                {"Empty String", 0, emptyStringValues, 2, 3},
                {"Empty String", 0, emptyStringValues, 3, 3},
                {"Concrete", 1, concreteValues, 0, 0},
                {"Concrete", 1, concreteValues, 0, 1},
                {"Concrete", 1, concreteValues, 0, 2},
                {"Concrete", 1, concreteValues, 0, 3},
                {"Concrete", 1, concreteValues, 1, 1},
                {"Concrete", 1, concreteValues, 1, 2},
                {"Concrete", 1, concreteValues, 1, 3},
                {"Concrete", 1, concreteValues, 2, 2},
                {"Concrete", 1, concreteValues, 2, 3},
                {"Concrete", 1, concreteValues, 3, 3},
                {"Uniform", 85, uniformValues, 0, 0},
                {"Uniform", 85, uniformValues, 0, 1},
                {"Uniform", 85, uniformValues, 0, 2},
                {"Uniform", 85, uniformValues, 0, 3},
                {"Uniform", 84, uniformValues, 1, 1},
                {"Uniform", 84, uniformValues, 1, 2},
                {"Uniform", 84, uniformValues, 1, 3},
                {"Uniform", 80, uniformValues, 2, 2},
                {"Uniform", 80, uniformValues, 2, 3},
                {"Uniform", 64, uniformValues, 3, 3},
                {"Non-Uniform", 45, nonUniformValues, 0, 0},
                {"Non-Uniform", 45, nonUniformValues, 0, 1},
                {"Non-Uniform", 45, nonUniformValues, 0, 2},
                {"Non-Uniform", 45, nonUniformValues, 0, 3},
                {"Non-Uniform", 45, nonUniformValues, 1, 1},
                {"Non-Uniform", 45, nonUniformValues, 1, 2},
                {"Non-Uniform", 45, nonUniformValues, 1, 3},
                {"Non-Uniform", 44, nonUniformValues, 2, 2},
                {"Non-Uniform", 44, nonUniformValues, 2, 3},
                {"Non-Uniform", 37, nonUniformValues, 3, 3}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultValues = values.delete(start, end);
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = (int)resultValues.modelCount();

        // *** assert ***
        String reason = String.format("<%s Values>.delete(%d, %d)", description, start, end);
        assertThat(reason, modelCount, is(equalTo(expectedModelCount)));
    }
}
