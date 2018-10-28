package at.fh.BPMN20OntologyTester.view.fxcontroller;

import java.util.Collections;

import org.semanticweb.owlapi.model.OWLEntity;

import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

/**
 * Handels user interactions on tab "Ontology"
 * 
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class OntologyTabFxController {

	// GUI Elements from Ontology Tab
	@FXML
	private Label lbClasses, lbObjProperties, lbDataProperties, lbDescUndocEntities, lbDescDocEntities;
	@FXML
	private ListView<String> lstUnDocumentedEntities;
	@FXML
	private ComboBox<String> cbDocumentedEnties;
	@FXML
	private TextArea taOntDescription;

	// Get initialized on startup of application
	private OWLModel ontology = null;

	public OntologyTabFxController() {
	}

	@FXML
	private void initialize() {
		try {
			appendLog("Read and initialize BPMN2.0 Ontology");
			ontology = OntologyHandler.getInstance().getBpmn20Ontology();

			// Show results on GUI
			showInitializedOntology(ontology);

		} catch (Exception e) {
			appendLog("Error while loading Ontology: " + e.getMessage());
			e.printStackTrace();
		}

	}

	private void appendLog(String text) {
		MainSceneFxController.getInstance().appendLog(text);
	}
	/**
	 * Shows main facts of loaded Ontology on GUI. Called while initializing this
	 * controller with default Ontology BPMN20.owl from resource folder which is not
	 * changeable.
	 */
	private void showInitializedOntology(OWLModel ontology) {
		lbClasses.setText("" + ontology.getClasses().size());
		lbObjProperties.setText("" + ontology.getObjectProperties().size());
		lbDataProperties.setText("" + ontology.getDataProperties().size());

		// show Classes without rdfs:comment annotation
		ObservableList<String> undocItems = FXCollections.observableArrayList();
		for (OWLEntity e : ontology.getUnDocumentedEntities()) {
			undocItems.add(e.getIRI().getShortForm());
		}
		lbDescUndocEntities.setText("Undocumented Entities (" + undocItems.size() + ")");
		Collections.sort(undocItems);
		lstUnDocumentedEntities.setItems(undocItems);

		// Show entities which rdfs:comment annotation
		ObservableList<String> docItems = FXCollections.observableArrayList();
		for (OWLEntity e : ontology.getDocumentedEntities()) {
			docItems.add(e.getIRI().getShortForm());
		}
		lbDescDocEntities.setText("Documented Entities (" + docItems.size() + ")");
		Collections.sort(docItems);
		cbDocumentedEnties.setItems(docItems);
	}

	/**
	 * Method called after button click on "Show Description" for element.
	 */
	@FXML
	private void onShowOntologyEntityDescription() {
		String strSelItem = cbDocumentedEnties.getSelectionModel().getSelectedItem();
		if (strSelItem != null && !strSelItem.isEmpty()) {
			OWLEntity entity = ontology.getEntityByName(strSelItem);
			taOntDescription.setText(ontology.getCommentOfEntity(entity));
		}
	}
}
