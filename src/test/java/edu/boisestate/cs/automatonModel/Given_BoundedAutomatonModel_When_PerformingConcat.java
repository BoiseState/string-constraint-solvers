package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import org.junit.Before;
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
public class Given_BoundedAutomatonModel_When_PerformingConcat {

    @Parameter(value = 3)
    public UnboundedAutomatonModel argModel;
    @Parameter(value = 2)
    public UnboundedAutomatonModel baseModel;
    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    private AutomatonModel concatModel;

    @Parameters(name = "{index}: {0} - Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 2;

        // create automata
        Automaton known = BasicAutomata.makeString("AB");
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                         .repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A'))
                                     .concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);
        nonUniform.determinize();
        nonUniform.minimize();

        // create automaton models
        UnboundedAutomatonModel knownModel =
                new UnboundedAutomatonModel(known,
                                            alphabet,
                                            initialBoundLength);
        UnboundedAutomatonModel uniformModel =
                new UnboundedAutomatonModel(uniform,
                                            alphabet,
                                            initialBoundLength);
        UnboundedAutomatonModel nonUniformModel =
                new UnboundedAutomatonModel(nonUniform,
                                            alphabet,
                                            initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Known base and Known arg", 1, knownModel, knownModel},
                {"Known base and Unknown Uniform arg",
                 21,
                 knownModel,
                 uniformModel},
                {"Known base and Unknown Non-uniform arg",
                 8,
                 knownModel,
                 nonUniformModel},
                {"Unknown Uniform base and Known arg",
                 21,
                 uniformModel,
                 knownModel},
                {"Unknown Uniform base and Unknown Uniform arg",
                 341,
                 uniformModel,
                 uniformModel},
                {"Unknown Uniform base and Unknown Non-uniform arg",
                 220,
                 uniformModel,
                 nonUniformModel},
                {"Unknown Non-uniform base and Known arg",
                 8,
                 nonUniformModel,
                 knownModel},
                {"Unknown Non-uniform base and Unknown Uniform arg",
                 220,
                 nonUniformModel,
                 uniformModel},
                {"Unknown Non-uniform base and Unknown Non-uniform arg",
                 78,
                 nonUniformModel,
                 nonUniformModel}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.concatModel = this.baseModel.concatenate(this.argModel);
    }

    @Test
    public void it_should_have_the_correct_model_count() {
        // *** act ***
        int modelCount = this.concatModel.modelCount().intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
