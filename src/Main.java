import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        Fianco fianco = new Fianco();
        GameController controller = new GameController(fianco);

        SwingUtilities.invokeLater(() -> {
            BoardGUI app = new BoardGUI(controller, fianco.getBoardState());
            app.setVisible(true);
        });
        System.out.println("This works.");
    }
}