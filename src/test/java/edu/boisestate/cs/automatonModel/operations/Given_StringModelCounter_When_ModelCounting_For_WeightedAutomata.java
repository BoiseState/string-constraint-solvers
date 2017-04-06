package edu.boisestate.cs.automatonModel.operations;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedAutomatonOperationTestUtilities;
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
import static edu.boisestate.cs.automatonModel.operations.StringModelCounter
        .ModelCount;
import static edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedAutomatonOperationTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_StringModelCounter_When_ModelCounting_For_WeightedAutomata {

    @Parameter(value = 2)
    public WeightedAutomaton automaton;
    @Parameter(value = 0) // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    private BigInteger modelCount;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: Automaton <{0}>, Bounding Length {2}, Expected MC {1}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automata
        WeightedAutomaton empty = makeEmpty();
        WeightedAutomaton emptyString = makeEmptyString();
        WeightedAutomaton emptyString2 = makeEmptyString();
        emptyString2.setInitialFactor(2);
        WeightedAutomaton emptyString3 = makeEmptyString();
        emptyString3.setNumEmptyStrings(3);
        WeightedAutomaton emptyString4 = makeEmptyString();
        emptyString4.setInitialFactor(2);
        emptyString4.setNumEmptyStrings(2);
        WeightedAutomaton concrete = getConcreteWeightedAutomaton(alphabet, "ABC");
        WeightedAutomaton boundUniform = getUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton boundNonUniform = getNonUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
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


        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty},
                {"Empty String", 1, emptyString},
                {"Empty String 2", 2, emptyString2},
                {"Empty String 3", 3, emptyString3},
                {"Empty String 4", 4, emptyString4},
                {"Concrete", 1, concrete},
                {"Uniform", 85, boundUniform},
                {"Non-Uniform", 45, boundNonUniform},
                {"Balanced Uniform 0", 1, balancedUniform0},
                {"Balanced Uniform 1", 4, balancedUniform1},
                {"Balanced Uniform 2", 16, balancedUniform2},
                {"Balanced Uniform 3", 64, balancedUniform3},
                {"Balanced Non-Uniform 0", 0, balancedNonUniform0},
                {"Balanced Non-Uniform 1", 1, balancedNonUniform1},
                {"Balanced Non-Uniform 2", 7, balancedNonUniform2},
                {"Balanced Non-Uniform 3", 37, balancedNonUniform3},
                {"Unbalanced Uniform 0", 64, unbalancedUniform0},
                {"Unbalanced Uniform 1", 64, unbalancedUniform1},
                {"Unbalanced Uniform 2", 64, unbalancedUniform2},
                {"Unbalanced Non-Uniform 0", 37, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 1", 37, unbalancedNonUniform1},
                {"Unbalanced Non-Uniform 2", 37, unbalancedNonUniform2},
                {"Non-Uniform delete(0,1)", 37, nonUniformDelete01},
                {"Non-Uniform delete(1,2)", 37, nonUniformDelete12},
                {"Unbalanced Non-Uniform 0 replace('B', 'A')", 37, replaceUnbalancedNonUniform0},
                {"Unbalanced Non-Uniform 1 replace('B', 'A')", 37, replaceUnbalancedNonUniform1},
                {"Unbalanced Non-Uniform 2 replace('B', 'A')", 37, replaceUnbalancedNonUniform2}
        });
    }

    @Before
    public void setup() {
        /* *** act ****/
        this.modelCount = ModelCount(this.automaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** assert ***
        assertThat(this.modelCount.intValue(), is(equalTo(this.expectedModelCount)));
    }
}
