package at.fh.BPMN20OntologyTester.view.fxcontroller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.semanticweb.owlapi.model.OWLEntity;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainSceneFxController {

	// GUI Elements from Ontology Tab
	@FXML
	private Label lbClasses, lbObjProperties, lbDataProperties, lbDescUndocEntities, lbDescDocEntities;
	@FXML
	private ListView<String> lstUnDocumentedEntities;
	@FXML
	private ComboBox<String> cbDocumentedEnties;
	@FXML
	private TextArea taOntDescription;

	// GUI Element from Tab "BPMN2OWL"
	@FXML
	private Label lbBPMN2OWLModelName, lbBPMN2OWLProcessAmount;
	@FXML
	private ListView<String> lstBPMN2OWLNotFoundInOWL, lstBPMN2OWLElementsFailedRestrictions;
	@FXML
	private TreeView<String> treeBPMN20OWL;

	// GUI Elements from "Test OWL" Tab
	@FXML
	private Button btLoadBPMN;

	// Logging required variables
	@FXML
	private TextArea taLog;
	private final SimpleDateFormat dateFormater;

	// Get initialized on startup of application
	private OWLModel ontology = null;
	// Get initialized when model gets loaded from user
	private BPMNModel bpmnModel = null;

	public MainSceneFxController() {
		dateFormater = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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

	public void appendLog(String text) {
		taLog.appendText(dateFormater.format(new Date()) + ":\t" + text + "\n");
	}

	// === TAB: ========================== Ontology
	// ======================================

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
		if (!strSelItem.isEmpty()) {
			OWLEntity entity = ontology.getEntityByName(strSelItem);
			taOntDescription.setText(ontology.getCommentOfEntity(entity));
		}
	}

	// === END
	// ===========================================================================

	// === TAB: ========================== BPMN2OWL
	// ----==================================
	@FXML
	private void onLoadBPMN() {

		try {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Load BPMN2.0 Process Modell");

			chooser.getExtensionFilters().add(new ExtensionFilter("BPMN 2.0", "*.bpmn"));
			chooser.getExtensionFilters().add(new ExtensionFilter("XML-BPMN", "*.xml"));

			// Handle selected file
			File selectedFile = chooser.showOpenDialog(null);
			// TODO: Test:
			// File selectedFile = new File("resource/ExampleProcessModel.bpmn")

			if (selectedFile != null) {
				BPMNModelHandler bpmnModelHandler = new BPMNModelHandler();
				bpmnModel = bpmnModelHandler.readModelFromFile(selectedFile);

				appendLog("Read BPMN-File <" + selectedFile.getAbsolutePath() + ">");

				showInitializedBPMN(bpmnModel);
			}
		} catch (Exception e) {
			appendLog("ERROR - Failed to load File <" + e.getMessage() + ">");
			e.printStackTrace();
		}
	}

	@FXML
	private void onBPMN2OWL() {
		appendLog("TODO - Method onBPMN2OWL not implemented yet! (Tab BPMN2OWL)");
	}

	@FXML
	private void onGenerateReport() {
		appendLog("TODO - Method onGenerateReport not implemented yet! (Tab BPMN2OWL)");
	}

	private void showInitializedBPMN(BPMNModel model) {
		String modelName = model.getModelDefinitions().getName();
		lbBPMN2OWLModelName.setText(modelName);
		lbBPMN2OWLProcessAmount.setText("" + model.getProcesses().size());

		//Show Elements not found in OWL
		ObservableList<String> elemNotFoundInOWL = FXCollections.observableArrayList();
		ontology.testElementsExsistInOntology(model).forEach(s -> elemNotFoundInOWL.add(s));
		Collections.sort(elemNotFoundInOWL);
		lstBPMN2OWLNotFoundInOWL.setItems(elemNotFoundInOWL);
		
		//Show Elements which were found in OWL but fial against restrictions
		ObservableList<String> elemFailOWL = FXCollections.observableArrayList();
		elemFailOWL.add("TODO");
		lstBPMN2OWLElementsFailedRestrictions.setItems(elemFailOWL);
		

		
		//Show Process as Tree
		TreeItem<String> rootItem = new TreeItem<String> (modelName);
		rootItem.setExpanded(true);
		for(org.camunda.bpm.model.bpmn.instance.Process p: model.getProcesses()) {
			TreeItem<String> procItem = new TreeItem<String>(p.getName());
			
			//Add Start and End Elements
			TreeItem<String> start = new TreeItem<String>("Start:");	
			TreeItem<String> end = new TreeItem<String>("End:");
			model.getStartEventsForProcess(p).forEach(e -> start.getChildren().add(new TreeItem<String>(e.getName())));
			model.getEndEventsForProcess(p).forEach(e -> end.getChildren().add(new TreeItem<String>(e.getName())));			
			procItem.getChildren().add(start);
			procItem.getChildren().add(end);
			
			//Add Process item to root
			rootItem.getChildren().add(procItem);
		}
		
		treeBPMN20OWL.setRoot(rootItem);

	}

	// === END
	// ===========================================================================

	// === TAB: ========================== OntologyTests
	// =================================
	// === END
	// ===========================================================================

}
