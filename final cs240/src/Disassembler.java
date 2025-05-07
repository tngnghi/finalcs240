import java.io.*;
import java.util.*;

public class Disassembler {
    private static final HashMap<String, String> disasm3 = new HashMap<>();
    private static final HashMap<String, String> disasm2 = new HashMap<>();
    private static final HashMap<String, String> disasm0 = new HashMap<>();
    private static final HashMap<String, String> disasm = new HashMap<>();
    private static final HashMap<String, String> register = new HashMap<>();

    static {
        disasm3.put("00000", "ADD");
        disasm3.put("00001", "SUB");
        disasm3.put("00010", "MUL");
        disasm3.put("00011", "DIV");
        disasm2.put("00100", "AND");
        disasm2.put("00101", "OR");
        disasm.put("00110", "NOT");
        disasm.put("00111", "LOAD");
        disasm.put("01000", "STORE");
        disasm.put("01001", "JUMP");
        disasm.put("01010", "RED");
        disasm.put("01011", "BLUE");
        disasm.put("01100", "GREEN");
        disasm0.put("01101", "YELLOW");
        disasm0.put("01110", "BLACK");
        disasm0.put("01111", "WHITE");
        disasm.put("10000", "ORANGE");
        disasm0.put("10001", "PINK");
        disasm2.put("10010", "GRAY");
        disasm.put("10011", "PURPLE");

        register.put("000", "R0");
        register.put("001", "R1");
        register.put("010", "R2");
        register.put("011", "R3");
        register.put("100", "R");
        register.put("101", "B");
        register.put("110", "G");
        register.put("111", "A0");
    }

    public static String[] readFile(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            return new String[0];
        }
        return lines.toArray(new String[0]);
    }

    static boolean jumpSearch(String bin) {
        return bin.startsWith("01001");
    }

    static String decode(String bin) {
        String ins = bin.substring(0, 5);
        String imm, rd, rs, rt;

        if (ins.equals("00111") || ins.equals("01000") || ins.equals("10011")) {
            rd = bin.substring(5, 8);
            imm = bin.substring(8);
            return disasm.get(ins) + " " + register.get(rd) + ", " + Integer.parseInt(imm, 2);
        } else if (ins.equals("01010") || ins.equals("01011") || ins.equals("01100") || ins.equals("10000")) {
            imm = bin.substring(8);
            return disasm.get(ins) + " " + Integer.parseInt(imm, 2);
        } else if (ins.equals("00110")) {
            rd = bin.substring(5, 8);
            return disasm.get(ins) + " " + register.get(rd);
        } else if (ins.equals("01001")) {
            imm = bin.substring(5);
            return disasm.get(ins) + " BRANCH" + Integer.parseInt(imm, 2);
        } else {
            rd = bin.substring(5, 8);
            rs = bin.substring(8, 11);
            rt = bin.substring(11, 14);

            if (disasm3.containsKey(ins)) {
                return disasm3.get(ins) + " " + register.get(rd) + ", " + register.get(rs) + ", " + register.get(rt);
            } else if (disasm2.containsKey(ins)) {
                return disasm2.get(ins) + " " + register.get(rd) + ", " + register.get(rs);
            } else if (disasm0.containsKey(ins)) {
                return disasm0.get(ins);
            }
        }
        return "UNKNOWN";
    }

    public static void main(String[] args) {
        String inputPath = "src/sample.mc";
        String outputPath = "src/output.asm";

        String[] fileContent = readFile(inputPath);
        int address = 300;
        int n = 0;

        List<String> outputLines = new ArrayList<>();

        for (String line : fileContent) {
            if (jumpSearch(line)) {
                address = Integer.parseInt(line.substring(5), 2);
                break;
            }
        }

        for (String line : fileContent) {
            if (address != 300) {
                address--;
                n++;
            }
            if (address == 0) {
                outputLines.add("BRANCH" + n + ":");
            }
            outputLines.add(decode(line));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (String line : outputLines) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Assembly written to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Failed to write file: " + e.getMessage());
        }
    }
}
