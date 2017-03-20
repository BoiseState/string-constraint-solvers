package edu.boisestate.cs.automatonModel;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigInteger;
import java.util.Arrays;

import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmpty;
import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmptyString;
import static edu.boisestate.cs.automatonModel.operations.weighted.WeightedAutomatonOperationTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_WeightedAutomatonModel_When_SplittingAutomatonByLength {

    @Parameter(value = 3)
    public WeightedAutomaton automaton;
    @Parameter(value = 2)
    public int expectedAutomataCount;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 0) // first data value (0) is default
    public String description;

    private WeightedAutomaton[] resultAutomata;

    private static int initialBoundLength;
    private static Alphabet alphabet;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton Model>.splitAutomatonByLength() - Expected MC = {1}, Expected Automata Count = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        alphabet = new Alphabet("A-D");
        initialBoundLength = 3;

        // create automata
        WeightedAutomaton empty = makeEmpty();
        WeightedAutomaton emptyString = makeEmptyString();
        WeightedAutomaton concrete = getConcreteWeightedAutomaton(alphabet, "ABC");
        WeightedAutomaton uniform = getUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton nonUniform = getNonUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton balancedUniform0 = balanced_Uniform_WeightedAutomaton(alphabet, 0);
        WeightedAutomaton balancedUniform1 = balanced_Uniform_WeightedAutomaton(alphabet, 1);
        WeightedAutomaton balancedUniform2 = balanced_Uniform_WeightedAutomaton(alphabet, 2);
        WeightedAutomaton balancedUniform3 = balanced_Uniform_WeightedAutomaton(alphabet, 3);
        WeightedAutomaton balancedNonUniform0 = balanced_NonUniform_WeightedAutomaton(alphabet, 0);
        WeightedAutomaton balancedNonUniform1 = balanced_NonUniform_WeightedAutomaton(alphabet, 1);
        WeightedAutomaton balancedNonUniform2 = balanced_NonUniform_WeightedAutomaton(alphabet, 2);
        WeightedAutomaton balancedNonUniform3 = balanced_NonUniform_WeightedAutomaton(alphabet, 3);
        WeightedAutomaton unbalancedUniform0 = unbalanced_Uniform_WeightedAutomaton_0();
        WeightedAutomaton unbalancedUniform1 = unbalanced_Uniform_WeightedAutomaton_1();
        WeightedAutomaton unbalancedUniform2 = unbalanced_Uniform_WeightedAutomaton_2();
        WeightedAutomaton unbalancedNonUniform0 = unbalanced_NonUniform_WeightedAutomaton_0();
        WeightedAutomaton unbalancedNonUniform1 = unbalanced_NonUniform_WeightedAutomaton_1();
        WeightedAutomaton unbalancedNonUniform2 = unbalanced_NonUniform_WeightedAutomaton_2();
        WeightedAutomaton nonUniformDelete01 = nonUniform_delete_01();
        WeightedAutomaton nonUniformDelete12 = nonUniform_delete_12();
        WeightedAutomaton replaceUnbalancedNonUniform0 = replaceUnbalancedNonUniform0();
        WeightedAutomaton replaceUnbalancedNonUniform1 = replaceUnbalancedNonUniform1();
        WeightedAutomaton replaceUnbalancedNonUniform2 = replaceUnbalancedNonUniform2();

        return Arrays.asList(new Object[][]{
                {"Empty", 0, 1, empty},
                {"Empty String", 1, 1, emptyString},
                {"Concrete", 1, 4, concrete},
                {"Uniform", 85, 4, uniform},
                {"Non-Uniform", 45, 4, nonUniform},
                {"Balanced Uniform 0", 1, 0, balancedUniform0},
                {"Balanced Uniform 1", 4, 1, balancedUniform1},
                {"Balanced Uniform 2", 16, 2, balancedUniform2},
                {"Balanced Uniform 3", 64, 3, balancedUniform3},
                {"Balanced Non-Uniform 0", 0, 0, balancedNonUniform0},
                {"Balanced Non-Uniform 1", 1, 1, balancedNonUniform1},
                {"Balanced Non-Uniform 2", 7, 2, balancedNonUniform2},
                {"Balanced Non-Uniform 3", 37, 3, balancedNonUniform3},
                {"Unbalanced Uniform 0", 64, 2, unbalancedUniform0},
                {"Unbalanced Uniform 1", 64, 2, unbalancedUniform1},
                {"Unbalanced Uniform 2", 64, 2, unbalancedUniform2},
                {"Unbalanced Non-Uniform 0", 37, 2, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 1", 37, 2, unbalancedNonUniform1},
                {"Unbalanced Non-Uniform 2", 37, 2, unbalancedNonUniform2},
                {"Non-Uniform delete(0,1)", 37, 2, nonUniformDelete01},
                {"Non-Uniform delete(1,2)", 37, 2, nonUniformDelete12},
                {"Unbalanced Non-Uniform 0 replace('B', 'A')", 37, 2, replaceUnbalancedNonUniform0},
                {"Unbalanced Non-Uniform 1 replace('B', 'A')", 37, 2, replaceUnbalancedNonUniform1},
                {"Unbalanced Non-Uniform 2 replace('B', 'A')", 37, 2, replaceUnbalancedNonUniform2},
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultAutomata = WeightedAutomatonModel.splitAutomatonByLength(automaton, initialBoundLength, alphabet);
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        BigInteger totalModelCount = BigInteger.ZERO;
        for (WeightedAutomaton automaton : resultAutomata) {
            totalModelCount = totalModelCount.add(StringModelCounter.ModelCount(automaton));
        }
        int modelCount = totalModelCount.intValue();

        // *** assert ***
        String reason = String.format( "Expected Model Count Invalid for <%s Automaton Model>.splitAutomatonByLength()", description);
        assertThat(reason, modelCount, is(equalTo(expectedModelCount)));
    }

    @Test
    public void it_should_have_the_correct_number_of_automata() {
        // *** act ***
        int automatonCount = resultAutomata.length;

        // *** assert ***
        String reason = String.format( "Expected Automaton Count Invalid for <%s Automaton Model>.splitAutomatonByLength()", description);
        assertThat(reason, automatonCount, is(equalTo(expectedAutomataCount)));
    }
}
