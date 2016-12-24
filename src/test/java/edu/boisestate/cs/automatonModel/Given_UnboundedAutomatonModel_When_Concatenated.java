package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.AutomatonModel;
import edu.boisestate.cs.automatonModel.UnboundedAutomatonModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakAccess")
@RunWith(Parameterized.class)
public class Given_UnboundedAutomatonModel_When_Concatenated {

    @Parameterized.Parameter(value = 2)
    public Automaton expected;
    @Parameterized.Parameter // first data value (0) is default
    public AutomatonModel model1;
    @Parameterized.Parameter(value = 1)
    public AutomatonModel model2;

    @Parameterized.Parameters(name = "{index}: AutomatonModel[{index}][0]" +
                                     ".concatenate(AutomatonModel[{index}][1]")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int boundLength = 10;

        // create automaton
        Automaton automaton00 = BasicAutomata.makeString("AB");
        Automaton automaton01 = BasicAutomata.makeString("CD");
        Automaton automaton10 = BasicAutomata.makeEmptyString();
        Automaton automaton11 = BasicAutomata.makeCharRange('A', 'D').repeat();
        Automaton automaton20 = BasicAutomata.makeCharRange('A', 'B').repeat();
        Automaton automaton21 = BasicAutomata.makeCharRange('C', 'D').repeat();

        // create automaton model1
        UnboundedAutomatonModel model00 =
                new UnboundedAutomatonModel(automaton00, alphabet, boundLength);
        UnboundedAutomatonModel model01 =
                new UnboundedAutomatonModel(automaton01, alphabet, boundLength);
        UnboundedAutomatonModel model10 =
                new UnboundedAutomatonModel(automaton10, alphabet, boundLength);
        UnboundedAutomatonModel model11 =
                new UnboundedAutomatonModel(automaton11, alphabet, boundLength);
        UnboundedAutomatonModel model20 =
                new UnboundedAutomatonModel(automaton20, alphabet, boundLength);
        UnboundedAutomatonModel model21 =
                new UnboundedAutomatonModel(automaton21, alphabet, boundLength);

        // create expected automata
        Automaton expected0 = BasicAutomata.makeString("ABCD");
        Automaton expected1 = BasicAutomata.makeCharRange('A', 'D').repeat();
        Automaton expected2 = createExpected2();
        ;

        // return list of parameters and expectations for test
        return Arrays.asList(new Object[][]{
                {model00, model01, expected0},
                {model10, model11, expected1},
                {model20, model21, expected2}
        });
    }

    private static Automaton createExpected2() {

        // create states
        State state0 = new State();
        State state1 = new State();

        // create transitions
        Transition transition00 = new Transition('A', 'B', state0);
        Transition transition01 = new Transition('C', 'D', state1);
        Transition transition11 = new Transition('C', 'D', state1);

        // add transitions to states
        state0.addTransition(transition00);
        state0.addTransition(transition01);
        state1.addTransition(transition11);

        // set accept states
        state0.setAccept(true);
        state1.setAccept(true);

        // create automaton
        Automaton automaton = new Automaton();

        // set automaton initial state
        automaton.setInitialState(state0);

        // return automaton
        return automaton;
    }

    @Test
    public void it_should_contain_the_correct_automaton() {

        // *** act ***
        UnboundedAutomatonModel result =
                (UnboundedAutomatonModel) this.model1.concatenate(this.model2);

        // *** assert ***
        Automaton actual = result.getAutomaton();
        actual.minimize();
        this.expected.minimize();
        assertThat(actual, is(equalTo(this.expected)));
    }
}
