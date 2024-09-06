import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardGUI extends JFrame {
    private static final int BOARD_SIZE = 9;  // 9x9 board
    private static final int TILE_SIZE = 80;  // Size of each tile

    private JPanel boardPanel;

    // Variables to track selected piece
    private Tile selectedTile = null;

    private BoardController controller;

    public BoardGUI(BoardController controller, int[][] boardState) {
        this.controller = controller;
        setTitle("9x9 Board Game");
        setSize(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize and add the board panel
        boardPanel = createBoardPanel(boardState);
        add(boardPanel);

    }

    private JPanel createBoardPanel(int[][] boardState) {
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

    private void redrawBoard(int[][] boardState) {
        // Remove the old board panel and add the new one
        getContentPane().remove(boardPanel);
        boardPanel = createBoardPanel(boardState);
        add(boardPanel);
        revalidate();
        repaint();
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

    public void MoveAPiece(Tile fromTile, Tile toTile) {
        RoundPiece movingPiece = fromTile.piece;
        fromTile.RemovePiece();
        toTile.AddPiece(movingPiece);
        controller.Move(fromTile.row, fromTile.col, toTile.row, toTile.col);
    }
}
