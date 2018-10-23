package at.fh.BPMN20OntologyTester;

import com.sun.javafx.application.LauncherImpl;

import at.fh.BPMN20OntologyTester.view.MainScene;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Hello world!
 *
 */
public class BPMN20OntologyTester 
{
	private static Stage rootStage = null;
	
	//Define all possible GUI-Screens here to switch between them
	private static Scene mainScene;
	
	public static void setRootStage(Stage stage) {
		rootStage = stage;
	}
	
	public static Stage getRootStage() {
		return rootStage;
	}
	
	public static Scene getMainScene() {
		return mainScene;
	}
	
	public static void showScene(Scene scene, Stage stage) {
		if(stage == null) {
			System.out.println("Error - Window(Stage) not set!");
			System.exit(1);
		}
		else {
			stage.setScene(scene);
			stage.show();
			
		}
			 
	}
	
	
    @SuppressWarnings("restriction")
	public static void main( String[] args )
    {
    	try {
    		//Start the GUI which loads and initializes Ontology
        	LauncherImpl.launchApplication(MainScene.class, args);
    	}
    	catch (Exception e)
    	{
    		//If we reach this part, some crazy shit is going on because GUI was unable to start!
    		System.out.println("Error occured: " +e.getMessage());
    		e.printStackTrace();
    	}
    	
    }
}
