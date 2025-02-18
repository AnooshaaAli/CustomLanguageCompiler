package LexicalAnalyzer;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private final List<String> errors = new ArrayList<>();

    public void reportError(String errorMessage) {
        errors.add(errorMessage);
        System.err.println("[Error] " + errorMessage);
    }

    public List<String> getErrors() {
        return errors;
    }
}
