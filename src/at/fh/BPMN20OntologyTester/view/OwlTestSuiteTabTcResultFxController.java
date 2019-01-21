/**
 * 
 */
package at.fh.BPMN20OntologyTester.view;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.camunda.bpm.model.bpmn.instance.Process;
import org.semanticweb.owlapi.model.OWLProperty;
import org.w3c.dom.Element;

import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNElement;
import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import at.fh.BPMN20OntologyTester.model.TestCase;
import at.fh.BPMN20OntologyTester.model.enums.TestCaseEnum;
import at.fh.BPMN20OntologyTester.view.dto.BPMNElementV2;
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
	private TreeView<BPMNElementV2> treeBPMNfailedRestrictions;

	@FXML
	private CheckBox cbIgnoreWarningRestrictions;

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

			BPMNElementV2 selectedNode = treeBPMNfailedRestrictions.getSelectionModel().getSelectedItem().getValue();

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

		TreeItem<BPMNElementV2> curSelElementFailedRestriction = treeBPMNfailedRestrictions.getSelectionModel()
				.getSelectedItem();

		// Get selected Error-Test of faild Restriction
		String selItem = lstFailedRestrictions.getSelectionModel().getSelectedItem();

		if (curSelElementFailedRestriction != null && selItem != null) {

			// Get selected BPMN-Element of tree with failed restrictions
			BPMNElementV2 selectedBPMNElement = curSelElementFailedRestriction.getValue();
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

		try {
			if (testcase != null) {
				testXmlNodesNotExistInOWL(testcase);
				testXmlAttriubtesNotExistinOWL(testcase);
				testXmlNodesFailRestrictions(testcase);
			}
		} catch (Exception e) {
			MainSceneFxController.getInstance().appendLog("Error - Error occured while tests: " + e.getMessage());
			e.printStackTrace();
		}
	};

	private void testXmlNodesNotExistInOWL(TestCase testcase) throws Exception {
		//Execute tests and show result
		testcase.executeTest(TestCaseEnum.XMLElementsAsOWLClasses);
		
		if(testcase.getResultsXmlNodesWithoutOWLClass().isEmpty()) {
			xmlNodeNotFoundInOWL.add("No issues found!");
		} else {
			testcase.getResultsXmlNodesWithoutOWLClass().forEach(e -> {
					xmlNodeNotFoundInOWL.add(e.getDomLocalName());
	
			});
			Collections.sort(xmlNodeNotFoundInOWL);
		}
	}

	private void testXmlAttriubtesNotExistinOWL(TestCase testcase) throws Exception {
		//Execute tests and show result
		testcase.executeTest(TestCaseEnum.XMlAttributesAsOWLProperties);
		
		if(testcase.getResultsXmlAttributesWithoutOWLProperty().isEmpty()) {
			xmlAttributesNotFoundInOWL.add("No issues found!");
		} else {
		
			testcase.getResultsXmlAttributesWithoutOWLProperty().forEach(attr -> {
				xmlAttributesNotFoundInOWL.add((String) attr);
			});
			Collections.sort(xmlAttributesNotFoundInOWL);
		}

	}

	private void testXmlNodesFailRestrictions(TestCase testcase) throws Exception {
		
		testcase.setIgnoreTcSpecificData(cbIgnoreWarningRestrictions.isSelected());

		//Execute tests and show result
		testcase.executeTest(TestCaseEnum.XMLElementFailOWLClassRestrictions);	
		Map<Element,Set<FailedOWLClassRestriction>> elementsFailedRestrictions = testcase.getResultsXmlNodesFailOWLRestrictions();

		// Build Tree to show results
		if (!elementsFailedRestrictions.isEmpty()) {
			TreeItem<BPMNElementV2> rootItem = new TreeItem<BPMNElementV2>(new BPMNElementV2("Failures"));

			rootItem.setExpanded(true);
			for (Element elem: elementsFailedRestrictions.keySet()) {
				String guiDisplayName = elem.getTagName().substring(elem.getTagName().indexOf(":")+1) + "(Err: "
							+ elementsFailedRestrictions.get(elem).size() + ")";

				BPMNElementV2 ev2 = new BPMNElementV2(elem,elementsFailedRestrictions.get(elem),guiDisplayName);
				
				TreeItem<BPMNElementV2> treeItem = new TreeItem<BPMNElementV2>(ev2);
				rootItem.getChildren().add(treeItem);
			}
			treeBPMNfailedRestrictions.setRoot(rootItem);
			treeBPMNfailedRestrictions.refresh();
		}
	}
}
