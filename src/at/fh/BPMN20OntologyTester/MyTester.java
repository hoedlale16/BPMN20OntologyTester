/**
 * 
 */
package at.fh.BPMN20OntologyTester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.camunda.bpm.model.xml.instance.DomElement;
import org.semanticweb.owlapi.model.OWLClass;

import com.sun.javafx.application.LauncherImpl;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.OWLTester;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.controller.Owl2BPMNMapper;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This is a not producitve main to test implementations in a fast way without loading a gUI and stuff
 * 
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class MyTester  extends Application {
	private static Scene owl2bpmnMappingDialogScene;
	
	private static BPMNModel createBPMNModel() throws Exception {
		File file = File.createTempFile("model", null);
		file.deleteOnExit();
		
		InputStream stream = new MyTester().getClass().getResourceAsStream("/resource/bpmn/ExampleProcessModel.bpmn");
		
		Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);	
		
		return BPMNModelHandler.readModelFromFile(file);
	}
	
	
	private static void showRestrictionsOfElement(String elementName, OWLModel ontology) throws Exception {
		System.out.println("All Restrictions of Class <"+elementName+">");
		
		OWLClass owlClass = ontology.getOWLClassByShortNameIgnoreCase(elementName);
		for(OWLClassRestriction r : ontology.getAllOWLClassRestrictionOfOWLClass(owlClass, true)) {
			System.out.println(r.toFormattedToString());

		}
		System.out.println("Done");
	}
	
	
	private static void testRestrictionofElement(DomElement element, BPMNModel model, OWLModel ontology) throws Exception {
		System.out.println("Test restrictions of Class <"+element.getLocalName()+">");
		//Get OWL2XML-Naming Mappig
		Owl2BPMNMapper owl2bpmnMapper = Owl2BPMNMapper.getInstance();
		
		Set<FailedOWLClassRestriction> failedRestrictions = OWLTester.testBPMNElementMeetOWLRestrictions(element, ontology,owl2bpmnMapper,false);
		//Just output for Elements which have failed restrictions
		if( failedRestrictions.size() > 0) {
			OWLClass owlClass = ontology.getOWLClassByShortNameIgnoreCase(element.getLocalName());
			int totalRestrictions = ontology.getAllOWLClassRestrictionOfOWLClass(owlClass, true).size();
			
			System.out.println("  - Total Restrictions:  " + totalRestrictions);
			System.out.println("  - Failed Restrictions: " + failedRestrictions.size());
		}
	}
	
	private static void generateNewMappingFile(OWLModel ontology) throws Exception {

		Owl2BPMNMapper.getInstance().generateNewMappingFile(ontology);
		File mappingFile = new File("C:/Users/Alexander/Desktop/OWL2XMLmapping.properties");
		Owl2BPMNMapper.getInstance().save(mappingFile);
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		try {
			System.out.println("---- Start -----");	
			//Initialize Ontology
			OntologyHandler owlHandler = OntologyHandler.getInstance();
			owlHandler.loadOntology(MyTester.class.getResourceAsStream("/resource/owl/BPMN20.owl"));
			OWLModel ontology = OntologyHandler.getInstance().getLoadedOntology().get();
			
			//Initialize OWL2XML Mapping
			Owl2BPMNMapper owl2xmlMapper = Owl2BPMNMapper.getInstance();
			owl2xmlMapper.loadMappingFromStream(MyTester.class.getResourceAsStream("/resource/owl/OWL2BPMNmapping.properties"));
			
			//Initialize Model
			BPMNModel model = createBPMNModel();
			
			// Start the GUI - Ontology get initialized when OntologyTabFxController get
			// initialized
			LauncherImpl.launchApplication(MyTester.class, args);

			
			
			System.out.println("---- Done -----");	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	
	@Override
	public void init() {
		try {
			owl2bpmnMappingDialogScene = loadScene("/resource/jfx/OWL2BPMNMappingDialog.fxml",null);
			
		} catch (Exception e) {
			System.out.println("Error initializing Application: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static Scene loadScene(String fxml, FxController controller) throws IOException {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader();
			if (controller != null) {
				fxmlLoader.setController(controller);
			}

			// Create Scene from given fXML path file
			fxmlLoader.setLocation(BPMN20OntologyTester.class.getResource(fxml));
			Scene scene = new Scene(fxmlLoader.load());
			// Set CSS
			scene.getStylesheets().clear();
			scene.getStylesheets().add("/resource/jfx/OntologyTesterfx.css");
			return scene;

		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Error while loading Scene <" + fxml + ">");
		}
	}

	/* (non-Javadoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage dialog) throws Exception {
		// TODO Auto-generated method stub
		
		dialog.initStyle(StageStyle.UTILITY);
		dialog.setTitle("Mapping OWL 2 BPMN-XML");
		dialog.setResizable(false);

		// Set Icon and Display stage...
		dialog.setScene(owl2bpmnMappingDialogScene);
		dialog.getIcons().add(new Image(BPMN20OntologyTester.class.getResource("/resource/pics/logo.jpg").toString()));

		dialog.show();
		
	}

}
