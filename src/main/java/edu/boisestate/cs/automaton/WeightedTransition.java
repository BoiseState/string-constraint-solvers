/*
 * dk.brics.automaton
 * 
 * Copyright (c) 2001-2011 Anders Moeller
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.boisestate.cs.automaton;

import java.io.Serializable;
import org.apache.commons.math3.fraction.Fraction;

/**
 * <tt>Automaton</tt> transition.
 * <p>
 * A transition, which belongs to a source state, consists of a Unicode
 * character interval and a destination state.
 *
 * @author Andrew Harris
 * @author Anders M&oslash;ller &lt;
 * <a href="mailto:amoeller@cs.au.dk">amoeller@cs.au.dk</a>&gt;
 */
public class WeightedTransition
        implements Serializable, Cloneable {

    /*
     * CLASS INVARIANT: min<=max
     */
    private char max;
    private char min;
    private WeightedState to;
    private int weightInt;
    
    private Fraction weight;

    /**
     * Returns destination of this transition.
     */
    public WeightedState getDest() {
        return to;
    }

    /**
     * Returns maximum of this transition interval.
     */
    public char getMax() {
        return max;
    }

    /**
     * Returns minimum of this transition interval.
     */
    public char getMin() {
        return min;
    }

    public int getWeightInt() {
        return weightInt;
    }
    
    public Fraction getWeight(){
    	return weight;
    }

    public void setWeightInt(int weight) {
        this.weightInt = weight;
    }
    
    public void setWeight(Fraction newWeight){
    	weight = newWeight;
    }

    public void setMin(char min) {
        this.min = min;
    }

    /**
     * Constructs a new singleton interval transition.
     *
     * @param c
     *         transition character
     * @param to
     *         destination state
     */
    public WeightedTransition(char c, WeightedState to) {
        min = max = c;
        this.to = to;
        this.weightInt = 1;
        weight = new Fraction(1,1);
    }

    /**
     * Constructs a new singleton interval transition.
     *
     * @param c
     *         transition character
     * @param to
     *         destination state
     * @param weight
     *         transition weight
     */
    public WeightedTransition(char c, WeightedState to, int weight) {
        min = max = c;
        this.to = to;
        this.weightInt = weight;
    }
    
    public  WeightedTransition(char c, WeightedState to, Fraction weight){
    	min = max = c;
    	this.to = to;
    	this.weight = weight;
    }

    /**
     * Constructs a new transition.
     * Both end points are included in the interval.
     *
     * @param min
     *         transition interval minimum
     * @param max
     *         transition interval maximum
     * @param to
     *         destination state
     */
    public WeightedTransition(char min, char max, WeightedState to) {
        if (max < min) {
            char t = max;
            max = min;
            min = t;
        }
        this.min = min;
        this.max = max;
        this.to = to;
        this.weightInt = 1;
        weight = new Fraction(1,1);
    }

    /**
     * Constructs a new transition.
     * Both end points are included in the interval.
     *
     * @param min
     *         transition interval minimum
     * @param max
     *         transition interval maximum
     * @param to
     *         destination state
     * @param weight
     *         transition weight
     */
    public WeightedTransition(char min,
                              char max,
                              WeightedState to,
                              int weight) {
        if (max < min) {
            char t = max;
            max = min;
            min = t;
        }
        this.min = min;
        this.max = max;
        this.to = to;
        this.weightInt = weight;
    }
    
    public WeightedTransition(char min,
    		char max,
    		WeightedState to,
    		Fraction weight) {
    	if (max < min) {
    		char t = max;
    		max = min;
    		min = t;
    	}
    	this.min = min;
    	this.max = max;
    	this.to = to;
    	this.weight = weight;
    }

    

    /**
     * Clones this transition.
     *
     * @return clone with same character interval and destination state
     */
    @Override
    public WeightedTransition clone() {
        try {
            return (WeightedTransition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks for equality.
     *
     * @param obj
     *         object to compare with
     *
     * @return true if <tt>obj</tt> is a transition with same character interval
     * and destination state as this transition.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WeightedTransition) {
            WeightedTransition t = (WeightedTransition) obj;
            return t.min == min &&
                   t.max == max &&
                   t.to == to &&
                   t.weightInt == weightInt &&
            	   t.weight.equals(weight);
        } else {
            return false;
        }
    }

    /**
     * Returns hash code. The hash code is based on the character interval (not
     * the destination state).
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return min * 2 + max * 3;
    }

    /**
     * Returns a string describing this state. Normally invoked via
     * {@link WeightedAutomaton#toString()}.
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("(");
        appendCharString(min, b);
        if (min != max) {
            b.append("-");
            appendCharString(max, b);
        }
        b.append(", ").append(weight).append(")");
        b.append(" -> ")
         .append(to.getNumber());
        return b.toString();
    }

    static void appendCharString(char c, StringBuilder b) {
        if (c >= 0x21 && c <= 0x7e && c != '\\' && c != '"') {
            b.append(c);
        } else {
            b.append("\\u");
            String s = Integer.toHexString(c);
            if (c < 0x10) {
                b.append("000").append(s);
            } else if (c < 0x100) {
                b.append("00").append(s);
            } else if (c < 0x1000) {
                b.append("0").append(s);
            } else {
                b.append(s);
            }
        }
    }

    void appendDot(StringBuilder b) {
        b.append(" -> ").append(to.getNumber()).append(" [label=\"");
        appendCharString(min, b);
        if (min != max) {
            b.append("-");
            appendCharString(max, b);
        }
        b.append(", ").append(weight);
        b.append("\"]\n");
    }

    void incMin() {
        min++;
    }
}
