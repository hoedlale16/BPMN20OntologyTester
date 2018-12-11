/**
 * 
 */
package at.fh.BPMN20OntologyTester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.FileUtils;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.controller.Owl2BpmnNamingMapper;
import at.fh.BPMN20OntologyTester.controller.OwlConformanceClassHandler;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This is a not producitve main to test implementations in a fast way without
 * loading a gUI and stuff
 * 
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
@SuppressWarnings("restriction")
public class MyTester extends Application {
	private static Scene owl2bpmnMappingDialogScene;

	private static BPMNModel createBPMNModel() throws Exception {
		File file = File.createTempFile("model", null);
		file.deleteOnExit();

		InputStream stream = new MyTester().getClass().getResourceAsStream("/resource/bpmn/ExampleProcessModel1.bpmn");

		Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

		return BPMNModelHandler.readModelFromFile(file);
	}

	private static List<String> execSPARQLselect(OWLModel ontology, String queryString) {
		
		List<String> sparqlResults = new ArrayList<String>();
		QueryExecution qexec = null;
		try {
			//Create Model from given Ontology
			Model model = ModelFactory.createOntologyModel();
			model.read(new FileInputStream(ontology.getFileCreatedFrom()),FileUtils.langXML);
			
			
			//Build Query and execute it
			StringBuilder sb = new StringBuilder();
			sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>").append("\r\n")
			  .append("PREFIX ns: <http://www.reiter.at/ontology/bpmn2.0#>").append("\r\n")		  
			  .append("PREFIX owl: <http://www.w3.org/2002/07/owl#>").append("\r\n")			  
			  .append(queryString);
					
			System.out.println("Execute Query: \n" + sb.toString());
			Query query = QueryFactory.create(sb.toString());
			qexec = QueryExecutionFactory.create(query, model);
			ResultSet resultSet = qexec.execSelect();
		
			//Collect Results of Query
			while (resultSet.hasNext()) {
				QuerySolution soln = resultSet.nextSolution();
	
				
				// Get a result variable by name.
				RDFNode n = soln.get(soln.varNames().next()); 
				if (n.isLiteral())
					sparqlResults.add(n.asLiteral().getLexicalForm());
	
				if (n.isResource()) {
					if (n.isURIResource())
						sparqlResults.add(n.asResource().getURI());
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (qexec != null) {
				qexec.close();
			}
		}
		return sparqlResults;
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		try {
			System.out.println("---- Start -----");
			// Initialize Ontology
			OntologyHandler owlHandler = OntologyHandler.getInstance();
			owlHandler.loadOntology(MyTester.class.getResourceAsStream("/resource/owl/BPMN20.owl"));
			OWLModel ontology = OntologyHandler.getInstance().getLoadedOntology().get();

			// Initialize OWL2XML Mapping
			Owl2BpmnNamingMapper owl2xmlMapper = Owl2BpmnNamingMapper.getInstance();
			owl2xmlMapper.loadMappingFromStream(
					MyTester.class.getResourceAsStream("/resource/owl/OWL2BPMNmapping.properties"));

			// Initialize ConformanceClassMapping
			OwlConformanceClassHandler ccHandler = OwlConformanceClassHandler.getInstance();
			ccHandler.load(MyTester.class.getResourceAsStream("/resource/owl/OWLconformanceClasses.properties"));

			// Initialize Model
			BPMNModel model = createBPMNModel();

			
			//Test SPARQL
			System.out.println("Execute SPARQL-Query");
			
			//SparqlQuery
			String testedClass = "Activity";
			String getAllInheritedClassesOf           = "SELECT ?superClass WHERE { ns:" + testedClass + 
																			      " rdfs:subClassOf ?superClass . }";
			String getRestrictionAffectedPropertiesOf = "SELECT ?onProperty WHERE { ns:" + testedClass
																				  + " rdfs:subClassOf ?subClasses . "
																				  + " ?subClasses (owl:Restriction)* ?restriction . "
																				  + " ?restriction owl:onProperty ?onProperty . }";
			for(String s: execSPARQLselect(ontology,getRestrictionAffectedPropertiesOf)) {
				System.out.println(s);
			}
			
			
			// Start the GUI - Ontology get initialized when OntologyTabFxController get
			// initialized
			// LauncherImpl.launchApplication(MyTester.class, args);

			/*Map<OWLConformanceClassEnum, List<BPMNElement>> confClasses = OWLTester
					.getConformanceClassOfElements(ontology, model, owl2xmlMapper);

			System.out.println("Model conformance class: " + ccHandler.getHighestConformanceClass(confClasses));
			for (OWLConformanceClassEnum confClass : confClasses.keySet()) {
				System.out.println(confClass + ": " + confClasses.get(confClass));
			}*/

			System.out.println("---- Done -----");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void init() {
		try {
			owl2bpmnMappingDialogScene = loadScene("/resource/jfx/DialogOwl2BpmnMapping.fxml", null);

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

	/*
	 * (non-Javadoc)
	 * 
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
