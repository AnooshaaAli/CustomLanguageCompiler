package automata;

import java.util.*;

class State {
    int id;
    Map<Character, List<State>> transitions = new HashMap<>();

    State(int id) {
        this.id = id;
    }

    void addTransition(char symbol, State next) {
        transitions.computeIfAbsent(symbol, k -> new ArrayList<>()).add(next);
    }

    public Map<Character, List<State>> getTransitions() {
        return transitions;
    }
}


