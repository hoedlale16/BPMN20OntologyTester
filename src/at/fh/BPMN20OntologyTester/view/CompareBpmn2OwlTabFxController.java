package at.fh.BPMN20OntologyTester.view;

import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.model.bpmn.instance.Process;
import org.semanticweb.owlapi.model.OWLProperty;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.OWL2BPMNMapper;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNElement;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import at.fh.BPMN20OntologyTester.model.TestCase;
import at.fh.BPMN20OntologyTester.model.TestCase.TestCaseEnum;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
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
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class CompareBpmn2OwlTabFxController implements FxController {

	// GUI Element from Tab "BPMN2OWL"
	@FXML
	private Label lbBPMN2OWLModelName, lbBPMN2OWLProcessAmount;
	@FXML
	private TextArea taRestrictionDescription;
	@FXML
	private ListView<String> lstXmlNodesNotAsOWLClass, lstAttributesNotAsProperty, lstFailedRestrictions;
	@FXML
	private TreeView<String> treeBPMN20OWL;
	@FXML
	private TreeView<BPMNElement> treeBPMNfailedRestrictions;

	@FXML
	private CheckBox cbIgnoreExtensionElements, cbIgnoreWarningRestrictions;

	@FXML
	private Button btLoadBPMN, btGenerateReport, btConvertToOWL;

	// Initialized when user load BPMN Model
	private TestCase testcase = null;

	// Lists to show testresults
	ObservableList<String> xmlNodeNotFoundInOWL = FXCollections.observableArrayList();
	ObservableList<String> xmlAttributesNotFoundInOWL = FXCollections.observableArrayList();

	ObservableList<String> failedRestrictionsOfXmlNode = FXCollections.observableArrayList();

	public CompareBpmn2OwlTabFxController() {
	}

	/**
	 * Helper methods to activate or deactivate Buttons on GUI depending an ontology
	 * is set or not
	 */
	private void updateActivationSateofButtons() {
		boolean ontologyExists = OntologyHandler.getInstance().getLoadedOntology().isPresent();

		btLoadBPMN.setDisable(!ontologyExists);
		btConvertToOWL.setDisable(!ontologyExists);
		btGenerateReport.setDisable(!ontologyExists);
	}

	@FXML
	private void initialize() {

		// Initialize GUI-Elements
		lstXmlNodesNotAsOWLClass.setItems(xmlNodeNotFoundInOWL);
		lstAttributesNotAsProperty.setItems(xmlAttributesNotFoundInOWL);
		lstFailedRestrictions.setItems(failedRestrictionsOfXmlNode);

		// Set User-Input Elements according ontology found or not
		updateActivationSateofButtons();
	}
	
	private TestCase createTestcase(BPMNModel processModel) {
		Optional<OWLModel> optOntology = OntologyHandler.getInstance().getLoadedOntology();
		if (optOntology.isPresent() && processModel != null) {
		 
			OWLModel ontology = optOntology.get();
			OWL2BPMNMapper owl2bpmnMapper = OWL2BPMNMapper.getInstance();
			return new TestCase(ontology, processModel, owl2bpmnMapper);
		}
		return null;
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
				
				// Create Testcase with given BPMN
				testcase = createTestcase(BPMNModelHandler.readModelFromFile(selectedFile));
				appendLog("Read BPMN-File <" + selectedFile.getAbsolutePath() + ">");

				// Show loaded BPMN Model Data and Testresults
				showModelOverview();
				showTcResults();
			}
		} catch (Exception e) {
			appendLog("ERROR - Failed to load File <" + e.getMessage() + ">");
			e.printStackTrace();
		}
	}

	@FXML
	private void onReset() {
		testcase = null;
		showModelOverview();
		showTcResults();
	}

	@FXML
	private void onUpdateTestCaseResults() {
		// Just refresh GUI
		showTcResults();
	}

	@FXML
	private void onBPMN2OWL() {
		appendLog("TODO - Method onBPMN2OWL not implemented yet! (Tab BPMN2OWL)");
	}

	@FXML
	private void onGenerateReport() {
		if (testcase != null ) {
			try {
				FileChooser chooser = new FileChooser();
				chooser.setTitle("Export Testcase report");
	
				chooser.getExtensionFilters().add(new ExtensionFilter("TestcaseReport", "*.txt"));
	
				// Handle selected file
				File selectedFile = chooser.showSaveDialog(null);
	
				if (selectedFile != null) {	
					FileWriter writer = new FileWriter(selectedFile);
					writer.write(testcase.getTestResultReport());
					writer.flush();
					writer.close();
					
					appendLog("Created Testaces report <" + selectedFile.getAbsolutePath() + ">");
				}
			} catch (Exception e) {
				appendLog("Error - Failed to create testcase report <" + e.getMessage() + ">");
				
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error!");
				alert.setHeaderText("Error creating testcase report");
				alert.setContentText("ERROR - Failed to write to File <" + e.getMessage() + ">");
				alert.showAndWait();
			} 
		}
	}

	@SuppressWarnings("rawtypes")
	@FXML
	private void onHandleClickedOnElementFailedRestrictions(MouseEvent event) {
		Node node = event.getPickResult().getIntersectedNode();
		// Accept clicks only on node cells, and not on empty spaces of the TreeView
		if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {

			BPMNElement selectedNode = treeBPMNfailedRestrictions.getSelectionModel().getSelectedItem().getValue();

			// Show failed restrictions for selectedNode
			failedRestrictionsOfXmlNode.clear();
			for (FailedOWLClassRestriction r : selectedNode
					.getFailedRestrictions(cbIgnoreWarningRestrictions.isSelected())) {
				failedRestrictionsOfXmlNode.add(r.getFormattedFailingReason());
			}
		}

	}

	@FXML
	private void onHandleClickedOnFailedRestriction(MouseEvent event) {

		TreeItem<BPMNElement> curSelElementFailedRestriction = treeBPMNfailedRestrictions.getSelectionModel()
				.getSelectedItem();

		// Get selected Error-Test of faild Restriction
		String selItem = lstFailedRestrictions.getSelectionModel().getSelectedItem();

		if (curSelElementFailedRestriction != null && selItem != null) {

			// Get selected BPMN-Element of tree with failed restrictions
			BPMNElement selectedBPMNElement = curSelElementFailedRestriction.getValue();
			// Get FailedRestriction Object with error text. Assume the error text is
			// unique!
			Optional<FailedOWLClassRestriction> rest = selectedBPMNElement.getFailedRestrictionWithErrorText(selItem);
			if (rest.isPresent()) {
				OWLProperty affectedProperty = rest.get().getRestriction().getOnProperty();

				// We know here that the onoltogy must present.
				OWLModel ontology = OntologyHandler.getInstance().getLoadedOntology().get();

				taRestrictionDescription.setText(ontology.getCommentOfEntity(affectedProperty));
			}
		}

	}

	private void showModelOverview() {

		// Reset GUI elements
		lbBPMN2OWLModelName.setText("");
		lbBPMN2OWLProcessAmount.setText("");
		treeBPMN20OWL.setRoot(null);

		// Show Elements for created testcase
		if (testcase != null) {
			BPMNModel model = testcase.getProcessModel();

			// Set Label-Texts
			lbBPMN2OWLModelName.setText(model.getModelDefinitions().getName());
			lbBPMN2OWLProcessAmount.setText("" + testcase.getProcessModel().getProcesses().size());

			// Show Model as tree
			TreeItem<String> rootItem = new TreeItem<String>(model.getModelDefinitions().getName());
			rootItem.setExpanded(true);
			for (org.camunda.bpm.model.bpmn.instance.Process p : model.getProcesses()) {
				TreeItem<String> procItem = new TreeItem<String>(p.getName());

				// Add Start and End Elements
				TreeItem<String> start = new TreeItem<String>("Start:");
				TreeItem<String> end = new TreeItem<String>("End:");
				model.getStartEventsForProcess(p)
						.forEach(e -> start.getChildren().add(new TreeItem<String>(e.getName())));
				model.getEndEventsForProcess(p).forEach(e -> end.getChildren().add(new TreeItem<String>(e.getName())));
				procItem.getChildren().add(start);
				procItem.getChildren().add(end);

				// Add Process item to root
				rootItem.getChildren().add(procItem);
			}

			treeBPMN20OWL.setRoot(rootItem);
		}
	}

	private void showTcResults() {
		// Clear TC-specific GUI elements and execute tests of testcase
		xmlNodeNotFoundInOWL.clear();
		xmlAttributesNotFoundInOWL.clear();
		failedRestrictionsOfXmlNode.clear();
		treeBPMNfailedRestrictions.setRoot(null);
		taRestrictionDescription.setText("");

		if (testcase != null) {
			testXmlNodesNotExistInOWL(testcase);
			testXmlAttriubtesNotExistinOWL(testcase);
			testXmlNodesFailRestrictions(testcase);
		}
	};

	private void testXmlNodesNotExistInOWL(TestCase testcase) {
		// Determine if Childs of ExteniosnElement should be ignored or not.
		testcase.setIgnoreTcSpecificData(cbIgnoreExtensionElements.isSelected());

		//Execute tests and show result
		testcase.executeTest(TestCaseEnum.XMLNodesAsOWLClass);
		
		testcase.getResultsXmlNodesWithoutOWLClass().forEach(e -> {
			//if (!xmlNodeNotFoundInOWL.contains(e.getDomLocalName())) {
				xmlNodeNotFoundInOWL.add(e.getDomLocalName());
			//}
		});
		Collections.sort(xmlNodeNotFoundInOWL);
		appendLog("Found <" + xmlNodeNotFoundInOWL.size()
				+ "> XML-Nodes in BPMN which have no OWL-Class in the ontology!");
	}

	private void testXmlAttriubtesNotExistinOWL(TestCase testcase) {
		testcase.setIgnoreTcSpecificData(cbIgnoreExtensionElements.isSelected());

		//Execute tests and show result
		testcase.executeTest(TestCaseEnum.XMlAttributesAsOWLProperties);
		testcase.getResultsXmlAttributesWithoutOWLProperty().forEach(attr -> {
			xmlAttributesNotFoundInOWL.add((String) attr);
		});

		Collections.sort(xmlAttributesNotFoundInOWL);
		appendLog("Found <" + xmlAttributesNotFoundInOWL.size()
				+ "> XML-Attributes in BPMN which have no OWL-Property in the ontology!");

	}

	private void testXmlNodesFailRestrictions(TestCase testcase) {
		
		testcase.setIgnoreTcSpecificData(cbIgnoreWarningRestrictions.isSelected());

		//Execute tests and show result
		testcase.executeTest(TestCaseEnum.XMLNodeFailOWLClassRestrictions);	
		Map<Process, List<BPMNElement>> elementsFailedRestrictions = testcase.getResultsXmlNodesFailOWLRestrictions();
		int totalFailedElements = 0;

		// Build Tree to show results
		if (!elementsFailedRestrictions.isEmpty()) {
			TreeItem<BPMNElement> rootItem = new TreeItem<BPMNElement>(
					new BPMNElement(testcase.getProcessModel().getModelDefinitionAsDomElement()));

			rootItem.setExpanded(true);
			for (Process proc : elementsFailedRestrictions.keySet()) {
				TreeItem<BPMNElement> procItem = new TreeItem<BPMNElement>(
						new BPMNElement(proc.getDomElement(), proc.getName()));
				procItem.setExpanded(true);

				// Now show failed elements
				for (BPMNElement element : elementsFailedRestrictions.get(proc)) {
					String guiDisplayName = element.getDomLocalName() + "(Err: "
							+ element.getFailedRestrictions().size() + ")";

					element.setGUIDisplayName(guiDisplayName);
					TreeItem<BPMNElement> failedelement = new TreeItem<BPMNElement>(element);
					procItem.getChildren().add(failedelement);

					totalFailedElements++;
				}
				
				rootItem.getChildren().add(procItem);
			}
			treeBPMNfailedRestrictions.setRoot(rootItem);
			treeBPMNfailedRestrictions.refresh();
		}

		// Give some Feedback in log
		appendLog("Warning - Found <" + totalFailedElements
				+ "> elements in BPMN which does not meet the defined restrictions in the ontology!");
	}

	private void appendLog(String text) {
		MainSceneFxController.getInstance().appendLog(text);
	}
}
