package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.UnaryOperation;
import edu.boisestate.cs.Alphabet;

public abstract class AutomatonModelManager {

    static protected AutomatonModelManager instance = null;
    protected Alphabet alphabet;

    public Alphabet getAlphabet() {
        return this.alphabet;
    }

    static public AutomatonModelManager getInstance(Alphabet alphabet,
                                                    int modelVersion,
                                                    int initialBoundLength) {

        if (modelVersion == 1) {
            UnboundedAutomatonModelManager.setInstance(alphabet,
                                                       initialBoundLength);
        } else if (modelVersion == 2) {
            BoundedAutomatonModelManager.setInstance(alphabet,
                                                     initialBoundLength);
        } else if (modelVersion == 3) {
            AggregateAutomatonModelManager.setInstance(alphabet,
                                                       initialBoundLength);
        }

        return instance;
    }

    public abstract AutomatonModel createString(String string);

    public abstract AutomatonModel createAnyString(int max);

    public abstract AutomatonModel allSuffixes(AutomatonModel model);

    public abstract AutomatonModel ignoreCase(AutomatonModel model);

    public abstract AutomatonModel replace(AutomatonModel model);

    public abstract AutomatonModel replace(AutomatonModel model,
                                           char find,
                                           char replace);

    public abstract AutomatonModel replaceFindKnown(AutomatonModel model,
                                                    char find);

    public abstract AutomatonModel replaceReplaceKnown(AutomatonModel model,
                                                       char replace);

    public abstract AutomatonModel replace(AutomatonModel model,
                                           String find,
                                           String replace);

    public abstract AutomatonModel reverse(AutomatonModel baseModel);

    public abstract AutomatonModel allPrefixes(AutomatonModel model);

    public abstract AutomatonModel toLowercase(AutomatonModel model);

    public abstract AutomatonModel toUppercase(AutomatonModel model);

    public abstract AutomatonModel trim(AutomatonModel model);

    public AutomatonModel assertContainedInOther(AutomatonModel model,
                                                 AutomatonModel
                                                         containingModel) {

        // get all substrings for containing model
        AutomatonModel substrings = this.allSubstrings(containingModel);

        // intersect substrings with model
        return model.intersect(substrings);
    }

    public AutomatonModel assertContainsOther(AutomatonModel model,
                                              AutomatonModel containedModel) {

        // create any string models
        AutomatonModel anyString1 = this.createAnyString();
        AutomatonModel anyString2 = this.createAnyString();

        // concatenate with contained model
        AutomatonModel x = anyString1.concatenate(containedModel)
                                     .concatenate(anyString2);

        // return intersection with model
        return model.intersect(x);
    }

    public AutomatonModel assertEmpty(AutomatonModel model) {

        // intersect model with empty string
        AutomatonModel empty = this.createEmptyString();
        return model.intersect(empty);
    }

    public AutomatonModel assertEndsWith(AutomatonModel model,
                                         AutomatonModel endingModel) {

        // get end model by concatenating any string and ending model
        AutomatonModel end = this.createAnyString().concatenate(endingModel);

        // return intersection of model and end
        return model.intersect(end);
    }

    public abstract AutomatonModel createAnyString();

    public AutomatonModel assertEquals(AutomatonModel model,
                                       AutomatonModel equalModel) {

        // return intersection of model with equal model
        return model.intersect(equalModel);
    }

    public AutomatonModel assertHasLength(AutomatonModel model,
                                          int min,
                                          int max) {

        // check min and max
        if (min > max) {
            return this.createEmpty();
        }

        // get any string with length between min and max
        AutomatonModel minMaxString = this.createAnyString(min, max);

        // return intersection of model and min max string
        return model.intersect(minMaxString);
    }

    public abstract AutomatonModel createAnyString(int min, int max);

    public abstract AutomatonModel createEmpty();

    public AutomatonModel assertNotContainedInOther(AutomatonModel model,
                                                    AutomatonModel
                                                            notContainingModel) {

        // get all substrings for the not containing model
        AutomatonModel substrings = this.allSubstrings(notContainingModel);

        // get complement of substrings
        AutomatonModel complement = substrings.complement();

        // return intersection of model and complement
        return model.intersect(complement);
    }

    protected Automaton performUnaryOperation(Automaton automaton,
                                              UnaryOperation operation) {

        // use operation
        Automaton result = operation.op(automaton);

        // bound automaton to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton alphabet = BasicAutomata.makeCharSet(charSet).repeat();
        result = result.intersection(alphabet);

        // return resulting automaton
        return result;
    }

    public abstract AutomatonModel allSubstrings(AutomatonModel model);

    public AutomatonModel assertNotContainsOther(AutomatonModel model,
                                                 AutomatonModel
                                                         notContainedModel) {

        // concat not contained model with any strings
        AutomatonModel concat = this.createAnyString()
                                    .concatenate(notContainedModel)
                                    .concatenate(this.createAnyString());

        // return model minus concatenation
        return model.minus(concat);
    }

    public AutomatonModel assertNotEmpty(AutomatonModel model) {

        // get empty string model
        AutomatonModel emptyString = this.createEmptyString();

        // return model minus empty string
        return model.minus(emptyString);
    }

    public abstract AutomatonModel createEmptyString();

    public AutomatonModel assertNotEquals(AutomatonModel model,
                                          AutomatonModel notEqualModel) {

        // subtract not equal model from model
        return model.minus(notEqualModel);
    }

    public AutomatonModel assertStartsWith(AutomatonModel model,
                                           AutomatonModel startingModel) {

        // get start model by concatenating starting model and any string
        AutomatonModel start =
                startingModel.concatenate(this.createAnyString());

        // return intersection of model and start
        return model.intersect(start);
    }

    public AutomatonModel substring(AutomatonModel model, int start, int end) {

        // check start and end indices
        if (start == end) {
            return this.createEmptyString();
        } else if (start == 0) {
            return this.prefix(model, end);
        }

        // get suffix
        AutomatonModel suffix = this.suffix(model, start);

        // return prefix from suffix as substring
        return this.prefix(suffix, end - start);
    }

    public abstract AutomatonModel prefix(AutomatonModel model, int end);

    public abstract AutomatonModel suffix(AutomatonModel model, int suffix);
}
