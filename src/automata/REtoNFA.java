package automata;

import java.util.*;

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

    private static void printNFA(NFA nfa) {
        Stack<Pair<State, String>> stack = new Stack<>();
        Set<State> visited = new HashSet<>();

        stack.push(new Pair<>(nfa.startState, "")); // Push root with empty prefix
        visited.add(nfa.startState);

        System.out.println("NFA Tree Representation:");

        while (!stack.isEmpty()) {
            Pair<State, String> currentPair = stack.pop();
            State current = currentPair.getKey();
            String prefix = currentPair.getValue();

            // Print current state
            System.out.println(prefix + "└── State " + current.id);

            List<Map.Entry<Character, List<State>>> transitions = new ArrayList<>(current.transitions.entrySet());
            Collections.reverse(transitions); // Reverse to maintain proper order

            for (int i = 0; i < transitions.size(); i++) {
                Map.Entry<Character, List<State>> entry = transitions.get(i);
                char symbol = entry.getKey();
                List<State> nextStates = entry.getValue();

                for (int j = nextStates.size() - 1; j >= 0; j--) {
                    State next = nextStates.get(j);
                    boolean isLast = (i == transitions.size() - 1) && (j == 0);

                    System.out.println(prefix + (isLast ? "    └── " : "    ├── ") + "[" + (symbol == 'ε' ? "ε" : symbol) + "] → State " + next.id);

                    if (!visited.contains(next)) {
                        stack.push(new Pair<>(next, prefix + (isLast ? "    " : "    │   ")));
                        visited.add(next);
                    }
                }
            }
        }

        System.out.println("End State: " + nfa.endState.id);
    }

    public static void main(String[] args) {
        String regex = "flip|flop|code";  // Example input
        NFA nfa = regexToNFA(regex);
        printNFA(nfa);
    }
}