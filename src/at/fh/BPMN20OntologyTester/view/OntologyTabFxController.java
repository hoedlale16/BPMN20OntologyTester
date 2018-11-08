package at.fh.BPMN20OntologyTester.view;

import java.util.Collections;

import org.apache.commons.codec.binary.StringUtils;
import org.semanticweb.owlapi.model.OWLEntity;

import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction;
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
	private Label lbClasses, lbObjProperties, lbDataProperties, lbDescUndocEntities;
	@FXML
	private ListView<String> lstUnDocumentedEntities, lstRestrictions;
	@FXML
	private ComboBox<String> cbOWLEntities;
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
		lbClasses.setText("" + ontology.getOWLClasses().size());
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
		ObservableList<String> entities= FXCollections.observableArrayList();
		for (OWLEntity e : ontology.getAllEntities()) {
			entities.add(e.getIRI().getShortForm());
		}
		Collections.sort(entities);
		cbOWLEntities.setItems(entities);
	}

	/**
	 * Method called after button click on "Show Description" for element.
	 */
	@FXML
	private void onShowOntologyEntityDetails() {
		String strSelItem = cbOWLEntities.getSelectionModel().getSelectedItem();
		if (strSelItem != null && !strSelItem.isEmpty()) {
			OWLEntity entity = ontology.getEntityByShortName(strSelItem);
			
			//Set description for selected Entity
			String description = ontology.getCommentOfEntity(entity);
			if(description == null || description.isEmpty())
				description = "No Description for Entity available!";
			taOntDescription.setText(description);
			
			//Show Restriction for class
			if (entity.isOWLClass()) {
			
				ObservableList<String> restItems = FXCollections.observableArrayList();
				for (OWLClassRestriction r : ontology.getOWLClassRestrictionOfOWLClass(entity.asOWLClass(), true)) {
					restItems.add(r.toFormattedToString());
				}
				
				if(restItems.isEmpty())
					restItems.add("No Restrictions for Class found");
			
				Collections.sort(restItems);
				lstRestrictions.setItems(restItems);
			}
			
		}
	}
}
