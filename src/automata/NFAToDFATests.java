package automata;

import java.util.*;

public class NFAToDFATests {
    public static void main(String[] args) {
        testEndsWithAB();
        testStartsWithA();
        testContainsAB();
        testBinaryDivisibleBy3();
        testEpsilonTransitions();
    }

    public static void testEndsWithAB() {
        State state0 = new State(0);
        State state1 = new State(1);
        State state2 = new State(2);

        state0.addTransition('a', state0);
        state0.addTransition('b', state0);
        state0.addTransition('a', state1);
        state1.addTransition('b', state2);

        NFA nfa = new NFA(state0, state2);
        System.out.println("\nTest Case: Ends with 'ab'");
        runConversion(nfa);
    }

    public static void testStartsWithA() {
        State state0 = new State(0);
        State state1 = new State(1);

        state0.addTransition('a', state1);
        state1.addTransition('a', state1);
        state1.addTransition('b', state1);

        NFA nfa = new NFA(state0, state1);
        System.out.println("\nTest Case: Starts with 'a'");
        runConversion(nfa);
    }

    public static void testContainsAB() {
        State state0 = new State(0);
        State state1 = new State(1);
        State state2 = new State(2);

        state0.addTransition('a', state0);
        state0.addTransition('b', state0);
        state0.addTransition('a', state1);
        state1.addTransition('b', state2);
        state2.addTransition('a', state2);
        state2.addTransition('b', state2);

        NFA nfa = new NFA(state0, state2);
        System.out.println("\nTest Case: Contains 'ab'");
        runConversion(nfa);
    }

    public static void testBinaryDivisibleBy3() {
        State state0 = new State(0);
        State state1 = new State(1);
        State state2 = new State(2);

        state0.addTransition('0', state0);
        state0.addTransition('1', state1);
        state1.addTransition('0', state2);
        state1.addTransition('1', state0);
        state2.addTransition('0', state1);
        state2.addTransition('1', state2);

        NFA nfa = new NFA(state0, state0);
        System.out.println("\nTest Case: Binary number divisible by 3");
        runConversion(nfa);
    }

    public static void testEpsilonTransitions() {
        State state0 = new State(0);
        State state1 = new State(1);
        State state2 = new State(2);

        state0.addTransition('\0', state1);
        state1.addTransition('a', state2);

        NFA nfa = new NFA(state0, state2);
        System.out.println("\nTest Case: NFA with epsilon transitions");
        runConversion(nfa);
    }

    private static void runConversion(NFA nfa) {
        System.out.println("Original NFA:");
        NFAtoDFAConverter.printNFAInfo(nfa);
        System.out.println("\nConverting NFA to DFA...");
        DFA dfa = NFAtoDFAConverter.convertNFAtoDFA(nfa);
        System.out.println("\nResulting DFA:");
        System.out.println(dfa);
    }
}
