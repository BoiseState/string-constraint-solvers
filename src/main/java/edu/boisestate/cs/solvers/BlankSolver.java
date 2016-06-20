package edu.boisestate.cs.solvers;

/**
 * The most basic instantiation of an ExtendedSolver.
 *
 * @author Scott Kausler
 */
public class BlankSolver
        extends ExtendedSolver<String> {

    @Override
    public void append(int id, int base, int arg, int start, int end) {

    }

    @Override
    public void append(int id, int base, int arg) {

    }

    @Override
    public void contains(boolean result, int base, int arg) {

    }

    @Override
    public void delete(int id, int base, int start, int end) {

    }

    @Override
    public void deleteCharAt(int id, int base, int loc) {

    }

    @Override
    public void endsWith(boolean result, int base, int arg) {

    }

    @Override
    public void equals(boolean result, int base, int arg) {

    }

    @Override
    public void equalsIgnoreCase(boolean result, int base, int arg) {

    }

    @Override
    public String getSatisfiableResult(int id) {
        return null;
    }

    @Override
    public void insert(int id, int base, int arg, int offset) {

    }

    @Override
    public void insert(int id,
                       int base,
                       int arg,
                       int offset,
                       int start,
                       int end) {

    }

    @Override
    public void isEmpty(boolean result, int base) {

    }

    @Override
    public boolean isSatisfiable(int id) {
        return false;
    }

    @Override
    public boolean isSingleton(int id) {
        return false;
    }

    @Override
    public boolean isSingleton(int id, String actualValue) {
        return false;
    }

    @Override
    public boolean isSound(int id, String actualValue) {
        return false;
    }

    @Override
    public void newConcreteString(int id, String string) {

    }

    @Override
    public void newSymbolicString(int id) {

    }

    @Override
    public void propagateSymbolicString(int id, int base) {

    }

    @Override
    public void replaceCharFindKnown(int id, int base, char find) {

    }

    @Override
    public void replaceCharKnown(int id, int base, char find, char replace) {

    }

    @Override
    public void replaceCharReplaceKnown(int id, int base, char replace) {

    }

    @Override
    public void replaceCharUnknown(int id, int base) {

    }

    @Override
    public String replaceEscapes(String value) {
        return value;
    }

    @Override
    public void replaceStrings(int id, int base, int argOne, int argTwo) {

    }

    @Override
    public void reverse(int id, int base) {

    }

    @Override
    public void revertLastPredicate() {

    }

    @Override
    public void setCharAt(int id, int base, int arg, int offset) {

    }

    @Override
    public void setLength(int id, int base, int length) {

    }

    @Override
    public void shutDown() {

    }

    @Override
    public void startsWith(boolean result, int base, int arg) {

    }

    @Override
    public void substring(int id, int base, int start) {

    }

    @Override
    public void substring(int id, int base, int start, int end) {

    }

    @Override
    public void toLowerCase(int id, int base) {

    }

    @Override
    public void toUpperCase(int id, int base) {

    }

    @Override
    public void trim(int id, int base) {

    }

}
