package LexicalAnalyzer;

import java.sql.Array;
import java.util.*;

class SymbolTable {
    private final Map<String, SymbolEntry> table = new LinkedHashMap<>();
    private int memoryAddress = 1000;
    private final Deque<String> scopeStack = new ArrayDeque<>();

    public SymbolTable() {
        scopeStack.push("global");
    }

    public Iterable<SymbolEntry> getEntries() {
        return table.values();
    }

    static class SymbolEntry {
        private final String name;
        private final String type;
        private final int memoryLocation;
        private final String scope;

        SymbolEntry(String name, String type, int memoryLocation, String scope) {
            this.name = name;
            this.type = type;
            this.memoryLocation = memoryLocation;
            this.scope = scope;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public int getMemoryLocation() {
            return memoryLocation;
        }

        public String getScope() {
            return scope;
        }
    }


    public void addSymbol(String name, String type) {
        String currentScope = scopeStack.peek();
        if (!table.containsKey(name)) {
            table.put(name, new SymbolEntry(name, type, memoryAddress++, currentScope));
        }
    }

    public void enterScope(String scopeName) {
        scopeStack.push(scopeName);
    }

    public void exitScope() {
        if (!scopeStack.isEmpty()) {
            scopeStack.pop();
        }
    }

    public void printSymbolTable() {
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("%-20s %-15s %-20s %-15s%n", "Name", "Type", "Memory Location", "Scope");
        System.out.println("-------------------------------------------------------------------------------");

        for (SymbolEntry entry : table.values()) {
            System.out.printf("%-20s %-15s %-20d %-15s%n",
                    entry.getName(),
                    entry.getType(),
                    entry.getMemoryLocation(),
                    entry.getScope());
        }
    }
}
