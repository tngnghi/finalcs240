# Custom ISA Toolchain — CS 240 Final Project

A three-part compiler toolchain built in Java for a custom 16-bit Instruction Set Architecture (ISA): Assembler, Disassembler, and a C-to-Assembly Compiler.

## The ISA

Custom 16-bit instruction format with 20 opcodes including arithmetic (ADD, SUB, MUL, DIV), logic (AND, OR, NOT), memory (LOAD, STORE), control flow (JUMP), and color/display instructions (RED, GREEN, BLUE, ORANGE, PINK, ...).

All instructions encode to 16-bit binary: `[5-bit opcode][3-bit Rd][8-bit immediate or 3+3+2 registers]`

## Components

### Assembler (`Assembler.java`)
- Reads `.asm` source files
- Two-pass: first pass collects label addresses, second pass encodes instructions
- Outputs `.bin` — one 16-bit binary string per instruction
- Handles labels, immediate values, register operands, and hex literals (e.g. `0x1F600`)

### Disassembler (`Disassembler.java`)
- Reads `.mc` (machine code) binary files
- Decodes each 16-bit word back to human-readable assembly
- Reconstructs branch labels from JUMP targets

### Compiler (`Compiler.java`)
- Reads a simplified subset of C
- Supports: variable declarations, assignment, for loops, if statements, print calls
- Outputs `.cal` intermediate assembly for the custom ISA
- Implements basic register allocation (R1–R3)

## Example

Input (`sample.asm`):
LOAD R1, 5
LOAD R2, 10
ADD R3, R1, R2
JUMP t1

Output (`output.bin`):
0011100100000101
0011101000001010
0000001100101000
0100100000000100

## Run

Open in any Java IDE (IntelliJ recommended) and run each class independently. Input/output file paths are set at the top of each `main()`.

## What I Learned

Implementing this project gave me hands-on understanding of how real compilers and CPU instruction pipelines work — from source code down to raw bits.
