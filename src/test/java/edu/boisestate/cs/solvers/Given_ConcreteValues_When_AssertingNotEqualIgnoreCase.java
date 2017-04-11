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
public class Given_ConcreteValues_When_AssertingNotEqualIgnoreCase {

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
    @Parameters(name = "{index}: <{0} Values>.assertNotEqualIgnoreCase(<{1} Values>) - Expected MC = {2}")
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
                {"Empty", "Empty", 0, emptyValues, emptyValues},
                {"Empty", "Empty String", 0, emptyValues, emptyStringValues},
                {"Empty", "Concrete Lower", 0, emptyValues, lowerConcreteValues},
                {"Empty", "Concrete Upper", 0, emptyValues, upperConcreteValues},
                {"Empty", "Uniform", 0, emptyValues, uniformValues},
                {"Empty", "Non-Uniform", 0, emptyValues, nonUniformValues},
                {"Empty String", "Empty", 0, emptyStringValues, emptyValues},
                {"Empty String", "Empty String", 0, emptyStringValues, emptyStringValues},
                {"Empty String", "Concrete Lower", 1, emptyStringValues, lowerConcreteValues},
                {"Empty String", "Concrete Upper", 1, emptyStringValues, upperConcreteValues},
                {"Empty String", "Uniform", 1, emptyStringValues, uniformValues},
                {"Empty String", "Non-Uniform", 1, emptyStringValues, nonUniformValues},
                {"Concrete Lower", "Empty", 0, lowerConcreteValues, emptyValues},
                {"Concrete Lower", "Empty String", 1, lowerConcreteValues, emptyStringValues},
                {"Concrete Lower", "Concrete Lower", 0, lowerConcreteValues, lowerConcreteValues},
                {"Concrete Lower", "Concrete Upper", 0, lowerConcreteValues, upperConcreteValues},
                {"Concrete Lower", "Uniform", 1, lowerConcreteValues, uniformValues},
                {"Concrete Lower", "Non-Uniform", 1, lowerConcreteValues, nonUniformValues},
                {"Concrete Upper", "Empty", 0, upperConcreteValues, emptyValues},
                {"Concrete Upper", "Empty String", 1, upperConcreteValues, emptyStringValues},
                {"Concrete Upper", "Concrete Lower", 0, upperConcreteValues, lowerConcreteValues},
                {"Concrete Upper", "Concrete Upper", 0, upperConcreteValues, upperConcreteValues},
                {"Concrete Upper", "Uniform", 1, upperConcreteValues, uniformValues},
                {"Concrete Upper", "Non-Uniform", 1, upperConcreteValues, nonUniformValues},
                {"Uniform", "Empty", 0, uniformValues, emptyValues},
                {"Uniform", "Empty String", 584, uniformValues, emptyStringValues},
                {"Uniform", "Concrete Lower", 577, uniformValues, lowerConcreteValues},
                {"Uniform", "Concrete Upper", 577, uniformValues, upperConcreteValues},
                {"Uniform", "Uniform", 585, uniformValues, uniformValues},
                {"Uniform", "Non-Uniform", 585, uniformValues, nonUniformValues},
                {"Non-Uniform", "Empty", 0, nonUniformValues, emptyValues},
                {"Non-Uniform", "Empty String", 185, nonUniformValues, emptyStringValues},
                {"Non-Uniform", "Concrete Lower", 181, nonUniformValues, lowerConcreteValues},
                {"Non-Uniform", "Concrete Upper", 181, nonUniformValues, upperConcreteValues},
                {"Non-Uniform", "Uniform", 185, nonUniformValues, uniformValues},
                {"Non-Uniform", "Non-Uniform", 185, nonUniformValues, nonUniformValues}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultValues = baseValues.assertNotEqualIgnoreCase(argValues);
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = (int)resultValues.modelCount();

        // *** assert ***
        String reason = String.format( "<%s Values>.assertNotEqualIgnoreCase(<%s Values>)", baseDescription, argDescription);
        assertThat(reason, modelCount, is(equalTo(expectedModelCount)));
    }
}
