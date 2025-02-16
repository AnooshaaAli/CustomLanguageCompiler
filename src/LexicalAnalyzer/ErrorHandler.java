package LexicalAnalyzer;

import java.util.ArrayList;
import java.util.List;

class ErrorHandler {
    private final List<String> errors = new ArrayList<>();

    public void addError(int line, String message) {
        errors.add("ERROR at line " + line + ": " + message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void printErrors() {
        if (errors.isEmpty()) {
            System.out.println("No syntax errors found.");
        } else {
            System.out.println("Syntax Errors:");
            for (String error : errors) {
                System.out.println(error);
            }
        }
    }
}
