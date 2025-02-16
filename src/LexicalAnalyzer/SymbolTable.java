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
        private final String value;

        SymbolEntry(String name, String type, String value, int memoryLocation) {
            this.name = name;
            this.type = type;
            this.value = value;
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

        public String getValue() {
            return value == null ? "" : value;
        }
    }

    public void addSymbol(String name, String type, String value) {
        if (!table.containsKey(name)) {
            table.put(name, new SymbolEntry(name, type, value, memoryAddress++));
        }
    }

    public void printSymbolTable() {
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-20s %-15s %-20s %-15s%n", "Name", "Type", "Memory Location", "Value");
        System.out.println("------------------------------------------------------------");

        for (SymbolEntry entry : table.values()) {
            System.out.printf("%-20s %-15s %-20d %-15s%n",
                    entry.getName(),
                    entry.getType(),
                    entry.getMemoryLocation(),
                    entry.getValue());
        }
    }
}
