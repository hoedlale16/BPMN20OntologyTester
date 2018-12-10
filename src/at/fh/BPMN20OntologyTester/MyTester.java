/**
 * 
 */
package at.fh.BPMN20OntologyTester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.sun.javafx.application.LauncherImpl;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.OWLTester;
import at.fh.BPMN20OntologyTester.controller.Owl2BpmnNamingMapper;
import at.fh.BPMN20OntologyTester.controller.OwlConformanceClassHandler;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNElement;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import at.fh.BPMN20OntologyTester.model.enums.OWLConformanceClassEnum;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.avicomp.ontapi.OntologyModel;

/**
 * This is a not producitve main to test implementations in a fast way without loading a gUI and stuff
 * 
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
@SuppressWarnings("restriction")
public class MyTester  extends Application {
	private static Scene owl2bpmnMappingDialogScene;
	
	private static BPMNModel createBPMNModel() throws Exception {
		File file = File.createTempFile("model", null);
		file.deleteOnExit();
		
		InputStream stream = new MyTester().getClass().getResourceAsStream("/resource/bpmn/ExampleProcessModel1.bpmn");
		
		Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);	
		
		return BPMNModelHandler.readModelFromFile(file);
	}
	
	private void testSparql(OWLModel ontology, String queryString) {
		Query query = QueryFactory.create(queryString) ;
		 Model model = ((OntologyModel)ontology).asGraphModel();
		//Model model = ((OntologyModel)ontology.getOntology());
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
		    ResultSet results = qexec.execSelect() ;
		    while (results.hasNext())
		    {
		      QuerySolution soln = results.nextSolution() ;
		      RDFNode x = soln.get("varName") ;       // Get a result variable by name.
		      Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
		      Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal

		    }
		  }
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
			Owl2BpmnNamingMapper owl2xmlMapper = Owl2BpmnNamingMapper.getInstance();
			owl2xmlMapper.loadMappingFromStream(MyTester.class.getResourceAsStream("/resource/owl/OWL2BPMNmapping.properties"));
			
			
			//Initialize ConformanceClassMapping
			OwlConformanceClassHandler ccHandler = OwlConformanceClassHandler.getInstance();
			ccHandler.load(MyTester.class.getResourceAsStream("/resource/owl/OWLconformanceClasses.properties"));
			
			//Initialize Model
			BPMNModel model = createBPMNModel();
			
			// Start the GUI - Ontology get initialized when OntologyTabFxController get
			// initialized
			//LauncherImpl.launchApplication(MyTester.class, args);
			
			
			Map<OWLConformanceClassEnum, List<BPMNElement>> confClasses = OWLTester.getConformanceClassOfElements(ontology, model, owl2xmlMapper);
			
			System.out.println("Model conformance class: " + ccHandler.getHighestConformanceClass(confClasses));
			for(OWLConformanceClassEnum confClass: confClasses.keySet()) {
				System.out.println(confClass +": " + confClasses.get(confClass));
			}

			
			
			System.out.println("---- Done -----");	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	
	@Override
	public void init() {
		try {
			owl2bpmnMappingDialogScene = loadScene("/resource/jfx/DialogOwl2BpmnMapping.fxml",null);
			
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
