public class BoardController {
    private Fianco fianco;
    private BoardGUI gui;

    private int activePlayer;

    public void switchActivePlayer() {
        if (activePlayer == 1) {
            activePlayer = 2;
        } else if (activePlayer == 2) {
            activePlayer = 1;
        } else {
            System.out.println("There should not be anything else than Player 1 or 2.");
        }
    }

    public BoardController(Fianco fianco) {
        this.fianco = fianco;
    }

    public boolean Move(int from_row, int from_col, int to_row, int to_col) {
        return fianco.Move(new Fianco.MoveCommand(from_row, from_col, to_row, to_col, activePlayer));
    }


}
