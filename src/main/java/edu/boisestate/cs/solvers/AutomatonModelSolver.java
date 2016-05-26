package edu.boisestate.cs.solvers;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.AutomatonModel;
import edu.boisestate.cs.automaton.AutomatonModelFactory;
import edu.boisestate.cs.automaton.operations.*;
import edu.boisestate.cs.util.Tuple;

public class AutomatonModelSolver
        extends ExtendedSolver<AutomatonModel> {

    private final AutomatonModelFactory modelFactory;

    public AutomatonModelSolver(AutomatonModelFactory modelFactory) {
        super();

        // initialize factory from parameter
        this.modelFactory = modelFactory;
    }

    public AutomatonModelSolver(AutomatonModelFactory modelFactory,
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
            AssertContainsOther contains =
                    new AssertContainsOther(argModel, this.modelFactory);
            baseModel = contains.execute(baseModel);

            // get satisfying arg model
            AssertContainedInOther
                    containedIn = new AssertContainedInOther(baseModel);
            argModel = containedIn.execute(argModel);

        } else { // false branch

            // TODO: False branch with appropriate bound tracking operations

            // cache base model as temp
            AutomatonModel tempModel = baseModel;

            // handle singleton arg model
            if (argModel.isSingleton()) {

                // prepend and concatenate any string models to arg model
                AutomatonModel x = this.modelFactory.createAnyString();
                x = x.clone()
                     .concatenate(argModel)
                     .concatenate(x);

                // get satisfying base model as temp
                tempModel = baseModel.minus(x);
            }

            // handle singleton base model
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
    public void deleteCharAt(int id, int base, int loc) {

        // delegate to delete method with start and end based on loc
        this.delete(id, base, loc, loc + 1);
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
                startModel = this.modelFactory.createEmptyString();

            } else {

                // get model prefix before start index
                PrefixKnown prefix = new PrefixKnown(start);
                startModel = prefix.execute(baseModel);
            }

            // get model suffix from end index
            SuffixKnown suffix = new SuffixKnown(end);
            AutomatonModel endModel = suffix.execute(baseModel);

            // concatenate end model to start model
            baseModel = startModel.concatenate(endModel);
        }

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
            AssertEndsWith endsWith =
                    new AssertEndsWith(argModel, this.modelFactory);
            baseModel = endsWith.execute(baseModel);

            // get satisfying arg model
            SuffixUnknown postfix = new SuffixUnknown();
            AutomatonModel tempModel = postfix.execute(baseModel);
            AssertContainedInOther
                    containedIn = new AssertContainedInOther(tempModel);
            argModel = containedIn.execute(argModel);

        } else {

            // TODO: False branch with appropriate bound tracking operations

            // cache temp as base model
            AutomatonModel tempModel = baseModel;

            // handle singleton arg model
            // if arg model is singleton
            if (argModel.isSingleton()) {

                // get satisfying arg model
                AutomatonModel x = this.modelFactory.createAnyString();
                x = x.concatenate(argModel);
                tempModel = baseModel.minus(x);
            }


            // handle singleton base model
            if (baseModel.isSingleton()) {

                // get satisfying arg model
                SuffixUnknown postfix = new SuffixUnknown();
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

        // perform equals
        baseModel = performBaseEquals(result, baseModel, argModel);
        argModel = performArgEquals(result, baseModel, argModel);

        // store result models
        this.symbolicStringMap.put(base, baseModel);
        this.symbolicStringMap.put(arg, argModel);
    }

    private static AutomatonModel performArgEquals(boolean result,
                                                   AutomatonModel baseModel,
                                                   AutomatonModel argModel) {

        if (result) {

            // get satisfying arg model
            AssertEquals eq = new AssertEquals(baseModel);
            argModel = eq.execute(argModel);

        } else {

            // get satisfying base model
            AssertNotEquals neqBase = new AssertNotEquals(argModel);
            AutomatonModel temp = neqBase.execute(baseModel);

            // get satisfying arg model
            AssertNotEquals neqArg = new AssertNotEquals(baseModel);
            argModel = neqArg.execute(argModel);

            // handle singleton base model
            if (baseModel.isSingleton()) {

                // get satisfying arg automaton
                AutomatonModel baseComplement = baseModel.complement();
                argModel = argModel.intersect(baseComplement);
            }
        }

        // return arg model
        return argModel;
    }

    private static AutomatonModel performBaseEquals(boolean result,
                                                    AutomatonModel baseModel,
                                                    AutomatonModel argModel) {

        if (result) {

            // get satisfying base model
            AssertEquals eq = new AssertEquals(argModel);
            baseModel = eq.execute(baseModel);

        } else {

            // get satisfying base model
            AssertNotEquals neqBase = new AssertNotEquals(argModel);
            baseModel = neqBase.execute(baseModel);

        }

        // return base model
        return baseModel;
    }

    @Override
    public void equalsIgnoreCase(boolean result, int base, int arg) {

        // get models
        AutomatonModel baseModel = this.symbolicStringMap.get(base);
        AutomatonModel argModel = this.symbolicStringMap.get(arg);

        // get ignore case equivalent automata
        IgnoreCase ignoreCase = new IgnoreCase();
        AutomatonModel baseIgnoreCase = ignoreCase.execute(baseModel);
        AutomatonModel argIgnoreCase = ignoreCase.execute(argModel);

        // perform equals with ignore case models
        baseModel = performBaseEquals(result, baseIgnoreCase, argIgnoreCase);
        argModel = performArgEquals(result, baseIgnoreCase, argIgnoreCase);

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

    private static AutomatonModel performInsert(int offset,
                                                AutomatonModel baseModel,
                                                AutomatonModel argModel) {

        if (offset >= 0) {

            // get prefix
            PrefixKnown prefix = new PrefixKnown(offset);
            AutomatonModel startModel = prefix.execute(baseModel);

            // get suffix
            SuffixKnown suffix = new SuffixKnown(offset);
            AutomatonModel endModel = suffix.execute(baseModel);

            // construct resulting automaton with concatenation
            baseModel = startModel.concatenate(argModel).concatenate(endModel);

        } else {

            // construct resulting automaton with concatenation
            baseModel = argModel.concatenate(baseModel);

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
        Substring substring = new Substring(start, end);
        AutomatonModel substrModel = substring.execute(argModel);

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
            AssertEmpty empty = new AssertEmpty(this.modelFactory);
            baseModel = empty.execute(baseModel);

        } else {

            // get satisfying automaton
            AssertNotEmpty notEmpty = new AssertNotEmpty();
            baseModel = notEmpty.execute(baseModel);

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
        return model.containsString(actualValue) && this.isSingleton(id);
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
        String arg1String = this.concreteStringMap.get(argOne);
        String arg2String = this.concreteStringMap.get(argTwo);

        // check if replace char operation
        if (arg1String != null &&
            arg1String.length() == 1 &&
            arg2String != null &&
            arg2String.length() == 1) {

            // get find and replace chars
            Tuple<Character, Boolean> findResult =
                    getCharFromString(arg1String);
            Tuple<Character, Boolean> replaceResult =
                    getCharFromString(arg2String);

            // get char values and known flags
            char find = findResult.getFirst();
            char replace = replaceResult.getFirst();
            boolean findKnown = findResult.getSecond();
            boolean replaceKnown = replaceResult.getSecond();

            // determine correct replace operation
            Operation replaceChar;
            if (findKnown && replaceKnown) {
                replaceChar = new ReplaceCharacterBothKnown(find, replace);
            } else if (findKnown) {
                replaceChar = new ReplaceCharacterFindKnown(find);
            } else if (replaceKnown) {
                replaceChar = new ReplaceCharacterReplaceKnown(replace);
            } else {
                replaceChar = new ReplaceCharacterBothUnknown();
            }

            // perform replace character operation
            baseModel = replaceChar.execute(baseModel);

        }
        // check if replace string operation
        else if (arg1String != null && arg2String != null) {

            // perform replace string operation
            ReplaceStringBothKnown replaceStr =
                    new ReplaceStringBothKnown(arg1String, arg2String);
            baseModel = replaceStr.execute(baseModel);
        }
        // replace string operation with symbolic string arguments
        else {
            System.err.println(
                    "AutomatonModelSolver.replace: at least one concrete " +
                    "string is null, this solver cannot currently perform " +
                    "replace with symbolic string parameters.");
        }

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
                Alphabet alphabet = this.modelFactory.getAlphabet();
                charValue = alphabet.getSymbolSet().iterator().next();

            }
        }

        // return results
        return new Tuple<>(charValue, isKnown);
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

        // perform operation
        Reverse reverse = new Reverse();
        baseModel = reverse.execute(baseModel);

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
            PrefixKnown prefix = new PrefixKnown(offset);
            AutomatonModel startModel = prefix.execute(baseModel);

            // get suffix
            SuffixKnown suffix = new SuffixKnown(offset + 1);
            AutomatonModel endModel = suffix.execute(baseModel);

            // get result from concatenation
            baseModel = startModel.concatenate(argModel).concatenate(endModel);
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
            baseModel = this.modelFactory.createEmptyString();

        } else {

            // concatenate any string model to base model
            AutomatonModel anyStr = this.modelFactory.createAnyString();
            baseModel = baseModel.concatenate(anyStr);

            // assert length for concatenated model
            AssertHasLength hasLength = new AssertHasLength(0, length);
            baseModel = hasLength.execute(baseModel);

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
            AssertStartsWith startsWith = new AssertStartsWith(argModel);
            baseModel = startsWith.execute(baseModel);

            // get satisfying arg model
            PrefixUnknown prefix = new PrefixUnknown();
            AutomatonModel x = prefix.execute(baseModel);
            AssertContainedInOther containedInOther =
                    new AssertContainedInOther(x);
            argModel = containedInOther.execute(argModel);

        } else {

            // initialize temp as base
            AutomatonModel temp = baseModel;

            // if arg is singleton
            if (argModel.isSingleton()) {

                // get satisfying base model
                AutomatonModel x = this.modelFactory.createAnyString();
                x = argModel.concatenate(x);
                temp = baseModel.minus(x);
            }

            // if base is singleton
            if (baseModel.isSingleton()) {

                // get satisfying arg model
                AutomatonModel baseComplement = baseModel.complement();
                argModel = argModel.intersect(baseComplement);
            }

            // set base from temp
            baseModel = temp;

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
        SuffixKnown suffix = new SuffixKnown(start);
        baseModel = suffix.execute(baseModel);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void substring(int id, int base, int start, int end) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform operation
        Substring substr = new Substring(start, end);
        baseModel = substr.execute(baseModel);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void toLowerCase(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform operation
        ToLowercase toLowercase = new ToLowercase();
        baseModel = toLowercase.execute(baseModel);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void toUpperCase(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // perform operation
        ToUppercase toUppercase = new ToUppercase();
        baseModel = toUppercase.execute(baseModel);

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }

    @Override
    public void trim(int id, int base) {

        // get model
        AutomatonModel baseModel = this.symbolicStringMap.get(base);

        // check model with length of one?
        AssertHasLength hasLength = new AssertHasLength(1, 1);
        AutomatonModel hasLengthModel = hasLength.execute(baseModel);
        AutomatonModel temp = baseModel.intersect(hasLengthModel);

        if (temp.equals(baseModel)) {

            // union empty string with base model?
            AutomatonModel emptyString = this.modelFactory.createEmptyString();
            baseModel = temp.union(emptyString);

        } else {

            // perform operation
            Trim trim = new Trim();
            baseModel = trim.execute(baseModel);

        }

        // store result model
        this.symbolicStringMap.put(id, baseModel);
    }
}
