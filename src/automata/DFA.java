package automata;

import java.util.*;

public class DFA {
    State startState;
    Set<State> acceptingStates = new HashSet<>();
    public Map<Integer, State> states = new HashMap<>();

    public DFA(State startState) {
        this.startState = startState;
        this.states.put(startState.id, startState);
    }

    public void addAcceptingState(State state) {
        this.acceptingStates.add(state);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DFA:\n");
        sb.append("Start state: ").append(startState.id).append("\n");
        sb.append("Accepting states: ");
        for (State state : acceptingStates) {
            sb.append(state.id).append(" ");
        }
        sb.append("\n");
        sb.append("Transitions:\n");

        List<Integer> stateIds = new ArrayList<>(states.keySet());
        Collections.sort(stateIds);

        for (int stateId : stateIds) {
            State state = states.get(stateId);
            sb.append("  State ").append(stateId).append(":\n");

            List<Character> symbols = new ArrayList<>(state.transitions.keySet());
            Collections.sort(symbols);

            for (char symbol : symbols) {
                List<State> nextStates = state.transitions.get(symbol);
                sb.append("    '").append(symbol).append("' → ");
                for (State nextState : nextStates) {
                    sb.append(nextState.id);
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public int process(String input) {
        State state = startState;
        Set<State> uniqueStates = new HashSet<>();

        uniqueStates.add(state);

        for (char c : input.toCharArray()) {
            if (state.transitions.containsKey(c)) {
                state = state.transitions.get(c).get(0);
            } else if (state.transitions.containsKey('*')) {
                state = state.transitions.get('*').get(0);
            } else {
                break;
            }
            uniqueStates.add(state);
        }

        //System.out.println("Unique states visited for '" + input + "': " + uniqueStates.size());
        return uniqueStates.size();
    }
}
