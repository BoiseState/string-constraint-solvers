package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.runners.Parameterized.*;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_UnboundedAutomatonModel_When_CheckedIfEmpty {

    @Parameter // first data value (0) is default
    public AutomatonModel model;

    @Parameter(value = 1)
    public boolean expected;

    @Parameters(name = "AutomatonModel[{index}].empty() = {1}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int boundLength = 10;

        // create automata
        Automaton automaton0 = BasicAutomata.makeEmptyString();
        Automaton automaton1 = BasicAutomata.makeCharRange('A', 'D').repeat();

        // create unbounded automaton models
        UnboundedAutomatonModel model0 =
                new UnboundedAutomatonModel(automaton0, alphabet, boundLength);
        UnboundedAutomatonModel model1 =
                new UnboundedAutomatonModel(automaton1, alphabet, boundLength);

        // return list of parameters and expectations for test
        return Arrays.asList(new Object[][]{
                {model0, true},
                {model1, false}
        });
    }

    @Test
    public void it_should_return_the_correct_value() {

        // *** act ***
        boolean actual = this.model.isEmpty();

        // *** assert ***
        assertThat(actual, is(equalTo(this.expected)));
    }
}
