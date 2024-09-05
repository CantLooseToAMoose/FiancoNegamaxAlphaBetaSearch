import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        Fianco fianco = new Fianco();
        BoardController controller = new BoardController(fianco);

        SwingUtilities.invokeLater(() -> {
            BoardGUI app = new BoardGUI(controller, fianco.getBoardState());
            app.setVisible(true);
        });
    }
}