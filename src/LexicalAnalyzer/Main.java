package LexicalAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filePath = "src/input/input.ohioohio";

        try {
            String inputCode = new String(Files.readAllBytes(Paths.get(filePath)));
            SymbolTable symbolTable = new SymbolTable();
            Lexer lexer = new Lexer(inputCode, symbolTable);
            List<Token> tokens = lexer.tokenize();

            System.out.println("\nTokenized Output:");
            for (Token token : tokens) {
                System.out.println(token);
            }

            System.out.println("Total number of tokens: " + tokens.size());
            symbolTable.printSymbolTable();

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}