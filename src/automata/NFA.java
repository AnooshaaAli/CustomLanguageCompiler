package automata;

class NFA {
    State startState;
    State endState;

    public NFA(State startState, State endState) {
        this.startState = startState;
        this.endState = endState;
    }
}