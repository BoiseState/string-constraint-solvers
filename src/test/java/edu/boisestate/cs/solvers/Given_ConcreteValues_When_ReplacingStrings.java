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
public class Given_ConcreteValues_When_ReplacingStrings {

    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 2)
    public ConcreteValues values;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 3)
    public String find;
    @Parameter(value = 4)
    public String replace;
    private ConcreteValues resultValues;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Values>.replaceChar({3},{4}) - Expected MC = {1}")
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
                {"Empty", 0, emptyValues, "A", "A"},
                {"Empty", 0, emptyValues, "A", "B"},
                {"Empty", 0, emptyValues, "A", "C"},
                {"Empty", 0, emptyValues, "A", "D"},
                {"Empty", 0, emptyValues, "B", "A"},
                {"Empty", 0, emptyValues, "B", "B"},
                {"Empty", 0, emptyValues, "B", "C"},
                {"Empty", 0, emptyValues, "B", "D"},
                {"Empty", 0, emptyValues, "C", "A"},
                {"Empty", 0, emptyValues, "C", "B"},
                {"Empty", 0, emptyValues, "C", "C"},
                {"Empty", 0, emptyValues, "C", "D"},
                {"Empty", 0, emptyValues, "D", "A"},
                {"Empty", 0, emptyValues, "D", "B"},
                {"Empty", 0, emptyValues, "D", "C"},
                {"Empty", 0, emptyValues, "D", "D"},
                {"Empty String", 1, emptyStringValues, "A", "A"},
                {"Empty String", 1, emptyStringValues, "A", "B"},
                {"Empty String", 1, emptyStringValues, "A", "C"},
                {"Empty String", 1, emptyStringValues, "A", "D"},
                {"Empty String", 1, emptyStringValues, "B", "A"},
                {"Empty String", 1, emptyStringValues, "B", "B"},
                {"Empty String", 1, emptyStringValues, "B", "C"},
                {"Empty String", 1, emptyStringValues, "B", "D"},
                {"Empty String", 1, emptyStringValues, "C", "A"},
                {"Empty String", 1, emptyStringValues, "C", "B"},
                {"Empty String", 1, emptyStringValues, "C", "C"},
                {"Empty String", 1, emptyStringValues, "C", "D"},
                {"Empty String", 1, emptyStringValues, "D", "A"},
                {"Empty String", 1, emptyStringValues, "D", "B"},
                {"Empty String", 1, emptyStringValues, "D", "C"},
                {"Empty String", 1, emptyStringValues, "D", "D"},
                {"Concrete", 1, concreteValues, "A", "A"},
                {"Concrete", 1, concreteValues, "A", "B"},
                {"Concrete", 1, concreteValues, "A", "C"},
                {"Concrete", 1, concreteValues, "A", "D"},
                {"Concrete", 1, concreteValues, "B", "A"},
                {"Concrete", 1, concreteValues, "B", "B"},
                {"Concrete", 1, concreteValues, "B", "C"},
                {"Concrete", 1, concreteValues, "B", "D"},
                {"Concrete", 1, concreteValues, "C", "A"},
                {"Concrete", 1, concreteValues, "C", "B"},
                {"Concrete", 1, concreteValues, "C", "C"},
                {"Concrete", 1, concreteValues, "C", "D"},
                {"Concrete", 1, concreteValues, "D", "A"},
                {"Concrete", 1, concreteValues, "D", "B"},
                {"Concrete", 1, concreteValues, "D", "C"},
                {"Concrete", 1, concreteValues, "D", "D"},
                {"Uniform", 85, uniformValues, "A", "A"},
                {"Uniform", 85, uniformValues, "A", "B"},
                {"Uniform", 85, uniformValues, "A", "C"},
                {"Uniform", 85, uniformValues, "A", "D"},
                {"Uniform", 85, uniformValues, "B", "A"},
                {"Uniform", 85, uniformValues, "B", "B"},
                {"Uniform", 85, uniformValues, "B", "C"},
                {"Uniform", 85, uniformValues, "B", "D"},
                {"Uniform", 85, uniformValues, "C", "A"},
                {"Uniform", 85, uniformValues, "C", "B"},
                {"Uniform", 85, uniformValues, "C", "C"},
                {"Uniform", 85, uniformValues, "C", "D"},
                {"Uniform", 85, uniformValues, "D", "A"},
                {"Uniform", 85, uniformValues, "D", "B"},
                {"Uniform", 85, uniformValues, "D", "C"},
                {"Uniform", 85, uniformValues, "D", "D"},
                {"Non-Uniform", 45, nonUniformValues, "A", "A"},
                {"Non-Uniform", 45, nonUniformValues, "A", "B"},
                {"Non-Uniform", 45, nonUniformValues, "A", "C"},
                {"Non-Uniform", 45, nonUniformValues, "A", "D"},
                {"Non-Uniform", 45, nonUniformValues, "B", "A"},
                {"Non-Uniform", 45, nonUniformValues, "B", "B"},
                {"Non-Uniform", 45, nonUniformValues, "B", "C"},
                {"Non-Uniform", 45, nonUniformValues, "B", "D"},
                {"Non-Uniform", 45, nonUniformValues, "C", "A"},
                {"Non-Uniform", 45, nonUniformValues, "C", "B"},
                {"Non-Uniform", 45, nonUniformValues, "C", "C"},
                {"Non-Uniform", 45, nonUniformValues, "C", "D"},
                {"Non-Uniform", 45, nonUniformValues, "D", "A"},
                {"Non-Uniform", 45, nonUniformValues, "D", "B"},
                {"Non-Uniform", 45, nonUniformValues, "D", "C"},
                {"Non-Uniform", 45, nonUniformValues, "D", "D"}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultValues = values.replace(find, replace);
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = (int)resultValues.modelCount();

        // *** assert ***
        String reason = String.format("<%s Values>.replaceChar(\"%s\", \"%s\")", description, find, replace);
        assertThat(reason, modelCount, is(equalTo(expectedModelCount)));
    }
}
