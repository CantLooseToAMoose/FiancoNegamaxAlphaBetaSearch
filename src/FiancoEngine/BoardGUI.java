package FiancoEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardGUI extends JFrame implements LogListener {
    private static final int BOARD_SIZE = 9;  // 9x9 board
    private static final int TILE_SIZE = 80;  // Size of each tile

    private JPanel boardPanel;
    private JPanel sidePanel;


    // Variables to track selected piece
    private Tile selectedTile = null;

    private GameController controller;

    // Player information
    private JLabel playerOneLabel;
    private JLabel playerOneTypeLabel;
    private JLabel playerTwoLabel;
    private JLabel playerTwoTypeLabel;

    // FiancoEngine.Logger for displaying events
    private JTextArea loggerTextArea;
    private Logger logger;

    public BoardGUI(GameController controller, int[][] boardState) {
        this.controller = controller;
        setTitle("FiancoEngine.Fianco");
        setSize((BOARD_SIZE * TILE_SIZE) + 230, (BOARD_SIZE * TILE_SIZE) + 30); // Adding space for the side panels
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize and add the board panel
        boardPanel = createBoardPanel(boardState);
        add(boardPanel, BorderLayout.CENTER);
        boolean isPlayerOneAI = false;
        boolean isPlayerTwoAI = false;

        // Initialize and add the right panel
        sidePanel = createRightPanel(isPlayerOneAI, isPlayerTwoAI);
        add(sidePanel, BorderLayout.EAST);


        //subscribe to logger
        Logger.getInstance().registerListener(this);
    }

    private JPanel createBoardPanel(int[][] boardState) {
        JPanel containerPanel=new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        panel.setPreferredSize(new Dimension(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE));
        for (int row = BOARD_SIZE - 1; row >= 0; row--) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Tile tile = new Tile(row, col);
                if (boardState[row][col] != 0) {
                    RoundPiece piece = new RoundPiece(boardState[row][col], tile);
                    tile.AddPiece(piece);
                }
                panel.add(tile);
            }
        }
        containerPanel.add(panel,BorderLayout.CENTER);
        // Create Board Annotations
        JPanel leftPanel = createLeftPanel();
        containerPanel.add(leftPanel, BorderLayout.WEST);

        JPanel bottomPanel = createBottomPanel();
        containerPanel.add(bottomPanel, BorderLayout.SOUTH);
        return containerPanel;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridLayout(BOARD_SIZE, 1));
        panel.setPreferredSize(new Dimension(30, BOARD_SIZE * TILE_SIZE)); // Set width for numbering

        for (int row = BOARD_SIZE; row > 0; row--) {
            JLabel numberLabel = new JLabel(String.valueOf(row), SwingConstants.CENTER);
            numberLabel.setPreferredSize(new Dimension(30, TILE_SIZE));
            panel.add(numberLabel);
        }

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new GridLayout(1, BOARD_SIZE));
        panel.setPreferredSize(new Dimension(BOARD_SIZE * TILE_SIZE, 30)); // Set height for letters

        for (char letter = 'A'; letter <= 'I'; letter++) {
            JLabel letterLabel = new JLabel(String.valueOf(letter), SwingConstants.CENTER);
            letterLabel.setPreferredSize(new Dimension(TILE_SIZE, 30));
            panel.add(letterLabel);
        }

        return panel;
    }

    private JPanel createRightPanel(boolean isPlayerOneAI, boolean isPlayerTwoAI) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(200, (BOARD_SIZE * TILE_SIZE) + 30));

        // Player One Info
        playerOneLabel = new JLabel("Player 1");
        playerOneLabel.setFont(new Font("Arial", Font.BOLD, 16));
        playerOneTypeLabel = new JLabel(isPlayerOneAI ? "AI" : "Human");
        panel.add(playerOneLabel);
        panel.add(playerOneTypeLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));  // Space between players

        // Player Two Info
        playerTwoLabel = new JLabel("Player 2");
        playerTwoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        playerTwoTypeLabel = new JLabel(isPlayerTwoAI ? "AI" : "Human");
        panel.add(playerTwoLabel);
        panel.add(playerTwoTypeLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 40)));  // Space between players and buttons

        // Start Button
        JButton continueButton = new JButton("Continue");
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueButton.addActionListener(e -> handleContinue());
        panel.add(continueButton);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));  // Space between buttons

        // Restart Button
        JButton restartButton = new JButton("Start/Restart");
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartButton.addActionListener(e -> handleRestart());
        panel.add(restartButton);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));  // Space between buttons

        // Undo Button
        JButton undoButton = new JButton("Undo");
        undoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        undoButton.addActionListener(e -> handleUndo());
        panel.add(undoButton);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));  // Space between buttons and logger

        // FiancoEngine.Logger Text Area
        loggerTextArea = new JTextArea(8, 15);  // 8 rows, 15 columns
        loggerTextArea.setEditable(false);  // Make the logger read-only
        JScrollPane scrollPane = new JScrollPane(loggerTextArea);  // Add scroll if needed
        panel.add(scrollPane);

        return panel;
    }


    private void handleContinue() {

        controller.continueGame();
        redrawBoard(controller.getBoardState());
        Logger.getInstance().log("Continue Game!");
    }

    private void handleRestart() {
        resetLogMessage();
        Logger.getInstance().log("Game Started/Restarted!");
        controller.restartGame();
        redrawBoard(controller.getBoardState());
    }


    private void handleUndo() {
        // Call your game controller's undo logic here
        controller.undo();
        redrawBoard(controller.getBoardState());  // Redraw the updated board
    }

    public synchronized void redrawBoard(int[][] boardState) {
        // Remove the old board panel and add the new one
        getContentPane().remove(boardPanel);
        boardPanel = createBoardPanel(boardState);
        add(boardPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Methods to update the AI/Human labels for players
    public void setPlayerOneTypeLabel(boolean isAI) {
        playerOneTypeLabel.setText(isAI ? "AI" : "Human");
        revalidate();
        repaint();
    }

    public void setPlayerTwoTypeLabel(boolean isAI) {
        playerTwoTypeLabel.setText(isAI ? "AI" : "Human");
        revalidate();
        repaint();
    }

    // Method to log events to the GUI
    private void showLogEvent(String event) {
        loggerTextArea.append(event + "\n");
        loggerTextArea.setCaretPosition(loggerTextArea.getDocument().getLength());  // Auto-scroll to the bottom
    }

    private void resetLogMessage() {
        loggerTextArea.setText("");
        loggerTextArea.setCaretPosition(loggerTextArea.getDocument().getLength());
    }

    @Override
    public void onLogEvent(String logMessage) {
        showLogEvent(logMessage);
    }

    private static class RoundPiece extends JPanel {
        private int playerColor;  // 0 = empty, 1 = white, 2 = black
        private Tile parentTile;

        public RoundPiece(int playerColor, Tile tile) {
            this.playerColor = playerColor;
            this.parentTile = tile;
            setOpaque(false); // Make the panel transparent
            setPreferredSize(new Dimension(TILE_SIZE - 10, TILE_SIZE - 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Set the color for the piece (white or black)
            if (playerColor == 1) {
                g.setColor(Color.WHITE);  // White piece
            } else if (playerColor == 2) {
                g.setColor(Color.BLACK);  // Black piece
            } else {
                return; // No piece to draw
            }

            // Draw the filled oval representing the piece
            g.fillOval(0, 0, getWidth(), getHeight());

            // Draw the black outline around the piece
            g.setColor(Color.BLACK);
            g.drawOval(0, 0, getWidth(), getHeight());
        }


        public int getPlayerColor() {
            return playerColor;
        }

        public void setPlayerColor(int playerColor) {
            this.playerColor = playerColor;
            repaint(); // Ensure the piece is repainted if its type changes
        }

        public void setParentTile(Tile tile) {
            this.parentTile = tile;
        }
    }

    private class Tile extends JPanel {
        private int row;
        private int col;
        private RoundPiece piece;

        public Tile(int row, int col) {
            this.row = row;
            this.col = col;
            setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
            setBackground((row + col) % 2 == 0 ? new Color(255, 255, 153) : new Color(153, 76, 0));

            // Add mouse listener to handle piece selection and movement
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleMouseClick();
                }
            });
        }

        public boolean AddPiece(RoundPiece piece) {
            if (this.piece != null) {
                return false;
            }
            this.piece = piece;
            this.add(piece);
            piece.setParentTile(this);
            revalidate();
            repaint();
            return true;
        }

        public boolean RemovePiece() {
            if (this.piece == null) {
                return false;
            }
            this.remove(this.piece);
            this.piece = null;
            revalidate();
            repaint();
            return true;
        }

        private void handleMouseClick() {
            if (selectedTile == null && this.piece != null) {
                // Select the piece on this tile
                selectedTile = this;
                setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
            } else if (selectedTile != null) {
                if (this.piece == null) {
                    // Move the piece to this empty tile
                    MoveAPiece(selectedTile, this);
                    // Deselect the tile
                    selectedTile.setBorder(null);
                    selectedTile = null;
                } else {
                    // If a piece is already selected and another piece is clicked, just deselect
                    selectedTile.setBorder(null);
                    selectedTile = null;
                }
            }
        }
    }

    public void MoveAPiece(Tile fromTile, Tile toTile) {
        if (controller.move(fromTile.row, fromTile.col, toTile.row, toTile.col)) {
            redrawBoard(controller.getBoardState());
        }
    }
}
