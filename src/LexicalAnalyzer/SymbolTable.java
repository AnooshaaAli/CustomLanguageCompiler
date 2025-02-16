package LexicalAnalyzer;

import java.util.*;

class SymbolTable {
    private final Map<String, SymbolEntry> table = new LinkedHashMap<>();
    private int memoryAddress = 1000;

    public Iterable<SymbolEntry> getEntries() {
        return table.values();
    }

    static class SymbolEntry {
        private final String name;
        private final String type;
        private final int memoryLocation;

        SymbolEntry(String name, String type, int memoryLocation) {
            this.name = name;
            this.type = type;
            this.memoryLocation = memoryLocation;
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

    }

    public void addSymbol(String name, String type) {
        if (!table.containsKey(name)) {
            table.put(name, new SymbolEntry(name, type, memoryAddress++));
        }
    }

    public void printSymbolTable() {
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-20s %-15s %-20s%n", "Name", "Type", "Memory Location");
        System.out.println("------------------------------------------------------------");

        for (SymbolEntry entry : table.values()) {
            System.out.printf("%-20s %-15s %-20d%n",
                    entry.getName(),
                    entry.getType(),
                    entry.getMemoryLocation());
        }
    }
}
