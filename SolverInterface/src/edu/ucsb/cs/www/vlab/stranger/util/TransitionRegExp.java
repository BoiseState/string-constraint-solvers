package edu.ucsb.cs.www.vlab.stranger.util;

 class TransitionRegExp {

    RegExp regExp;
    State to;
    
    public TransitionRegExp(Transition trans) {
        this.to = trans.to;
        if (trans.min == Character.MIN_VALUE && trans.max == Character.MAX_VALUE) {
            this.regExp = RegExp.makeAnyString();
        } else {
            this.regExp = RegExp.makeCharRange(trans.min, trans.max);
        }
    }
    
    public TransitionRegExp(RegExp regExp, State to) {
        this.regExp = regExp;
        this.to = to;
    }
}
