package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigInteger;
import java.util.Arrays;

import static dk.brics.automaton.BasicAutomata.makeEmpty;
import static dk.brics.automaton.BasicAutomata.makeEmptyString;
import static edu.boisestate.cs.automatonModel.operations.AutomatonOperationTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_AggregateAutomataModel_When_SplittingAutomatonByLength {

    @Parameter(value = 3)
    public Automaton automaton;
    @Parameter(value = 2)
    public int[] expectedAutomataCounts;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 0) // first data value (0) is default
    public String description;

    private Automaton[] resultAutomata;

    private static int initialBoundLength;
    private static Alphabet alphabet;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.splitAutomatonByLength() - Expected MC = {1}, Expected Automata Count = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        alphabet = new Alphabet("A-D");
        initialBoundLength = 3;

        // create automata
        Automaton empty = makeEmpty();
        Automaton emptyString = makeEmptyString();
        Automaton concrete = getConcreteAutomaton(alphabet, "ABC");
        Automaton uniform = getUniformBoundedAutomaton(alphabet, initialBoundLength);
        Automaton nonUniform = getNonUniformBoundedAutomaton(alphabet, initialBoundLength);
        Automaton uniform0 = getUniformBoundedSubAutomaton(alphabet, 0);
        Automaton uniform1 = getUniformBoundedSubAutomaton(alphabet, 1);
        Automaton uniform2 = getUniformBoundedSubAutomaton(alphabet, 2);
        Automaton uniform3 = getUniformBoundedSubAutomaton(alphabet, 3);
        Automaton nonUniform0 = getNonUniformBoundedSubAutomaton(alphabet, 0);
        Automaton nonUniform1 = getNonUniformBoundedSubAutomaton(alphabet, 1);
        Automaton nonUniform2 = getNonUniformBoundedSubAutomaton(alphabet, 2);
        Automaton nonUniform3 = getNonUniformBoundedSubAutomaton(alphabet, 3);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, new int[]{0,0,0,0}, empty},
                {"Empty String", 1, new int[]{1,0,0,0}, emptyString},
                {"Concrete", 1, new int[]{0,0,0,1}, concrete},
                {"Uniform", 85, new int[]{1,4,16,64}, uniform},
                {"Non-Uniform", 45, new int[]{0,1,7,37}, nonUniform},
                {"Uniform 0", 1, new int[]{1,0,0,0}, uniform0},
                {"Uniform 1", 4, new int[]{0,4,0,0}, uniform1},
                {"Uniform 2", 16, new int[]{0,0,16,0}, uniform2},
                {"Uniform 3", 64, new int[]{0,0,0,64}, uniform3},
                {"Non-Uniform 0", 0, new int[]{0,0,0,0}, nonUniform0},
                {"Non-Uniform 1", 1, new int[]{0,1,0,0}, nonUniform1},
                {"Non-Uniform 2", 7, new int[]{0,0,7,0}, nonUniform2},
                {"Non-Uniform 3", 37, new int[]{0,0,0,37}, nonUniform3}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultAutomata = AggregateAutomataModel.splitAutomatonByLength(automaton, initialBoundLength, alphabet);
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        BigInteger totalModelCount = BigInteger.ZERO;
        for (Automaton automaton : resultAutomata) {
            totalModelCount = totalModelCount.add(StringModelCounter.ModelCount(automaton));
        }
        int modelCount = totalModelCount.intValue();

        // *** assert ***
        String reason = String.format( "Expected Model Count Invalid for <%s Automaton Model>.splitAutomatonByLength()", description);
        assertThat(reason, modelCount, is(equalTo(expectedModelCount)));
    }

    @Test
    public void it_should_have_the_correct_automata() {
        // *** act ***
        int[] automataCounts = new int[resultAutomata.length];
        for (int i = 0; i < automataCounts.length; i++) {
            BigInteger count = StringModelCounter.ModelCount(resultAutomata[i]);
            automataCounts[i] = count.intValue();
        }

        // *** assert ***
        String reason = String.format( "Expected Automata Counts Invalid for <%s Automaton Model>.splitAutomatonByLength()", description);
        assertThat(reason, Arrays.asList(automataCounts), contains(expectedAutomataCounts));
    }
}
