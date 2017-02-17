package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static edu.boisestate.cs.automatonModel.operations
        .AutomatonOperationTestUtilities.getConcreteAutomaton;
import static edu.boisestate.cs.automatonModel.operations
        .AutomatonOperationTestUtilities.getNonUniformUnboundAutomaton;
import static edu.boisestate.cs.automatonModel.operations
        .AutomatonOperationTestUtilities.getUniformBoundedAutomaton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_PreciseInsertion_For_BoundedAutomata {

    @Parameter(value = 3)
    public Automaton baseAutomaton;
    @Parameter(value = 5)
    public Automaton argAutomaton;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 2)
    public int expectedModelCount;
    @Parameter(value = 4)
    public int offset;
    private Automaton insertedAutomaton;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.delete({4}, <{1} Automaton>) - Expected" +
                       " MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // get Automata
        Automaton empty = BasicAutomata.makeEmpty();
        Automaton emptyString = BasicAutomata.makeEmptyString();
        Automaton concrete = getConcreteAutomaton(alphabet, "ABC");
        Automaton uniform = getUniformBoundedAutomaton(alphabet,
                                                       initialBoundLength);
        Automaton nonUniform = getNonUniformUnboundAutomaton(alphabet,
                                                             initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", 0, empty, 0, empty},
                {"Empty", "Empty String", 0, empty, 0, emptyString},
                {"Empty", "Concrete", 0, empty, 0, concrete},
                {"Empty", "Uniform", 0, empty, 0, uniform},
                {"Empty", "Non-Uniform", 0, empty, 0, nonUniform},
                {"Empty", "Empty", 0, empty, 1, empty},
                {"Empty", "Empty String", 0, empty, 1, emptyString},
                {"Empty", "Concrete", 0, empty, 1, concrete},
                {"Empty", "Uniform", 0, empty, 1, uniform},
                {"Empty", "Non-Uniform", 0, empty, 1, nonUniform},
                {"Empty", "Empty", 0, empty, 2, empty},
                {"Empty", "Empty String", 0, empty, 2, emptyString},
                {"Empty", "Concrete", 0, empty, 2, concrete},
                {"Empty", "Uniform", 0, empty, 2, uniform},
                {"Empty", "Non-Uniform", 0, empty, 2, nonUniform},
                {"Empty", "Empty", 0, empty, 3, empty},
                {"Empty", "Empty String", 0, empty, 3, emptyString},
                {"Empty", "Concrete", 0, empty, 3, concrete},
                {"Empty", "Uniform", 0, empty, 3, uniform},
                {"Empty", "Non-Uniform", 0, empty, 3, nonUniform},
                {"Empty String", "Empty", 0, empty, 0, empty},
                {"Empty String", "Empty String", 1, empty, 0, emptyString},
                {"Empty String", "Concrete", 1, empty, 0, concrete},
                {"Empty String", "Uniform", 85, empty, 0, uniform},
                {"Empty String", "Non-Uniform", 45, empty, 0, nonUniform},
                {"Empty String", "Empty", 0, empty, 1, empty},
                {"Empty String", "Empty String", 0, empty, 1, emptyString},
                {"Empty String", "Concrete", 0, empty, 1, concrete},
                {"Empty String", "Uniform", 0, empty, 1, uniform},
                {"Empty String", "Non-Uniform", 0, empty, 1, nonUniform},
                {"Empty String", "Empty", 0, empty, 2, empty},
                {"Empty String", "Empty String", 0, empty, 2, emptyString},
                {"Empty String", "Concrete", 0, empty, 2, concrete},
                {"Empty String", "Uniform", 0, empty, 2, uniform},
                {"Empty String", "Non-Uniform", 0, empty, 2, nonUniform},
                {"Empty String", "Empty", 0, empty, 3, empty},
                {"Empty String", "Empty String", 0, empty, 3, emptyString},
                {"Empty String", "Concrete", 0, empty, 3, concrete},
                {"Empty String", "Uniform", 0, empty, 3, uniform},
                {"Empty String", "Non-Uniform", 0, empty, 3, nonUniform},
                {"Concrete", "Empty", 0, empty, 0, empty},
                {"Concrete", "Empty String", 1, empty, 0, emptyString},
                {"Concrete", "Concrete", 1, empty, 0, concrete},
                {"Concrete", "Uniform", 85, empty, 0, uniform},
                {"Concrete", "Non-Uniform", 45, empty, 0, nonUniform},
                {"Concrete", "Empty", 0, empty, 1, empty},
                {"Concrete", "Empty String", 1, empty, 1, emptyString},
                {"Concrete", "Concrete", 1, empty, 1, concrete},
                {"Concrete", "Uniform", 85, empty, 1, uniform},
                {"Concrete", "Non-Uniform", 45, empty, 1, nonUniform},
                {"Concrete", "Empty", 0, empty, 2, empty},
                {"Concrete", "Empty String", 1, empty, 2, emptyString},
                {"Concrete", "Concrete", 1, empty, 2, concrete},
                {"Concrete", "Uniform", 85, empty, 2, uniform},
                {"Concrete", "Non-Uniform", 45, empty, 2, nonUniform},
                {"Concrete", "Empty", 0, empty, 3, empty},
                {"Concrete", "Empty String", 1, empty, 3, emptyString},
                {"Concrete", "Concrete", 1, empty, 3, concrete},
                {"Concrete", "Uniform", 85, empty, 3, uniform},
                {"Concrete", "Non-Uniform", 45, empty, 3, nonUniform},
                {"Uniform", "Empty", 0, empty, 0, empty},
                {"Uniform", "Empty String", 85, empty, 0, emptyString},
                {"Uniform", "Concrete", 85, empty, 0, concrete},
                {"Uniform", "Uniform", 585, empty, 0, uniform},
                {"Uniform", "Non-Uniform", -1, empty, 0, nonUniform},
                {"Uniform", "Empty", 0, empty, 1, empty},
                {"Uniform", "Empty String", 84, empty, 1, emptyString},
                {"Uniform", "Concrete", 84, empty, 1, concrete},
                {"Uniform", "Uniform", 584, empty, 1, uniform},
                {"Uniform", "Non-Uniform", -1, empty, 1, nonUniform},
                {"Uniform", "Empty", 0, empty, 2, empty},
                {"Uniform", "Empty String", 80, empty, 2, emptyString},
                {"Uniform", "Concrete", 80, empty, 2, concrete},
                {"Uniform", "Uniform", 580, empty, 2, uniform},
                {"Uniform", "Non-Uniform", -1, empty, 2, nonUniform},
                {"Uniform", "Empty", 0, empty, 3, empty},
                {"Uniform", "Empty String", 64, empty, 3, emptyString},
                {"Uniform", "Concrete", 64, empty, 3, concrete},
                {"Uniform", "Uniform", 564, empty, 3, uniform},
                {"Uniform", "Non-Uniform", -1, empty, 3, nonUniform},
                {"Non-uniform", "Empty", 0, empty, 0, empty},
                {"Non-uniform", "Empty String", 45, empty, 0, emptyString},
                {"Non-uniform", "Concrete", 45, empty, 0, concrete},
                {"Non-uniform", "Uniform", -1, empty, 0, uniform},
                {"Non-uniform", "Non-Uniform", -1, empty, 0, nonUniform},
                {"Non-uniform", "Empty", 0, empty, 1, empty},
                {"Non-uniform", "Empty String", 45, empty, 1, emptyString},
                {"Non-uniform", "Concrete", 45, empty, 1, concrete},
                {"Non-uniform", "Uniform", -1, empty, 1, uniform},
                {"Non-uniform", "Non-Uniform", -1, empty, 1, nonUniform},
                {"Non-uniform", "Empty", 0, empty, 2, empty},
                {"Non-uniform", "Empty String", 44, empty, 2, emptyString},
                {"Non-uniform", "Concrete", 44, empty, 2, concrete},
                {"Non-uniform", "Uniform", -1, empty, 2, uniform},
                {"Non-uniform", "Non-Uniform", -1, empty, 2, nonUniform},
                {"Non-uniform", "Empty", 0, empty, 3, empty},
                {"Non-uniform", "Empty String", 37, empty, 3, emptyString},
                {"Non-uniform", "Concrete", 37, empty, 3, concrete},
                {"Non-uniform", "Uniform", -1, empty, 3, uniform},
                {"Non-uniform", "Non-Uniform", -1, empty, 3, nonUniform}
        });
    }

    @Before
    public void setup() {

        // *** arrange ***
        PreciseInsert insert = new PreciseInsert(this.offset);

        // *** act ***
        this.insertedAutomaton = insert.op(baseAutomaton, argAutomaton);
        this.insertedAutomaton.minimize();

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.insertedAutomaton)
                                           .intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
