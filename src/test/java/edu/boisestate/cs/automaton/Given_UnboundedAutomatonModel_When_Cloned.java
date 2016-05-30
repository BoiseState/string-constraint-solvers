package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Given_UnboundedAutomatonModel_When_Cloned {

    private UnboundedAutomatonModel clone;
    private UnboundedAutomatonModel model;

    @Before
    public void setup() {

        // *** arrange ***
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int boundLength = 10;

        // create automaton
        Automaton automaton = BasicAutomata.makeCharRange('A', 'D').repeat();

        // create automaton model1
        this.model = new UnboundedAutomatonModel(automaton,
                                                 alphabet,
                                                 boundLength);

        // *** act ***
        this.clone = (UnboundedAutomatonModel) this.model.clone();
    }

    @Test
    public void it_should_return_a_model_that_is_not_the_same_instance() {

        // *** assert ***
        assertThat(this.model, is(not(sameInstance(this.clone))));
    }

    @Test
    public void
    it_should_return_a_model_with_an_automaton_that_is_not_the_same_instance() {

        // *** assert ***
        Automaton actualAutomaton = this.model.getAutomaton();
        Automaton expectedAutomaton = this.clone.getAutomaton();
        assertThat(actualAutomaton, is(not(sameInstance(expectedAutomaton))));
    }

    @Test
    public void it_should_return_a_model_with_an_equal_automaton() {

        // *** assert ***
        Automaton actualAutomaton = this.model.getAutomaton();
        Automaton expectedAutomaton = this.clone.getAutomaton();
        assertThat(actualAutomaton, is(equalTo(expectedAutomaton)));
    }
}
