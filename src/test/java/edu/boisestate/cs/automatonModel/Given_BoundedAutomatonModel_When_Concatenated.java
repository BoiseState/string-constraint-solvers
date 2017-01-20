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
public class Given_BoundedAutomatonModel_When_Concatenated {

    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 4)
    public BoundedAutomatonModel argModel;
    @Parameter // first data value (0) is default
    public String baseDescription;
    @Parameter(value = 3)
    public BoundedAutomatonModel baseModel;
    @Parameter(value = 2)
    public int expectedModelCount;
    private AutomatonModel concatModel;

    @Parameters(name = "{index}: <{0} Automaton Model>.concat(<{1} Automaton " +
                       "Model>) - Expected MC = {2}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 2;

        // create automata
        Automaton concrete = BasicAutomata.makeString("AB");
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                         .repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A'))
                                     .concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);

        // bound automata
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                          .repeat(0, initialBoundLength);
        concrete = concrete.intersection(bounding);
        uniform = uniform.intersection(bounding);
        nonUniform = nonUniform.intersection(bounding);
        concrete.determinize();
        concrete.minimize();
        uniform.determinize();
        uniform.minimize();
        nonUniform.determinize();
        nonUniform.minimize();

        // create automaton models
        BoundedAutomatonModel concreteModel =
                new BoundedAutomatonModel(concrete,
                                          alphabet,
                                          initialBoundLength);
        BoundedAutomatonModel uniformModel = new BoundedAutomatonModel(uniform,
                                                                       alphabet,
                                                                       initialBoundLength);
        BoundedAutomatonModel nonUniformModel = new BoundedAutomatonModel(
                nonUniform,
                alphabet,
                initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Concrete base",
                 "Concrete arg",
                 1,
                 concreteModel,
                 concreteModel},
                {"Concrete base",
                 "Uniform arg",
                 21,
                 concreteModel,
                 uniformModel},
                {"Concrete base",
                 "Non-uniform arg",
                 8,
                 concreteModel,
                 nonUniformModel},
                {"Uniform base",
                 "Concrete arg",
                 21,
                 uniformModel,
                 concreteModel},
                {"Uniform base",
                 "Uniform arg",
                 341,
                 uniformModel,
                 uniformModel},
                {"Uniform base",
                 "Non-uniform arg",
                 148,
                 uniformModel,
                 nonUniformModel},
                {"Non-uniform base",
                 "Concrete arg",
                 8,
                 nonUniformModel,
                 concreteModel},
                {"Non-uniform base",
                 "Uniform arg",
                 148,
                 nonUniformModel,
                 uniformModel},
                {"Non-uniform base",
                 "Non-uniform arg",
                 60,
                 nonUniformModel,
                 nonUniformModel},
                });
    }

    @Before
    public void setup() {
        // *** act ***
        this.concatModel = this.baseModel.concatenate(this.argModel);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.concatModel.modelCount().intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
