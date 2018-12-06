package at.fh.BPMN20OntologyTester.view;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.camunda.bpm.model.bpmn.instance.Process;

import at.fh.BPMN20OntologyTester.BPMN20OntologyTester;
import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.Owl2BpmnNamingMapper;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNElement;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import at.fh.BPMN20OntologyTester.model.TestCase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * Handels user interactions on tab "Ontology Tests"
 * 
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class OwlTestSuiteTabFxController implements FxController {

	@FXML
	private TabPane tabPane;

	@FXML
	private TextArea taTestLog;

	@FXML
	private ListView<File> lstTestcases;

	@FXML
	private Button btStartTests, btRemoveTest, btLoadTestSuite, btReset, btGenerateDetailedReport;

	// List of Testcases just filled
	ObservableList<File> testCases = FXCollections.observableArrayList();

	public OwlTestSuiteTabFxController() {
	}

	@FXML
	private void initialize() {
		lstTestcases.setItems(testCases);
	}

	private void resetTestSuite() {
		testCases.clear();
		taTestLog.clear();

		// Set button to execute tests
		// Get disabled when pressed and tests created
		btStartTests.setDisable(false);

		// Remove old testcase tabs
		if (tabPane.getTabs().size() > 1) {
			tabPane.getTabs().removeIf(x -> {
				return !x.getText().equals("Overview");
			});
		}
	}

	@FXML
	private void onResetTestsuite() {
		resetTestSuite();
	}

	@FXML
	private void onLoadTestSuite() {
		try {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("BPMN2.0 Process Modells");

			chooser.setInitialDirectory(new File(System.getProperty("user.home")));
			/*
			 * chooser.getExtensionFilters().addAll(new ExtensionFilter("BPMN 2.0",
			 * "*.bpmn"), new ExtensionFilter("XML-BPMN", "*.xml"));
			 */
			// Handle selected dir
			File selectedDir = chooser.showDialog(null);
			if (selectedDir != null) {

				if (selectedDir.isDirectory()) {

					// Reset old Data
					resetTestSuite();

					for (File file : selectedDir.listFiles()) {
						if (file.isFile() && ("bpmn".equals(FilenameUtils.getExtension(file.getAbsolutePath()))
								|| "xml".equals(FilenameUtils.getExtension(file.getAbsolutePath())))) {
							testCases.add(file);
						}
					}

				}
			}
		} catch (Exception e) {
			appendLog("ERROR - Failed to load File <" + e.getMessage() + ">");
			e.printStackTrace();
		}
	}

	@FXML
	private void onRemoveSelectedTestcase() {
		File selectedItem = lstTestcases.getSelectionModel().getSelectedItem();
		if (selectedItem != null) {
			testCases.remove(selectedItem);
		}
	}

	@FXML
	private void onCreateAndExecuteTestcases() {
		// Disable button to avoid multiple triggers
		btStartTests.setDisable(true);

		// Use a sepaerate Thread to create TestResultTabs
		Task<Map<TestCase, Tab>> ontologyTestsTask = new Task<Map<TestCase, Tab>>() {
			@Override
			public Map<TestCase, Tab> call() throws Exception {
				Map<TestCase, Tab> testResultTabs = new HashMap<TestCase, Tab>();
				for (File f : testCases) {
					this.updateMessage("Test <" + f.getName() + ">");
					// Create Tab which triggers the execution of the test
					TestCase testcase = new TestCase(BPMNModelHandler.readModelFromFile(f));
					Tab tab = BPMN20OntologyTester.getTestSuiteTestResultTag(testcase);
					testResultTabs.put(testcase, tab);
				}
				return testResultTabs;
			}
		};

		// Show loading screen while running
		Stage loadingScreen= showLoadingScreenWhileTask(ontologyTestsTask);
		loadingScreen.show();

		//Trigger action after test results and tabs are created
		ontologyTestsTask.setOnSucceeded(e -> {
			StringBuilder testSuiteLog = new StringBuilder();

			Map<TestCase, Tab> testResultTabs = ontologyTestsTask.getValue();
			for (TestCase tc : testResultTabs.keySet()) {
				// Append overview log and add created Tab to tapPane
				appendOverviewLog(tc, testSuiteLog);
				tabPane.getTabs().add(testResultTabs.get(tc));
			}
			
			//Hide Loadingscreen and set overview log
			loadingScreen.hide();
			taTestLog.setText(testSuiteLog.toString());
		});
		
		//Start thread
		new Thread(ontologyTestsTask).start();
	}
	
	@FXML
	private void onExportReport() {
		if (!taTestLog.getText().isEmpty()) {
			try {
				FileChooser chooser = new FileChooser();
				chooser.setTitle("Export Testsuite report");

				chooser.getExtensionFilters().add(new ExtensionFilter("Testsuite Report", "*.txt"));

				// Handle selected file
				File selectedFile = chooser.showSaveDialog(null);

				if (selectedFile != null) {
					FileWriter writer = new FileWriter(selectedFile);
					writer.write(taTestLog.getText());
					writer.flush();
					writer.close();

					appendLog("Created Testsuite report <" + selectedFile.getAbsolutePath() + ">");
				}
			} catch (Exception e) {
				appendLog("Error - Failed to create testsuite report <" + e.getMessage() + ">");

				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error!");
				alert.setHeaderText("Error creating Testsuite report");
				alert.setContentText("ERROR - Failed to write to File <" + e.getMessage() + ">");
				alert.showAndWait();
			}
		}
	}
	
	/**
	 * Helper Method to create a Loading Screen while given task is running
	 * @param task
	 * @return
	 */
	private Stage showLoadingScreenWhileTask(Task<?> task) {
		ProgressBar pBar = new ProgressBar();
		pBar.progressProperty().bind(task.progressProperty());
		pBar.setMinWidth(400);
		Label statusLabel = new Label();
		statusLabel.textProperty().bind(task.messageProperty());
		statusLabel.setTextAlignment(TextAlignment.CENTER);
		statusLabel.setAlignment(Pos.CENTER);
		BorderPane root = new BorderPane(pBar,statusLabel,null,null,null);
		
		Stage loadingStage = new Stage();
		loadingStage.setScene(new Scene(root));
		loadingStage.setTitle("BPMN2.0 Ontology Tester");
		loadingStage.setMinWidth(400);
		loadingStage.setMinHeight(50);
		
		return loadingStage;
	}

	/**
	 * Helper Method to generate Oberview Log entry for given Testcase
	 * @param testcase
	 * @param testSuiteLog
	 */
	private void appendOverviewLog(TestCase testcase,StringBuilder testSuiteLog) {
		testSuiteLog.append(
				"Test file <" + testcase.getProcessModel().getFileFromWhomModelWasCreated().getAbsolutePath() + ">")
				.append("\n");
		testSuiteLog.append("  <").append(testcase.getResultsXmlNodesWithoutOWLClass().size())
				.append("> XML-Nodes wihtout OWL-Class").append("\n");
		testSuiteLog.append("  <").append(testcase.getResultsXmlAttributesWithoutOWLProperty().size())
				.append("> XML-Attribut without OWL-Property").append("\n");

		int totalXmlNodesFailedRestrictions = 0;
		for (Process proc : testcase.getResultsXmlNodesFailOWLRestrictions().keySet()) {
			for (BPMNElement element : testcase.getResultsXmlNodesFailOWLRestrictions().get(proc)) {
				totalXmlNodesFailedRestrictions += element.getFailedRestrictions().size();
			}
		}
		testSuiteLog.append("  <").append(totalXmlNodesFailedRestrictions)
				.append("> XML-Nodes which failed defined OWL-Restrictions").append("\n");

		testSuiteLog.append("----------------------------").append("\n");
	}
	

	private void appendLog(String text) {
		MainSceneFxController.getInstance().appendLog(text);
	}
}
