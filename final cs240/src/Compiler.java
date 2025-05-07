import java.io.*;
import java.util.*;

public class Compiler {

    private static int labelCount = 0;

    public static void main(String[] args) throws IOException {
        String inputFilePath = "src/sample.c";
        String outputFilePath = "src/output.cal";

        List<String> lines = readFile(inputFilePath);
        List<String> output = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("int ")) {
                output.addAll(compileDeclaration(line));
            } else if (line.contains("=") && line.endsWith(";")) {
                output.addAll(compileAssignment(line));
            } else if (line.startsWith("if")) {
                output.addAll(compileIf(line));
            } else if (line.startsWith("for")) {
                output.addAll(compileFor(line));
            } else if (line.startsWith("printf")) {
                output.add("PINK"); // Print
            }
        }

        writeFile(outputFilePath, output);
        System.out.println("Compilation complete. Output written to: " + outputFilePath);
    }

    private static List<String> compileDeclaration(String line) {
        List<String> code = new ArrayList<>();
        // Example: int a = 5; or int c = a + b;
        line = line.replace("int", "").replace(";", "").trim();
        if (line.contains("=")) {
            String[] parts = line.split("=");
            String var = parts[0].trim();
            String rhs = parts[1].trim();

            // If RHS is a number, process as normal
            try {
                int val = Integer.parseInt(rhs);
                code.add("LOAD R1, " + val);
                code.add("STORE R1, " + var);
            } catch (NumberFormatException e) {
                // RHS is an expression like a + b
                code.addAll(compileAssignment(var + " = " + rhs + ";"));
            }
        }
        return code;
    }


    private static List<String> compileAssignment(String line) {
        List<String> code = new ArrayList<>();
        // Example: a = b + 1;
        line = line.replace(";", "").trim();
        String[] parts = line.split("=");
        String lhs = parts[0].trim();
        String rhs = parts[1].trim();

        if (rhs.contains("+")) {
            String[] ops = rhs.split("\\+");
            code.add("LOAD R2, " + ops[0].trim());
            code.add("LOAD R3, " + ops[1].trim());
            code.add("ADD R1, R2, R3");
        } else {
            code.add("LOAD R1, " + rhs);
        }
        code.add("STORE R1, " + lhs);
        return code;
    }

    private static List<String> compileIf(String line) {
        List<String> code = new ArrayList<>();
        // Simplified: if (a == b)
        int label = labelCount++;
        String condition = line.substring(line.indexOf('(') + 1, line.indexOf(')'));
        String[] parts = condition.split("==");
        code.add("LOAD R2, " + parts[0].trim());
        code.add("LOAD R3, " + parts[1].trim());
        code.add("SUB R1, R2, R3");
        code.add("JUMP IFZERO" + label);
        code.add("..."); // placeholder for else
        code.add("LABEL IFZERO" + label);
        return code;
    }

    private static List<String> compileFor(String line) {
        List<String> code = new ArrayList<>();
        int labelStart = labelCount++;
        int labelEnd = labelCount++;
        String[] parts = line.substring(line.indexOf('(') + 1, line.indexOf(')')).split(";");
        String init = parts[0].trim();      // i = 0
        String cond = parts[1].trim();      // i < 10
        String update = parts[2].trim();    // i++

        code.addAll(compileAssignment(init));
        code.add("LABEL LOOP" + labelStart);
        code.add("LOAD R2, " + cond.split("<")[0].trim());
        code.add("LOAD R3, " + cond.split("<")[1].trim());
        code.add("SUB R1, R3, R2");
        code.add("JUMP IFZERO" + labelEnd);
        code.add("..."); // Loop body placeholder
        code.addAll(compileAssignment(update.replace("++", " = " + cond.split("<")[0].trim() + " + 1")));
        code.add("JUMP LOOP" + labelStart);
        code.add("LABEL IFZERO" + labelEnd);
        return code;
    }

    private static List<String> readFile(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) lines.add(line);
        return lines;
    }

    private static void writeFile(String path, List<String> lines) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        for (String line : lines) writer.write(line + "\n");
        writer.close();
    }
}
