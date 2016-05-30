package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakAccess")
@RunWith(Parameterized.class)
public class Given_UnboundedAutomatonModel_When_GettingAnAcceptedStringExample {

    @Parameterized.Parameter // first data value (0) is default
    public AutomatonModel model;
    @Parameterized.Parameter(value = 1)
    public String example;

    @Parameterized.Parameters(name = "{index}: AutomatonModel[{index}]" +
                                     ".getAcceptedStringExample()")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int boundLength = 10;

        // create automaton
        Automaton[] automata = new Automaton[4];
        automata[0] = BasicAutomata.makeCharRange('A', 'D').repeat();
        automata[1] = BasicAutomata.makeString("ABCD");
        automata[2] = BasicAutomata.makeEmptyString();
        automata[3] = BasicAutomata.makeEmpty();

        // create automaton models
        AutomatonModel[] models = new AutomatonModel[4];
        for (int i = 0; i < automata.length; i++) {
            models[i] = new UnboundedAutomatonModel(automata[i],
                                                    alphabet,
                                                    boundLength);
        }

        // create expected strings
        String[] expected = new String[4];
        expected[0] = "";
        expected[1] = "ABCD";
        expected[2] = "";
        expected[3] = null;

        // return list of parameters and expectations for test
        Object[][] params = new Object[4][2];
        for (int i = 0; i < params.length; i++) {
            params[i][0] = models[i];
            params[i][1] = expected[i];
        }
        return Arrays.asList(params);
    }

    @Test
    public void it_should_return_the_expected_string() {

        // *** act ***
        String actual = this.model.getAcceptedStringExample();

        // *** assert ***
        assertThat(actual, is(equalTo(this.example)));
    }
}
