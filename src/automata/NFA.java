package automata;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

class NFA {
    State startState;
    State endState;

    public NFA(State startState, State endState) {
        this.startState = startState;
        this.endState = endState;
    }

    public int countStates() {
        Set<State> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();

        queue.add(startState);
        visited.add(startState);

        while (!queue.isEmpty()) {
            State current = queue.poll();
            for (Character symbol : current.getTransitions().keySet()) {
                for (State next : current.getTransitions().get(symbol)) {
                    if (!visited.contains(next)) {
                        visited.add(next);
                        queue.add(next);
                    }
                }
            }
        }
        return visited.size();
    }
}