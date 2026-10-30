package simulator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GUI {
    private static Path currentFilePath;
    private static JTextArea registerTextArea; // Reference to the register display
    private static JTable memoryTable;
    private static final int DEFAULT_MAX_STEPS = 200000;

    private static final Highlighter.HighlightPainter PAINTER = 
        new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 255, 100));
    // Variable to hold the reference to the currently active highlight
    private static Object currentHighlightTag = null;

    static class MemoryGridModel extends AbstractTableModel {
        private final int ADDRESS_COLUMN_COUNT = 1;
        private final int DATA_COLUMNS = 8;
        private final int ROW_COUNT = 0xF000 / 16;
        private final String[] columnNames;
        private final int START_ADDRESS = 0x0;

        public MemoryGridModel() {
            columnNames = new String[ADDRESS_COLUMN_COUNT + DATA_COLUMNS];
            columnNames[0] = "Address";
            for (int i = 0; i < DATA_COLUMNS; i++) {
                columnNames[i + ADDRESS_COLUMN_COUNT] = String.format("+%02X", i * 2);
            }
        }

        @Override
        public int getRowCount() {
            return ROW_COUNT;
        }

        @Override
        public int getColumnCount() {
            return ADDRESS_COLUMN_COUNT + DATA_COLUMNS;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            int baseAddress = START_ADDRESS + (rowIndex * DATA_COLUMNS * 2);
            if (columnIndex == 0) {
                return String.format("0x%08X", baseAddress);
            } else {
                int offset = (columnIndex - ADDRESS_COLUMN_COUNT) * 2;

                int low_val = CPU.memory.read(baseAddress + offset) & 0xFF;
                int high_val = CPU.memory.read(baseAddress + offset + 1) & 0xFF;
                int value = (low_val) | (high_val << 8);

                return String.format("%08X", value);
            }
        }
    }

    private static void openFileAndPopulate(JTextArea textArea, Component parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setDialogTitle("Open Source File");

        if (fileChooser.showOpenDialog(parentComponent) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            Path path = selectedFile.toPath();

            try {
                String content = Files.readString(path);
                textArea.setText(content);
                textArea.setCaretPosition(0);
                currentFilePath = path;

                textArea.getHighlighter().removeAllHighlights();
                currentHighlightTag = null;
                resetCPU();

                updateRegistersDisplay();
                updateMemoryDisplay();

            } catch (IOException e) {
                JOptionPane.showMessageDialog(parentComponent, "Could not read file: " + e.getMessage(),
                        "File Read Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static boolean saveFileFromText(JTextArea textArea, Component parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setDialogTitle("Save Source File");

        if (currentFilePath != null) { // we have a file open already
            String content = textArea.getText();
            try {
                Files.writeString(currentFilePath, content);

                // reset the state of the CPU and UI after saving
                textArea.getHighlighter().removeAllHighlights();
                currentHighlightTag = null;
                resetCPU();
                updateRegistersDisplay();
                updateMemoryDisplay();

                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parentComponent,
                        "Could not save file: " + currentFilePath.getFileName() + "\nError: " + e.getMessage(),
                        "File Write Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } 
        // we don't have an open file, save a new file.
        int result = fileChooser.showSaveDialog(parentComponent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            Path path = selectedFile.toPath();
            String content = textArea.getText();

            try {
                Files.writeString(path, content);
                currentFilePath = path;
                JOptionPane.showMessageDialog(parentComponent,
                        "File saved successfully to:\n" + selectedFile.getAbsolutePath(), "Save Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parentComponent,
                        "Could not save file: " + selectedFile.getName() + "\nError: " + e.getMessage(),
                        "File Write Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    private static void resetCPU() {
        CPU.memory = new Memory();
        CPU.registers = new int[Registers.values().length];
        CPU.registers[Registers.SP.ordinal()] = 0xffff;
        CPU.halt = false;
        CPU.alu = new ALU();

        updateRegistersDisplay();
        updateMemoryDisplay();
    }

    private static void runFile(Path path, int maxSteps) {
        if (path == null) {
            JOptionPane.showMessageDialog(null, "Please open a file first.", "Run Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Running long tasks should ideally be done on a worker thread
        // we don't want to freeze the UI while the program is running
        new Thread(() -> {
            try {
                resetCPU();
                List<String> lines = Files.readAllLines(path);
                String src = String.join("\n", lines);
                AsmIR code = Assembler.assemble(src);

                int iter = 0;
                while (!CPU.halt && iter++ < maxSteps) {
                    CPU.step(code);

                    // Note: This must be run on the EDT, so we call the helper methods
                    updateRegistersDisplay();
                    updateMemoryDisplay();
                }

                if (iter >= maxSteps) {
                    showError(null, "Execution Error",
                            "Program exceeded the maximum step count of " + maxSteps
                                    + ". Check for an infinite loop.");
                }
            } catch (IOException e) {
                showError(null, "File Error", "Could not read " + path.getFileName()
                        + ":\n" + e.getMessage());
            } catch (IllegalArgumentException e) {
                showError(null, "Assembly Error", e.getMessage());
            } catch (RuntimeException e) {
                showError(null, "Runtime Error", e.getMessage());
            }
        }).start(); // Start the run on a separate thread
    }

    private static int step(Path path, Component parentComponent) {
        if (path == null) {
            JOptionPane.showMessageDialog(parentComponent, "Please open a file first.", "Step Error",
                    JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        try {
            List<String> lines = Files.readAllLines(path);
            String src = String.join("\n", lines).trim();
            AsmIR code = Assembler.assemble(src);

            int line_no;
            if (CPU.registers[Registers.PC.ordinal()] == code.lines.size()) {
                line_no = code.lines.getLast();
            } else {
                line_no = code.lines.get(CPU.registers[Registers.PC.ordinal()]);
            }

            CPU.step(code);

            updateRegistersDisplay();
            updateMemoryDisplay();

            return line_no;
        } catch (IOException e) {
            showError(parentComponent, "File Error",
                    "Could not read " + path.getFileName() + ":\n" + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(parentComponent, "Assembly Error", e.getMessage());
        } catch (RuntimeException e) {
            showError(parentComponent, "Runtime Error", e.getMessage());
        }
        return -1;
    }

    private static void showError(Component parentComponent, String title, String message) {
        String displayMessage = (message == null || message.isBlank())
                ? "The operation failed without a detailed error message."
                : message;
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                parentComponent, displayMessage, title, JOptionPane.ERROR_MESSAGE));
    }

    private static String getRegisters() {
        StringBuilder val = new StringBuilder();
        for (int i = 0; i < CPU.registers.length; i++) {
            val.append(String.format("%s: 0x%04X = %d\n", Registers.values()[i], 
                        CPU.registers[i], CPU.registers[i]));
        }
        return val.toString();
    }

    // these are helpers to update the register and memory UI, they run on the EDT.
    public static void updateRegistersDisplay() {
        if (registerTextArea == null)
            return;
        SwingUtilities.invokeLater(() -> {
            registerTextArea.setText(getRegisters());
            registerTextArea.setCaretPosition(0);
        });
    }
    
    public static void updateMemoryDisplay() {
        if (memoryTable == null)
            return;
        SwingUtilities.invokeLater(() -> {
            ((MemoryGridModel) memoryTable.getModel()).fireTableDataChanged();
        });
    }

    private static void highlightLine(JTextArea textArea, int lineNumber) {
        Highlighter highlighter = textArea.getHighlighter();
        if (currentHighlightTag != null) {
            highlighter.removeHighlight(currentHighlightTag);
            currentHighlightTag = null;
        }

        if (lineNumber <= 0)
            return;

        try {
            // offset is zero based
            int startOffset = textArea.getLineStartOffset(lineNumber - 1);
            int endOffset = textArea.getLineEndOffset(lineNumber - 1);
            // highlight line and move the caret to the beginning of the line.
            currentHighlightTag = highlighter.addHighlight(startOffset, endOffset, PAINTER);
            textArea.setCaretPosition(startOffset);
        } catch (BadLocationException e) {
            System.err.println("Error highlighting line: " + e.getMessage());
        }
    }

    // selects an entry from the table based on the memory address
    private static void selectMemoryEntry(JTable table, int address) {
        if (address >= 0xF000 || address < 0) {
            table.clearSelection();
            JOptionPane.showMessageDialog(null, "Address out of range.", 
                    "Search Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int row = (address & 0xFFF0) / (16);
        int col = (address - (address & 0xFFF0)) / 2;
        table.setRowSelectionInterval(row, row);
        table.setColumnSelectionInterval(col, col);
        Rectangle rect = table.getCellRect(row, col, true);
        table.scrollRectToVisible(rect);
        System.out.printf("Memory address 0x%08X selected at Row %d, Col %d\n",
                address, row, col);
    }

    public static void showNotification(JFrame frame, String message) {
        JWindow toast = new JWindow(frame);
        toast.setLayout(new BorderLayout());

        JLabel label = new JLabel(message);
        label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                    ));
        toast.add(label);
        toast.pack();

        // top-right of parent frame
        int x = frame.getX() + frame.getWidth() - toast.getWidth() - 20;
        int y = frame.getY() + 40;
        toast.setLocation(x, y);

        toast.setVisible(true);

        // auto-hide after 2 seconds
        new Timer(2000, e -> toast.dispose()).start();
    }

    public static void main(String[] args) {
        // schedule code on the EDT.
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("16-bit CPU simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 750);

        // Prepare shared components
        JTextArea codeArea = new JTextArea();
        MemoryGridModel memoryModel = new MemoryGridModel();
        JTable memoryTableInstance = new JTable(memoryModel); // Instantiate table
        memoryTableInstance.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Store static references
        memoryTable = memoryTableInstance;

        JPanel leftPanel = createLeftPanel(frame, codeArea);

        JPanel rightContainer = createRightPanel(frame, memoryTable);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightContainer);
        splitPane.setDividerLocation(0.6);
        splitPane.setOneTouchExpandable(true);

        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        frame.setVisible(true);

        resetCPU();
    }

    private static JPanel createLeftPanel(JFrame frame, JTextArea codeArea) {
        JPanel leftPanel = new JPanel(new BorderLayout());

        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        codeArea.setText("; Load a file using the 'Open' button or edit this file, save, and run!");
        JScrollPane codeScrollPane = new JScrollPane(codeArea);
        codeScrollPane.setBorder(BorderFactory.createTitledBorder("Instructions"));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton openButton = new JButton("📂 Open");
        openButton.addActionListener(e -> openFileAndPopulate(codeArea, frame));
        toolBar.add(openButton);

        toolBar.addSeparator();

        JButton stepCodeButton = new JButton("⏩ Step");
        stepCodeButton.addActionListener(e -> {
            int line_no = step(currentFilePath, frame);
            if (line_no >= 0) {
                highlightLine(codeArea, line_no);
            }
        });
        toolBar.add(stepCodeButton);

        JButton runButton = new JButton("▶ Run");
        JSpinner maxStepsSpinner = new JSpinner(
                new SpinnerNumberModel(DEFAULT_MAX_STEPS, 1, 10000000, 1000));
        maxStepsSpinner.setToolTipText("Maximum instructions before execution stops");
        JLabel maxStepsLabel = new JLabel("Max steps:");
        runButton.addActionListener(e -> {
            int maxSteps = (Integer) maxStepsSpinner.getValue();
            runFile(currentFilePath, maxSteps);
        });
        toolBar.add(runButton);
        toolBar.add(maxStepsLabel);
        toolBar.add(maxStepsSpinner);

        JButton stopButton = new JButton("⏹ Stop");
        stopButton.addActionListener(e -> {
            CPU.halt = true;
            // PC holds the address of the next instruction, which might be out of
            // bounds if execution finished.
        });
        toolBar.add(stopButton);

        JButton resetButton = new JButton("↻ Reset");
        resetButton.addActionListener(e -> {
            resetCPU();
            showNotification(frame, "CPU state reset.");
        });
        toolBar.add(resetButton);
        toolBar.addSeparator();

        JButton saveButton = new JButton("💾 Save");
        saveButton.addActionListener(e -> {
            saveFileFromText(codeArea, frame);
            showNotification(frame, "File saved successfuly."); 
        });
        toolBar.add(saveButton);

        leftPanel.add(toolBar, BorderLayout.NORTH);
        leftPanel.add(codeScrollPane, BorderLayout.CENTER);
        return leftPanel;
    }


    private static JPanel createRightPanel(JFrame frame, JTable memoryTable) {
        JPanel rightContainer = new JPanel(new BorderLayout());

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField, BorderLayout.CENTER);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            try {
                int address = Integer.parseInt(searchField.getText(), 16);
                selectMemoryEntry(memoryTable, address);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null,
                        "Incorrect address format. Numbers should be in base 16 and should not include prefixes like 0x.",
                        "Search Error",
                        JOptionPane.ERROR_MESSAGE);
                System.err.println("Error while parsing address: " + ex.getMessage());
            }
        });
        searchPanel.add(searchButton, BorderLayout.EAST);
        rightContainer.add(searchPanel, BorderLayout.NORTH);

        JPanel dataPanel = new JPanel(new BorderLayout());

        // register area
        JTextArea registerAreaInstance = new JTextArea(5, 20);
        registerAreaInstance.setEditable(false);
        registerAreaInstance.setFont(new Font("Monospaced", Font.BOLD, 12));
        registerTextArea = registerAreaInstance; // Store static reference

        JScrollPane registerScrollPane = new JScrollPane(registerAreaInstance);
        registerScrollPane.setBorder(BorderFactory.createTitledBorder("Registers"));
        dataPanel.add(registerScrollPane, BorderLayout.NORTH);

        // memory view
        JScrollPane memoryScrollPane = new JScrollPane(memoryTable);
        memoryScrollPane.setBorder(BorderFactory.createTitledBorder("Memory View"));
        dataPanel.add(memoryScrollPane, BorderLayout.CENTER);

        rightContainer.add(dataPanel, BorderLayout.CENTER);
        return rightContainer;
    }

}
