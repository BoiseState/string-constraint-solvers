package edu.boisestate.cs.automaton;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmpty;
import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmptyString;
import static edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedAutomatonOperationTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_WeightedAutomaton_When_Determinizing {

    @Parameter(value = 2)
    public WeightedAutomaton automaton;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 0) // first data value (0) is default
    public String baseDescription;

    private WeightedAutomaton resultAutomaton;

    private static int initialBoundLength;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.determinize() ->" +
                       " Expected MC = {1}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        initialBoundLength = 3;

        // create automata
        WeightedAutomaton empty = makeEmpty();
        WeightedAutomaton emptyString = makeEmptyString();
        WeightedAutomaton concrete = getConcreteWeightedAutomaton(alphabet, "ABC");
        WeightedAutomaton uniformBounded = getUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton nonUniformBounded = getNonUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton uniformUnbounded = getUniformUnboundedWeightedAutomaton(alphabet);
        WeightedAutomaton nonUniformUnbounded = getNonUniformUnboundedWeightedAutomaton(alphabet);

        // unbalanced nonuniform bounded automaton 0
        WeightedAutomaton unbalancedNonUniformBounded0 = new WeightedAutomaton();
        WeightedState q0_0 = new WeightedState();
        WeightedState q1_0 = new WeightedState();
        WeightedState q2_0 = new WeightedState();
        WeightedState q3_0 = new WeightedState();
        q1_0.setAccept(true);
        q3_0.setAccept(true);
        q0_0.addTransition(new WeightedTransition('A', q1_0, 1));
        q0_0.addTransition(new WeightedTransition('A', q2_0, 1));
        q0_0.addTransition(new WeightedTransition('C', 'D', q2_0, 1));
        q1_0.addTransition(new WeightedTransition('A', q3_0, 2));
        q1_0.addTransition(new WeightedTransition('C', 'D', q3_0, 1));
        q2_0.addTransition(new WeightedTransition('A', q3_0, 1));
        unbalancedNonUniformBounded0.setInitialState(q0_0);

        // unbalanced nonuniform bounded automaton 1
        WeightedAutomaton unbalancedNonUniformBounded1 = new WeightedAutomaton();
        WeightedState q0_1 = new WeightedState();
        WeightedState q1_1 = new WeightedState();
        WeightedState q2_1 = new WeightedState();
        WeightedState q3_1 = new WeightedState();
        q1_1.setAccept(true);
        q3_1.setAccept(true);
        q0_1.addTransition(new WeightedTransition('A', q1_1, 4));
        q0_1.addTransition(new WeightedTransition('A', q2_1, 4));
        q0_1.addTransition(new WeightedTransition('C', 'D', q2_1, 4));
        q1_1.addTransition(new WeightedTransition('A', q3_1, 2));
        q1_1.addTransition(new WeightedTransition('C', 'D', q3_1, 1));
        q2_1.addTransition(new WeightedTransition('A', q3_1, 1));
        unbalancedNonUniformBounded1.setInitialState(q0_1);

        // unbalanced nonuniform bounded automaton 2
        WeightedAutomaton unbalancedNonUniformBounded2 = new WeightedAutomaton();
        WeightedState q0_2 = new WeightedState();
        WeightedState q1_2 = new WeightedState();
        WeightedState q2_2 = new WeightedState();
        WeightedState q3_2 = new WeightedState();
        q1_2.setAccept(true);
        q3_2.setAccept(true);
        q0_2.addTransition(new WeightedTransition('A', q1_2, 4));
        q0_2.addTransition(new WeightedTransition('A', q2_2, 8));
        q0_2.addTransition(new WeightedTransition('D', q2_2, 4));
        q1_2.addTransition(new WeightedTransition('A', q3_2, 3));
        q1_2.addTransition(new WeightedTransition('D', q3_2, 1));
        q2_2.addTransition(new WeightedTransition('A', q3_2, 1));
        unbalancedNonUniformBounded2.setInitialState(q0_2);


        // unbalanced nonuniform bounded automaton 3
        WeightedAutomaton unbalancedNonUniformBounded3 = new WeightedAutomaton();
        WeightedState q0_3 = new WeightedState();
        WeightedState q1_3 = new WeightedState();
        WeightedState q2_3 = new WeightedState();
        WeightedState q3_3 = new WeightedState();
        q3_3.setAccept(true);
        q0_3.addTransition(new WeightedTransition('A', q1_3, 4));
        q0_3.addTransition(new WeightedTransition('A', q2_3, 4));
        q0_3.addTransition(new WeightedTransition('C', 'D', q2_3, 4));
        q1_3.addTransition(new WeightedTransition('A', q3_3, 2));
        q1_3.addTransition(new WeightedTransition('C', 'D', q3_3, 1));
        q2_3.addTransition(new WeightedTransition('A', q3_3, 1));
        unbalancedNonUniformBounded3.setInitialState(q0_3);

        // unbalanced nonuniform bounded automaton 4
        WeightedAutomaton unbalancedNonUniformBounded4 = new WeightedAutomaton();
        WeightedState q0_4 = new WeightedState();
        WeightedState q1_4 = new WeightedState();
        WeightedState q2_4 = new WeightedState();
        WeightedState q3_4 = new WeightedState();
        q3_4.setAccept(true);
        q0_4.addTransition(new WeightedTransition('A', q1_4, 4));
        q0_4.addTransition(new WeightedTransition('A', q2_4, 8));
        q0_4.addTransition(new WeightedTransition('D', q2_4, 4));
        q1_4.addTransition(new WeightedTransition('A', q3_4, 3));
        q1_4.addTransition(new WeightedTransition('D', q3_4, 1));
        q2_4.addTransition(new WeightedTransition('A', q3_4, 1));
        unbalancedNonUniformBounded4.setInitialState(q0_4);

        // unbalanced nonuniform bounded automaton 5
        WeightedAutomaton unbalancedNonUniformBounded5 = new WeightedAutomaton();
        WeightedState q0_5 = new WeightedState();
        WeightedState q1_5 = new WeightedState();
        WeightedState q2_5 = new WeightedState();
        WeightedState q3_5 = new WeightedState();
        q1_5.setAccept(true);
        q3_5.setAccept(true);
        q0_5.addTransition(new WeightedTransition('A', q1_5, 7));
        q0_5.addTransition(new WeightedTransition('A', q2_5, 5));
        q0_5.addTransition(new WeightedTransition('C', 'D', q2_5, 5));
        q1_5.addTransition(new WeightedTransition('A', q3_5, 2));
        q1_5.addTransition(new WeightedTransition('C', 'D', q3_5, 3));
        q2_5.addTransition(new WeightedTransition('A', q3_5, 6));
        unbalancedNonUniformBounded5.setInitialState(q0_5);

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty},
                {"Empty String", 1, emptyString},
                {"Concrete", 1, concrete},
                {"Balanced Uniform Unbounded", 85, uniformUnbounded},
                {"Balanced Non-Uniform Unbounded", 45, nonUniformUnbounded},
                {"Balanced Uniform Bounded", 85, uniformBounded},
                {"Balanced Non-Uniform Bounded", 45, nonUniformBounded},
                {"Unbalanced Non-Uniform Bounded 0", 8, unbalancedNonUniformBounded0},
                {"Unbalanced Non-Uniform Bounded 1", 32, unbalancedNonUniformBounded1},
                {"Unbalanced Non-Uniform Bounded 2", 32, unbalancedNonUniformBounded2},
                {"Unbalanced Non-Uniform Bounded 3", 28, unbalancedNonUniformBounded3},
                {"Unbalanced Non-Uniform Bounded 4", 28, unbalancedNonUniformBounded4},
                {"Unbalanced Non-Uniform Bounded 5", 40, unbalancedNonUniformBounded5}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        automaton.determinize();
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(automaton, initialBoundLength)
                                           .intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
