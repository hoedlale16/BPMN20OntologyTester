/**
 * 
 */
package at.fh.BPMN20OntologyTester.view;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.model.bpmn.instance.Process;
import org.semanticweb.owlapi.model.OWLProperty;

import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNElement;
import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import at.fh.BPMN20OntologyTester.model.TestCase;
import at.fh.BPMN20OntologyTester.model.enums.TestCaseEnum;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

/**
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class OwlTestSuiteTabTcResultFxController implements FxController {

	// GUI Elements
	
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

	// Initialized when tab is created
	private final TestCase testcase;

	// Lists to show testresults
	ObservableList<String> xmlNodeNotFoundInOWL = FXCollections.observableArrayList();
	ObservableList<String> xmlAttributesNotFoundInOWL = FXCollections.observableArrayList();
	ObservableList<String> failedRestrictionsOfXmlNode = FXCollections.observableArrayList();

	/**
	 * 
	 */
	public OwlTestSuiteTabTcResultFxController(TestCase testcase) {
		this.testcase = testcase;
	}
	
	@FXML
	private void initialize() {
		// Initialize GUI-Elements
		lstXmlNodesNotAsOWLClass.setItems(xmlNodeNotFoundInOWL);
		lstAttributesNotAsProperty.setItems(xmlAttributesNotFoundInOWL);
		lstFailedRestrictions.setItems(failedRestrictionsOfXmlNode);
		
		//Show results
		showTcResults();
	}
	

	@FXML
	private void onUpdateTestCaseResults() {
		showTcResults();

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
		testcase.executeTest(TestCaseEnum.XMLElementsAsOWLClasses);
		
		testcase.getResultsXmlNodesWithoutOWLClass().forEach(e -> {
			//if (!xmlNodeNotFoundInOWL.contains(e.getDomLocalName())) {
				xmlNodeNotFoundInOWL.add(e.getDomLocalName());
			//}
		});
		Collections.sort(xmlNodeNotFoundInOWL);
	}

	private void testXmlAttriubtesNotExistinOWL(TestCase testcase) {
		testcase.setIgnoreTcSpecificData(cbIgnoreExtensionElements.isSelected());

		//Execute tests and show result
		testcase.executeTest(TestCaseEnum.XMlAttributesAsOWLProperties);
		testcase.getResultsXmlAttributesWithoutOWLProperty().forEach(attr -> {
			xmlAttributesNotFoundInOWL.add((String) attr);
		});

		Collections.sort(xmlAttributesNotFoundInOWL);

	}

	private void testXmlNodesFailRestrictions(TestCase testcase) {
		
		testcase.setIgnoreTcSpecificData(cbIgnoreWarningRestrictions.isSelected());

		//Execute tests and show result
		testcase.executeTest(TestCaseEnum.XMLElementFailOWLClassRestrictions);	
		Map<Process, List<BPMNElement>> elementsFailedRestrictions = testcase.getResultsXmlNodesFailOWLRestrictions();

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
				}			
				rootItem.getChildren().add(procItem);
			}
			treeBPMNfailedRestrictions.setRoot(rootItem);
			treeBPMNfailedRestrictions.refresh();
		}
	}
}
