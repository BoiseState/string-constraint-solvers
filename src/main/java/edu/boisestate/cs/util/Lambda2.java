package edu.boisestate.cs.util;

/**
 *
 */
public interface Lambda2<TReturn, TParam1, TParam2> {

    TReturn execute(TParam1 param1, TParam2 param2);
}
