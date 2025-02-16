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
        boolean expectSemicolon = false;

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
                    if (tempIndex < code.length() && code.charAt(tempIndex) == '=') {
                        expectSemicolon = true;
                    } else if (isTypeKeyword(token.getValue())) {
                        expectSemicolon = true;
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
            } else if (current == ';') {
                expectSemicolon = false;
                tokens.add(new Token(Token.Type.PUNCTUATOR, ";"));
                index++;
            } else {
                tokens.add(new Token(Token.Type.UNKNOWN, String.valueOf(current)));
                index++;
            }
            // Check if a semicolon is missing before a newline or block start
            if (expectSemicolon && (current == '\n')) {
                errorHandler.addError(lineNumber, "Missing semicolon before this line.");
                expectSemicolon = false;
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
        return new Token(Token.Type.IDENTIFIER, word);
    }

    private Token processNumber() {
        int start = index;
        while (index < code.length() && Character.isDigit(code.charAt(index))) {
            index++;
        }
        if (index < code.length() && code.charAt(index) == '.') {
            index++;
            while (index < code.length() && Character.isDigit(code.charAt(index))) {
                index++;
            }
            String value = code.substring(start, index);
            //symbolTable.addSymbol(value, "DECIMAL");
            return new Token(Token.Type.DECIMAL, code.substring(start, index));
        }
        String value = code.substring(start, index);
        //symbolTable.addSymbol(value, "INTEGER");
        return new Token(Token.Type.INTEGER, code.substring(start, index));
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
        index++;
        String value = code.substring(start, index);
        //symbolTable.addSymbol(value, "STRING");
        return new Token(Token.Type.STRING, code.substring(start, index));
    }

    private Token processSingleLineComment() {
        int start = index;
        while (index < code.length() && code.charAt(index) != '>') {
            index++;
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
        index += 3; // Skip '>>>'
        String value = code.substring(start, index);
        //symbolTable.addSymbol(value, "COMMENT");
        return new Token(Token.Type.COMMENT, code.substring(start, index));
    }
}
