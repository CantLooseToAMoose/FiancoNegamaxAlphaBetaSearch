import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardGUI extends JFrame {
    private static final int BOARD_SIZE = 9;  // 9x9 board
    private static final int TILE_SIZE = 80;  // Size of each tile

    // 9x9 board state: 0 = empty, 1 = white piece, 2 = black piece
    private int[][] boardState = new int[BOARD_SIZE][BOARD_SIZE];
    private JPanel boardPanel;

    // Variables to track selected piece
    private Tile selectedTile = null;

    public BoardGUI() {
        setTitle("9x9 Board Game");
        setSize(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize the board state (example: alternating pieces)
        initializeBoardState();

        // Initialize and add the board panel
        boardPanel = createBoardPanel();
        add(boardPanel);

        // Example of changing the board state and redrawing the board
        // This is just for demonstration purposes
        // In a real application, you would call redrawBoard() when the board state changes
        SwingUtilities.invokeLater(() -> {
            redrawBoard();
        });
    }

    private JPanel createBoardPanel() {
        JPanel panel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Tile tile = new Tile(row, col);
                if (boardState[row][col] != 0) {
                    RoundPiece piece = new RoundPiece(boardState[row][col], tile);
                    tile.AddPiece(piece);
                }
                panel.add(tile);
            }
        }
        return panel;
    }

    private void redrawBoard() {
        // Remove the old board panel and add the new one
        getContentPane().remove(boardPanel);
        boardPanel = createBoardPanel();
        add(boardPanel);
        revalidate();
        repaint();
    }

    private void initializeBoardState() {
        boardState = new int[][]{
                {1, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 1, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 2, 0, 0, 0},
                {0, 0, 2, 0, 0, 0, 2, 0, 0},
                {0, 2, 0, 0, 0, 0, 0, 2, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2}};
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
            if (playerColor == 1) {
                g.setColor(Color.WHITE);  // White piece
            } else if (playerColor == 2) {
                g.setColor(Color.BLACK);  // Black piece
            } else {
                return; // No piece to draw
            }
            g.fillOval(0, 0, getWidth(), getHeight());
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
            setBackground((row + col) % 2 == 0 ? Color.LIGHT_GRAY : Color.DARK_GRAY);

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
                System.out.println("Select piece on a Tile");
                // Select the piece on this tile
                selectedTile = this;
                setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
            } else if (selectedTile != null) {
                if (this.piece == null) {
                    System.out.println("Move Piece to different Tile");
                    // Move the piece to this empty tile
                    MoveAPiece(selectedTile, this);
                    // Deselect the tile
                    selectedTile.setBorder(null);
                    selectedTile = null;
                } else {
                    System.out.println("You can not Move a Piece on another Piece");
                    // If a piece is already selected and another piece is clicked, just deselect
                    selectedTile.setBorder(null);
                    selectedTile = null;
                }
            }
        }
    }

    public void MoveAPiece(Tile from, Tile to) {
        RoundPiece movingPiece = from.piece;
        from.RemovePiece();
        to.AddPiece(movingPiece);
        boardState[to.row][to.col] = movingPiece.getPlayerColor(); // Update board state
        boardState[from.row][from.col] = 0; // Clear previous position
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BoardGUI app = new BoardGUI();
            app.setVisible(true);
        });
    }
}
