package automata;

import java.util.*;
import java.util.stream.Collectors;

public class REtoNFA {
    private static int stateCounter = 0;

    public static String dissolveRange(String regex, int index) {
        StringBuilder result = new StringBuilder();
        int i = index;

        while (i < regex.length()) {
            char c = regex.charAt(i);

            // Check if we have a character class starting
            if (c == '[') {
                int closingBracket = regex.indexOf(']', i);
                if (closingBracket != -1) {
                    String rangeContent = regex.substring(i + 1, closingBracket); // Extract content inside []
                    StringBuilder expandedRange = new StringBuilder("("); // Use parentheses for alternation

                    int j = 0;
                    while (j < rangeContent.length()) {
                        if (j + 2 < rangeContent.length() && rangeContent.charAt(j + 1) == '-') {
                            // Expand range like a-z
                            char start = rangeContent.charAt(j);
                            char end = rangeContent.charAt(j + 2);
                            for (char ch = start; ch <= end; ch++) {
                                expandedRange.append(ch).append("|");
                            }
                            j += 3; // Move past "a-z"
                        } else {
                            // Just append normal characters like '0' in [a-z0-9]
                            expandedRange.append(rangeContent.charAt(j)).append("|");
                            j++;
                        }
                    }

                    // Remove last '|' and close parentheses
                    if (expandedRange.length() > 1) {
                        expandedRange.setLength(expandedRange.length() - 1);
                    }
                    expandedRange.append(")");

                    result.append(expandedRange);
                    i = closingBracket + 1; // Move past ']'
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
        first.endState.addTransition('ε', second.startState);
        return new NFA(first.startState, second.endState);
    }

    private static NFA union(NFA first, NFA second) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);

        start.addTransition('ε', first.startState);
        start.addTransition('ε', second.startState);

        first.endState.addTransition('ε', end);
        second.endState.addTransition('ε', end);

        return new NFA(start, end);
    }

    private static NFA kleeneStar(NFA nfa) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);

        start.addTransition('ε', nfa.startState);
        nfa.endState.addTransition('ε', end);
        nfa.endState.addTransition('ε', nfa.startState);
        start.addTransition('ε', end);

        return new NFA(start, end);
    }

    private static NFA plus(NFA nfa) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);

        // Start state directly transitions to the first occurrence of nfa
        start.addTransition('ε', nfa.startState);

        // The NFA loops back to itself to allow multiple occurrences
        nfa.endState.addTransition('ε', nfa.startState);

        // Final transition to the end state
        nfa.endState.addTransition('ε', end);

        return new NFA(start, end);
    }
/*
    public static NFA regexToNFA(String regex) {
        Stack<NFA> stack = new Stack<>();
        Stack<NFA> stack2 = new Stack<>();

        for (char c : regex.toCharArray()) {
            if (c == '|') {
                NFA temp = stack.pop();
                stack2.push(temp);
            } else {
                if(!stack.empty()){
                    NFA first = stack.pop();
                    NFA second = (createBasicNFA(c));
                    stack.push(concatenate(first, second));
                }
                else{
                    stack.push(createBasicNFA(c));
                }
            }
        }

        if(!stack.empty()){
            NFA temp = stack.pop();
            stack2.push(temp);
        }

        while(stack2.size() > 1){
            NFA second = stack2.pop();
            NFA first = stack2.pop();
            NFA union = union(first, second);
            stack2.push(union);
        }
        return stack2.pop();
    }
*/

    public static NFA regexToNFA(String regex) {
        Stack<NFA> tempStack = new Stack<>();
        Stack<NFA>  permStack = new Stack<>();
        Stack<NFA>  parenStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();

        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);
            if (c == '[') {
                regex = dissolveRange(regex, i);
                System.out.println("Dissolved Regex: " + regex);
                i = i - 1;
                continue;
            } else if (c == '(') {
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
                    }
                }
                tempStack.push(permStack.pop());
            } else if (c == '|') {
                operatorStack.push(c);
                NFA temp = tempStack.pop();
                permStack.push(temp);
            } else if (c == '*') {
                operatorStack.push(c);
                NFA temp = tempStack.pop();
                permStack.push(temp);
            } else if (c == '+') {
                operatorStack.push(c);
                NFA temp = tempStack.pop();
                permStack.push(temp);
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

        if (!tempStack.empty()) {
            permStack.push(tempStack.pop());
        }

        while (!operatorStack.isEmpty() && permStack.size() > 1 && (operatorStack.peek() == '*' || operatorStack.peek() == '+' || operatorStack.peek() == '|')) {
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
            }
        }

        return permStack.pop();
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

                System.out.printf("| %-10s | %-10s | %-20s |\n", "q" + current.id, (symbol == 'ε' ? "ε" : symbol), nextStatesStr);

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

        String KEYWORDS = "flip|twist|flop|spin|echo|capture|global|return|func|code";
        String BOOLEAN = "true|false";
        String DATATYPES = "rizz|alpha|beta|gamma";
        String IDENTIFIERS = "[a-z]+";
        String OPERATORS = "+|*|-|/|%|=|<=|>=|==|!=|<|>";
        String STRING = "\"([^\"]*)\"";
        String COMMENT = "<<<[\\s\\S]*?>>>|<<.*?>>";
        String INTEGER = "\\b\\d+\\b";
        String DECIMAL = "\\b\\d+\\.\\d{1,5}\\b";
        String CHARACTER = "'[^']'";
        String PUNCTUATOR = "[{}(),;\\[\\]]";

        NFA identifier_nfa = regexToNFA(COMMENT);
        printNFATransitionTable(identifier_nfa);
    }
}