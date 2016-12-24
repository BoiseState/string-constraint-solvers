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

@SuppressWarnings({"WeakAccess", "Duplicates"})
@RunWith(Parameterized.class)
public class Given_UnboundedAutomatonModel_When_Minused {

    @Parameterized.Parameter(value = 2)
    public Automaton expected;
    @Parameterized.Parameter // first data value (0) is default
    public AutomatonModel model0;
    @Parameterized.Parameter(value = 1)
    public AutomatonModel model1;

    @Parameterized.Parameters(name = "{index}: AutomatonModel[{index}][0]" +
                                     ".minus(AutomatonModel[{index}][1]")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int boundLength = 10;

        // create automaton
        Automaton[][] automata = new Automaton[4][2];
        automata[0][0] = BasicAutomata.makeCharRange('A', 'D').repeat();
        automata[0][1] = BasicAutomata.makeCharRange('C', 'D').repeat();
        automata[1][0] = BasicAutomata.makeCharRange('A', 'B').repeat();
        automata[1][1] = BasicAutomata.makeCharRange('B', 'C').repeat();
        automata[2][0] = BasicAutomata.makeCharRange('A', 'D').repeat();
        automata[2][1] = BasicAutomata.makeEmptyString();
        automata[3][0] = BasicAutomata.makeCharRange('A', 'D').repeat();
        automata[3][1] = BasicAutomata.makeEmpty();

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
        expected[3] = BasicAutomata.makeCharRange('A', 'D').repeat();

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

        // create transitions
        Transition transition00 = new Transition('C', 'D', state0);
        Transition transition01 = new Transition('A', 'B', state1);
        Transition transition11 = new Transition('A', 'D', state1);

        // add transitions to states
        state0.addTransition(transition00);
        state0.addTransition(transition01);
        state1.addTransition(transition11);

        // set accept states
        state0.setAccept(false);
        state1.setAccept(true);

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

        // create transitions
        Transition transition00 = new Transition('B', state0);
        Transition transition01 = new Transition('A', state1);
        Transition transition11 = new Transition('A', 'B', state1);

        // add transitions to states
        state0.addTransition(transition00);
        state0.addTransition(transition01);
        state1.addTransition(transition11);

        // set accept states
        state0.setAccept(false);
        state1.setAccept(true);

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

        // create transitions
        Transition transition01 = new Transition('A', 'D', state1);
        Transition transition11 = new Transition('A', 'D', state1);

        // add transitions to states
        state0.addTransition(transition01);
        state1.addTransition(transition11);

        // set accept states
        state0.setAccept(false);
        state1.setAccept(true);

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
                (UnboundedAutomatonModel) this.model0.minus(this.model1);

        // *** assert ***
        Automaton actual = result.getAutomaton();
        assertThat(actual, is(equalTo(this.expected)));
    }
}
