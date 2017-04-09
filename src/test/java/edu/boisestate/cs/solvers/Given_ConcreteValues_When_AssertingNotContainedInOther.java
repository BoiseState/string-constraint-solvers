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
public class Given_ConcreteValues_When_AssertingNotContainedInOther {

    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 4)
    public ConcreteValues argValues;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 3)
    public ConcreteValues baseValues;
    @Parameter(value = 2)
    public int expectedModelCount;
    private ConcreteValues resultValues;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Values>.assertNotContainedInOther(<{1} Values>) - Expected MC = {2}")
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
                {"Empty", "Empty", 0, emptyValues, emptyValues},
                {"Empty", "Empty String", 0, emptyValues, emptyStringValues},
                {"Empty", "Concrete", 0, emptyValues, concreteValues},
                {"Empty", "Uniform", 0, emptyValues, uniformValues},
                {"Empty", "Non-Uniform", 0, emptyValues, nonUniformValues},
                {"Empty String", "Empty", 0, emptyStringValues, emptyValues},
                {"Empty String", "Empty String", 0, emptyStringValues, emptyStringValues},
                {"Empty String", "Concrete", 0, emptyStringValues, concreteValues},
                {"Empty String", "Uniform", 0, emptyStringValues, uniformValues},
                {"Empty String", "Non-Uniform", 0, emptyStringValues, nonUniformValues},
                {"Concrete", "Empty", 0, concreteValues, emptyValues},
                {"Concrete", "Empty String", 1, concreteValues, emptyStringValues},
                {"Concrete", "Concrete", 0, concreteValues, concreteValues},
                {"Concrete", "Uniform", 1, concreteValues, uniformValues},
                {"Concrete", "Non-Uniform", 1, concreteValues, nonUniformValues},
                {"Uniform", "Empty", 0, uniformValues, emptyValues},
                {"Uniform", "Empty String", 84, uniformValues, emptyStringValues},
                {"Uniform", "Concrete", 78, uniformValues, concreteValues},
                {"Uniform", "Uniform", 84, uniformValues, uniformValues},
                {"Uniform", "Non-Uniform", 83, uniformValues, nonUniformValues},
                {"Non-Uniform", "Empty", 0, nonUniformValues, emptyValues},
                {"Non-Uniform", "Empty String", 45, nonUniformValues, emptyStringValues},
                {"Non-Uniform", "Concrete", 42, nonUniformValues, concreteValues},
                {"Non-Uniform", "Uniform", 45, nonUniformValues, uniformValues},
                {"Non-Uniform", "Non-Uniform", 44, nonUniformValues, nonUniformValues}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultValues = baseValues.assertNotContainedInOther(argValues);
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = resultValues.getValues().size();

        // *** assert ***
        String reason = String.format( "<%s Values>.assertNotContainedInOther(<%s Values>)", baseDescription, argDescription);
        assertThat(reason, modelCount, is(equalTo(expectedModelCount)));
    }
}
