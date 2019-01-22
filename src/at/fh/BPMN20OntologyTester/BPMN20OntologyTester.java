package at.fh.BPMN20OntologyTester;

import java.io.IOException;

import com.sun.javafx.application.LauncherImpl;

import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.controller.XmlElement2OWLClassesMapper;
import at.fh.BPMN20OntologyTester.controller.OwlConformanceClassHandler;
import at.fh.BPMN20OntologyTester.controller.XmlAttribute2OWLPropertyMapper;
import at.fh.BPMN20OntologyTester.model.TestCase;
import at.fh.BPMN20OntologyTester.view.OwlTestSuiteTabTcResultFxController;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Main Class to start Application. Initialize all GUI-Elements.
 * 
 * @author Alexander Hödl IMA16 - Information Management University of applied
 *         Sciences FH JOANNEUM
 *
 */
@SuppressWarnings("restriction")
public class BPMN20OntologyTester extends Application {

	// The main scene of the Application where all other GUIs are added as Tab
	private static Scene mainScene;
	private static Scene owl2bpmnMappingDialogScene;
	
	public static Stage getOWL2BPMNMappingDialog() {
		
		Stage dialog = new Stage();
		dialog.initStyle(StageStyle.UTILITY);
		dialog.setTitle("Mapping OWL 2 BPMN-XML");
		dialog.setScene(owl2bpmnMappingDialogScene);
		dialog.setResizable(false);

		// Set Icon and Display stage...
		dialog.getIcons().add(new Image(BPMN20OntologyTester.class.getResource("/resource/appData/logo.jpg").toString()));

		return dialog;
	}
	
	public static Stage getLoadingScreenWhileTask(Task<?> task) {
		ProgressBar pBar = new ProgressBar();
		pBar.progressProperty().bind(task.progressProperty());
		pBar.setMinWidth(400);
		Label statusLabel = new Label();
		statusLabel.textProperty().bind(task.messageProperty());
		statusLabel.setTextAlignment(TextAlignment.CENTER);
		statusLabel.setAlignment(Pos.CENTER);
		BorderPane root = new BorderPane(pBar,statusLabel,null,null,null);
		
		Stage loadingStage = new Stage();
		loadingStage.setScene(new Scene(root));
		loadingStage.initStyle(StageStyle.UNDECORATED);
		loadingStage.setResizable(false);
		loadingStage.setTitle("BPMN2.0 Ontology Tester");
		loadingStage.setMinWidth(400);
		loadingStage.setMinHeight(50);
		
		return loadingStage;
	}

	
	
	public static Tab getTestSuiteTestResultTab(TestCase testcase) {
		try {
			Tab tab = new Tab();
			tab.setText(testcase.getFileNameOfProcessMOdelCreatedOf());
			OwlTestSuiteTabTcResultFxController controller = new OwlTestSuiteTabTcResultFxController(testcase);
			Scene scene = loadScene("/resource/jfx/TabOwlTestsuiteTabTCResult.fxml",controller);		
			tab.setContent(scene.getRoot());		
			return tab;
		} catch (Exception e) {
			//Sollte in Produktionsbetrieb nie passieren, daher nur auf die Console im Fall des Falls
			System.out.println("Error initializing Application: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void init() {
		try {
			// Read and initialize Ontology - If an error occurred here just continue
			// building the GUI
			intitalizeAppData("/resource/owl/BPMN20_v1.0.2.owl", 
							  "/resource/owl/XmlAttributes2OWLPropertiesMapping.properties",
							  "/resource/owl/XmlElements2OWLClassesMapping.properties",
							  "/resource/owl/OWLconformanceClasses.properties");

			// Load GUI- Scenes for from FXML, create a new tabs and add to tabPane of
			// mainScene
			mainScene = loadScene("/resource/jfx/MainScene.fxml", null);
			addNewTab("Ontology", loadScene("/resource/jfx/TabOntology.fxml", null), mainScene);	
			addNewTab("Compare BPMN", loadScene("/resource/jfx/TabCompareBpmn2Owl.fxml", null), mainScene);
			addNewTab("OWL Testsuite", loadScene("/resource/jfx/TabOwlTestSuite.fxml", null), mainScene);
			addNewTab("Knowledge-DB", loadScene("/resource/jfx/TabOwlSparqlExecution.fxml", null), mainScene);
			
			//Load additional dialogs
			owl2bpmnMappingDialogScene = loadScene("/resource/jfx/DialogOwl2BpmnMapping.fxml",null);
			
		} catch (Exception e) {
			System.out.println("Error initializing Application: " + e.getMessage());
		}
	}

	private void intitalizeAppData(String ontologyResourcePath, 
								  String xmlAttribute2owlPropertiesMappingPath,
								  String xmlElements2owlClassesMappingPath, String conformanceClassMappingPath) {
		try {
			// Load Ontology from given resourcePath
			OntologyHandler owlHandler = OntologyHandler.getInstance();
			owlHandler.loadOntology(getClass().getResourceAsStream(ontologyResourcePath));
			if (!owlHandler.getLoadedOntology().isPresent())
				throw new Exception("Error! - Unable to read Ontology from <" + ontologyResourcePath + ">");

			// Load OWL2XML Mapping files from given resourcePath
			XmlElement2OWLClassesMapper xmlElem2owlClassMapper = XmlElement2OWLClassesMapper.getInstance();
			XmlAttribute2OWLPropertyMapper xmlAttr2owlPropMapper = XmlAttribute2OWLPropertyMapper.getInstance();		
			xmlElem2owlClassMapper.loadMappingFromStream(getClass().getResourceAsStream(xmlElements2owlClassesMappingPath));
			xmlAttr2owlPropMapper.loadMappingFromStream(getClass().getResourceAsStream(xmlAttribute2owlPropertiesMappingPath));
	
			//Load Conformance Class mapping
			OwlConformanceClassHandler confClassMapper = OwlConformanceClassHandler.getInstance();
			confClassMapper.load(getClass().getResourceAsStream(conformanceClassMappingPath));
			if (!confClassMapper.hasMappings())
				throw new Exception(
						"Warning! - Unable to read conformance Classes Mapping file from <" + xmlElements2owlClassesMappingPath + ">");

		} catch (Exception e) {
			// Just continue, and print message to console.
			System.out.println(e.getMessage());
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
	private static Scene loadScene(String fxml, FxController controller) throws IOException {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader();
			if (controller != null) {
				fxmlLoader.setController(controller);
			}

			// Create Scene from given fXML path file
			fxmlLoader.setLocation(BPMN20OntologyTester.class.getResource(fxml));
			Scene scene = new Scene(fxmlLoader.load());
			
			//Store Controller to have access to it later
			scene.setUserData(fxmlLoader.getController());
			
			// Set CSS
			scene.getStylesheets().clear();
			scene.getStylesheets().add("/resource/jfx/OntologyTesterfx.css");
			return scene;

		} catch (Exception e) {
			e.printStackTrace();
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

		stage.getIcons().add(new Image(getClass().getResource("/resource/appData/logo.jpg").toString()));

		stage.setScene(mainScene);
		stage.show();
	}

	@Override
	public void stop() {
	}

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
