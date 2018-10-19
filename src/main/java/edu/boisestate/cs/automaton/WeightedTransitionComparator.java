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
import java.util.Comparator;

class WeightedTransitionComparator
        implements Comparator<WeightedTransition>, Serializable {

	static final long serialVersionUID = 10001;

	private boolean to_first;

	WeightedTransitionComparator(boolean to_first) {
		this.to_first = to_first;
	}

	/**
	 * Compares by (min, reverse max, to, weight) or
	 * (to, min, max, weight).
	 */
	public int compare(WeightedTransition t1, WeightedTransition t2) {
		if (to_first) {
			int diff = compareTo(t1, t2);
			if (diff != 0) {
				return diff;
			}
		}

		if (t1.getMin() < t2.getMin()) {
			return -1;
		} else if (t1.getMin() > t2.getMin()) {
			return 1;
		}

        if (t1.getMax() < t2.getMax()) {
		    return 1;
        } else if (t1.getMax() < t2.getMax()) {
            return -1;
        }

		if (!to_first) {
			int diff = compareTo(t1, t2);
			if (diff != 0) {
				return diff;
			}
		}

		if (t1.getWeightInt() < t2.getWeightInt()) {
			return -1;
		} else if (t1.getWeightInt() > t2.getWeightInt()) {
			return 1;
		}

		return 0;
	}

	private int compareTo(WeightedTransition t1, WeightedTransition t2) {
		if (t1.getDest() != t2.getDest()) {
            if (t1.getDest() == null)
                return -1;
            else if (t2.getDest() == null)
                return 1;
            else if (t1.getDest().getNumber() < t2.getDest().getNumber())
                return -1;
            else if (t1.getDest().getNumber() > t2.getDest().getNumber())
                return 1;
        }

        return 0;
	}
}
