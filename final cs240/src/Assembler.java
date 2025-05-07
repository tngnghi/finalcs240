import java.io.*;
import java.util.*;

public class Assembler {
    private static final HashMap<String, String> asm = new HashMap<>();
    private static final HashMap<String, String> register = new HashMap<>();
    private static final HashMap<String, Integer> labelAddress = new HashMap<>();
    static int address = 0;

    private static final List<String> outputBinary = new ArrayList<>();

    static {
        asm.put("ADD", "00000");
        asm.put("SUB", "00001");
        asm.put("MUL", "00010");
        asm.put("DIV", "00011");
        asm.put("AND", "00100");
        asm.put("OR", "00101");
        asm.put("NOT", "00110");
        asm.put("LOAD", "00111");
        asm.put("STORE", "01000");
        asm.put("JUMP", "01001");
        asm.put("RED", "01010");
        asm.put("BLUE", "01011");
        asm.put("GREEN", "01100");
        asm.put("YELLOW", "01101");
        asm.put("BLACK", "01110");
        asm.put("WHITE", "01111");
        asm.put("ORANGE", "10000");
        asm.put("PINK", "10001");
        asm.put("GRAY", "10010");
        asm.put("PURPLE", "10011");

        register.put("fill", "000");
        register.put("R0", "000");
        register.put("R1", "001");
        register.put("R2", "010");
        register.put("R3", "011");
        register.put("R", "100");
        register.put("B", "101");
        register.put("G", "110");
        register.put("A0", "111");
    }

    public static String[] readFile(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return lines.toArray(new String[0]);
    }

    public static String fillBinaryString(String binaryString, int desiredLength) {
        if (binaryString.length() >= desiredLength) {
            return binaryString;
        }
        return "0".repeat(desiredLength - binaryString.length()) + binaryString;
    }

    static void encode(String cal) {
        if (cal.isEmpty()) return;

        String[] code = cal.split(" ");
        String ins = code[0];
        String rd, rs, rt;
        int imm = 0;
        boolean existImm = true;
        address++;

        if (cal.endsWith(":")) {
            String label = cal.substring(0, cal.length() - 1);
            labelAddress.put(label, address);
            return;
        }

        if (asm.containsKey(ins)) {
            if (code[code.length - 1].contains("0x1F6")) {
                code[code.length - 1] = code[code.length - 1].substring(5);
            }

            try {
                imm = Integer.parseInt(code[code.length - 1]);
            } catch (NumberFormatException e) {
                existImm = false;
            }

            rd = (code.length > 1) ? code[1].replace(",", "") : "fill";
            rs = (code.length > 2) ? code[2].replace(",", "") : "fill";
            rt = (code.length > 3) ? code[3].replace(",", "") : "fill";

            if (ins.equals("RED") || ins.equals("BLUE") || ins.equals("GREEN") || ins.equals("ORANGE")) {
                rd = "fill";
            }

            String binaryInstruction;

            if (ins.equals("JUMP")) {
                int jumpAddr = labelAddress.getOrDefault(rd, 0);
                binaryInstruction = asm.get(ins) + fillBinaryString(Integer.toBinaryString(jumpAddr), 11);
            } else if (existImm) {
                binaryInstruction = asm.get(ins) + register.get(rd) + fillBinaryString(Integer.toBinaryString(imm), 8);
            } else {
                binaryInstruction = asm.get(ins) + register.get(rd) + register.get(rs) + register.get(rt) + "00";
            }

            outputBinary.add(binaryInstruction);
        }
    }

    public static void writeBinaryToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String bin : outputBinary) {
                writer.write(bin);
                writer.newLine();
            }
            System.out.println("Binary output written to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing to binary file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String filePath = "src/sample.asm"; // Input file
        String outputPath = "src/output.bin";   // Output binary file
        String[] fileContent = readFile(filePath);

        // First pass to collect labels
        for (String line : fileContent) {
            if (line.trim().endsWith(":")) {
                address++;
                labelAddress.put(line.trim().replace(":", ""), address);
            } else {
                address++;
            }
        }

        address = 0;

        // Second pass to encode instructions
        for (String line : fileContent) {
            encode(line.trim());
        }

        writeBinaryToFile(outputPath);
    }
}
