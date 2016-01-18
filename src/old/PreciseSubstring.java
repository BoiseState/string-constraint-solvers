/**
 * An extended EJSA operation for a more precise substring operation.
 */
package old;

import dk.brics.automaton.Automaton;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;

public class PreciseSubstring extends UnaryOperation{
	private int start;
	private int end;

	public PreciseSubstring(int start, int end){
		this.start=start;
		this.end=end;
	}

	@Override
	public CharSet charsetTransfer(CharSet arg0) {
		return arg0;
	}

	@Override
	public Automaton op(Automaton a) {
		if(start==end)
			return Automaton.makeEmptyString();
		else if(start==0){
			PreciseSuffix s=new PreciseSuffix(end);
			return s.op(a);
		}
		Automaton temp;
		PrecisePrefix p=new PrecisePrefix(start);
		temp=p.op(a);
		PreciseSuffix s=new PreciseSuffix(end-start);
		return s.op(temp);
	}

	@Override
	public int getPriority() {
		return 4;
	}

	@Override
	public String toString() {
		return "precise substring";
	}
    @Override
    public boolean equals(Object obj) {
        return obj instanceof PreciseSubstring;
    }

}
