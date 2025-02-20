package LexicalAnalyzer;
import automata.DFA;
import automata.NFA;
import automata.NFAtoDFAConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static automata.REtoNFA.*;

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

            // ----------------------- REGEX --------------------------- //
            String KEYWORDS = "\\(f\\.l\\.i\\.p\\)\\|\\(t\\.w\\.i\\.s\\.t\\)\\|\\(f\\.l\\.o\\.p\\)\\|\\(s\\.p\\.i\\.n\\)\\|\\(e\\.c\\.h\\.o\\)\\|\\(c\\.a\\.p\\.t\\.u\\.r\\.e\\)\\|\\(g\\.l\\.o\\.b\\.a\\.l\\)\\|\\(r\\.e\\.t\\.u\\.r\\.n\\)\\|\\(f\\.u\\.n\\.c\\)\\|\\(c\\.o\\.d\\.e\\)";
            String BOOLEAN = "\\(t\\.r\\.u\\.e\\)\\|\\(f\\.a\\.l\\.s\\.e\\)";
            String DATATYPES = "\\(r\\.i\\.z\\.z\\)\\|\\(a\\.l\\.p\\.h\\.a\\)\\|\\(b\\.e\\.t\\.a\\)\\|\\(g\\.a\\.m\\.m\\.a\\)";
            String IDENTIFIERS = "\\[a-z]\\+";
            String OPERATORS = "+\\|*\\|-\\|/\\|%\\|=\\|<=\\|>=\\|==\\|!=\\|<\\|>";
            String STRING = "\".\\(\\[A-Z]\\|\\[a-z]\\|[0-9]\\| \\|!\\|@\\|#\\|$\\|%\\|^\\|&\\|+\\|*\\|.\\||\\|-\\|_\\|=\\|{\\|}\\|(\\|)\\|[\\|]\\|:\\|;\\|\"\\|,\\|<\\|>\\|/\\|?\\)\\*\\.\"";
            String COMMENT = "<\\.<\\.<\\.\\(\\[A-Z]\\|\\[a-z]\\|\\[0-9]\\| \\|!\\|\"\\|#\\|$\\|%\\|&\\|'\\|,\\|+\\|*\\|.\\||\\|-\\|/\\|:\\|;\\|<\\|=\\|>\\|?\\|@\\|^\\|_\\|`\\|{\\|}\\|(\\|)\\|[\\|]\\|~\\|'\n'\\)\\*\\.>\\.>\\.>\\|<\\.<\\.\\(\\[A-Z]\\|\\[a-z]\\|\\[0-9]\\| \\|!\\|\"\\|#\\|$\\|%\\|&\\|'\\|,\\|+\\|*\\|.\\||\\|-\\|/\\|:\\|;\\|<\\|=\\|>\\|?\\|@\\|^\\|_\\|`\\|{\\|}\\|(\\|)\\|[\\|]\\|~\\)\\*\\.>\\.>";
            //String COMM = "<.<.<.([a-b]|[0-1]|{|}|~)*.>.>.>|<.<.([a-b]|[0-1]|{|}|~)*.>.>";
            String INTEGER = "\\[0-9]\\+";
            String DECIMAL = "\\[0-9]\\+\\..\\.[0-9]\\?\\.[0-9]\\?\\.[0-9]\\?\\.[0-9]\\?\\.[0-9]\\?";
            //String OPT = "\\[0-1]\\?";
            //String OPT2 = "\\[0-1]\\?";
            String CHARACTER = "'\\.\\(\\[A-Z]\\|[a-z]\\|[0-9]\\| \\|!\\|@\\|#\\|$\\|%\\|^\\|&\\|+\\|*\\|.\\||\\|-\\|_\\|=\\|{\\|}\\|(\\|)\\|[\\|]\\|:\\|;\\|\"\\|,\\|<\\|>\\|/\\|?\\)\\.'";
            String PUNCTUATOR = "{\\|}\\|,\\|;\\|[\\|]\\|(\\|)";

            List<NFA> nfaList = new ArrayList<>();
            nfaList.add(regexToNFA(KEYWORDS));
            nfaList.add(regexToNFA(BOOLEAN));
            nfaList.add(regexToNFA(DATATYPES));
            nfaList.add(regexToNFA(IDENTIFIERS));
            nfaList.add(regexToNFA(OPERATORS));
            nfaList.add(regexToNFA(STRING));
            nfaList.add(regexToNFA(COMMENT));
            nfaList.add(regexToNFA(INTEGER));
            nfaList.add(regexToNFA(DECIMAL));
            nfaList.add(regexToNFA(CHARACTER));
            nfaList.add(regexToNFA(PUNCTUATOR));

            NFA finalNFA = combineAllNFAs(nfaList);
            //printNFATransitionTable(finalNFA);

            System.out.println("\nTotal States of NFA: " + finalNFA.countStates());

            DFA dfa = NFAtoDFAConverter.convertNFAtoDFA(finalNFA);
            //System.out.println("\nResulting DFA:");
            //System.out.println(dfa);

            int dfaStateCount = dfa.states.size();
            System.out.println("Total states in DFA: " + dfaStateCount);

            int uniqueStateCount = 0;
            for (Token token : tokens) {
                uniqueStateCount += dfa.process(token.getValue());
            }

            System.out.println("Total number of unique states visisted: " + uniqueStateCount);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}