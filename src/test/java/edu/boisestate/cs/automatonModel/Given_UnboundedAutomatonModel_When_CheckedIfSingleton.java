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
public class Given_UnboundedAutomatonModel_When_CheckedIfSingleton {

    @Parameterized.Parameter(value = 1)
    public boolean expected;
    @Parameterized.Parameter // first data value (0) is default
    public AutomatonModel model;

    @Parameterized.Parameters(name = "AutomatonModel[{index}].isSingleton() =" +
                                     " {1}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int boundLength = 10;

        // create automata
        Automaton automaton0 = BasicAutomata.makeCharRange('A', 'D').repeat();
        Automaton automaton1 = BasicAutomata.makeEmptyString();
        Automaton automaton2 = BasicAutomata.makeEmpty();
        Automaton automaton3 = BasicAutomata.makeString("ABCD");
        Automaton automaton4 =
                BasicAutomata.makeChar('A')
                             .concatenate(BasicAutomata.makeChar('B'))
                             .concatenate(BasicAutomata.makeChar('C'))
                             .concatenate(BasicAutomata.makeChar('D'));

        // create unbounded automaton models
        UnboundedAutomatonModel model0 =
                new UnboundedAutomatonModel(automaton0, alphabet, boundLength);
        UnboundedAutomatonModel model1 =
                new UnboundedAutomatonModel(automaton1, alphabet, boundLength);
        UnboundedAutomatonModel model2 =
                new UnboundedAutomatonModel(automaton2, alphabet, boundLength);
        UnboundedAutomatonModel model3 =
                new UnboundedAutomatonModel(automaton3, alphabet, boundLength);
        UnboundedAutomatonModel model4 =
                new UnboundedAutomatonModel(automaton4, alphabet, boundLength);

        // return list of parameters and expectations for test
        return Arrays.asList(new Object[][]{
                {model0, false},
                {model1, true},
                {model2, false},
                {model3, true},
                {model4, true}
        });
    }

    @Test
    public void it_should_return_the_correct_value() {

        // *** act ***
        boolean actual = this.model.isSingleton();

        // *** assert ***
        assertThat(actual, is(equalTo(this.expected)));
    }
}
