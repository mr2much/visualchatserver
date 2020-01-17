import java.io.IOException;

import com.lockward.controller.ConnectionController;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

	private Scene mainScene;
	private String serverStatus = "offline";
	private ConnectionController connectionController = new ConnectionController();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(15, 15, 15, 15));
		grid.setAlignment(Pos.CENTER);

		stage.setTitle("Server - " + serverStatus);

		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);

		Button btnConnect = new Button("Connect");

		btnConnect.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				try {
					connectionController.connect();
					connectionController.process();

					checkConnectionStatus();
				} catch (IOException ex) {
					updateWindowTitle(ex.getMessage());
				}
			}
		});

		Button btnDisconnect = new Button("Disconnect");

		btnDisconnect.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				try {
					connectionController.close();
					checkConnectionStatus();
				} catch (IOException ex) {
					updateWindowTitle(ex.getMessage());
				}
			}

		});

		vbox.getChildren().addAll(btnConnect, btnDisconnect);
		grid.add(vbox, 0, 0);

		Scene scene = new Scene(grid, 350, 250);
		mainScene = scene;

		stage.setScene(scene);
		stage.show();
	}

	private void checkConnectionStatus() {
		if (connectionController.isClosed()) {
			serverStatus = "offline";
		} else {
			serverStatus = "online";
		}

		updateWindowTitle(serverStatus);
	}

	private void updateWindowTitle(String message) {
		Stage stage = (Stage) mainScene.getWindow();
		stage.setTitle("Server - " + message);
	}

}
