package at.fh.BPMN20OntologyTester.view;

import at.fh.BPMN20OntologyTester.controller.FxController;
import javafx.fxml.FXML;

/**
 * Handels user interactions on tab "Ontology Tests"
 * 
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class OwlTestSuiteTabFxController implements FxController {

	public OwlTestSuiteTabFxController() {
	}

	@FXML
	private void initialize() {
	}

	private void appendLog(String text) {
		MainSceneFxController.getInstance().appendLog(text);
	}

	// TODO: Define Button Functionality and stuff
}
