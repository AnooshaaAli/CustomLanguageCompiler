package LexicalAnalyzer;

import java.util.*;

public class Lexer {
    private static final Set<String> KEYWORDS = Set.of("flip", "twist", "flop", "spin", "echo", "capture", "global", "return", "func", "code");
    private static final Set<String> DATATYPES = Set.of("rizz", "alpha", "beta", "gamma");
    private static final Set<Character> OPERATORS = Set.of('+', '-', '*', '/', '%', '^', '=', '<', '>');
    private static final Set<Character> PUNCTUATORS = Set.of('{', '}', '(', ')', ',', '[', ']');

    private final String code;
    private int index = 0;
    private final List<Token> tokens = new ArrayList<>();
    private final SymbolTable symbolTable;
    private final ErrorHandler errorHandler;
    private String lastDataType = null;
    private int lineNumber = 1;

    public Lexer(String code, SymbolTable symbolTable, ErrorHandler errorHandler) {
        this.code = code;
        this.symbolTable = symbolTable;
        this.errorHandler = errorHandler;
    }

    public List<Token> tokenize() {

        while (index < code.length()) {
            char current = code.charAt(index);

            if (current == '\n') {
                lineNumber++;
                index++;
            } else if (Character.isWhitespace(current)) {
                index++;
            } else if (current == '<' && peekAhead("<<<")) {
                tokens.add(processMultiLineComment());
            } else if (current == '<' && peekAhead("<<")) {
                tokens.add(processSingleLineComment());
            } else if (Character.isLetter(current)) {
                Token token = processIdentifierOrKeyword();
                tokens.add(token);
                if (token.getType() == Token.Type.IDENTIFIER) {

                    int tempIndex = index;
                    while (tempIndex < code.length() && Character.isWhitespace(code.charAt(tempIndex))) {
                        tempIndex++;
                    }
                }
            } else if (current == '{') {
                symbolTable.enterScope("local");
                tokens.add(new Token(Token.Type.PUNCTUATOR, "{"));
                index++;
            } else if (current == '}') {
                symbolTable.exitScope();
                tokens.add(new Token(Token.Type.PUNCTUATOR, "}"));
                index++;
            } else if (Character.isDigit(current)) {
                tokens.add(processNumber());
            } else if (OPERATORS.contains(current)) {
                tokens.add(processOperator());
            } else if (PUNCTUATORS.contains(current)) {
                tokens.add(new Token(Token.Type.PUNCTUATOR, String.valueOf(code.charAt(index++))));
            } else if (current == '"') {
                tokens.add(processString());
            } else if (current == '\'') {
                tokens.add(processCharacter());
            } else if (current == ';') {
                tokens.add(new Token(Token.Type.PUNCTUATOR, ";"));
                index++;
            } else {
                errorHandler.reportError("Unexpected token '" + current + "' at line " + lineNumber);
                index++;
            }
        }
        return tokens;
    }

    private boolean isTypeKeyword(String word) {
        return word.equals("int") || word.equals("float") || word.equals("char") ||
                word.equals("string") || word.equals("bool") || word.equals("double") ||
                word.equals("rizz") || word.equals("alpha") || word.equals("beta");
    }

    private boolean peekAhead(String match) {
        return code.startsWith(match, index);
    }

    private Token processIdentifierOrKeyword() {
        int start = index;
        while (index < code.length() && Character.isLetterOrDigit(code.charAt(index))) {
            index++;
        }
        String word = code.substring(start, index);

        if (!word.equals(word.toLowerCase())) {
            errorHandler.reportError("Invalid identifier: '" + word + "' at line " + lineNumber + ". Identifiers must be lowercase.");
            return new Token(Token.Type.UNKNOWN, word);
        }

        if (KEYWORDS.contains(word)) {
            return new Token(Token.Type.KEYWORD, word);
        }

        if (DATATYPES.contains(word)) {
            lastDataType = word;
            return new Token(Token.Type.DATATYPE, word);
        }

        boolean isFunction = (index < code.length() && code.charAt(index) == '(');
        if (isFunction) {
            symbolTable.addSymbol(word, "function");
            return new Token(Token.Type.FUNCTION, word);
        }
        else if (lastDataType != null) {
            symbolTable.addSymbol(word, lastDataType);
            lastDataType = null;
        }

        if (Character.isDigit(word.charAt(0))) {
            errorHandler.reportError("Invalid identifier '" + word + "' at line " + lineNumber);
        }

        return new Token(Token.Type.IDENTIFIER, word);
    }

    private Token processNumber() {
        int start = index;
        boolean hasDecimalPoint = false;
        boolean hasExponent = false;
        int decimalCount = 0;

        while (index < code.length() && (Character.isDigit(code.charAt(index)) || code.charAt(index) == '.' || code.charAt(index) == 'e')) {
            char currentChar = code.charAt(index);

            if (currentChar == '.') {
                if (hasDecimalPoint) {
                    errorHandler.reportError("Malformed number at line " + lineNumber + ": multiple decimal points.");
                    return new Token(Token.Type.UNKNOWN, code.substring(start, index));
                }
                hasDecimalPoint = true;
            }
            else if (currentChar == 'e' ) {
                if (hasExponent) {
                    errorHandler.reportError("Malformed number at line " + lineNumber + ": multiple exponents.");
                    return new Token(Token.Type.UNKNOWN, code.substring(start, index));
                }
                hasExponent = true;

                // Check for sign after exponent
                if (index + 1 < code.length() && (code.charAt(index + 1) == '+' || code.charAt(index + 1) == '-')) {
                    index++;
                }
            } else if (hasDecimalPoint) {
                decimalCount++;
            }

            index++;
        }

        String number = code.substring(start, index);

        // Check if decimal places exceed 5
        if (hasDecimalPoint && decimalCount > 5) {
            number = code.substring(start, index-decimalCount+5);
            errorHandler.reportError("Decimal number exceeds 5 decimal places: " + number + " at line " + lineNumber);
            return new Token(Token.Type.DECIMAL, number); // Mark as unknown if invalid
        }

        else if (code.charAt(index) == 'E') {
            errorHandler.reportError("Malformed number " + number + " at line " + lineNumber);
            return new Token(Token.Type.UNKNOWN, number); // Mark as unknown if invalid
        }

        return new Token(hasDecimalPoint ? Token.Type.DECIMAL : Token.Type.INTEGER, number);
    }

    private Token processOperator() {
        char current = code.charAt(index++);
        if (index < code.length() && (current == '=' || code.charAt(index) == '=')) {
            //symbolTable.addSymbol(String.valueOf(current), "OPERATOR");
            return new Token(Token.Type.OPERATOR, current + String.valueOf(code.charAt(index++)));
        }
        //symbolTable.addSymbol(String.valueOf(current), "OPERATOR");
        return new Token(Token.Type.OPERATOR, String.valueOf(current));
    }

    private Token processString() {
        int start = index++;
        while (index < code.length() && code.charAt(index) != '"') {
            index++;
        }

        if (index >= code.length()) {
            errorHandler.reportError("Unterminated string literal at line " + lineNumber);
            return new Token(Token.Type.UNKNOWN, code.substring(start));
        }

        index++;
        String value = code.substring(start, index);
        //symbolTable.addSymbol(value, "STRING");
        return new Token(Token.Type.STRING, code.substring(start, index));
    }

    private Token processCharacter() {
        int start = index;

        if (index >= code.length() - 1 || code.charAt(index) != '\'') {
            errorHandler.reportError("Unterminated character literal at line " + lineNumber);
            return new Token(Token.Type.UNKNOWN, "'");
        }

        index++; // Move past opening quote

        char character;
        if (index < code.length() && code.charAt(index) == '\\') {
            // Handling escape sequences
            if (index + 1 < code.length()) {
                char next = code.charAt(index + 1);
                switch (next) {
                    case 'n': character = '\n'; break;
                    case 't': character = '\t'; break;
                    case 'r': character = '\r'; break;
                    case 'b': character = '\b'; break;
                    case 'f': character = '\f'; break;
                    case '\\': character = '\\'; break;
                    case '\'': character = '\''; break;
                    case '\"': character = '\"'; break;
                    default:
                        errorHandler.reportError("Invalid escape sequence at line " + lineNumber);
                        return new Token(Token.Type.UNKNOWN, code.substring(start, index + 2));
                }
                index += 2; // Move past escape sequence
            } else {
                errorHandler.reportError("Unfinished escape sequence at line " + lineNumber);
                return new Token(Token.Type.UNKNOWN, code.substring(start, index));
            }
        } else {
            // Normal character
            if (index < code.length()) {
                character = code.charAt(index);
                index++;
            } else {
                errorHandler.reportError("Unterminated character literal at line " + lineNumber);
                return new Token(Token.Type.UNKNOWN, "'");
            }
        }

        // NEW CHECK: Ensure there's no extra character before the closing quote
        if (index < code.length() && code.charAt(index) != '\'') {
            errorHandler.reportError("Invalid character literal (too many characters) at line " + lineNumber);
            // Skip ahead to find the next valid token
            while (index < code.length() && code.charAt(index) != '\'') {
                index++;
            }
            if (index < code.length()) index++; // Move past closing quote if found
            return new Token(Token.Type.UNKNOWN, code.substring(start, index));
        }

        index++; // Move past closing quote
        return new Token(Token.Type.CHARACTER, code.substring(start, index));
    }

    private Token processSingleLineComment() {
        int start = index;
        while (index < code.length() && code.charAt(index) != '>' && code.charAt(index) != '\n') {
            index++;
        }

        if (code.charAt(index) == '\n') {
            errorHandler.reportError("Unterminated single-line comment at line " + lineNumber);
            return new Token(Token.Type.UNKNOWN, code.substring(start));
        }

        index += 2; // Skip '>>'
        String value = code.substring(start, index);
        //symbolTable.addSymbol(value, "COMMENT");
        return new Token(Token.Type.COMMENT, code.substring(start, index));
    }

    private Token processMultiLineComment() {
        int start = index;
        while (index < code.length() && !peekAhead(">>>")) {
            index++;
        }

        if (index >= code.length()) {
            errorHandler.reportError("Unterminated multi-line comment at line " + lineNumber);
            return new Token(Token.Type.UNKNOWN, code.substring(start));
        }

        index += 3; // Skip '>>>'
        String value = code.substring(start, index);
        //symbolTable.addSymbol(value, "COMMENT");
        return new Token(Token.Type.COMMENT, code.substring(start, index));
    }
}
