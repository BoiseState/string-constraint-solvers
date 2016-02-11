package edu.ucsb.cs.www.vlab.stranger;


import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;


/**
 * Utility functions to parse a PHP regex and output it into a corresponding GML
 * snippet.
 */
public class RegExpToAuto {
	
	public static boolean debug = true;
    public static void debug(String s){
    	if (debug)
    		System.out.println(s);
    }
	
	static int id = 0;

	static String indent(int indentLevel) {
		String s = "";
		for (int i = 0; i < indentLevel; i++)
			s += "\t";
		return s;
	}

	interface Re {
		StrangerAutomaton toAuto();

		StrangerAutomaton toAuto(int indentLevel);
	}

	static class AnyChar implements Re {
		public StrangerAutomaton toAuto(int indentLevel) {
			return StrangerAutomaton.makeDot(++id);
		}

		public StrangerAutomaton toAuto() {
			return toAuto(0);
		}
	}

	static class ReString implements Re {
		private java.lang.String s;

		public ReString(java.lang.String s) {
			this.s = s;
		}

		public ReString(Character ch) {
			this.s = "" + ch;
		}

		public void append(char ch) {
			s += ch;
		}

		public void append(java.lang.String str) {
			s += str;
		}

		public StrangerAutomaton toAuto(int indentLevel) {
			return StrangerAutomaton.makeString(this.s, ++id);
		}

		public StrangerAutomaton toAuto() {
			return toAuto(0);
		}
	}

	static class ReList implements Re {
		private LinkedList<Re> relist = new LinkedList<Re>();

		public ReList() {
		}

		public ReList(Re re) {
			relist.add(re);
		}

		public void add(int idx, Re re) {
			relist.add(idx, re);
		}

		public void add(Re re) {
			add(relist.size(), re);
		}

		public void add(List<Re> l) {
			relist.addAll(l);
		}

		public StrangerAutomaton toAuto(int indentLevel) {
			if (relist.size() == 1)
				return relist.get(0).toAuto(indentLevel);

			StrangerAutomaton auto = StrangerAutomaton.makeEmptyString(++id);
			for (Re re : relist)
				auto = auto.concatenate(re.toAuto(indentLevel + 1), ++id);
			return auto;
		}

		public StrangerAutomaton toAuto() {
			return toAuto(0);
		}

	}
	
	static class ReUnion implements Re {
		private LinkedList<Re> relist = new LinkedList<Re>();

		public ReUnion() {
		}

		public ReUnion(Re re) {
			relist.add(re);
		}

		public void add(int idx, Re re) {
			relist.add(idx, re);
		}

		public void add(Re re) {
			add(relist.size(), re);
		}

		public void add(List<Re> l) {
			relist.addAll(l);
		}

		public StrangerAutomaton toAuto(int indentLevel) {
			if (relist.size() == 1)
				return relist.get(0).toAuto(indentLevel);

			StrangerAutomaton auto = relist.get(0).toAuto(indentLevel);
			for (Re re : relist)
				auto = auto.union(re.toAuto(indentLevel + 1), ++id);
			return auto;
		}

		public StrangerAutomaton toAuto() {
			return toAuto(0);
		}

	}

	static class Closure implements Re {
		private Re re;

		public Closure(Re re) {
			this.re = re;
		}

		public StrangerAutomaton toAuto(int indentLevel) {
			return re.toAuto(indentLevel + 1).closure(++id);
		}

		public StrangerAutomaton toAuto() {
			return toAuto(0);
		}
	}

	static class Star implements Re {
		private Re re;

		public Star(Re re) {
			this.re = re;
		}

		public StrangerAutomaton toAuto(int indentLevel) {
			StrangerAutomaton retMe = re.toAuto(indentLevel + 1);
			retMe = retMe.kleensStar(++id);
			return retMe;
		}

		public StrangerAutomaton toAuto() {
			return toAuto(0);
		}
	}

	static class CharClass implements Re {
		private List<Character> clazz = new LinkedList<Character>();

		public CharClass() {
		}

		public CharClass(char[] chars) {
			for (char ch : chars)
				clazz.add(ch);
		}

		public void add(char ch) {
			clazz.add(ch);
		}

		public StrangerAutomaton toAuto(int indentLevel) {
			StrangerAutomaton auto = StrangerAutomaton.makeEmptyString(++id);
			for (Character ch : clazz) {
				auto = auto.union(StrangerAutomaton.makeString(ch.toString(), ++id), ++id);
			}
			return auto;
		}

		public StrangerAutomaton toAuto() {
			return toAuto(0);
		}
	}

	// a few meta-characters
	static char ccurly = '}';
	static char ocurly = '{';
	static char backslash = '\\';
	static char point = '.';
	static char minus = '-';
	static char dollar = '$';
	static char circum = '^';
	static char csqbra = ']';
	static char osqbra = '[';
	static char union = '|';
	static char cbra = ')';
	static char obra = '(';
	static char plus = '+';
	static char star = '*';
	static char slash = '/';
	static char single_quote = '\'';
	static char opointy = '<';
	static char question_mark = '?';

	private static boolean isMeta(char ch) {
		return ch == ccurly || ch == ocurly || ch == backslash || ch == point
				|| ch == minus || ch == dollar || ch == circum || ch == csqbra
				|| ch == osqbra || ch == union || ch == cbra || ch == obra
				|| ch == plus || ch == star || ch == slash
				|| ch == single_quote || ch == opointy;
	}

	/**
	 * Returns the character in unicode form, except if the character is a
	 * lowercase character.
	 */
	private static String encode(char c) {
		StringBuilder b = new StringBuilder();
		if (c >= '!' && c <= '~')
			b.append(c);
		else {
			b.append("\\x");
			b.append(Integer.toHexString(c));
		}
		return b.toString();
	}

	/** Reverses the transformation that is performed in encode(). */
	private static char decode(String s) {
		throw new RuntimeException();
		/*
		 * if (s.length() == 1) { return s.charAt(0); } if (!s.startsWith("u"))
		 * { throw new RuntimeException("SNH"); } if (!(s.length() == 5)) {
		 * throw new RuntimeException("SNH"); } String hexString =
		 * s.substring(1,5); String dec = new java.math.BigInteger(hexString,
		 * 16).toString(); return (char) Integer.valueOf(dec).intValue();
		 */
	}

	// ********************************************************************************

	
	/**
	 * converts a PHP regex into a StrangerAutomaton.
	 * 
	 * @throws UnsupportedRegexException
	 *             for unsupported regexes;
	 * @param preg
	 *            : true if this regex is perl-compatible, false if it is posix
	 *            (ereg)
	 * @param id: this is only used for debugging purposes 
	 */
	public static StrangerAutomaton convertPhpRegexToAutomaton(
			String phpRegexOrig, boolean preg, int id) {
		debug("============");
		debug("start_convertPhpRegexToAutomaton(" + phpRegexOrig + ")");
		if (phpRegexOrig == null){
			debug("regular expression is null so overapproximatin to makeAnyString");
			debug("end_convertPhpRegexToAutomaton(" + phpRegexOrig + ")");
			debug("============");
			return StrangerAutomaton.makeAnyString(id);
		}
		else if(phpRegexOrig.isEmpty()){
			debug("regular expression is empty so using makeEmptyString");
			debug("end_convertPhpRegexToAutomaton(" + phpRegexOrig + ")");
			debug("============");
			return StrangerAutomaton.makeEmptyString(id);
		}
		RegExpToAuto.id = id;
		char[] chTokens = phpRegexOrig.toCharArray();
		Character[] charTokens = new Character[chTokens.length];
		for (int i = 0; i < chTokens.length; i++)
			charTokens[i] = chTokens[i];
		StrangerAutomaton auto = convertPhpRegexToAutomaton(Arrays.asList(charTokens), preg);
		auto.setID(RegExpToAuto.id);
		debug("end_convertPhpRegexToAutomaton(" + phpRegexOrig + ")");
		debug("============");
		return auto;
	}

	/**
	 * converts a PHP regex into a StrangerAutomaton.
	 * 
	 * @throws UnsupportedRegexException
	 *             for unsupported regexes;
	 * @param preg
	 *            : true if this regex is perl-compatible, false if it is posix
	 *            (ereg)
	 */
	private static StrangerAutomaton convertPhpRegexToAutomaton(
			List<Character> phpRegexOrig, boolean preg) {

		// we don't like empty regexes
		if (phpRegexOrig.isEmpty()) {
			throw new RuntimeException("Empty regex");
		}

		// make a copy for the following work
		List<Character> phpRegex = new LinkedList<Character>(phpRegexOrig);

		if (preg) {
			// if the preg regex is not delimited...
			if (!phpRegex.get(0).equals(RegExpToAuto.slash)
					|| !phpRegex.get(phpRegex.size() - 1).equals(
							RegExpToAuto.slash)) {
				throw new UnsupportedRegexException(
						"Undelimited preg regexp: \"" + phpRegexOrig + "\"");
			}
			// peel off delimiter
			phpRegex = phpRegex.subList(1, phpRegex.size() - 1);
		}

		return parseSub(phpRegex.listIterator());
	}

	// ********************************************************************************

	/** Parses a subsequence: ... -> [...,...,...]. */
	private static StrangerAutomaton parseSub(ListIterator<Character> iter) {

		Stack<Re> stack = new Stack<Re>();
		String regString = "";

		String seq = "";
		
		stack.push(new ReUnion());
		while (iter.hasNext()) {

			// the current symbol
			Character sym = iter.next();

			// lookahead
			Character look = null;
			if (iter.hasNext()) {
				look = iter.next();
				iter.previous();
			}

			// this will be set to true if lookahead detects
			// a meta-character and handles it; in this case,
			// the current symbol must not be treated again below
			boolean done = false;

			if (look != null) {

				// if we are not at the end

				// making a look ahead to see if we have
				// to do something unusual for the current symbol

			}

			if (!done) {

				if (sym.equals(RegExpToAuto.star)) {
					// we don't distinguish between greedy and non-greedy
					// operators:
					// consume ?, if present
					if (look != null
							&& look.equals(RegExpToAuto.question_mark))
						iter.next();
					Re top = stack.pop();
					stack.push(new Star(top));
				} else if (sym.equals(RegExpToAuto.plus)) {
					// we don't distinguish between greedy and non-greedy
					// operators:
					// consume ?, if present
					if (look != null
							&& look.equals(RegExpToAuto.question_mark))
						iter.next();
					Re top = stack.pop();
					stack.push(new Closure(top));
				} else if (sym.equals(RegExpToAuto.obra)) {
					// start of subpattern
					stack.push(new ReUnion());
					System.out.println(stack.size());
				} else if (sym.equals(RegExpToAuto.cbra)) {
					// end of subpattern
					LinkedList<Re> tmp = new LinkedList<Re>();
					for (;;) {
						Re re = stack.pop();
						if (re instanceof ReUnion) {
							ReUnion reunion = (ReUnion) re;
							ReList relistCurrentScope = new ReList();
							relistCurrentScope.add(tmp);
							reunion.add(relistCurrentScope);
							ReList relistUpperScope = new ReList();
							// package the current scope reunion into an upper scope relist so that we do not
							// confuse the upper scope
							relistUpperScope.add(reunion); 
							stack.push(relistUpperScope);
							break;
						} else
							tmp.add(0, re);
					}
					// break;
				} else if (sym.equals(RegExpToAuto.osqbra)) {
					// start of character class
					stack.push(parseCharClass(iter));
				} else if (sym.equals(RegExpToAuto.union)) {
					// TODO: implement the union
					LinkedList<Re> tmp = new LinkedList<Re>();
					for (;;) {
						Re re = stack.pop();
						if (re instanceof ReUnion) {
							ReUnion reunion = (ReUnion) re;
							ReList relist = new ReList();
							relist.add(tmp);
							reunion.add(relist);
							stack.push(reunion);
							break;
						} else
							tmp.add(0, re);
					}
					// StringBuilder toEnd = parseSub(iter);
					// gmlRegex.insert(0, "{[");
					// gmlRegex.append("],");
					// gmlRegex.append(toEnd);
					// gmlRegex.append('}');
				} else if (sym.equals(RegExpToAuto.point)) {
					// any character
					stack.push(new AnyChar());
				} else if (sym.equals(RegExpToAuto.backslash)) {
					// an escape
					String escaped = "" + escape(iter);
					if (iter.hasNext()) {
						look = iter.next();
						iter.previous();
					}
					if ((look == null || (look != plus && look != star))
							&& !stack.isEmpty()
							&& stack.peek() instanceof ReString) {
						ReString top = (ReString) stack.pop();
						top.append(escaped);
						stack.push(top);
					} else {
						stack.push(new ReString(escaped));
					}
					// gmlRegex.append(seq);
					// gmlRegex.append(escaped);
					// seq = ",";
				} else if (sym.equals(RegExpToAuto.ocurly)) {
					// repetition;
					// automaton could become quite large;
					// here is how it would work:
					// - determine the number of repetitions
					// - determine the regex that is to be repeated
					// (easy: from start of the current subsequence to here)
					// - if the number of repetitions is a constant {x}:
					// - concat the regex this number of times
					// - else if the number of repetitions is {x,} (i.e.,
					// unbounded):
					// - repeat x times, and add a star-repetition
					// - else if the number of repetitions is a range a{2,4}
					// - [a,a,a^,a^] (where ^ is option, i.e, ? in usual regex
					// syntax)
					throw new UnsupportedRegexException("{");
				} else if (sym.equals(RegExpToAuto.circum)) {
					// "start of line"
					throw new UnsupportedRegexException("^");
				} else if (sym.equals(RegExpToAuto.dollar)) {
					// "end of line"
					throw new UnsupportedRegexException("$");
				} else {
					// not a meta-character
					if ((look == null || (look != plus && look != star))
							&& !stack.isEmpty()
							&& stack.peek() instanceof ReString) {
						ReString top = (ReString) stack.pop();
						top.append(sym);
						stack.push(top);
					} else {
						stack.push(new ReString(sym));
					}
				}
			}
		}

		ReUnion reunionGlobal;
		LinkedList<Re> tmp = new LinkedList<Re>();
		for (;;) {
			Re re = stack.pop();
			if (re instanceof ReUnion) {
				reunionGlobal = (ReUnion) re;
				ReList relist = new ReList();
				relist.add(tmp);
				reunionGlobal.add(relist);
				break;
			} else
				tmp.add(0, re);
		}

		return reunionGlobal.toAuto(1);

	}

	// ********************************************************************************

	/** Parses a character class: [...]. */
	private static CharClass parseCharClass(ListIterator<Character> iter) {

		CharClass clazz = new CharClass();
		HashSet<Character> charClass = new HashSet<Character>();
		Character first = iter.next();
		boolean negate = false;

		// check whether the first symbol in the character class is a ^;
		// in this case, we have a negated character class
		if (!first.equals(RegExpToAuto.circum)) {
			// push back
			iter.previous();
		} else {
			negate = true;
		}

		while (iter.hasNext()) {

			// the current symbol
			Character sym = iter.next();

			// lookahaed
			Character look = null;
			if (iter.hasNext()) {
				look = iter.next();
				iter.previous();
			}

			// this will be set to true if lookahead detects
			// a meta-character and handles it; in this case,
			// the current symbol must not be treated again below
			boolean done = false;

			if (look != null) {

				// if we are not at the end

				// making a look ahead to see if we have
				// to do something unusual for the current symbol

				if (look.equals(RegExpToAuto.minus)
						&& !sym.equals(RegExpToAuto.backslash)) {
					// character range lying ahead (but only if the minus was
					// not escaped)

					Character rangeStart = sym;
					iter.next();
					Character rangeEnd = iter.next();
					charClass.addAll(makeRange(rangeStart, rangeEnd));

					done = true;
				}
			}

			if (!done) {
				if (sym.equals(RegExpToAuto.csqbra)) {
					// end of character class
					break;

					// } else if (sym.equals(minus)) {
					// // character range
					// // already handled by lookahead

				} else if (sym.equals(RegExpToAuto.backslash)) {
					// an escape

					Character escaped = escape(iter);
					charClass.add(escaped);

				} else {
					// not a meta-character
					charClass.add(sym);
				}
			}
		}

		// check whether the first symbol in the character class is a ^;
		// in this case, we have a negated character class
		if (negate) {
			HashSet<Character> negCharClass = new HashSet<Character>();
			for (char ch = 0; ch < 128; ch++) {
				if (!charClass.contains(ch))
					negCharClass.add(ch);
			}
			charClass = negCharClass;
		}

		for (Character ch : charClass)
			clazz.add(ch);

		return clazz;

	}

	// ********************************************************************************

	/** Converts a character range: a-z => {a,b,c,...,z}. */
	private static HashSet<Character> makeRange(char start, char end) {

		HashSet<Character> range = new HashSet<Character>();

		if (start >= end) {
			throw new RuntimeException("faulty regex: " + start + "-" + end);
		}

		while (start <= end) {
			range.add(start);
			start++;
		}

		return range;
	}

	// ********************************************************************************

	// handles backslash escaping;
	// expects the iterator to be right after the backslash
	private static Character escape(ListIterator<Character> iter) {

		// safety check
		if (!iter.previous().equals(RegExpToAuto.backslash)) {
			throw new RuntimeException("SNH");
		}
		iter.next();

		Character retMe;
		Character escaped = iter.next();
		if (Character.isLetterOrDigit(escaped)) {
			// has a special meaning, not supported yet
			throw new UnsupportedRegexException();
		} else {
			// a simple escape of a metacharacter
			retMe = escaped;
		}

		return retMe;
	}

}
