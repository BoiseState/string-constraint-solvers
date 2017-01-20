package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.PreciseDelete;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
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
public class Given_AggregateBoundedAutomata_When_PreciselyDeleted {

    @Parameter(value = 2)
    public AggregateAutomataModel model;
    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 4)
    public int end;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 3)
    public int start;
    private AutomatonModel deleteModel;


    @Parameters(name = "{index}: <{0} Automaton Model>.delete({3}, {4}) - Expected MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automata
        Automaton concrete = BasicAutomata.makeString("ABC");
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                         .repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A'))
                                     .concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);

        // determinize and minimize automata
        concrete.determinize();
        concrete.minimize();
        uniform.determinize();
        uniform.minimize();
        nonUniform.determinize();
        nonUniform.minimize();

        // create bounded automata
        Automaton chars = BasicAutomata.makeCharSet(alphabet.getCharSet());
        Automaton[] concreteAutomata = new Automaton[initialBoundLength + 1];
        Automaton[] uniformAutomata = new Automaton[initialBoundLength + 1];
        Automaton[] nonUniformAutomata = new Automaton[initialBoundLength + 1];
        for (int i = 0; i <= initialBoundLength; i++) {
            concreteAutomata[i] = concrete.intersection(chars.repeat(i, i));
            uniformAutomata[i] = uniform.intersection(chars.repeat(i, i));
            nonUniformAutomata[i] = nonUniform.intersection(chars.repeat(i, i));
        }

        // create aggregate bounded automata models
        AggregateAutomataModel concreteModel = new AggregateAutomataModel(concreteAutomata, alphabet, initialBoundLength);
        AggregateAutomataModel uniformModel = new AggregateAutomataModel(uniformAutomata, alphabet, initialBoundLength);
        AggregateAutomataModel nonUniformModel = new AggregateAutomataModel(nonUniformAutomata, alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Concrete", 1, concreteModel, 0, 0},
                {"Concrete", 1, concreteModel, 0, 1},
                {"Concrete", 1, concreteModel, 0, 2},
                {"Concrete", 1, concreteModel, 0, 3},
                {"Concrete", 1, concreteModel, 1, 1},
                {"Concrete", 1, concreteModel, 1, 2},
                {"Concrete", 1, concreteModel, 1, 3},
                {"Concrete", 1, concreteModel, 2, 2},
                {"Concrete", 1, concreteModel, 2, 3},
                {"Concrete", 1, concreteModel, 3, 3},
                {"Uniform", 85, uniformModel, 0, 0},
                {"Uniform", 22, uniformModel, 0, 1},
                {"Uniform", 7, uniformModel, 0, 2},
                {"Uniform", 4, uniformModel, 0, 3},
                {"Uniform", 84, uniformModel, 1, 1},
                {"Uniform", 24, uniformModel, 1, 2},
                {"Uniform", 12, uniformModel, 1, 3},
                {"Uniform", 80, uniformModel, 2, 2},
                {"Uniform", 32, uniformModel, 2, 3},
                {"Uniform", 64, uniformModel, 3, 3},
                {"Non-uniform", 45, nonUniformModel, 0, 0},
                {"Non-uniform", 21, nonUniformModel, 0, 1},
                {"Non-uniform", 6, nonUniformModel, 0, 2},
                {"Non-uniform", 3, nonUniformModel, 0, 3},
                {"Non-uniform", 45, nonUniformModel, 1, 1},
                {"Non-uniform", 21, nonUniformModel, 1, 2},
                {"Non-uniform", 9, nonUniformModel, 1, 3},
                {"Non-uniform", 44, nonUniformModel, 2, 2},
                {"Non-uniform", 23, nonUniformModel, 2, 3},
                {"Non-uniform", 37, nonUniformModel, 3, 3}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        this.deleteModel = this.model.delete(this.start, this.end);

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = this.deleteModel.modelCount().intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
