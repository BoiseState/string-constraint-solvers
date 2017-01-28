package edu.boisestate.cs.solvers;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.AutomatonModel;
import edu.boisestate.cs.automatonModel.AutomatonModelManager;
import edu.boisestate.cs.util.Tuple;

public class AutomatonModelSolver
        extends ExtendedSolver<AutomatonModel> {

    protected final AutomatonModelManager modelManager;

    public AutomatonModelSolver(AutomatonModelManager modelManager) {
        super();

        // initialize factory from parameter
        this.modelManager = modelManager;
    }

    public AutomatonModelSolver(AutomatonModelManager modelManager,
                                int initialBound) {
        super(initialBound);

        // initialize factory from parameter
        this.modelManager = modelManager;
    }

    @Override
    public void append(int id, int base, int arg, int start, int end) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        // get substring model
        AutomatonModel substrModel = argModel.substring(start, end);

        // append substring model to base model
        baseModel = baseModel.concatenate(substrModel);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void append(int id, int base, int arg) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        // perform operation
        baseModel = baseModel.concatenate(argModel);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void contains(boolean result, int base, int arg) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        // true branch
        if (result) {

            // get satisfying base model
            baseModel = baseModel.assertContainsOther(argModel);

            // get satisfying arg model
            argModel = argModel.assertContainedInOther(baseModel);

        } else { // false branch

            // TODO: False branch with appropriate bound tracking operations

            // get satisfying base model as temp
           AutomatonModel tempModel = baseModel.assertNotContainsOther(argModel);

            // get satisfying arg model
            argModel = argModel.assertNotContainedInOther(baseModel);

            // set base model from temp
            baseModel = tempModel;
        }

        // store result models
        this.symbolicStringMap.put(base, baseModel);
        this.symbolicStringMap.put(arg, argModel);
    }

    @Override
    public void deleteCharAt(int id, int base, int loc) {

        // delegate to delete method with start and end based on loc
        this.delete(id, base, loc, loc + 1);
    }

    @Override
    public void delete(int id, int base, int start, int end) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform delete
        baseModel = baseModel.delete(start, end);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void endsWith(boolean result, int base, int arg) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        if (result) {

            // get satisfying base model
            baseModel = baseModel.assertEndsWith(argModel);

            // get satisfying arg model
            argModel = argModel.assertEndsOther(baseModel);
        } else {

            // TODO: False branch with appropriate bound tracking operations

            // get satisfying base model as temp
            AutomatonModel tempModel = baseModel.assertNotEndsWith(argModel);

            // get satisfying arg model
            argModel = argModel.assertNotEndsOther(baseModel);

            // set base model from temp
            baseModel = tempModel;
        }

        // store result models
        this.symbolicStringMap.put(base, baseModel);
        this.symbolicStringMap.put(arg, argModel);
    }

    @Override
    public void equals(boolean result, int base, int arg) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        // perform equals
        if (result) {

            // get satisfying base model
            baseModel = baseModel.assertEquals(argModel);

            // get satisfying arg model
            argModel = argModel.assertEquals(baseModel);
        } else {

            // TODO: False branch with appropriate bound tracking operations

            // get satisfying base model as temp
            AutomatonModel tempModel = baseModel.assertNotEquals(argModel);

            // get satisfying arg model
            argModel = argModel.assertNotEquals(baseModel);

            // set base model from temp
            baseModel = tempModel;
        }

        // store result models
        this.symbolicStringMap.put(base, baseModel);
        this.symbolicStringMap.put(arg, argModel);
    }

    @Override
    public void equalsIgnoreCase(boolean result, int base, int arg) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        // perform equals
        if (result) {

            // get satisfying base model
            baseModel = baseModel.assertEqualsIgnoreCase(argModel);

            // get satisfying arg model
            argModel = argModel.assertEqualsIgnoreCase(baseModel);
        } else {

            // TODO: False branch with appropriate bound tracking operations

            // get satisfying base model as temp
            AutomatonModel tempModel = baseModel.assertNotEqualsIgnoreCase(argModel);

            // get satisfying arg model
            argModel = argModel.assertNotEqualsIgnoreCase(baseModel);

            // set base model from temp
            baseModel = tempModel;
        }

        // store result models
        this.symbolicStringMap.put(base, baseModel);
        this.symbolicStringMap.put(arg, argModel);
    }

    @Override
    public String getSatisfiableResult(int id) {

        // get model
        AutomatonModel model = this.symbolicStringMap.get(id);
        return model.getAcceptedStringExample();
    }

    @Override
    public void insert(int id, int base, int arg, int offset) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        baseModel = performInsert(offset, baseModel, argModel);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    private AutomatonModel performInsert(int offset,
                                         AutomatonModel baseModel,
                                         AutomatonModel argModel) {

        if (offset >= 0) {

            // get prefix
            AutomatonModel startModel = baseModel.prefix(offset);

            // get suffix
            AutomatonModel endModel = baseModel.suffix(offset);

            // construct resulting automaton with concatenation
            baseModel = startModel.concatenateIndividual(argModel)
                                  .concatenateIndividual(endModel);

        } else {

            // construct resulting automaton with concatenation
            baseModel = argModel.concatenateIndividual(baseModel);

        }
        return baseModel;
    }

    @Override
    public void insert(int id,
                       int base,
                       int arg,
                       int offset,
                       int start,
                       int end) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        // get substring from arg model
        AutomatonModel substrModel = argModel.substring(start, end);

        baseModel = performInsert(offset, baseModel, substrModel);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void isEmpty(boolean result, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        if (result) {

            // get satisfying automaton
            baseModel = baseModel.assertEmpty();

        } else {

            // get satisfying automaton
            baseModel = baseModel.assertNotEmpty();

        }

        // store result models
        this.symbolicStringMap.put(base, baseModel);
    }

    @Override
    public boolean isSatisfiable(int id) {

        // get model
        AutomatonModel model = this.symbolicStringMap.get(id);

        // return true if not empty
        return !model.isEmpty();
    }

    @Override
    public boolean isSingleton(int id, String actualValue) {

        // get model
        AutomatonModel model = this.symbolicStringMap.get(id);

        // return singleton status
        return model.containsString(actualValue) && model.isSingleton();
    }

    @Override
    public boolean isSingleton(int id) {

        // get model
        AutomatonModel model = this.symbolicStringMap.get(id);

        // return singleton status
        return model.isSingleton();
    }

    @Override
    public boolean isSound(int id, String actualValue) {

        // get model
        AutomatonModel model = this.symbolicStringMap.get(id);

        // get value model
        AutomatonModel value =
                this.modelManager.createString(actualValue);

        // intersect models
        AutomatonModel intersection = model.intersect(value);

        // sound if intersection is not empty
        return !intersection.isEmpty();
    }

    @Override
    public void newConcreteString(int id, String string) {

        // create new automaton model from string
        AutomatonModel model = this.modelManager.createString(string);

        // store new model
        this.symbolicStringMap.put(id, model);

        // store string value
        this.concreteStringMap.put(id, string);
    }

    @Override
    public void newSymbolicString(int id) {

        // create new symbolic string
        AutomatonModel model =
                this.modelManager.createAnyString(this.initialBound);

        // store new model
        this.symbolicStringMap.put(id, model);
    }

    @Override
    public void propagateSymbolicString(int id, int base) {

        // get model
        AutomatonModel model = this.symbolicStringMap.get(base);

        // clone model
        AutomatonModel clone = model.clone();

        // store clone
        this.symbolicStringMap.put(id, clone);
    }

    @Override
    public void replaceCharFindKnown(int id, int base, char find) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform replace string operation
        baseModel = baseModel.replaceFindKnown(find);

        // store result model
        this.symbolicStringMap.put(id, baseModel);

    }

    @Override
    public void replaceCharKnown(int id, int base, char find, char replace) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform replace string operation
        baseModel = baseModel.replace(find, replace);

        // store result model
        this.symbolicStringMap.put(id, baseModel);

    }

    @Override
    public void replaceCharReplaceKnown(int id, int base, char replace) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform replace string operation
        baseModel = baseModel.replaceReplaceKnown(replace);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void replaceCharUnknown(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform replace string operation
        baseModel = baseModel.replaceChar();

        // store result model
        this.symbolicStringMap.put(id, baseModel);

    }

    @Override
    public String replaceEscapes(String value) {

        // all unicode characters supported
        return value;
    }

    @Override
    public void replaceStrings(int id, int base, int argOne, int argTwo) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        String arg1String = this.concreteStringMap.get(argOne);
        String arg2String = this.concreteStringMap.get(argTwo);

        // perform replace string operation
        baseModel = baseModel.replace(arg1String, arg2String);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void reverse(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform operation
        baseModel = baseModel.reverse();

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void setCharAt(int id, int base, int arg, int offset) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        if (offset >= 0) {

            // get prefix
            AutomatonModel startModel = baseModel.prefix(offset);

            // get suffix
            AutomatonModel endModel = baseModel.suffix(offset + 1);

            // get result from concatenation
            baseModel = startModel.concatenateIndividual(argModel)
                                  .concatenateIndividual(endModel);
        }

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void setLength(int id, int base, int length) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        if (length == 0) {

            // set result model as empty string model
            baseModel = this.modelManager.createEmptyString();

        } else {

            // concatenate any string model to base model
            AutomatonModel anyStr = this.modelManager.createAnyString();
            baseModel = baseModel.concatenateIndividual(anyStr);

            // assert length for concatenated model
            baseModel = baseModel.assertHasLength(0, length);

        }

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void shutDown() {
        // nothing needed
    }

    @Override
    public void startsWith(boolean result, int base, int arg) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        if (result) {

            // get satisfying base model
            baseModel = baseModel.assertStartsWith(argModel);

            // get satisfying arg model
            argModel = argModel.assertStartsOther(baseModel);

        } else {

            // get satisfying base model as temp
            AutomatonModel tempModel = baseModel.assertNotStartsWith(argModel);

            // get satisfying arg model
            argModel = argModel.assertNotStartsOther(baseModel);

            // set base model from temp
            baseModel = tempModel;
        }

        // store result models
        this.symbolicStringMap.put(base, baseModel);
        this.symbolicStringMap.put(arg, argModel);
    }

    @Override
    public void substring(int id, int base, int start) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform operation
        baseModel = baseModel.suffix(start);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void substring(int id, int base, int start, int end) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform operation
        baseModel = baseModel.substring(start, end);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void toLowerCase(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform operation
        baseModel = baseModel.toLowercase();

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void toUpperCase(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform operation
        baseModel = baseModel.toUppercase();

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void trim(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform operation
        baseModel = baseModel.trim();

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    private Tuple<Character, Boolean> getCharFromString(String string) {

        // initialize result variables
        boolean isKnown = true;
        char charValue;

        // attempt to parse char value from string
        try {

            // parse string to int
            int tempVal = Integer.parseInt(string);

            // if value falls between 0 and 9, not known char value
            if (tempVal >= 0 || tempVal < 10) {
                isKnown = false;
            }

            // set char value via cast
            charValue = (char) tempVal;

        } catch (NumberFormatException e) {

            // if string is not empty
            if (!string.isEmpty()) {

                // set value to first char in string
                charValue = string.charAt(0);

            } else {

                // set value to first value from alphabet
                Alphabet alphabet = this.modelManager.getAlphabet();
                charValue = alphabet.getSymbolSet().iterator().next();

            }
        }

        // return results
        return new Tuple<>(charValue, isKnown);
    }
}
