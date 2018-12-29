/**
 * 
 */
package at.fh.BPMN20OntologyTester.view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import at.fh.BPMN20OntologyTester.BPMN20OntologyTester;
import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import at.fh.BPMN20OntologyTester.model.OwlSparqlQueryFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
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
			String query = OwlSparqlQueryFactory.getAttributesWithRestrictions(selectedClass);
			taSparqlQuery.setText(query);
		} else {
			appendLog("No class selected! Select one from ComboBox");
		}

	}

	@FXML
	private void onLoadQueryAllInheritedClasses() {
		String selectedClass = cbClasses.getSelectionModel().getSelectedItem();
		if (selectedClass != null && !selectedClass.isEmpty()) {
			String query = OwlSparqlQueryFactory.getAllInhertiedClasses(selectedClass);

			taSparqlQuery.setText(query);
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
			Task<Map<String,List<String>>> executionTask = new Task<Map<String,List<String>>>() {
				@Override
				public Map<String,List<String>> call() throws Exception {
					this.updateMessage("Execute query");
					return execSPARQLselect(ontology, query);
				}
			};
	
			// Show loading screen while running
			Stage loadingScreen = BPMN20OntologyTester.getLoadingScreenWhileTask(executionTask);
			loadingScreen.show();
	
			// Trigger action after test results and tabs are created
			executionTask.setOnSucceeded(e -> {
				taResults.clear();
				
				//Show results
				executionTask.getValue().forEach( (q,r) -> {
					taSparqlQuery.setText(q);

					if(r.isEmpty()) {
						taResults.setText("No results found!");
					} else {
						r.forEach( s-> taResults.appendText(s +"\n"));
					}
				});
				
	
				// Hide Loadingscreen and set overview log
				loadingScreen.hide();
				btExecute.setDisable(false);
			});
	
			executionTask.setOnFailed(e -> {
				appendLog("Error occured while executing query: " + executionTask.getException().getMessage());
	
				
				taResults.setText("Error in Query!");
				
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

	private Map<String,List<String>> execSPARQLselect(OWLModel ontology, String queryString) throws FileNotFoundException {

		Map<String, List<String>> results = new HashMap<String,List<String>>();
		
		QueryExecution qexec = null;
		try {
			// Create Model from given Ontology
			Model model = ModelFactory.createOntologyModel();
			model.read(new FileInputStream(ontology.getFileCreatedFrom()), FileUtils.langXML);
		
			Query query = QueryFactory.create(queryString);
			qexec = QueryExecutionFactory.create(query, model);
			ResultSet resultSet = qexec.execSelect();

			// Collect Results of Query
			List<String> queryResults = new ArrayList<String>();
			while (resultSet.hasNext()) {
				QuerySolution soln = resultSet.nextSolution();
						
				//Collect all variable-results of row and build a single string representing the row
				StringBuilder sb = new StringBuilder();
				Iterator<String> varIterator = soln.varNames();
				while(varIterator.hasNext()) {
					RDFNode n = soln.get(varIterator.next());
					if (n.isLiteral())
						sb.append(n.asLiteral().getLexicalForm());

					if (n.isResource()) {
						if (n.isURIResource())
							sb.append(n.asResource().getURI());
					}	
					
					if(varIterator.hasNext()) {
						sb.append(", ");
					}
				}	
				
				String rowResult = sb.toString();
				if(! rowResult.isEmpty())
					queryResults.add(rowResult);

				
				// Get a result variable by name.
				/*RDFNode n = soln.get(soln.varNames().next());
				if (n.isLiteral())
					queryResults.add(n.asLiteral().getLexicalForm());

				if (n.isResource()) {
					if (n.isURIResource())
						queryResults.add(n.asResource().getURI());
				}*/
			}
			
			//Add result 
			results.put(queryString, queryResults);
		} finally {
			if (qexec != null) {
				qexec.close();
			}
		}
		return results;
	}

	private void appendLog(String text) {
		MainSceneFxController.getInstance().appendLog(text);
	}
}
