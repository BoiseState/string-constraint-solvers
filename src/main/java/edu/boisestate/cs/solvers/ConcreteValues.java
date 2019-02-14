package edu.boisestate.cs.solvers;

import edu.boisestate.cs.Alphabet;

import java.util.*;

//Special class to hold the values
//of concrete strings together
//with the feasibility
public class ConcreteValues {

	private final Alphabet alphabet;
	private final int initialBoundLength;
	private final Map<String, Long> values;
	private long exceptionCount;

	public Set<String> getValues() {
		return values.keySet();
	}

	//always feasible in the root nodes
	//of the graph
	public ConcreteValues(Alphabet alphabet,
			int initialBoundLength,
			String value) {

		this.alphabet = alphabet;
		this.initialBoundLength = initialBoundLength;

		this.values = new TreeMap<>();
		this.values.put(value, (long) 1);
		exceptionCount = 0;
	}

	//creates an infeasible
	//concrete value
	public ConcreteValues(Alphabet alphabet, int initialBoundLength) {

		this.alphabet = alphabet;
		this.initialBoundLength = initialBoundLength;

		this.values = new TreeMap<>();
		exceptionCount = 0;
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues(Alphabet alphabet,
			int initialBoundLength,
			Collection<String> values) {

		this.alphabet = alphabet;
		this.initialBoundLength = initialBoundLength;

		this.values = new TreeMap<>();
		for (String s : values) {
			long count = updateCount(this.values, s, 1);
			this.values.put(s, count);
		}
		exceptionCount = 0;
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues(Alphabet alphabet,
			int initialBoundLength,
			Map<String, Long> values) {

		this.alphabet = alphabet;
		this.initialBoundLength = initialBoundLength;

		this.values = new TreeMap<>(values);
		exceptionCount = 0;
	}

	private static long updateCount(Map<String, Long> map,
			String key,
			long prevCount) {
		long count = prevCount;
		if (map.containsKey(key)) {
			count += map.get(key);
		}
		if (count == 0) {
			return prevCount;
		}
		return count;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ConcreteValues) {
			ConcreteValues other = (ConcreteValues) o;
			Map<String, Long> otherValues = other.values;

			if (this.values.size() != otherValues.size()) {
				return false;
			}

			for (String str : this.values.keySet()) {
				if (!otherValues.containsKey(str)) {
					return false;
				} else if (!values.get(str).equals(otherValues.get(str))) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder("( ");
		if(!values.isEmpty()){
			for (String str : this.values.keySet()) {
				output.append(str).append("{")
				.append(values.get(str))
				.append("} | ");
			}
			output.delete(output.length() - 2, output.length());
		}
		output.append(") ");

		return output.toString();
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertContainedInOther(ConcreteValues containing) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each substring in values
		for (String substr : this.values.keySet()) {
			long prevCount = values.get(substr);

			// for each possible string
			for (String string : containing.values.keySet()) {
				// if the string contains the substring
				if (string.contains(substr)) {
					long prevArgCount = containing.values.get(string);
					// add substring to result list
					long count = updateCountCorrect(results, substr, prevCount, prevArgCount);
					results.put(substr, count);

					// no need to keep iterating, break the loop
					break;
				}
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertContainsOther(ConcreteValues substring) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);

			// for each possible substring
			for (String substr : substring.values.keySet()) {
				// if the string contains the substring
				if (string.contains(substr)) {
					// add string to result list
					long prevArgCount = substring.values.get(substr);
					long count = updateCountCorrect(results, string, prevCount, prevArgCount);
					results.put(string, count);

					// no need to keep iterating, break the loop
					break;
				}
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertEndsOther(ConcreteValues containing) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each suffix in values
		for (String suffix : this.values.keySet()) {
			long prevCount = values.get(suffix);

			// for each possible string
			for (String string : containing.values.keySet()) {
				// if the string ends with the suffix
				if (string.endsWith(suffix)) {
					// add suffix to result list
					long prevArgCount = containing.values.get(string);
					long count = updateCountCorrect(results, suffix, prevCount, prevArgCount);
					results.put(suffix, count);

					// no need to keep iterating, break the loop
					break;
				}
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertEndsWith(ConcreteValues suffix) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);

			// for each possible suffix
			for (String suf : suffix.values.keySet()) {
				// if the string ends with the suffix
				if (string.endsWith(suf)) {
					// add string to result list
					long prevArgCount = suffix.values.get(string);
					long count = updateCountCorrect(results, string, prevCount,prevArgCount);
					results.put(string, count);

					// no need to keep iterating, break the loop
					break;
				}
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertEqual(ConcreteValues other) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);

			// for each possible other string
			for (String otherString : other.values.keySet()) {
				// if the string equals the other
				if (string.equals(otherString)) {
					long prevArgCount = other.values.get(string);
					// add string to result list
					long count = updateCountCorrect(results, string, prevCount, prevArgCount);
					results.put(string, count);

					// no need to keep iterating, break the loop
					break;
				}
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertEqualIgnoreCase(ConcreteValues other) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);

			// for each possible other string
			for (String otherString : other.values.keySet()) {
				// if the string equals the other ignoring case
				if (string.equalsIgnoreCase(otherString)) {
					long prevArgCount = other.values.get(string);
					// add string to result list
					long count = updateCountCorrect(results, string, prevCount, prevArgCount);
					results.put(string, count);

					// no need to keep iterating, break the loop
					break;
				}
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertIsEmpty() {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);

			// if string is empty
			if (string.isEmpty()) {
				// add string to results
				long count = updateCount(results, string, prevCount);
				results.put(string, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertNotContainedInOther(ConcreteValues containing) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each substring in values
		for (String substr : this.values.keySet()) {
			long prevCount = values.get(substr);
			// initialize flag
			boolean notContained = false;
			// for each possible string
			for (String string : containing.values.keySet()) {
				// if the string does contain the substring
				if (!string.contains(substr)) {
					// unset flag
					notContained = true;

					// no need to keep iterating, break the loop
					break;
				}
			}
			// if all containing values do not contain the substring
			if (notContained) {
				long count = updateCount(results, substr, prevCount);
				results.put(substr, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertNotContainsOther(ConcreteValues substring) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);
			// initialize flag
			boolean flag = false;
			// for each possible substring
			long prevArgCount = 0;
			for (String substr : substring.values.keySet()) {
				// if the string does contain the substring
				if (!string.contains(substr)) {
					// unset flag
					flag = true;
					prevArgCount = substring.values.get(substr);
					// no need to keep iterating, break the loop
					break;
				}
			}
			// if all containing values do not have the suffix
			if (flag) {
				
				long count = updateCountCorrect(results, string, prevCount, prevArgCount);
				results.put(string, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertNotEmpty() {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);
			// if string is empty
			if (!string.isEmpty()) {
				// add string to results
				long count = updateCount(results, string, prevCount);
				results.put(string, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertNotEndsOther(ConcreteValues containing) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each suffix in values
		for (String suffix : this.values.keySet()) {
			long prevCount = values.get(suffix);
			// initialize flag
			boolean flag = false;
			// for each possible string
			for (String string : containing.values.keySet()) {
				// if the string does end with the suffix
				if (!string.endsWith(suffix)) {
					// unset flag
					flag = true;

					// no need to keep iterating, break the loop
					break;
				}
			}
			// if all containing values do not have the suffix
			//eas: not sure what to do with that one - will do later
			//when will implement notEndsWith in the acyclic weighted
			if (flag) {
				long count = updateCount(results, suffix, prevCount);
				results.put(suffix, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertNotEndsWith(ConcreteValues suffix) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);
			// initialize flag
			boolean flag = false;
			// for each possible suffix
			for (String suf : suffix.values.keySet()) {
				// if the string does end with the suffix
				if (!string.endsWith(suf)) {
					// unset flag
					flag = true;

					// no need to keep iterating, break the loop
					break;
				}
			}
			// if all containing values do not have the suffix
			if (flag) {
				long count = updateCount(results, string, prevCount);
				results.put(string, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertNotEqual(ConcreteValues other) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);
			// initialize flag
			boolean flag = false;
			// for each possible other string
			long prevArgCount = 0;
			for (String otherString : other.values.keySet()) {
				// if the string does equal the other
				if (!string.equals(otherString)) {
					// unset flag
					flag = true;
					prevArgCount = other.values.get(otherString);
					// no need to keep iterating, break the loop
					break;
				}
			}
			// if all containing values do not have the suffix
			if (flag) {
				long count = updateCountCorrect(results, string, prevCount, prevArgCount);
				results.put(string, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertNotEqualIgnoreCase(ConcreteValues other) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);
			// initialize flag
			boolean flag = false;
			// for each possible other string
			for (String otherString : other.values.keySet()) {
				// if the string does equal the other ignoring case
				if (!string.equalsIgnoreCase(otherString)) {
					// unset flag
					flag = true;

					// no need to keep iterating, break the loop
					break;
				}
			}
			// if all containing values do not have the suffix
			if (flag) {
				//eas: will fix it later
				long count = updateCount(results, string, prevCount);
				results.put(string, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertNotStartsOther(ConcreteValues containing) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each prefix in values
		for (String prefix : this.values.keySet()) {
			long prevCount = values.get(prefix);
			// initialize flag
			boolean flag = false;
			// for each possible string
			for (String string : containing.values.keySet()) {
				// if the string does start with the prefix
				if (!string.startsWith(prefix)) {
					// unset flag
					flag = true;

					// no need to keep iterating, break the loop
					break;
				}
			}
			// if all containing values do not have the prefix
			if (flag) {
				//TODO: will fix it later
				long count = updateCount(results, prefix, prevCount);
				results.put(prefix, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertNotStartsWith(ConcreteValues prefix) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);
			// initialize flag
			boolean flag = false;
			// for each possible prefix
			for (String pre : prefix.values.keySet()) {
				// if the string does start with the prefix
				if (!string.startsWith(pre)) {
					// unset flag
					flag = true;

					// no need to keep iterating, break the loop
					break;
				}
			}
			// if all containing values do not have the suffix
			if (flag) {
				//TODO: fix it later
				long count = updateCount(results, string, prevCount);
				results.put(string, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertStartsOther(ConcreteValues containing) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each prefix in values
		for (String prefix : this.values.keySet()) {
			long prevCount = values.get(prefix);

			// for each possible string
			for (String string : containing.values.keySet()) {
				// if the string starts with the prefix
				if (string.startsWith(prefix)) {
					// add prefix to result list
					//TODO: fix it later
					long count = updateCount(results, prefix, prevCount);
					results.put(prefix, count);

					// no need to keep iterating, break the loop
					break;
				}
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues assertStartsWith(ConcreteValues prefix) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);

			// for each possible prefix
			for (String pre : prefix.values.keySet()) {
				// if the string starts with the prefix
				if (string.startsWith(pre)) {
					// add string to result list
					//TODO: fix it later
					long count = updateCount(results, string, prevCount);
					results.put(string, count);

					// no need to keep iterating, break the loop
					break;
				}
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues concat(ConcreteValues arg) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in both base and arg values
		for (String baseStr : this.values.keySet()) {
			long prevCount = values.get(baseStr);
			for (String argStr : arg.values.keySet()) {
				//eas: 2-13-19
				//but arg count could also be multiple
				long prevArgCount = arg.values.get(argStr);
				// add concatenation of strings to result list
				String concatenated = baseStr.concat(argStr);
				long count = updateCountCorrect(results, concatenated, prevCount, prevArgCount);
				results.put(concatenated, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	private static long updateCountCorrect(Map<String, Long> map,
			String key,
			long prevCount, long prevArgCount) {
		long count = prevCount * prevArgCount;
		if (map.containsKey(key)) {
			count += map.get(key);
		}
		if (count == 0) {
			return prevCount;
		}
		return count;
	}


	/**
	 * return@ the copy of itself
	 **/
	public ConcreteValues copy() {
		return new ConcreteValues(alphabet, initialBoundLength, this.values);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues delete(int start, int end) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			try {
				// add deleted string to result list
				StringBuilder strBuilder = new StringBuilder(str);
				strBuilder.delete(start, end);
				String deleted = strBuilder.toString();
				long count = updateCount(results, deleted, prevCount);
				results.put(deleted, count);
			} catch (Exception e) {
				this.exceptionCount += 1;
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues deleteCharAt(int loc) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			try {
				// add deleted string to result list
				StringBuilder strBuilder = new StringBuilder(str);
				strBuilder.deleteCharAt(loc);
				String deleted = strBuilder.toString();
				long count = updateCount(results, deleted, prevCount);
				results.put(deleted, count);
			} catch (Exception e) {
				this.exceptionCount += 1;
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues insert(int offset, ConcreteValues arg) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in both base and arg values
		for (String baseStr : this.values.keySet()) {
			long prevCount = values.get(baseStr);
			for (String argStr : arg.values.keySet()) {
				try {
					// add result of string insertion to result list
					StringBuilder strBuilder = new StringBuilder(baseStr);
					strBuilder.insert(offset, argStr);
					String inserted = strBuilder.toString();
					long prevArgCount = arg.values.get(argStr);
					long count = updateCountCorrect(results, inserted, prevCount, prevArgCount);
					results.put(inserted, count);
				} catch (Exception e) {
					this.exceptionCount += 1;
				}
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	public long modelCount() {
		long count = 0;
		for (String s : values.keySet()) {
			count += values.get(s);
		}
		return count;
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues replace(char find, char replace) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			// add replaced string to result list
			String replaced = str.replace(find, replace);
			long count = updateCount(results, replaced, prevCount);
			results.put(replaced, count);
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues replace(String find, String replace) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in both base and arg values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			// add replaced string to result list
			String replaced = str.replace(find, replace);
			//so the arguments are not automata
			long count = updateCount(results, replaced, prevCount);
			results.put(replaced, count);
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues replaceChar() {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			for (char find : this.alphabet.getSymbolSet()) {
				for (char replace : this.alphabet.getSymbolSet()) {
					// add replaced string to result list
					String replaced = str.replace(find, replace);
					long count = updateCount(results, replaced, prevCount);
					results.put(replaced, count);
				}
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues replaceFindKnown(char find) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			for (char replace : this.alphabet.getSymbolSet()) {
				// add replaced string to result list
				String replaced = str.replace(find, replace);
				long count = updateCount(results, replaced, prevCount);
				results.put(replaced, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues replaceReplaceKnown(char replace) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			for (char find : this.alphabet.getSymbolSet()) {
				// add replaced string to result list
				String replaced = str.replace(find, replace);
				long count = updateCount(results, replaced, prevCount);
				results.put(replaced, count);
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues reverse() {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			// add replaced string to result list
			StringBuilder strBuilder = new StringBuilder(str);
			strBuilder.reverse();
			String reversed = strBuilder.toString();
			long count = updateCount(results, reversed, prevCount);
			results.put(reversed, count);
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues setCharAt(int offset, ConcreteValues arg) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in both base and arg values
		for (String baseStr : this.values.keySet()) {
			long prevCount = values.get(baseStr);
			for (String argStr : arg.values.keySet()) {
				try {
					// add result of setting character to result list
					StringBuilder strBuilder = new StringBuilder(baseStr);
					strBuilder.setCharAt(offset, argStr.charAt(0));
					String charSet = strBuilder.toString();
					//TODO: might need some adjustements
					long count = updateCount(results, charSet, prevCount);
					results.put(charSet, count);
				} catch (Exception e) {
					this.exceptionCount += 1;
				}
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues setLength(int length) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in both base and arg values
		for (String string : this.values.keySet()) {
			long prevCount = values.get(string);
			try {
				// add result of setting character to result list
				StringBuilder strBuilder = new StringBuilder(string);
				strBuilder.setLength(length);
				String lengthSet = strBuilder.toString();
				long count = updateCount(results, lengthSet, prevCount);
				results.put(lengthSet, count);
			} catch (Exception e) {
				this.exceptionCount += 1;
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(this.alphabet, length, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues substring(int start, int end) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			try {
				// add substring to result list
				String substring = str.substring(start, end);
				long count = updateCount(results, substring, prevCount);
				results.put(substring, count);
			} catch (Exception e) {
				this.exceptionCount += 1;
			}
		}
		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues substring(int start) {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			try {
				// add substring to result list
				String substring = str.substring(start);
				long count = updateCount(results, substring, prevCount);
				results.put(substring, count);
			} catch (Exception e) {
				this.exceptionCount += 1;
			}
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues toLowerCase() {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			// add lowercase string to result list
			String lower = str.toLowerCase();
			long count = updateCount(results, lower, prevCount);
			results.put(lower, count);
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues toUpperCase() {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			// add uppercase string to result list
			String lower = str.toUpperCase();
			long count = updateCount(results, lower, prevCount);
			results.put(lower, count);
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}

	@SuppressWarnings("Duplicates")
	public ConcreteValues trim() {
		// initialize result map
		Map<String, Long> results = new HashMap<>();

		// for each string in values
		for (String str : this.values.keySet()) {
			long prevCount = values.get(str);
			// add trimmed string to result list
			String trimmed = str.trim();
			long count = updateCount(results, trimmed, prevCount);
			results.put(trimmed, count);
		}

		// return new concrete values from result list
		return new ConcreteValues(alphabet, initialBoundLength, results);
	}
}
