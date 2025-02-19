package automata;

import java.util.*;

class DFA {
    State startState;
    Set<State> acceptingStates = new HashSet<>();
    Map<Integer, State> states = new HashMap<>();

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
}
