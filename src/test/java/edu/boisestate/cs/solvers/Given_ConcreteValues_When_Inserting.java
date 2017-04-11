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
public class Given_ConcreteValues_When_Inserting {

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
    @Parameters(name = "{index}: <{0} Values>.insert({4}, <{1} Values>) - Expected MC = {2}")
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
                {"Empty", "Empty", 0, emptyValues, 0, emptyValues},
                {"Empty", "Empty", 0, emptyValues, 1, emptyValues},
                {"Empty", "Empty", 0, emptyValues, 2, emptyValues},
                {"Empty", "Empty", 0, emptyValues, 3, emptyValues},
                {"Empty", "Empty String", 0, emptyValues, 0, emptyStringValues},
                {"Empty", "Empty String", 0, emptyValues, 1, emptyStringValues},
                {"Empty", "Empty String", 0, emptyValues, 2, emptyStringValues},
                {"Empty", "Empty String", 0, emptyValues, 3, emptyStringValues},
                {"Empty", "Concrete", 0, emptyValues, 0, concreteValues},
                {"Empty", "Concrete", 0, emptyValues, 1, concreteValues},
                {"Empty", "Concrete", 0, emptyValues, 2, concreteValues},
                {"Empty", "Concrete", 0, emptyValues, 3, concreteValues},
                {"Empty", "Uniform", 0, emptyValues, 0, uniformValues},
                {"Empty", "Uniform", 0, emptyValues, 1, uniformValues},
                {"Empty", "Uniform", 0, emptyValues, 2, uniformValues},
                {"Empty", "Uniform", 0, emptyValues, 3, uniformValues},
                {"Empty", "Non-Uniform", 0, emptyValues, 0, nonUniformValues},
                {"Empty", "Non-Uniform", 0, emptyValues, 1, nonUniformValues},
                {"Empty", "Non-Uniform", 0, emptyValues, 2, nonUniformValues},
                {"Empty", "Non-Uniform", 0, emptyValues, 3, nonUniformValues},
                {"Empty String", "Empty", 0, emptyStringValues, 0, emptyValues},
                {"Empty String", "Empty", 0, emptyStringValues, 1, emptyValues},
                {"Empty String", "Empty", 0, emptyStringValues, 2, emptyValues},
                {"Empty String", "Empty", 0, emptyStringValues, 3, emptyValues},
                {"Empty String", "Empty String", 1, emptyStringValues, 0, emptyStringValues},
                {"Empty String", "Empty String", 0, emptyStringValues, 1, emptyStringValues},
                {"Empty String", "Empty String", 0, emptyStringValues, 2, emptyStringValues},
                {"Empty String", "Empty String", 0, emptyStringValues, 3, emptyStringValues},
                {"Empty String", "Concrete", 1, emptyStringValues, 0, concreteValues},
                {"Empty String", "Concrete", 0, emptyStringValues, 1, concreteValues},
                {"Empty String", "Concrete", 0, emptyStringValues, 2, concreteValues},
                {"Empty String", "Concrete", 0, emptyStringValues, 3, concreteValues},
                {"Empty String", "Uniform", 85, emptyStringValues, 0, uniformValues},
                {"Empty String", "Uniform", 0, emptyStringValues, 1, uniformValues},
                {"Empty String", "Uniform", 0, emptyStringValues, 2, uniformValues},
                {"Empty String", "Uniform", 0, emptyStringValues, 3, uniformValues},
                {"Empty String", "Non-Uniform", 45, emptyStringValues, 0, nonUniformValues},
                {"Empty String", "Non-Uniform", 0, emptyStringValues, 1, nonUniformValues},
                {"Empty String", "Non-Uniform", 0, emptyStringValues, 2, nonUniformValues},
                {"Empty String", "Non-Uniform", 0, emptyStringValues, 3, nonUniformValues},
                {"Concrete", "Empty", 0, concreteValues, 0, emptyValues},
                {"Concrete", "Empty", 0, concreteValues, 1, emptyValues},
                {"Concrete", "Empty", 0, concreteValues, 2, emptyValues},
                {"Concrete", "Empty", 0, concreteValues, 3, emptyValues},
                {"Concrete", "Empty String", 1, concreteValues, 0, emptyStringValues},
                {"Concrete", "Empty String", 1, concreteValues, 1, emptyStringValues},
                {"Concrete", "Empty String", 1, concreteValues, 2, emptyStringValues},
                {"Concrete", "Empty String", 1, concreteValues, 3, emptyStringValues},
                {"Concrete", "Concrete", 1, concreteValues, 0, concreteValues},
                {"Concrete", "Concrete", 1, concreteValues, 1, concreteValues},
                {"Concrete", "Concrete", 1, concreteValues, 2, concreteValues},
                {"Concrete", "Concrete", 1, concreteValues, 3, concreteValues},
                {"Concrete", "Uniform", 85, concreteValues, 0, uniformValues},
                {"Concrete", "Uniform", 85, concreteValues, 1, uniformValues},
                {"Concrete", "Uniform", 85, concreteValues, 2, uniformValues},
                {"Concrete", "Uniform", 85, concreteValues, 3, uniformValues},
                {"Concrete", "Non-Uniform", 45, concreteValues, 0, nonUniformValues},
                {"Concrete", "Non-Uniform", 45, concreteValues, 1, nonUniformValues},
                {"Concrete", "Non-Uniform", 45, concreteValues, 2, nonUniformValues},
                {"Concrete", "Non-Uniform", 45, concreteValues, 3, nonUniformValues},
                {"Uniform", "Empty", 0, uniformValues, 0, emptyValues},
                {"Uniform", "Empty", 0, uniformValues, 1, emptyValues},
                {"Uniform", "Empty", 0, uniformValues, 2, emptyValues},
                {"Uniform", "Empty", 0, uniformValues, 3, emptyValues},
                {"Uniform", "Empty String", 85, uniformValues, 0, emptyStringValues},
                {"Uniform", "Empty String", 84, uniformValues, 1, emptyStringValues},
                {"Uniform", "Empty String", 80, uniformValues, 2, emptyStringValues},
                {"Uniform", "Empty String", 64, uniformValues, 3, emptyStringValues},
                {"Uniform", "Concrete", 85, uniformValues, 0, concreteValues},
                {"Uniform", "Concrete", 84, uniformValues, 1, concreteValues},
                {"Uniform", "Concrete", 80, uniformValues, 2, concreteValues},
                {"Uniform", "Concrete", 64, uniformValues, 3, concreteValues},
                {"Uniform", "Uniform", 7225, uniformValues, 0, uniformValues},
                {"Uniform", "Uniform", 7140, uniformValues, 1, uniformValues},
                {"Uniform", "Uniform", 6800, uniformValues, 2, uniformValues},
                {"Uniform", "Uniform", 5440, uniformValues, 3, uniformValues},
                {"Uniform", "Non-Uniform", 3825, uniformValues, 0, nonUniformValues},
                {"Uniform", "Non-Uniform", 3780, uniformValues, 1, nonUniformValues},
                {"Uniform", "Non-Uniform", 3600, uniformValues, 2, nonUniformValues},
                {"Uniform", "Non-Uniform", 2880, uniformValues, 3, nonUniformValues},
                {"Non-Uniform", "Empty", 0, nonUniformValues, 0, emptyValues},
                {"Non-Uniform", "Empty", 0, nonUniformValues, 1, emptyValues},
                {"Non-Uniform", "Empty", 0, nonUniformValues, 2, emptyValues},
                {"Non-Uniform", "Empty", 0, nonUniformValues, 3, emptyValues},
                {"Non-Uniform", "Empty String", 45, nonUniformValues, 0, emptyStringValues},
                {"Non-Uniform", "Empty String", 45, nonUniformValues, 1, emptyStringValues},
                {"Non-Uniform", "Empty String", 44, nonUniformValues, 2, emptyStringValues},
                {"Non-Uniform", "Empty String", 37, nonUniformValues, 3, emptyStringValues},
                {"Non-Uniform", "Concrete", 45, nonUniformValues, 0, concreteValues},
                {"Non-Uniform", "Concrete", 45, nonUniformValues, 1, concreteValues},
                {"Non-Uniform", "Concrete", 44, nonUniformValues, 2, concreteValues},
                {"Non-Uniform", "Concrete", 37, nonUniformValues, 3, concreteValues},
                {"Non-Uniform", "Uniform", 3825, nonUniformValues, 0, uniformValues},
                {"Non-Uniform", "Uniform", 3825, nonUniformValues, 1, uniformValues},
                {"Non-Uniform", "Uniform", 3740, nonUniformValues, 2, uniformValues},
                {"Non-Uniform", "Uniform", 3145, nonUniformValues, 3, uniformValues},
                {"Non-Uniform", "Non-Uniform", 2025, nonUniformValues, 0, nonUniformValues},
                {"Non-Uniform", "Non-Uniform", 2025, nonUniformValues, 1, nonUniformValues},
                {"Non-Uniform", "Non-Uniform", 1980, nonUniformValues, 2, nonUniformValues},
                {"Non-Uniform", "Non-Uniform", 1665, nonUniformValues, 3, nonUniformValues}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultValues = baseValues.insert(offset, argValues);
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = (int)resultValues.modelCount();

        // *** assert ***
        String reason = String.format( "<%s Values>.insert(%d, <%s Values>)", baseDescription, offset, argDescription);
        assertThat(reason, modelCount, is(equalTo(expectedModelCount)));
    }
}
