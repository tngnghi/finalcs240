import java.io.*;
import java.util.*;

public class Compiler {
    private static int labelCounter = 0;
    private static Map<String, String> varRegMap = new HashMap<>();
    private static int nextReg = 1; // R1-R3 available for variables

    public static void main(String[] args) throws IOException {
        String inputPath = "src/sample2.c";
        String outputPath = "src/output.cal";

        List<String> cCode = readFile(inputPath);
        List<String> calCode = new ArrayList<>();

        try {
            for (String line : cCode) {
                line = line.trim().replaceAll("//.*", "");
                if (line.isEmpty()) continue;

                try {
                    if (line.startsWith("print(")) {
                        handlePrintStatement(line, calCode);
                    } else if (line.startsWith("int ")) {
                        handleDeclaration(line, calCode);
                    } else if (line.startsWith("for ")) {
                        handleForLoop(line, calCode);
                    } else if (line.startsWith("if ")) {
                        handleIfStatement(line, calCode);
                    } else if (line.contains("=")) {
                        handleAssignment(line, calCode);
                    } else if (line.contains("PINK")) {
                        calCode.add("PINK");
                    } else if (line.contains("WHITE")) {
                        calCode.add("WHITE");
                    } else if (line.contains("RED")) {
                        calCode.add("RED");
                    } else if (line.contains("BLUE")) {
                        calCode.add("BLUE");
                    }
                } catch (Exception e) {
                    System.err.println("Error processing line: " + line);
                    e.printStackTrace();
                }
            }
        } finally {
            // Ensure output is written
            writeFile(outputPath, calCode);
            System.out.println("Compilation complete. Output written to " + outputPath);
        }
    }

    private static void handleDeclaration(String line, List<String> calCode) {
        String[] parts = line.split("\\s+|=|;");
        String var = parts[1].trim();
        String value = parts.length > 3 ? parts[3].trim() : "0";

        String reg = allocateRegister(var);
        calCode.add("LOAD " + reg + ", " + value + " ; " + var + " declaration");
    }

    private static void handleForLoop(String line, List<String> calCode) {
        int loopLabel = labelCounter++;
        String[] parts = line.split("[();]");
        String[] init = parts[1].split("=");
        String[] cond = parts[2].split("[<=]");
        String[] inc = parts[3].split("\\+\\+");

        // Initialization
        String var = init[0].replaceAll("int ", "").trim();
        String reg = allocateRegister(var);
        calCode.add("LOAD " + reg + ", " + init[1].trim() + " ; for init");

        // Loop start
        calCode.add("LOOP" + loopLabel + ":");

        // Condition check
        String limitReg = allocateRegister("_limit");
        calCode.add("LOAD " + limitReg + ", " + cond[1].trim());
        calCode.add("SUB R3, " + reg + ", " + limitReg);
        calCode.add("GRAY R3, R0, END" + loopLabel + " ; Branch if i > limit");

        // Loop body
        calCode.add("; Loop body goes here");

        // Increment
        calCode.add("ADD " + reg + ", " + reg + ", 1");

        // Loop back
        calCode.add("JUMP LOOP" + loopLabel);

        // Loop end
        calCode.add("END" + loopLabel + ":");
    }

    private static void handleIfStatement(String line, List<String> calCode) {
        int ifLabel = labelCounter++;
        String[] parts = line.split("\\)\\s*", 2);
        String condition = parts[0].replaceFirst(".*\\(", "").trim();
        String action = parts.length > 1 ? parts[1].replaceAll("[{};]", "").trim() : "";

        if (condition.contains("&&")) {
            handleCompoundCondition(condition, action, ifLabel, calCode);
        } else if (condition.contains("%")) {
            handleModuloCondition(condition, action, ifLabel, calCode);
        } else {
            throw new RuntimeException("Unsupported condition: " + condition);
        }

        calCode.add("END_IF_" + ifLabel + ":");
    }

    private static void handleCompoundCondition(String condition, String action, int label, List<String> calCode) {
        String[] subConds = condition.split("&&");
        String tempLabel = "SKIP_" + label;

        // First condition
        handleModuloCondition(subConds[0].trim(), tempLabel, label, calCode);

        // Second condition
        handleModuloCondition(subConds[1].trim(), tempLabel, label, calCode);

        // Action if both conditions met
        processAction(action, calCode);
        calCode.add("JUMP END_IF_" + label);
        calCode.add(tempLabel + ":");
    }

    private static void handleModuloCondition(String condition, String action, int label, List<String> calCode) {
        String[] parts = condition.split("%");
        String var = parts[0].trim();
        String[] modParts = parts[1].split("==");
        String modVal = modParts[0].trim();
        String compareVal = modParts[1].trim();

        String tempReg = allocateRegister("_temp");
        calCode.add("LOAD " + tempReg + ", " + modVal + " ; Modulo operation");
        calCode.add("DIV R3, " + getVarRegister(var) + ", " + tempReg);
        calCode.add("GRAY A0, " + compareVal + ", SKIP_" + label + " ; Branch if not divisible");

        // Perform action if condition is true
        processAction(action, calCode);
        calCode.add("JUMP END_IF_" + label);
        calCode.add("SKIP_" + label + ":");
    }

    private static void processAction(String action, List<String> calCode) {
        if (action.startsWith("print")) {
            String content = action.replaceAll(".*\\(\"|\"\\).*", "");
            calCode.add("LOAD R1, \"" + content + "\"");
            calCode.add("PINK ; Print " + content);
        } else if (action.equals("WHITE")) {
            calCode.add("WHITE");
        } else if (action.equals("RED")) {
            calCode.add("RED");
        } else if (action.equals("BLUE")) {
            calCode.add("BLUE");
        }
    }

    private static void handlePrintStatement(String line, List<String> calCode) {
        String content = line.replaceAll(".*\\(\"|\"\\).*", "");
        calCode.add("LOAD R1, \"" + content + "\"");
        calCode.add("PINK ; Print " + content);
    }

    private static void handleAssignment(String line, List<String> calCode) {
        String[] parts = line.split("=");
        String lhs = parts[0].trim();
        String rhs = parts[1].replaceAll(";", "").trim();

        if (rhs.contains("+")) {
            String[] operands = rhs.split("\\+");
            calCode.add("ADD " + getVarRegister(lhs) + ", " +
                    getVarRegister(operands[0].trim()) + ", " +
                    getVarRegister(operands[1].trim()));
        }
    }

    private static String allocateRegister(String var) {
        if (!varRegMap.containsKey(var)) {
            if (nextReg > 3) throw new RuntimeException("Out of registers");
            String reg = "R" + nextReg++;
            varRegMap.put(var, reg);
            return reg;
        }
        return varRegMap.get(var);
    }

    private static String getVarRegister(String var) {
        if (!varRegMap.containsKey(var)) {
            throw new RuntimeException("Undeclared variable: " + var);
        }
        return varRegMap.get(var);
    }

    private static List<String> readFile(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static void writeFile(String filename, List<String> content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (String line : content) {
                bw.write(line);
                bw.newLine();
            }
        }
    }
}
