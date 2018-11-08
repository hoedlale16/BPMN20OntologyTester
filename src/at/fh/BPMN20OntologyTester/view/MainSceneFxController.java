package at.fh.BPMN20OntologyTester.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;


/**
 * Handles user interactions and initializations of the main frame of the application.
 * 
 * 
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class MainSceneFxController {

	// GUI Elements from Main GUI
	@FXML
	private TextArea taLog;
	private final SimpleDateFormat dateFormater;
	
	private static MainSceneFxController theInstance = null;
	
	public static MainSceneFxController getInstance() {
		if(theInstance == null)
			theInstance = new MainSceneFxController();
		
		return theInstance;
	}
	
	public MainSceneFxController() {
		dateFormater = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		theInstance = this;
	}

	@FXML
	private void initialize() {
	}

	public void appendLog(String text) {
		taLog.appendText(dateFormater.format(new Date()) + ":\t" + text + "\n");
	}	
}
