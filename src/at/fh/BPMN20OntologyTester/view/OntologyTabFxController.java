package at.fh.BPMN20OntologyTester.view;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.semanticweb.owlapi.model.OWLEntity;

import at.fh.BPMN20OntologyTester.BPMN20OntologyTester;
import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.controller.Owl2BPMNMapper;
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Handels user interactions on tab "Ontology"
 * 
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class OntologyTabFxController implements FxController {

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
	private final OWLModel ontology;
	private final Owl2BPMNMapper owl2bpmnMapping;
	
	public OntologyTabFxController() {
				
		//Init local ontology variable
		Optional<OWLModel> optOntology = OntologyHandler.getInstance().getLoadedOntology();
		if (optOntology.isPresent()) {
			this.ontology = optOntology.get();
			appendLog("Read and initialized BPMN2.0 Ontology from ressource folder");
		} else {
			appendLog("Unable to load Ontology from ressource folder. Unable to verify ontology!");
			this.ontology = null;
		} 
		
		//Init local owl2bpmn varaibel
		this.owl2bpmnMapping = Owl2BPMNMapper.getInstance();
		if(! this.owl2bpmnMapping.hasMappings())
			appendLog("Found no mappings for OWL2BPMN convertion");
		else
			appendLog("Loaded <" + owl2bpmnMapping.getLoadedMapping().size() + "> OWL2BPMN mapping entries");
	}

	@FXML
	private void initialize() {
		//Show loaded Ontology on GUI
		showInitializedOntology(this.ontology);
	}
	

	private void appendLog(String text) {
		MainSceneFxController.getInstance().appendLog(text);
	}

	/**
	 * Shows main facts of loaded Ontology on GUI. 
	 */
	private void showInitializedOntology(OWLModel ontology) {
		if (ontology != null) {
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
			ObservableList<String> entities = FXCollections.observableArrayList();
			for (OWLEntity e : ontology.getAllEntities()) {
				entities.add(e.getIRI().getShortForm());
			}
			Collections.sort(entities);
			cbOWLEntities.setItems(entities);
		}
	}

	/**
	 * Method called after button click on "Show Description" for element.
	 */
	@FXML
	private void onShowOntologyEntityDetails() {
		String strSelItem = cbOWLEntities.getSelectionModel().getSelectedItem();
		if (strSelItem != null && !strSelItem.isEmpty()) {
			OWLEntity entity = ontology.getEntityByShortName(strSelItem);

			// Set description for selected Entity
			String description = ontology.getCommentOfEntity(entity);
			if (description == null || description.isEmpty())
				description = "No Description for Entity available!";
			taOntDescription.setText(description);

			// Show Restriction for class
			if (entity.isOWLClass()) {

				ObservableList<String> restItems = FXCollections.observableArrayList();

				try {
					for (OWLClassRestriction r : ontology.getAllOWLClassRestrictionOfOWLClass(entity.asOWLClass(),
							true)) {
						restItems.add(r.toFormattedToString());
					}
				} catch (Exception e) {
					appendLog("Error occured while parsing OWL-Restrictions for class <"
							+ entity.getIRI().getShortForm() + ">");
				}

				if (restItems.isEmpty())
					restItems.add("No Restrictions for Class found");

				Collections.sort(restItems);
				lstRestrictions.setItems(restItems);
			}

		}
	}

	@FXML
	private void onShowOwl2BPMNMapping() {
		
		try {
			Stage dialog = BPMN20OntologyTester.getOWL2BPMNMappingDialog();
			dialog.show();
		} catch (Exception e) {
			appendLog("Error while open mapping dialog: " + e.getMessage());
		}
	}
}
