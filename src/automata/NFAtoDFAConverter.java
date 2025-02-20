package automata;

import java.util.*;

public class NFAtoDFAConverter {
    private static int nextStateId = 0;

    // Helper method to get epsilon closure of a state
    private static Set<State> epsilonClosure(State state) {
        Set<State> closure = new HashSet<>();
        Stack<State> stack = new Stack<>();

        stack.push(state);
        closure.add(state);

        while (!stack.isEmpty()) {
            State current = stack.pop();

            // Get all states reachable through epsilon transitions
            List<State> epsilonTransitions = current.transitions.getOrDefault('\0', Collections.emptyList());
            for (State nextState : epsilonTransitions) {
                if (!closure.contains(nextState)) {
                    closure.add(nextState);
                    stack.push(nextState);
                }
            }
        }

        return closure;
    }

    // Helper method to get epsilon closure of a set of states
    private static Set<State> epsilonClosure(Set<State> states) {
        Set<State> closure = new HashSet<>();

        for (State state : states) {
            closure.addAll(epsilonClosure(state));
        }

        return closure;
    }

    // Helper method to get all states reachable from a set of states on a given symbol
    private static Set<State> move(Set<State> states, char symbol) {
        Set<State> result = new HashSet<>();

        for (State state : states) {
            List<State> nextStates = state.transitions.getOrDefault(symbol, Collections.emptyList());
            result.addAll(nextStates);
        }

        return result;
    }

    // Helper method to get a unique ID for a set of states
    private static String getStateSetId(Set<State> states) {
        List<Integer> stateIds = new ArrayList<>();
        for (State state : states) {
            stateIds.add(state.id);
        }
        Collections.sort(stateIds);

        StringBuilder sb = new StringBuilder();
        for (int id : stateIds) {
            sb.append(id).append("_");
        }
        return sb.toString();
    }

    public static DFA convertNFAtoDFA(NFA nfa) {
        // Reset state ID counter
        nextStateId = 0;

        // Get alphabets (excluding epsilon)
        Set<Character> alphabet = new HashSet<>();
        LinkedList<State> queue = new LinkedList<>();
        queue.add(nfa.startState);
        Set<State> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            State current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            for (char symbol : current.transitions.keySet()) {
                if (symbol != '\0') {  // Skip epsilon
                    alphabet.add(symbol);
                }

                for (State nextState : current.transitions.get(symbol)) {
                    if (!visited.contains(nextState)) {
                        queue.add(nextState);
                    }
                }
            }
        }

        // Start DFA construction
        Map<String, State> dfaStates = new HashMap<>();
        Map<String, Set<State>> nfaStateSets = new HashMap<>();
        Queue<Set<State>> unmarkedStateSets = new LinkedList<>();

        // Create start state for DFA
        Set<State> startStateSet = epsilonClosure(nfa.startState);
        String startStateSetId = getStateSetId(startStateSet);
        State dfaStartState = new State(nextStateId++);
        dfaStates.put(startStateSetId, dfaStartState);
        nfaStateSets.put(startStateSetId, startStateSet);
        unmarkedStateSets.add(startStateSet);

        DFA dfa = new DFA(dfaStartState);

        // Main algorithm
        while (!unmarkedStateSets.isEmpty()) {
            Set<State> currentStateSet = unmarkedStateSets.poll();
            String currentStateSetId = getStateSetId(currentStateSet);
            State dfaCurrentState = dfaStates.get(currentStateSetId);

            // Check if current state set contain any accepting state
            if (currentStateSet.contains(nfa.endState)) {
                dfa.addAcceptingState(dfaCurrentState);
            }

            // Process each symbol in the alphabet
            for (char symbol : alphabet) {
                Set<State> nextStateSet = epsilonClosure(move(currentStateSet, symbol));

                if (nextStateSet.isEmpty()) continue;

                String nextStateSetId = getStateSetId(nextStateSet);

                // Create new DFA state if needed
                if (!dfaStates.containsKey(nextStateSetId)) {
                    State dfaNextState = new State(nextStateId++);
                    dfaStates.put(nextStateSetId, dfaNextState);
                    nfaStateSets.put(nextStateSetId, nextStateSet);
                    unmarkedStateSets.add(nextStateSet);
                    dfa.states.put(dfaNextState.id, dfaNextState);
                }

                // Add transition
                State dfaNextState = dfaStates.get(nextStateSetId);
                dfaCurrentState.addTransition(symbol, dfaNextState);
            }
        }

        return dfa;
    }

    //NFA print function
    public static void printNFAInfo(NFA nfa) {
        System.out.println("NFA Information:");
        System.out.println("Start state: " + nfa.startState.id);
        System.out.println("End state: " + nfa.endState.id);

        // show NFA using BFS
        Queue<State> queue = new LinkedList<>();
        Set<State> visited = new HashSet<>();

        queue.add(nfa.startState);

        while (!queue.isEmpty()) {
            State current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            System.out.println("State " + current.id + " transitions:");
            for (Map.Entry<Character, List<State>> entry : current.transitions.entrySet()) {
                char symbol = entry.getKey();
                System.out.print("  On '" + (symbol == '\0' ? "ε" : symbol) + "' to: ");
                for (State nextState : entry.getValue()) {
                    System.out.print(nextState.id + " ");
                    queue.add(nextState);
                }
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        // Example NFA accepts ending with 'ab'
        // States: 0 (start) -> 1 -> 2 (accept)
        // Transitions:
        // State 0: 'a' -> 0, 'b' -> 0, 'a' -> 1
        // State 1: 'b' -> 2
        // State 2: final state no transitions

        State state0 = new State(0);
        State state1 = new State(1);
        State state2 = new State(2);

        // Add transitions
        state0.addTransition('a', state0); // 0 -> 0 (on a)
        state0.addTransition('b', state0); // 0 -> 0 (on b)
        state0.addTransition('a', state1); // 0 -> 1 (on a)
        state1.addTransition('b', state2); // 1 -> 2 (on b)


        NFA nfa = new NFA(state0, state2);

        System.out.println("Original NFA:");
        printNFAInfo(nfa);


        System.out.println("\nConverting NFA to DFA...");
        DFA dfa = convertNFAtoDFA(nfa);

        // Show DFA
        System.out.println("\nResulting DFA:");
        System.out.println(dfa);

        System.out.println("\nExample DFA operation:");
        System.out.println("String 'ababab' should be accepted (ends with 'ab')");
        System.out.println("String 'ababba' should be rejected (doesn't end with 'ab')");
    }
}