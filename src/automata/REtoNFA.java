package automata;

import java.util.*;
import java.util.stream.Collectors;

public class REtoNFA {
    private static int stateCounter = 0;

    public static String expandRange(String regex, int index) {
        StringBuilder result = new StringBuilder();
        int i = index;

        while (i < regex.length()) {
            char c = regex.charAt(i);

            if (c == '[') {
                int closingBracket = regex.indexOf(']', i);
                if (closingBracket != -1) {
                    String rangeContent = regex.substring(i + 1, closingBracket);
                    StringBuilder expandedRange;
                    if (regex.charAt(i - 1) != '\\')
                        expandedRange = new StringBuilder("\\(");
                    else
                        expandedRange = new StringBuilder("(");

                    boolean hasRange = false;
                    int j = 0;
                    while (j < rangeContent.length()) {
                        if (j + 2 < rangeContent.length() && rangeContent.charAt(j + 1) == '-') {
                            char start = rangeContent.charAt(j);
                            char end = rangeContent.charAt(j + 2);
                            for (char ch = start; ch <= end; ch++) {
                                expandedRange.append(ch).append("\\|");
                            }
                            j += 3;
                            hasRange = true;
                        } else {
                            expandedRange.append(rangeContent.charAt(j)).append("\\|");
                            j++;
                        }
                    }

                    if (expandedRange.charAt(expandedRange.length() - 1) == '|') {
                        expandedRange.setLength(expandedRange.length() - 1);
                    }

                    expandedRange.append(")");

                    result.append(expandedRange);
                    i = closingBracket + 1;
                } else {
                    result.append(c);
                    i++;
                }
            } else {
                result.append(c);
                i++;
            }
        }

        return result.toString();
    }

    private static NFA createBasicNFA(char symbol) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);
        start.addTransition(symbol, end);
        return new NFA(start, end);
    }

    private static NFA concatenate(NFA first, NFA second) {
        first.endState.addTransition('\0', second.startState);
        return new NFA(first.startState, second.endState);
    }

    private static NFA union(NFA first, NFA second) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);

        start.addTransition('\0', first.startState);
        start.addTransition('\0', second.startState);

        first.endState.addTransition('\0', end);
        second.endState.addTransition('\0', end);

        return new NFA(start, end);
    }

    private static NFA kleeneStar(NFA nfa) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);

        start.addTransition('\0', nfa.startState);
        nfa.endState.addTransition('\0', end);
        nfa.endState.addTransition('\0', nfa.startState);
        start.addTransition('\0', end);

        return new NFA(start, end);
    }

    private static NFA plus(NFA nfa) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);

        // Start state directly transitions to the first occurrence of nfa
        start.addTransition('\0', nfa.startState);

        // The NFA loops back to itself to allow multiple occurrences
        nfa.endState.addTransition('\0', nfa.startState);

        // Final transition to the end state
        nfa.endState.addTransition('\0', end);

        return new NFA(start, end);
    }

    private static NFA optional(NFA nfa) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);

        start.addTransition('ε', end);

        start.addTransition('ε', nfa.startState);
        nfa.endState.addTransition('ε', end);

        return new NFA(start, end);
    }

    public static NFA regexToNFA(String regex) {
        Stack<NFA> tempStack = new Stack<>();
        Stack<NFA>  permStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();

        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);
            if (c == '\\') {
                i++;
                c = regex.charAt(i);
                if (c == '[') {
                    String beforeRange = regex.substring(0, i);
                    String afterRange = expandRange(regex, i);
                    regex = beforeRange + afterRange;
                    System.out.println("Expanded Regex: " + regex);
                    c = regex.charAt(i);
                }
                if (c == '(') {
                    if (!tempStack.empty()) {
                        permStack.push(tempStack.pop());
                    }
                    operatorStack.push(c);
                } else if (c == ')') {
                    permStack.push(tempStack.pop());
                    while (!(operatorStack.peek() == '(')) {
                        char operator = operatorStack.pop();
                        if (operator == '|') {
                            NFA second = permStack.pop();
                            NFA first = permStack.pop();
                            permStack.push(union(first, second));
                        } else if (operator == '*') {
                            NFA nfa = permStack.pop();
                            permStack.push(kleeneStar(nfa));
                        } else if (operator == '+') {
                            NFA nfa = permStack.pop();
                            permStack.push(plus(nfa));
                        } else if (operator == '?') {
                            NFA nfa = permStack.pop();
                            permStack.push(optional(nfa));
                        } else if (operator == '.') {
                            operatorStack.push(c);
                            NFA second = permStack.pop();
                            NFA first = permStack.pop();
                            permStack.push(concatenate(first, second));
                        }
                    }
                    operatorStack.pop();
                    tempStack.push(permStack.pop());
                } else if (c == '|') {
                    operatorStack.push(c);
                    NFA temp = tempStack.pop();
                    permStack.push(temp);
                } else if (c == '*') {
                    NFA temp = tempStack.pop();
                    tempStack.push(kleeneStar(temp));
                } else if (c == '+') {
                    NFA temp = tempStack.pop();
                    tempStack.push(plus(temp));
                } else if (c == '?') {
                    NFA nfa = tempStack.pop();
                    tempStack.push(optional(nfa));
                } else if (c == '.') {
                    operatorStack.push(c);
                    NFA temp = tempStack.pop();
                    permStack.push(temp);
                } else {
                    i--;
                    c = regex.charAt(i);
                    if (!tempStack.empty()) {
                        NFA first = tempStack.pop();
                        NFA second = (createBasicNFA(c));
                        tempStack.push(concatenate(first, second));
                    } else {
                        tempStack.push(createBasicNFA(c));
                    }
                }
        } else {
                if (!tempStack.empty()) {
                    NFA first = tempStack.pop();
                    NFA second = (createBasicNFA(c));
                    tempStack.push(concatenate(first, second));
                } else {
                    tempStack.push(createBasicNFA(c));
                }
            }
        }

        while (!tempStack.empty()) {
            permStack.push(tempStack.pop());
        }

        while (!operatorStack.isEmpty()) {
            char operator = operatorStack.pop();
            if (operator == '|') {
                NFA second = permStack.pop();
                NFA first = permStack.pop();
                permStack.push(union(first, second));
            } else if (operator == '*') {
                NFA nfa = permStack.pop();
                permStack.push(kleeneStar(nfa));
            } else if (operator == '+') {
                NFA nfa = permStack.pop();
                permStack.push(plus(nfa));
            } else if (operator == '?') {
                NFA nfa = permStack.pop();
                permStack.push(optional(nfa));
            } else if (operator == '.') {
                NFA second = permStack.pop();
                NFA first = permStack.pop();
                permStack.push(concatenate(first, second));
            }
        }

        return permStack.pop();
    }

    public static NFA combineAllNFAs(List<NFA> nfas) {
        if (nfas.isEmpty()) return null;

        NFA combinedNFA = nfas.get(0);
        for (int i = 1; i < nfas.size(); i++) {
            combinedNFA = union(combinedNFA, nfas.get(i));
        }

        return combinedNFA;
    }

    private static void printNFATransitionTable(NFA nfa) {
        Set<State> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();

        queue.add(nfa.startState);
        visited.add(nfa.startState);

        System.out.println("NFA Transition Table:");
        System.out.println("-------------------------------------------------");
        System.out.printf("| %-10s | %-10s | %-20s |\n", "State", "Symbol", "Next States");
        System.out.println("-------------------------------------------------");

        while (!queue.isEmpty()) {
            State current = queue.poll();

            for (Map.Entry<Character, List<State>> entry : current.transitions.entrySet()) {
                char symbol = entry.getKey();
                List<State> nextStates = entry.getValue();

                String nextStatesStr = nextStates.stream()
                        .map(state -> "q" + state.id)  // Formatting state names
                        .collect(Collectors.joining(", "));

                System.out.printf("| %-10s | %-10s | %-20s |\n", "q" + current.id, (symbol == '\0' ? "ε" : symbol), nextStatesStr);

                for (State next : nextStates) {
                    if (!visited.contains(next)) {
                        queue.add(next);
                        visited.add(next);
                    }
                }
            }
        }

        System.out.println("-------------------------------------------------");
        System.out.println("Start State: q" + nfa.startState.id);
        System.out.println("End State: q" + nfa.endState.id);
    }


    public static void main(String[] args) {

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
        printNFATransitionTable(finalNFA);

        System.out.println("Total States: " + finalNFA.countStates());
    }
}