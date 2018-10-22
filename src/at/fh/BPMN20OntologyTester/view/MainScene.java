package at.fh.BPMN20OntologyTester.view;

import java.io.IOException;

import at.fh.BPMN20OntologyTester.BPMN20OntologyTester;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainScene extends Application {
	
	//Represents the scene of this Screen
	private Scene scene = null;
	
	@Override
	public void init() {
		//Path to FXML which represents the scene
		String fxml = "/jfx/MainScene.fxml";
 
		try {		
			System.out.println(BPMN20OntologyTester.class);
			//Try to load scene from fxml
			scene = new Scene( FXMLLoader.load(getClass().getResource(fxml)));
			scene.getStylesheets().clear();
			scene.getStylesheets().add("/jfx/OntologyTesterfx.css");
			
		} catch (IOException e) {
			System.out.println("Error while loading Scene <" +fxml + ">");
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage stage) {
		
		stage.setTitle("BPMN2.0 Ontology Tester");
		stage.setResizable(false);
		
		BPMN20OntologyTester.setRootStage(stage);
		BPMN20OntologyTester.showScene(scene, stage);		
	}

	@Override
	public void stop() {
	}
}
