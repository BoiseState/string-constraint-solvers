package edu.boisestate.cs.solvers;

import edu.boisestate.cs.automaton.AutomatonModel;
import edu.boisestate.cs.automaton.AutomatonModelFactory;
import edu.boisestate.cs.automaton.operations.*;

public class AggregateEJSASolver
        extends ExtendedSolver<AutomatonModel> {

    private final AutomatonModelFactory modelFactory;

    public AggregateEJSASolver(AutomatonModelFactory modelFactory) {
        super();

        // initialize factory from parameter
        this.modelFactory = modelFactory;
    }

    public AggregateEJSASolver(AutomatonModelFactory modelFactory,
                               int initialBound) {
        super(initialBound);

        // initialize factory from parameter
        this.modelFactory = modelFactory;
    }

    @Override
    public void append(int id, int base, int arg, int start, int end) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        // get substring model
        Substring substr = new Substring(start, end);
        AutomatonModel substrModel = substr.execute(argModel);

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
            AssertContains contains = new AssertContains(argModel);
            baseModel = contains.execute(baseModel);

            // get satisfying arg model
            AssertContainedIn containedIn = new AssertContainedIn(baseModel);
            argModel = containedIn.execute(argModel);

        } else { // false branch

            // cache base model as temp
            AutomatonModel tempModel = baseModel;

            // if arg model is singleton
            if (argModel.isSingleton()) {

                // prepend and concatenate any string models to arg model
                AutomatonModel x = this.modelFactory.createAnyString();
                x = x.clone()
                     .concatenate(argModel)
                     .concatenate(x);

                // get satisfying base model as temp
                tempModel = baseModel.minus(x);
            }

            // if base model is singleton
            if (baseModel.isSingleton()) {

                // get satisfying arg model
                AutomatonModel complement = baseModel.complement();
                argModel = argModel.intersect(complement);
            }

            // set base model from temp
            baseModel = tempModel;
        }

        // store result models
        this.symbolicStringMap.put(base, baseModel);
        this.symbolicStringMap.put(arg, argModel);
    }

    @Override
    public void delete(int id, int base, int start, int end) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        if (start < end) {

            // declare start model
            AutomatonModel startModel;

            // if start index is 0
            if (start == 0) {

                // set start as empty
                startModel = this.modelFactory.createEmpty();

            } else {

                // get model prefix before start index
                Prefix prefix = new Prefix(start);
                startModel = prefix.execute(baseModel);
            }

            // get model suffix from end index
            Suffix suffix = new Suffix(end);
            AutomatonModel endModel = suffix.execute(baseModel);

            // concatenate end model to start model
            baseModel = startModel.concatenate(endModel);
        }

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void deleteCharAt(int id, int base, int loc) {

        // delegate to delete method with start and end based on loc
        this.delete(id, base, loc, loc + 1);
    }

    @Override
    public void endsWith(boolean result, int base, int arg) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        if (result) {

            // get satisfying base model
            AssertEndsWith endsWith = new AssertEndsWith(argModel);
            baseModel = endsWith.execute(baseModel);

            // get satisfying arg model
            Postfix postfix = new Postfix();
            AutomatonModel tempModel = postfix.execute(baseModel);
            AssertContainedIn containedIn = new AssertContainedIn(tempModel);
            argModel = containedIn.execute(argModel);

        } else {

            // cache temp as base model
            AutomatonModel tempModel = baseModel;

            // if arg model is singleton
            if (argModel.isSingleton()) {

                // get satisfying arg model
                AutomatonModel x = this.modelFactory.createAnyString();
                x = x.concatenate(argModel);
                tempModel = baseModel.minus(x);
            }


            if (baseModel.isSingleton()) {

                // get satisfying arg model
                Postfix postfix = new Postfix();
                AutomatonModel x = postfix.execute(baseModel);
                AutomatonModel argComplement = argModel.complement();
                argModel = x.intersect(argComplement);
            }

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

        // store result models
        this.symbolicStringMap.put(base, baseModel);
        this.symbolicStringMap.put(arg, argModel);
    }

    @Override
    public void equalsIgnoreCase(boolean result, int base, int arg) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

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

        // store result model
        this.symbolicStringMap.put(id, baseModel);
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

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void isEmpty(boolean result, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

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
    public boolean isSingleton(int id) {

        // get model
        AutomatonModel model = this.symbolicStringMap.get(id);

        // return singleton status
        return model.isSingleton();
    }

    @Override
    public boolean isSingleton(int id, String actualValue) {

        // get model
        AutomatonModel model = this.symbolicStringMap.get(id);

        // return singleton status
        return model.containsString(actualValue) && this.isSingleton(id);
    }

    @Override
    public boolean isSound(int id, String actualValue) {

        // get model
        AutomatonModel model = this.symbolicStringMap.get(id);

        // get value model
        AutomatonModel value =
                this.modelFactory.createString(actualValue);

        // intersect models
        AutomatonModel intersection = model.intersect(value);

        // sound if intersection is not empty
        return !intersection.isEmpty();
    }

    @Override
    public void newConcreteString(int id, String string) {

        // create new automaton model from string
        AutomatonModel model = this.modelFactory.createString(string);

        // store new model
        this.symbolicStringMap.put(id, model);

        // store string value
        this.concreteStringMap.put(id, string);
    }

    @Override
    public void newSymbolicString(int id) {

        // create new symbolic string
        AutomatonModel model =
                this.modelFactory.createAnyString(this.initialBound);

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
    public void replace(int id, int base, int argOne, int argTwo) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel arg1Model = this.symbolicStringMap.get(argOne);
        AutomatonModel arg2Model = this.symbolicStringMap.get(argTwo);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public String replaceEscapes(String value) {

        // all unicode characters supported
        return value;
    }

    @Override
    public void reverse(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void setCharAt(int id, int base, int arg, int offset) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void setLength(int id, int base, int length) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

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

        // store result models
        this.symbolicStringMap.put(base, baseModel);
        this.symbolicStringMap.put(arg, argModel);
    }

    @Override
    public void substring(int id, int base, int start) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void substring(int id, int base, int start, int end) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void toLowerCase(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void toUpperCase(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void trim(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }
}
