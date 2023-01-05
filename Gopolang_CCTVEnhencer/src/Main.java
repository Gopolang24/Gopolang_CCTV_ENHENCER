import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		clientPane root = new clientPane(primaryStage);
		primaryStage.setScene(new Scene(root, 390, 190));
		///Set title
		primaryStage.setTitle("Iron Lock CCTV Enhancer");
		///Show stage
		primaryStage.show();
	}

}
