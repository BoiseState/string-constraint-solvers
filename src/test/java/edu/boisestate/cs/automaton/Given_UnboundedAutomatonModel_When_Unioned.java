package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import edu.boisestate.cs.Alphabet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings({"WeakAccess", "Duplicates"})
@RunWith(Parameterized.class)
public class Given_UnboundedAutomatonModel_When_Unioned {

    @Parameterized.Parameter(value = 2)
    public Automaton expected;
    @Parameterized.Parameter // first data value (0) is default
    public AutomatonModel model0;
    @Parameterized.Parameter(value = 1)
    public AutomatonModel model1;

    @Parameterized.Parameters(name = "{index}: AutomatonModel[{index}][0]" +
                                     ".union(AutomatonModel[{index}][1]")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int boundLength = 10;

        // create automaton
        Automaton[][] automata = new Automaton[4][2];
        automata[0][0] = BasicAutomata.makeCharRange('A', 'B').repeat();
        automata[0][1] = BasicAutomata.makeCharRange('C', 'D').repeat();
        automata[1][0] = BasicAutomata.makeCharRange('A', 'B').repeat();
        automata[1][1] = BasicAutomata.makeCharRange('B', 'C').repeat();
        automata[2][0] = BasicAutomata.makeString("ABCD");
        automata[2][1] = BasicAutomata.makeString("DCBA");
        automata[3][0] = BasicAutomata.makeCharRange('A', 'C').repeat();
        automata[3][1] = BasicAutomata.makeString("DDDD");

        // create automaton models
        AutomatonModel[][] models = new AutomatonModel[4][2];
        for (int i = 0; i < automata.length; i++) {
            for (int j = 0; j < automata[i].length; j++) {
                models[i][j] = new UnboundedAutomatonModel(automata[i][j],
                                                           alphabet,
                                                           boundLength);
            }
        }

        // create expected automata
        Automaton[] expected = new Automaton[4];
        expected[0] = createExpected0();
        expected[1] = createExpected1();
        expected[2] = createExpected2();
        expected[3] = createExpected3();

        // return list of parameters and expectations for test
        Object[][] params = new Object[4][3];
        for (int i = 0; i < params.length; i++) {

            // param 0 is model 0
            params[i][0] = models[i][0];

            // param 1 is model 1
            params[i][1] = models[i][1];

            // param 2 is expected automaton
            params[i][2] = expected[i];
        }
        return Arrays.asList(params);
    }

    private static Automaton createExpected0() {

        // create states
        State state0 = new State();
        State state1 = new State();
        State state2 = new State();

        // create transitions
        Transition transition01 = new Transition('A', 'B', state1);
        Transition transition02 = new Transition('C', 'D', state2);
        Transition transition11 = new Transition('A', 'B', state1);
        Transition transition22 = new Transition('C', 'D', state2);

        // add transitions to states
        state0.addTransition(transition01);
        state0.addTransition(transition02);
        state1.addTransition(transition11);
        state2.addTransition(transition22);

        // set accept states
        state0.setAccept(true);
        state1.setAccept(true);
        state2.setAccept(true);

        // create automaton
        Automaton automaton = new Automaton();

        // set initial state for automaton
        automaton.setInitialState(state0);

        // return automaton
        return automaton;
    }

    private static Automaton createExpected1() {

        // create states
        State state0 = new State();
        State state1 = new State();
        State state2 = new State();

        // create transitions
        Transition transition00 = new Transition('B', state0);
        Transition transition01 = new Transition('A', state1);
        Transition transition02 = new Transition('C', state2);
        Transition transition11 = new Transition('A', 'B', state1);
        Transition transition22 = new Transition('B', 'C', state2);

        // add transitions to states
        state0.addTransition(transition00);
        state0.addTransition(transition01);
        state0.addTransition(transition02);
        state1.addTransition(transition11);
        state2.addTransition(transition22);

        // set accept states
        state0.setAccept(true);
        state1.setAccept(true);
        state2.setAccept(true);

        // create automaton
        Automaton automaton = new Automaton();

        // set initial state for automaton
        automaton.setInitialState(state0);

        // return automaton
        return automaton;
    }

    private static Automaton createExpected3() {

        // create states
        State state0 = new State();
        State state1 = new State();
        State state2 = new State();
        State state3 = new State();
        State state4 = new State();
        State state5 = new State();

        // create transitions
        Transition transition01 = new Transition('A', 'C', state1);
        Transition transition11 = new Transition('A', 'C', state1);
        Transition transition02 = new Transition('D', state2);
        Transition transition23 = new Transition('D', state3);
        Transition transition34 = new Transition('D', state4);
        Transition transition45 = new Transition('D', state5);

        // add transitions to states
        state0.addTransition(transition01);
        state0.addTransition(transition02);
        state1.addTransition(transition11);
        state2.addTransition(transition23);
        state3.addTransition(transition34);
        state4.addTransition(transition45);

        // set accept states
        state0.setAccept(true);
        state1.setAccept(true);
        state2.setAccept(false);
        state3.setAccept(false);
        state4.setAccept(false);
        state5.setAccept(true);

        // create automaton
        Automaton automaton = new Automaton();

        // set initial state for automaton
        automaton.setInitialState(state0);

        // return automaton
        return automaton;
    }

    private static Automaton createExpected2() {

        // create states
        State state0 = new State();
        State state1 = new State();
        State state2 = new State();
        State state3 = new State();
        State state4 = new State();
        State state5 = new State();
        State state6 = new State();
        State state7 = new State();
        State state8 = new State();

        // create transitions
        Transition transition01 = new Transition('A', state1);
        Transition transition12 = new Transition('B', state2);
        Transition transition23 = new Transition('C', state3);
        Transition transition34 = new Transition('D', state4);
        Transition transition05 = new Transition('D', state5);
        Transition transition56 = new Transition('C', state6);
        Transition transition67 = new Transition('B', state7);
        Transition transition78 = new Transition('A', state8);

        // add transitions to states
        state0.addTransition(transition01);
        state0.addTransition(transition05);
        state1.addTransition(transition12);
        state2.addTransition(transition23);
        state3.addTransition(transition34);
        state5.addTransition(transition56);
        state6.addTransition(transition67);
        state7.addTransition(transition78);

        // set accept states
        state0.setAccept(false);
        state1.setAccept(false);
        state2.setAccept(false);
        state3.setAccept(false);
        state4.setAccept(true);
        state5.setAccept(false);
        state6.setAccept(false);
        state7.setAccept(false);
        state8.setAccept(true);

        // create automaton
        Automaton automaton = new Automaton();

        // set initial state for automaton
        automaton.setInitialState(state0);

        // return automaton
        return automaton;
    }

    @Test
    public void it_should_return_the_expected_automaton() {

        // *** act ***
        UnboundedAutomatonModel result =
                (UnboundedAutomatonModel) this.model0.union(this.model1);

        // *** assert ***
        Automaton actual = result.getAutomaton();
        assertThat(actual, is(equalTo(this.expected)));
    }
}
