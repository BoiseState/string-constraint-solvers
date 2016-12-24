package edu.boisestate.cs.automatonModel.operations;

public class StringModelCounterUtilities {

    // updates the string representation in the model counter as
    // a regular expression
    static String updateCurrentString(String initialString,
                                      char minChar,
                                      char maxChar,
                                      int transitionCount) {
        // maintain current string data
        String currentString;

        // if single char transition and
        // string not empty and
        // has the character already as end
        if (transitionCount == 1
                && initialString.length() > 0
                && initialString.endsWith(String.valueOf(minChar))) {

            // concatenate regex count symbol to end
            currentString = initialString + "{2}";

        }
        // if single char transition and
        // string not empty and
        // has multiple of the character already as end
        else if (transitionCount == 1
                && initialString.length() > 0
                // detect regular expression with regular expression
                && initialString.matches(String.format("%c\\{\\d+\\}$",
                                                       minChar))) {
            // get index of current char count in string
            int charCountIndexStart = initialString.lastIndexOf('{') + 1;
            int charCountIndexEnd = initialString.length() - 1;
            String charCountString = initialString
                    .substring(charCountIndexStart, charCountIndexEnd);

            // get value for character count
            int charCount = Integer.parseInt(charCountString);

            // increment char count value
            charCount++;

            // replace current char count with new char count
            currentString = initialString.substring(0, charCountIndexStart)
                    + charCount + "}";
        }
        // if single char transition and new character in sequence
        else if (transitionCount == 1) {
            // add new character to end of current string
            currentString = initialString + minChar;

        }
        // if range char transition and string not empty and
        // has the range already as end
        else if (initialString.length() > 0
                // detect regular expression with regular expression
                && initialString.matches(String.format("\\[%c-%c\\]$",
                                                       minChar,
                                                       maxChar))) {
            // concatenate regex count symbol to end
            currentString = initialString + "{2}";
        }
        // if range char transition and string not empty and
        // has multiple of the range already as end
        else if (initialString.length() > 0
                // detect regular expression with regular expression
                && initialString.matches(
                String.format("\\[%c-%c\\]\\{\\d+\\}$",
                              minChar,
                              maxChar))) {
            // get index of current char count in string
            int charCountIndexStart = initialString.lastIndexOf('{') + 1;
            int charCountIndexEnd = initialString.length() - 1;
            String charCountString = initialString
                    .substring(charCountIndexStart, charCountIndexEnd);

            // get value for character count
            int charCount = Integer.parseInt(charCountString);

            // increment char count value
            charCount++;

            // replace current char count with new char count
            currentString = initialString.substring(0, charCountIndexStart)
                    + charCount + "}";
        }
        // if range char transition and new range in sequence
        else {
            // add current character range to end of current string
            currentString = initialString + "[" + minChar + "-" + maxChar + "]";
        }

        return currentString;
    }

    public static void PrintValidString(String currentString) {
        System.out.format("Valid String: %s\n", currentString);
    }
}
