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
public class Given_UnboundedAutomatonModel_When_Complemented {

    @Parameterized.Parameter(value = 1)
    public Automaton expected;
    @Parameterized.Parameter // first data value (0) is default
    public AutomatonModel model;

    @Parameterized.Parameters(name = "{index}: AutomatonModel[{index}]" +
                                     ".complement()")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int boundLength = 10;

        // create automaton
        Automaton automaton0 = BasicAutomata.makeCharRange('A', 'B').repeat();
        Automaton automaton1 = BasicAutomata.makeCharRange('A', 'D').repeat();
        Automaton automaton2 = BasicAutomata.makeString("ABCD");

        // create automaton model1
        UnboundedAutomatonModel model0 =
                new UnboundedAutomatonModel(automaton0, alphabet, boundLength);
        UnboundedAutomatonModel model1 =
                new UnboundedAutomatonModel(automaton1, alphabet, boundLength);
        UnboundedAutomatonModel model2 =
                new UnboundedAutomatonModel(automaton2, alphabet, boundLength);

        // create expected automata
        Automaton expected0 = BasicAutomata.makeCharRange('A', 'D')
                                           .repeat()
                                           .minus(automaton0);
        Automaton expected1 = BasicAutomata.makeEmpty();
        Automaton expected2 = BasicAutomata.makeCharRange('A', 'D')
                                           .repeat()
                                           .minus(automaton2);
        ;

        // return list of parameters and expectations for test
        return Arrays.asList(new Object[][]{
                {model0, expected0},
                {model1, expected1},
                {model2, expected2}
        });
    }

    @Test
    public void it_should_contain_the_correct_automaton() {

        // *** act ***
        UnboundedAutomatonModel complement =
                (UnboundedAutomatonModel) this.model.complement();

        // *** assert ***
        Automaton actual = complement.getAutomaton();
        assertThat(actual, is(equalTo(expected)));
    }
}
