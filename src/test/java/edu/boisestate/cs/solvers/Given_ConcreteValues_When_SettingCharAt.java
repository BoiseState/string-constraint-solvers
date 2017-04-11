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
public class Given_ConcreteValues_When_SettingCharAt {

    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 5)
    public ConcreteValues argValues;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 3)
    public ConcreteValues baseValues;
    @Parameter(value = 2)
    public int expectedModelCount;
    @Parameter(value = 4)
    public int offset;
    private ConcreteValues resultValues;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Values>.setCharAt({4}, <{1} Values>) - Expected MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create values
        ConcreteValues emptyValues = getEmptyValues(alphabet);
        ConcreteValues emptyStringValues = getEmptyStringValues(alphabet);
        ConcreteValues concreteValues = getConcreteValues(alphabet, "ABC");
        ConcreteValues uniformValues = getUniformValues(alphabet, initialBoundLength);
        ConcreteValues nonUniformValues = getNonUniformValues(alphabet, initialBoundLength);

        // create arg values
        ConcreteValues aValues = getConcreteValues(alphabet, "A");
        ConcreteValues bValues = getConcreteValues(alphabet, "B");
        ConcreteValues cValues = getConcreteValues(alphabet, "C");
        ConcreteValues dValues = getConcreteValues(alphabet, "D");

        return Arrays.asList(new Object[][]{
                {"Empty", "'A'", 0, emptyValues, 0, aValues},
                {"Empty", "'B'", 0, emptyValues, 0, bValues},
                {"Empty", "'C'", 0, emptyValues, 0, cValues},
                {"Empty", "'D'", 0, emptyValues, 0, dValues},
                {"Empty", "'A'", 0, emptyValues, 1, aValues},
                {"Empty", "'B'", 0, emptyValues, 1, bValues},
                {"Empty", "'C'", 0, emptyValues, 1, cValues},
                {"Empty", "'D'", 0, emptyValues, 1, dValues},
                {"Empty", "'A'", 0, emptyValues, 2, aValues},
                {"Empty", "'B'", 0, emptyValues, 2, bValues},
                {"Empty", "'C'", 0, emptyValues, 2, cValues},
                {"Empty", "'D'", 0, emptyValues, 2, dValues},
                {"Empty String", "'A'", 0, emptyStringValues, 0, aValues},
                {"Empty String", "'B'", 0, emptyStringValues, 0, bValues},
                {"Empty String", "'C'", 0, emptyStringValues, 0, cValues},
                {"Empty String", "'D'", 0, emptyStringValues, 0, dValues},
                {"Empty String", "'A'", 0, emptyStringValues, 1, aValues},
                {"Empty String", "'B'", 0, emptyStringValues, 1, bValues},
                {"Empty String", "'C'", 0, emptyStringValues, 1, cValues},
                {"Empty String", "'D'", 0, emptyStringValues, 1, dValues},
                {"Empty String", "'A'", 0, emptyStringValues, 2, aValues},
                {"Empty String", "'B'", 0, emptyStringValues, 2, bValues},
                {"Empty String", "'C'", 0, emptyStringValues, 2, cValues},
                {"Empty String", "'D'", 0, emptyStringValues, 2, dValues},
                {"Concrete", "'A'", 1, concreteValues, 0, aValues},
                {"Concrete", "'B'", 1, concreteValues, 0, bValues},
                {"Concrete", "'C'", 1, concreteValues, 0, cValues},
                {"Concrete", "'D'", 1, concreteValues, 0, dValues},
                {"Concrete", "'A'", 1, concreteValues, 1, aValues},
                {"Concrete", "'B'", 1, concreteValues, 1, bValues},
                {"Concrete", "'C'", 1, concreteValues, 1, cValues},
                {"Concrete", "'D'", 1, concreteValues, 1, dValues},
                {"Concrete", "'A'", 1, concreteValues, 2, aValues},
                {"Concrete", "'B'", 1, concreteValues, 2, bValues},
                {"Concrete", "'C'", 1, concreteValues, 2, cValues},
                {"Concrete", "'D'", 1, concreteValues, 2, dValues},
                {"Uniform", "'A'", 84, uniformValues, 0, aValues},
                {"Uniform", "'B'", 84, uniformValues, 0, bValues},
                {"Uniform", "'C'", 84, uniformValues, 0, cValues},
                {"Uniform", "'D'", 84, uniformValues, 0, dValues},
                {"Uniform", "'A'", 80, uniformValues, 1, aValues},
                {"Uniform", "'B'", 80, uniformValues, 1, bValues},
                {"Uniform", "'C'", 80, uniformValues, 1, cValues},
                {"Uniform", "'D'", 80, uniformValues, 1, dValues},
                {"Uniform", "'A'", 64, uniformValues, 2, aValues},
                {"Uniform", "'B'", 64, uniformValues, 2, bValues},
                {"Uniform", "'C'", 64, uniformValues, 2, cValues},
                {"Uniform", "'D'", 64, uniformValues, 2, dValues},
                {"Non-Uniform", "'A'", 45, nonUniformValues, 0, aValues},
                {"Non-Uniform", "'B'", 45, nonUniformValues, 0, bValues},
                {"Non-Uniform", "'C'", 45, nonUniformValues, 0, cValues},
                {"Non-Uniform", "'D'", 45, nonUniformValues, 0, dValues},
                {"Non-Uniform", "'A'", 44, nonUniformValues, 1, aValues},
                {"Non-Uniform", "'B'", 44, nonUniformValues, 1, bValues},
                {"Non-Uniform", "'C'", 44, nonUniformValues, 1, cValues},
                {"Non-Uniform", "'D'", 44, nonUniformValues, 1, dValues},
                {"Non-Uniform", "'A'", 37, nonUniformValues, 2, aValues},
                {"Non-Uniform", "'B'", 37, nonUniformValues, 2, bValues},
                {"Non-Uniform", "'C'", 37, nonUniformValues, 2, cValues},
                {"Non-Uniform", "'D'", 37, nonUniformValues, 2, dValues}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultValues = baseValues.setCharAt(offset, argValues);
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = (int)resultValues.modelCount();

        // *** assert ***
        String reason = String.format( "<%s Values>.setCharAt(%d, <%s Values>)", baseDescription, offset, argDescription);
        assertThat(reason, modelCount, is(equalTo(expectedModelCount)));
    }
}
