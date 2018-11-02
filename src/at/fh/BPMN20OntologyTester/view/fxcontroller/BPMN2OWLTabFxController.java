package at.fh.BPMN20OntologyTester.view.fxcontroller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.OWLTester;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Handles user interactions on tab "BPMN2OWL"
 * 
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class BPMN2OWLTabFxController {

	// GUI Element from Tab "BPMN2OWL"
	@FXML
	private Label lbBPMN2OWLModelName, lbBPMN2OWLProcessAmount;
	@FXML
	private ListView<String> lstBPMN2OWLNotFoundInOWL, lstBPMN2OWLElementsFailedRestrictions;
	@FXML
	private TreeView<String> treeBPMN20OWL;
	@FXML
	private CheckBox chkIgnoreExtensionElements;

	// Get initialized on startup of application
	private BPMNModel bpmnModel = null;

	public BPMN2OWLTabFxController() {
	}

	@FXML
	private void initialize() {
	}

	private void appendLog(String text) {
		MainSceneFxController.getInstance().appendLog(text);
	}
	
	private void showInitializedBPMN(BPMNModel model) throws OWLOntologyCreationException, FileNotFoundException {
		String modelName = model.getModelDefinitions().getName();
		lbBPMN2OWLModelName.setText(modelName);
		lbBPMN2OWLProcessAmount.setText("" + model.getProcesses().size());

		// Show Elements not found in OWL
		OWLModel ontology = OntologyHandler.getInstance().getBpmn20Ontology();
		
		ObservableList<String> elemNotFoundInOWL = FXCollections.observableArrayList();
		
		//Determine if Childs of ExteniosnElement shoud be ignored or not.
		boolean ignoreExtensionElements = chkIgnoreExtensionElements.isSelected();
		
		OWLTester.testElementsExsistInOntology(ontology, model,ignoreExtensionElements).forEach(s -> elemNotFoundInOWL.add(s));
		Collections.sort(elemNotFoundInOWL);
		lstBPMN2OWLNotFoundInOWL.setItems(elemNotFoundInOWL);

		// Show Elements which were found in OWL but fial against restrictions
		ObservableList<String> elemFailOWL = FXCollections.observableArrayList();
		elemFailOWL.add("TODO");
		lstBPMN2OWLElementsFailedRestrictions.setItems(elemFailOWL);

		// Show Process as Tree
		TreeItem<String> rootItem = new TreeItem<String>(modelName);
		rootItem.setExpanded(true);
		for (org.camunda.bpm.model.bpmn.instance.Process p : model.getProcesses()) {
			TreeItem<String> procItem = new TreeItem<String>(p.getName());

			// Add Start and End Elements
			TreeItem<String> start = new TreeItem<String>("Start:");
			TreeItem<String> end = new TreeItem<String>("End:");
			model.getStartEventsForProcess(p).forEach(e -> start.getChildren().add(new TreeItem<String>(e.getName())));
			model.getEndEventsForProcess(p).forEach(e -> end.getChildren().add(new TreeItem<String>(e.getName())));
			procItem.getChildren().add(start);
			procItem.getChildren().add(end);

			// Add Process item to root
			rootItem.getChildren().add(procItem);
		}

		treeBPMN20OWL.setRoot(rootItem);

	}

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
	private void onIgnoreExtenionElements() {
		try {
			if(bpmnModel != null)
				showInitializedBPMN(bpmnModel);
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

	

}
