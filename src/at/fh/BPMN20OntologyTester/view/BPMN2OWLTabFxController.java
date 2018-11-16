package at.fh.BPMN20OntologyTester.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Set;

import org.camunda.bpm.model.xml.instance.DomElement;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.OWLTester;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNElement;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
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
	private ListView<String> lstBPMN2OWLNotFoundInOWL, lstFailedRestrictions;
	@FXML
	private TreeView<String> treeBPMN20OWL;
	@FXML
	private TreeView<BPMNElement> treeBPMNfailedRestrictions;
	
	@FXML
	private CheckBox chkIgnoreExtensionElements;

	// Get initialized on startup of application
	private BPMNModel bpmnModel = null;

	public BPMN2OWLTabFxController() {
	}

	@FXML
	private void initialize() {
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

			if (selectedFile != null) {
				bpmnModel = BPMNModelHandler.readModelFromFile(selectedFile);
				appendLog("Read BPMN-File <" + selectedFile.getAbsolutePath() + ">");

				//Set Main Information at labels
				String modelName = bpmnModel.getModelDefinitions().getName();
				lbBPMN2OWLModelName.setText(modelName);
				lbBPMN2OWLProcessAmount.setText("" + bpmnModel.getProcesses().size());
				
				// Show Process as Tree
				showBPMNasTree(bpmnModel);
				
				// show Elements which are exist in BPMN but no OWL-Class found in OWL
				showBPMNElementsNotExistInOWL(bpmnModel);
				

				// Show Elements which were found in OWL but does not meed all restrictions of OWL
				showBPMNElementsNotMeedOWLRestrictions(bpmnModel);
			}
		} catch (Exception e) {
			appendLog("ERROR - Failed to load File <" + e.getMessage() + ">");
			e.printStackTrace();
		}
	}

	
	@FXML
	private void onIgnoreExtenionElements() {
		try {
			if(bpmnModel != null) {
				// Refresh Elements which are exist in BPMN but no OWL-Class found in OWL
				showBPMNElementsNotExistInOWL(bpmnModel);
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
	
	@FXML
	private void handleClickedOnElementFailedRestrictions(MouseEvent event) {
		Node node = event.getPickResult().getIntersectedNode();
		// Accept clicks only on node cells, and not on empty spaces of the TreeView
	    if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
	    	
	    	BPMNElement selectedNode = treeBPMNfailedRestrictions.getSelectionModel().getSelectedItem().getValue();
	        
	        //Represents the processTreeItem - Elements are in the same line and the absolute root-item is not shown in javafX
	        TreeItem<BPMNElement> processTreeItem = treeBPMNfailedRestrictions.getSelectionModel().getSelectedItem().getParent();
	        
	        //Show failed restrictions for selectedNode	        	        
	        ObservableList<String> failedRestrictions = FXCollections.observableArrayList();
	        for(FailedOWLClassRestriction r : selectedNode.getFailedRestrictions()) {
	        	failedRestrictions.add(r.getFailingReason());
	        }
	        //Collections.sort(elemNotFoundInOWL);
	        lstFailedRestrictions.setItems(failedRestrictions);
	    }
		
	}


	private void appendLog(String text) {
		MainSceneFxController.getInstance().appendLog(text);
	}
	
	private void showBPMNasTree(BPMNModel model) {
		TreeItem<String> rootItem = new TreeItem<String>(model.getModelDefinitions().getName());
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

	private void showBPMNElementsNotExistInOWL(BPMNModel model) throws OWLOntologyCreationException, FileNotFoundException {
		OWLModel ontology = OntologyHandler.getInstance().getBpmn20Ontology();		
		ObservableList<String> elemNotFoundInOWL = FXCollections.observableArrayList();
		//Determine if Childs of ExteniosnElement shoud be ignored or not.
		boolean ignoreExtensionElements = chkIgnoreExtensionElements.isSelected();
		OWLTester.testBPMNElementsExsistAsOWLClasses(ontology, model,ignoreExtensionElements).forEach(s -> elemNotFoundInOWL.add(s));
		Collections.sort(elemNotFoundInOWL);
		lstBPMN2OWLNotFoundInOWL.setItems(elemNotFoundInOWL);
		
		//Give some Feedback in log
		if( ! elemNotFoundInOWL.isEmpty()) {
			appendLog("Warning - Found <" + elemNotFoundInOWL.size() + "> elements in BPMN which have no OWL-Class in the ontology!");
		}
		
	}
	
	private void showBPMNElementsNotMeedOWLRestrictions(BPMNModel model) throws Exception {
		OWLModel ontology = OntologyHandler.getInstance().getBpmn20Ontology();	
		
		TreeItem<BPMNElement> rootItem = new TreeItem<BPMNElement>(new BPMNElement(model.getModelDefinitionAsDomElement()));
		rootItem.setExpanded(true);
		
		int failedElements = 0;
		for (org.camunda.bpm.model.bpmn.instance.Process proc : model.getProcesses()) {
			TreeItem<BPMNElement> procItem = new TreeItem<BPMNElement>(new BPMNElement(proc.getDomElement(),proc.getName()));
			procItem.setExpanded(true);
			
			//Iterate now over all Elements of the process
			for(DomElement domElement: model.getProcessElementsAsDomElements(proc)) {		
				//Check for failed restrictions
				Set<FailedOWLClassRestriction> failedRestrictions = OWLTester.testBPMNElementMeetOWLRestrictions(domElement, ontology);
				if( failedRestrictions.size() > 0) {		
					failedElements++;
					
					BPMNElement elem = new BPMNElement(domElement,failedRestrictions);
					//Create treeItem and show in GUI
					TreeItem<BPMNElement> failedelement = new TreeItem<BPMNElement>(elem);
					procItem.getChildren().add(failedelement);
				}
			}
			
			// Add Process item to root if there are failed Elements 
			if(procItem.getChildren().size() > 0) {
				rootItem.getChildren().add(procItem);
			}
			
		}
		
		treeBPMNfailedRestrictions.setRoot(rootItem);
		
		//Give some Feedback in log
		if( failedElements > 0) {
			appendLog("Warning - Found <" + failedElements + "> elements in BPMN which does not meet the defined restrictions in the ontology!");
		}
	}	
}
