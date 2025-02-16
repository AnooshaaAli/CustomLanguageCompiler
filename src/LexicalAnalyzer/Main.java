package LexicalAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filePath = "src/input/input.ohio";

        try {
            String inputCode = new String(Files.readAllBytes(Paths.get(filePath)));
            SymbolTable symbolTable = new SymbolTable();
            ErrorHandler errorHandler = new ErrorHandler();
            Lexer lexer = new Lexer(inputCode, symbolTable, errorHandler);
            List<Token> tokens = lexer.tokenize();

            System.out.println("\nTokenized Output:");
            for (Token token : tokens) {
                System.out.println(token);
            }

            System.out.println("Total number of tokens: " + tokens.size());
            System.out.println("\nSymbol table:");
            symbolTable.printSymbolTable();
            errorHandler.printErrors();

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}