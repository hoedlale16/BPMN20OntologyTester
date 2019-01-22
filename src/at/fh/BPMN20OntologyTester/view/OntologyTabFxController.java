package at.fh.BPMN20OntologyTester.view;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

import org.semanticweb.owlapi.model.OWLEntity;

import at.fh.BPMN20OntologyTester.BPMN20OntologyTester;
import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.XmlElement2OWLClassesMapper;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.controller.XmlAttribute2OWLPropertyMapper;
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

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
	
	
	private ObservableList<String> undocItems = FXCollections.observableArrayList();
	private ObservableList<String> entities = FXCollections.observableArrayList();
	private ObservableList<String> restItems = FXCollections.observableArrayList();

	public OntologyTabFxController() {
	}

	@FXML
	private void initialize() {
				
		lstUnDocumentedEntities.setItems(undocItems);
		cbOWLEntities.setItems(entities);
		lstRestrictions.setItems(restItems);
		
		//Clear content - loaded when ontology is initialized
		undocItems.clear();
		entities.clear();
		restItems.clear();
		taOntDescription.clear();

		// Init local ontology variable
		Optional<OWLModel> optOntology = OntologyHandler.getInstance().getLoadedOntology();
		if (optOntology.isPresent()) {
			String filePath = optOntology.get().getFileCreatedFrom().getAbsolutePath();
			
			//Hide bad hack loading file from resource directory
			if (filePath.endsWith(".tmp"))
				filePath = "/resource/owl/BPMN20.owl";
			
			appendLog("Read and initialized BPMN2.0 Ontology from <" + filePath + ">");
			// Show loaded Ontology on GUI
			showInitializedOntology(optOntology.get());
		} else {
			appendLog("Unable to load Ontology from ressource folder. Unable to verify ontology!");
		}

		// Init local owl2bpmn varaibel
		if (!XmlElement2OWLClassesMapper.getInstance().hasMappings())
			appendLog("Found no mappings for OWL2BPMN convertion");
		else
			appendLog("Loaded <" + XmlElement2OWLClassesMapper.getInstance().getLoadedMapping().size()
					+ "> OWL2BPMN mapping entries");

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
			
			for (OWLEntity e : ontology.getUnDocumentedEntities()) {
				undocItems.add(e.getIRI().getShortForm());
			}
			lbDescUndocEntities.setText("Undocumented Entities (" + undocItems.size() + ")");
			Collections.sort(undocItems);

			// Show entities which rdfs:comment annotation
			
			for (OWLEntity e : ontology.getAllEntities()) {
				entities.add(e.getIRI().getShortForm());
			}
			Collections.sort(entities);
			
		}
	}

	/**
	 * Method called after button click on "Show Description" for element.
	 */
	@FXML
	private void onShowOntologyEntityDetails() {
		String strSelItem = cbOWLEntities.getSelectionModel().getSelectedItem();
		if (strSelItem != null && !strSelItem.isEmpty()) {

			// When we can select something, the ontology must be present
			OWLModel ontology = OntologyHandler.getInstance().getLoadedOntology().get();
			OWLEntity entity = ontology.getEntityByShortName(strSelItem);

			// Set description for selected Entity
			String description = ontology.getCommentOfEntity(entity);
			if (description == null || description.isEmpty())
				description = "No Description for Entity available!";
			taOntDescription.setText(description);

			// Show Restriction for class
			restItems.clear();
			if (entity.isOWLClass()) {
				try {
					for (OWLClassRestriction r : ontology.getAllOWLClassRestrictionOfOWLClass(entity.asOWLClass())) {
						restItems.add(r.toFormattedToString());
					}
				} catch (Exception e) {
					appendLog("Error occured while parsing OWL-Restrictions for class <"
							+ entity.getIRI().getShortForm() + ">");
					e.printStackTrace();
				}

				if (restItems.isEmpty())
					restItems.add("No Restrictions for Class found");

				Collections.sort(restItems);
				
			}

		}
	}

	@FXML
	private void onLoadAnotherOntology() {
		try {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Load BPMN2.0 Ontology");

			chooser.getExtensionFilters().add(new ExtensionFilter("BPMN 2.0 Ontology", "*.owl"));

			// Handle selected file
			File selectedFile = chooser.showOpenDialog(null);

			if (selectedFile != null) {
				OntologyHandler.getInstance().loadOntology(selectedFile);
				//Refresh content for new loaded Ontology
				initialize();
				
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("New Ontology loaded!");
				alert.setHeaderText("New ontology initializied");
				alert.setContentText("Take care to reexecute loading of process models on other register cards to get a valid result");
				alert.showAndWait();
				
			}
		} catch (Exception e) {
			appendLog("ERROR - Failed to loading Ontoogy from File <" + e.getMessage() + ">");
			e.printStackTrace();
		}
	}

	@FXML
	private void onShowXmlElements2OwlClassesMapping() {

		try {
			Stage dialog = BPMN20OntologyTester.getOWL2BPMNMappingDialog();
			dialog.showAndWait();
			appendLog("Applied changes for mapping XmlElements2OWLClasses - Mapping contains now <"
					+ XmlElement2OWLClassesMapper.getInstance().getLoadedMapping().size() + "> entries");

		} catch (Exception e) {
			appendLog("Error while open mapping dialog: " + e.getMessage());
		}
	}
	
	@FXML
	private void onShowXmlAttribute2OwlPropertiesMapping() {
		//TODO
	}
	

	private void appendLog(String text) {
		MainSceneFxController.getInstance().appendLog(text);
	}
}
