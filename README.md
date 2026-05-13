16-bit CPU Architecture Simulator
A functional, software-based simulation of a 16-bit Central Processing Unit (CPU) developed in Java. This project demonstrates the fundamental principles of computer architecture, including instruction fetching, decoding, and execution.

## Project Overview
This simulator mimics the behavior of a physical processor. It includes a custom Instruction Set Architecture (ISA) and manages data flow between the Arithmetic Logic Unit (ALU), general-purpose registers, and simulated memory.

## Key Features
16-bit Data Path: Processes data in 16-bit increments.

Custom ALU: Supports core operations including Addition, Subtraction, AND, OR, and NOT, with dedicated status flags (Zero, Carry).

Register File: Includes a set of general-purpose registers and a Program Counter (PC).

Instruction Set: A custom-designed ISA for memory manipulation, arithmetic, and logical branching.

OOP Design: Built using clean Object-Oriented Programming principles in Java for modularity and scalability.

## Architecture Components
ALU (Arithmetic Logic Unit): The "brain" that handles all mathematical and logical computations.

Control Unit: Decodes opcodes and coordinates the timing of the fetch-decode-execute cycle.

Memory Simulation: A simulated RAM environment used for loading instructions and storing data.

Registers: High-speed storage locations for immediate operand access.

## Technologies Used
Language: Java

Paradigm: Object-Oriented Programming (OOP)

Tools: Java Streams, Collections API

## How to Run
Ensure you have JDK 8 or higher installed.

Clone the repository:

Bash
git clone https://github.com/your-username/cpu-simulator.git
Compile the source files:

Bash
javac *.java
Run the main simulator class:

Bash
java Main
## Author
Befekadu Asfaw Tilahun

Electrical and Computer Engineering Student, AAiT
