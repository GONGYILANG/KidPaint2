import javafx.application.Application;
import javafx.stage.Stage;

public class KidPaint2 extends Application {
    final static String title = "KidPaint 2.0";
    @Override
    public void start(Stage stage) throws Exception {
        try {
            GetNameDialog dialog = new GetNameDialog(title);
            stage.setTitle(title);
            String username = dialog.getPlayername();
            MainWindow mainWindow = new MainWindow(stage, username);
//            mainWindow.username = dialog.getPlayername();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
