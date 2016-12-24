package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.AutomatonModel;
import edu.boisestate.cs.automatonModel.UnboundedAutomatonModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_UnboundedAutomatonModel_When_CheckedForContainedString {

    @Parameter(value = 2)
    public boolean expected;
    @Parameter // first data value (0) is default
    public AutomatonModel model;
    @Parameter(value = 1)
    public String string;

    @Parameters(name = "AutomatonModel[{index}].contains(\"{1}\") = {2}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int boundLength = 10;

        // create automata
        Automaton automaton0 = BasicAutomata.makeCharRange('A', 'D').repeat();
        Automaton automaton1 = BasicAutomata.makeCharRange('A', 'B').repeat();

        // create unbounded automaton models
        UnboundedAutomatonModel model0 =
                new UnboundedAutomatonModel(automaton0, alphabet, boundLength);
        UnboundedAutomatonModel model1 =
                new UnboundedAutomatonModel(automaton1, alphabet, boundLength);

        // create strings
        String string0 = "ABCD";
        String string1 = "ABCD";

        // return list of parameters and expectations for test
        return Arrays.asList(new Object[][]{
                {model0, string0, true},
                {model1, string1, false}
        });
    }

    @Test
    public void it_should_return_the_correct_value() {

        // *** act ***
        boolean actual = this.model.containsString(this.string);

        // *** assert ***
        assertThat(actual, is(equalTo(this.expected)));
    }
}
