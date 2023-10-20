package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Démarre l'application
 * 
 * @author Christian Jeune
 * @version 1.0
 */
public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("pageDaccueil.fxml"));
			Scene scene = new Scene(root);
			Image image = new Image("/snake.png");
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setTitle("Snake Game");
			primaryStage.getIcons().add(image);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
