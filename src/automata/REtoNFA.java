package automata;

import java.util.*;
import java.util.stream.Collectors;

public class REtoNFA {
    private static int stateCounter = 0;

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
        String IDENTIFIERS = "[a-z][a-z0-9]*";
        String OPERATORS = "[+\\-*/%^]|=|<=|>=|==|!=|<|>";
        String STRING = "\"([^\"]*)\"";
        String COMMENT = "<<<[\\s\\S]*?>>>|<<.*?>>";
        String INTEGER = "\\b\\d+\\b";
        String DECIMAL = "\\b\\d+\\.\\d{1,5}\\b";
        String CHARACTER = "'[^']'";
        String PUNCTUATOR = "[{}(),;\\[\\]]";

        NFA keyword_nfa = regexToNFA(KEYWORDS);
        printNFATransitionTable(keyword_nfa);
    }
}