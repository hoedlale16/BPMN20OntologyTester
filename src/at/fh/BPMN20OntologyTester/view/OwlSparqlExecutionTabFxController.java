/**
 * 
 */
package at.fh.BPMN20OntologyTester.view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.semanticweb.owlapi.model.OWLClass;

import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class OwlSparqlExecutionTabFxController implements FxController {

	@FXML
	private TextArea taSparqlQuery, taResults;

	@FXML
	private ComboBox<String> cbClasses;

	@FXML
	private Button btInheritedClasses, btAllAttriubtesWithRestrictions, btExecute;

	ObservableList<String> owlClasses = FXCollections.observableArrayList();

	public OwlSparqlExecutionTabFxController() {

	}

	@FXML
	private void initialize() {
		cbClasses.setItems(owlClasses);

		Optional<OWLModel> optOntology = OntologyHandler.getInstance().getLoadedOntology();
		if (optOntology.isPresent()) {
			for (OWLClass c : optOntology.get().getOWLClasses()) {
				owlClasses.add(c.getIRI().getShortForm());
			}
		} else {
			appendLog("Unable to load Ontology from ressource folder. Unable to verify ontology!");
		}
	}

	@FXML
	private void onLoadQueryAllAttributesWithRestrictions() {

		String selectedClass = cbClasses.getSelectionModel().getSelectedItem();
		if (selectedClass != null && !selectedClass.isEmpty()) {
			String attributesWithRestrictions = "SELECT ?onProperty WHERE { ns:" + selectedClass
					+ " rdfs:subClassOf ?subClasses . " + " ?subClasses (owl:Restriction)* ?restriction . "
					+ " ?restriction owl:onProperty ?onProperty . }";

			taSparqlQuery.setText(attributesWithRestrictions);
		} else {
			appendLog("No class selected! Select one from ComboBox");
		}

	}

	@FXML
	private void onLoadQueryAllInheritedClasses() {
		String selectedClass = cbClasses.getSelectionModel().getSelectedItem();
		if (selectedClass != null && !selectedClass.isEmpty()) {
			String allInhertidedClasses = "SELECT ?superClass WHERE { ns:" + selectedClass
					+ " rdfs:subClassOf ?superClass . }";

			taSparqlQuery.setText(allInhertidedClasses);
		} else {
			appendLog("No class selected! Select one from ComboBox");
		}

	}

	@FXML
	private void onExecuteQuery() {
		if (! taSparqlQuery.getText().isEmpty()) {
			// Disable button to avoid multiple triggers
			btExecute.setDisable(true);
			String query = taSparqlQuery.getText();
			OWLModel ontology = OntologyHandler.getInstance().getLoadedOntology().get();
	
			// Use a sepaerate Thread to execute Query
			Task<List<String>> executionTask = new Task<List<String>>() {
				@Override
				public List<String> call() throws Exception {
					this.updateMessage("Execute query");
					return execSPARQLselect(ontology, query);
				}
			};
	
			// Show loading screen while running
			Stage loadingScreen = showLoadingScreenWhileTask(executionTask);
			loadingScreen.show();
	
			// Trigger action after test results and tabs are created
			executionTask.setOnSucceeded(e -> {
				taResults.clear();
				for (String s : executionTask.getValue()) {
					taResults.appendText(s + "\n");
				}
	
				// Hide Loadingscreen and set overview log
				loadingScreen.hide();
				btExecute.setDisable(false);
			});
	
			executionTask.setOnFailed(e -> {
				appendLog("Error occured while executing query: " + executionTask.getException().getMessage());
	
				// Hide Loadingscreen and set overview log
				loadingScreen.hide();
				btExecute.setDisable(false);
			});
	
			// Start thread if there is a query
			new Thread(executionTask).start();
		} else {
			appendLog("No Query written to execute!");
		}
	}

	private List<String> execSPARQLselect(OWLModel ontology, String queryString) throws FileNotFoundException {

		List<String> sparqlResults = new ArrayList<String>();
		QueryExecution qexec = null;
		try {
			// Create Model from given Ontology
			Model model = ModelFactory.createOntologyModel();
			model.read(new FileInputStream(ontology.getFileCreatedFrom()), FileUtils.langXML);

			// Build Query and execute it
			StringBuilder sb = new StringBuilder();
			sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>").append("\r\n")
					.append("PREFIX ns: <http://www.reiter.at/ontology/bpmn2.0#>").append("\r\n")
					.append("PREFIX owl: <http://www.w3.org/2002/07/owl#>").append("\r\n").append(queryString);

			System.out.println("Execute Query: \n" + sb.toString());
			Query query = QueryFactory.create(sb.toString());
			qexec = QueryExecutionFactory.create(query, model);
			ResultSet resultSet = qexec.execSelect();

			// Collect Results of Query
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
		} finally {
			if (qexec != null) {
				qexec.close();
			}
		}
		return sparqlResults;
	}

	/**
	 * Helper Method to create a Loading Screen while given task is running
	 * 
	 * @param task
	 * @return
	 */
	private Stage showLoadingScreenWhileTask(Task<?> task) {
		ProgressBar pBar = new ProgressBar();
		pBar.progressProperty().bind(task.progressProperty());
		pBar.setMinWidth(400);
		Label statusLabel = new Label();
		statusLabel.textProperty().bind(task.messageProperty());
		statusLabel.setTextAlignment(TextAlignment.CENTER);
		statusLabel.setAlignment(Pos.CENTER);
		BorderPane root = new BorderPane(pBar, statusLabel, null, null, null);

		Stage loadingStage = new Stage();
		loadingStage.setScene(new Scene(root));
		loadingStage.setTitle("BPMN2.0 Ontology Tester");
		loadingStage.setMinWidth(400);
		loadingStage.setMinHeight(50);

		return loadingStage;
	}

	private void appendLog(String text) {
		MainSceneFxController.getInstance().appendLog(text);
	}
}
