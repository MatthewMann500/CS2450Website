import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;


public class website extends Application {

    
    public static void main(String[] args) {
        launch(args);
    }
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    int width = gd.getDisplayMode().getWidth();
    int height = gd.getDisplayMode().getHeight();
    @Override
    public void start(Stage primaryStage) {
        
        primaryStage.setTitle("Simple JavaFX App");

        Button btn = new Button();
        btn.setText("Click me!");
        btn.setOnAction(e -> handleButtonClick());

        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, width, height));

        primaryStage.show();
    }

    private void handleButtonClick() {
        System.out.println("Button clicked!");
    }
}
