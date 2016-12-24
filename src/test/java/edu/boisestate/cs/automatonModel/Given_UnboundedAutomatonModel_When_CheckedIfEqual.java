package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
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

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_UnboundedAutomatonModel_When_CheckedIfEqual {

    @Parameterized.Parameter(value = 2)
    public boolean expected;
    @Parameterized.Parameter // first data value (0) is default
    public AutomatonModel model1;
    @Parameterized.Parameter(value = 1)
    public AutomatonModel model2;

    @Parameterized.Parameters(name = "{index}: AutomatonModel == " +
                                     "AutomatonModel = {2}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int boundLength = 10;

        // create automata
        Automaton automaton00 = BasicAutomata.makeCharRange('A', 'D').repeat();
        Automaton automaton01 = BasicAutomata.makeCharRange('A', 'B').repeat();
        Automaton automaton10 = BasicAutomata.makeCharRange('A', 'D').repeat();
        Automaton automaton11 = BasicAutomata.makeCharRange('A', 'D').repeat();
        Automaton automaton20 = BasicAutomata.makeCharRange('A', 'D').repeat();
        Automaton automaton21 = automaton20;

        // create unbounded automaton models
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

        // return list of parameters and expectations for test
        return Arrays.asList(new Object[][]{
                {model00, model01, false},
                {model10, model11, true},
                {model20, model21, true}
        });
    }

    @Test
    public void it_should_return_the_correct_value() {

        // *** act ***
        boolean actual = this.model1.equals(model2);

        // *** assert ***
        assertThat(actual, is(equalTo(this.expected)));
    }
}
