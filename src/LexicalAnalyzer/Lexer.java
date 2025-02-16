package LexicalAnalyzer;

import java.util.*;

public class Lexer {
    private static final Set<String> KEYWORDS = Set.of("flip", "twist", "flop", "spin", "echo", "capture", "global", "return", "func", "code");
    private static final Set<String> DATATYPES = Set.of("rizz", "alpha", "beta", "gamma");
    private static final Set<Character> OPERATORS = Set.of('+', '-', '*', '/', '%', '^', '=', '<', '>');
    private static final Set<Character> PUNCTUATORS = Set.of('{', '}', '(', ')', ',', ';', '[', ']');

    private final String code;
    private int index = 0;
    private final List<Token> tokens = new ArrayList<>();
    private final SymbolTable symbolTable;
    private String lastDataType = null;

    public Lexer(String code, SymbolTable symbolTable) {
        this.code = code;
        this.symbolTable = symbolTable;
    }

    public List<Token> tokenize() {
        while (index < code.length()) {
            char current = code.charAt(index);

            if (Character.isWhitespace(current)) {
                index++;
            } else if (current == '<' && peekAhead("<<<")) {
                tokens.add(processMultiLineComment());
            } else if (current == '<' && peekAhead("<<")) {
                tokens.add(processSingleLineComment());
            } else if (Character.isLetter(current)) {
                tokens.add(processIdentifierOrKeyword());
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
            } else {
                tokens.add(new Token(Token.Type.UNKNOWN, String.valueOf(current)));
                index++;
            }
        }
        return tokens;
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
