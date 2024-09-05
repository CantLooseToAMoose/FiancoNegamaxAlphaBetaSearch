public class BoardController {
    private Fianco fianco;
    private BoardGUI gui;

    public BoardController(Fianco fianco) {
        this.fianco = fianco;
    }

    public boolean Move(Fianco.MoveCommand moveCommand) {
        return fianco.Move(moveCommand);
    }


}
