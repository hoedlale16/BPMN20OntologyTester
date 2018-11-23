package at.fh.BPMN20OntologyTester;

import java.io.IOException;

import com.sun.javafx.application.LauncherImpl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Main Class to start Application. Initialize all GUI-Elements.
 * 
 * @author Alexander Hödl IMA16 - Information Management University of applied
 *         Sciences FH JOANNEUM
 *
 */
@SuppressWarnings("restriction")
public class BPMN20OntologyTester extends Application {

	// Define all possible GUI-Screens here to switch between them
	private static Scene mainScene;

	public static Scene getMainScene() {
		return mainScene;
	}

	@Override
	public void init() {
		try {
			mainScene = loadScene("/resource/jfx/MainScene.fxml");

			// Load Scenes for the tabs from FXML, create a new tabs and add to tabPane of
			// mainScene
			addNewTab("Ontology", loadScene("/resource/jfx/Ontology.fxml"), mainScene);
			addNewTab("BPMN2OWL", loadScene("/resource/jfx/BPMN2OWL.fxml"), mainScene);
			addNewTab("OWL Tests", loadScene("/resource/jfx/OntologyTests.fxml"), mainScene);
		} catch (IOException e) {
			System.out.println("Error initializing Application: " + e.getMessage());
		}
	}

	/**
	 * Reads FXML-File from given Path and creates a scene out of it.
	 * 
	 * @param fxml
	 *            - path to fxml
	 * @return Scene Object created out of fxml
	 * @throws IOException
	 */
	private Scene loadScene(String fxml) throws IOException {
		try {
			// Try to load scene from fxml
			Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxml)));
			scene.getStylesheets().clear();
			scene.getStylesheets().add("/resource/jfx/OntologyTesterfx.css");
			return scene;

		} catch (IOException e) {
			throw new IOException("Error while loading Scene <" + fxml + ">");
		}
	}

	/**
	 * Helper-Method to create a new tab and add to tabPane in Main-Scene.
	 * 
	 * @param tabName
	 * @param newTabScene
	 * @param mainScene
	 */
	private void addNewTab(String tabName, Scene newTabScene, Scene mainScene) {
		Tab tab = new Tab();
		tab.setText(tabName);
		tab.setContent(newTabScene.getRoot());

		// Attention if you change the fxml. This code can cause a cast exception if the
		// structure does not fit like below:
		// Scene
		// -> BoardPane
		// -> Center: TabPane
		BorderPane mainBorder = (BorderPane) mainScene.getRoot();
		TabPane mainTabPane = (TabPane) mainBorder.getCenter();
		mainTabPane.getTabs().add(tab);
	}

	@Override
	public void start(Stage stage) {

		stage.setTitle("BPMN2.0 Ontology Tester");
		stage.setMaximized(true);
		stage.setMinWidth(1024);
		stage.setMinHeight(768);

		stage.getIcons().add(new Image(getClass().getResource("/resource/pics/logo.jpg").toString()));

		stage.setScene(mainScene);
		stage.show();
	}

	@Override
	public void stop() {
	}

	@SuppressWarnings("restriction")
	public static void main(String[] args) {
		try {
			// Start the GUI - Ontology get initialized when OntologyTabFxController get
			// initialized
			LauncherImpl.launchApplication(BPMN20OntologyTester.class, args);
		} catch (Exception e) {
			// If we reach this part, some crazy shit is going on because GUI was unable to
			// start!
			System.out.println("Error occured: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

	}

}
