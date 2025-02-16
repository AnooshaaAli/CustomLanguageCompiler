package LexicalAnalyzer;

public class Token {
    public enum Type {
        KEYWORD, IDENTIFIER, NUMBER, BOOLEAN, CHARACTER, OPERATOR, STRING, DATATYPE, DECIMAL, INTEGER, PUNCTUATOR, UNKNOWN, FUNCTION, COMMENT
    }

    private final Type type;
    private final String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Token{" + "type=" + type + ", value='" + value + '\'' + '}';
    }
}

