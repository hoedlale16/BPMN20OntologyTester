package at.fh.BPMN20OntologyTester.view.fxcontroller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Participant;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainSceneFxController {
	
	//GUI Elements from Ontology Tab
	@FXML private Label lbClasses;
	@FXML private Label lbObjProperties;
	@FXML private Label lbDataProperties;
	@FXML private Label lbDescUndocEntities;
	@FXML private ListView<String> lstUnDocumentedEntities;
	@FXML private ChoiceBox<String> cbDocumentedEnties;
	@FXML private TextArea taOntDescription;
	
	
	//GUI Elements from "Test OWL" Tab
	@FXML private Button btLoadBPMN;
	
	
	//Logging required variables
	@FXML private TextArea taLog;
	private final SimpleDateFormat dateFormater;
	
	//Get initialized on startup of application
	private OWLModel ontology;

	
	public MainSceneFxController() {
		dateFormater = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}
	
	
	@FXML
	private void initialize()
	{   
		try {
			appendLog("Read and initialize BPMN2.0 Ontology");
			ontology = OntologyHandler.getInstance().getBpmn20Ontology();
			
			//Show results on GUI
			showInitializedOntology();

		} catch (Exception e) {
			appendLog("Error while loading Ontology: " + e.getMessage());
			e.printStackTrace();
		}		
		
		
	}
	
	private void showInitializedOntology() {
		lbClasses.setText("" + ontology.getClasses().size() );
		lbObjProperties.setText("" + ontology.getObjectProperties().size());
		lbDataProperties.setText("" + ontology.getDataProperties().size());
		
		//show Classes without rdfs:comment annotation
		ObservableList<String> undocItems =FXCollections.observableArrayList();
        for(OWLEntity e : ontology.getUnDocumentedEntities()) {
        	undocItems.add(e.getIRI().getShortForm());
        }
        lbDescUndocEntities.setText("Undocumented Entities (" + undocItems.size() + ")");
        lstUnDocumentedEntities.setItems(undocItems);
        
        //Show entities which rdfs:comment annotation
        ObservableList<String> docItems =FXCollections.observableArrayList();
        for(OWLEntity e : ontology.getDocumentedEntities()) {
        	docItems.add(e.getIRI().getShortForm());
        }
        cbDocumentedEnties.setItems(docItems);
        

        
        //TESTING AREA
       OWLClass activity = (OWLClass) ontology.getEntityByName("Activity");
       Set<OWLClassAxiom> rest = ontology.getRestrictionsOfClass(activity);
       for(OWLClassAxiom r: rest) {
    	   System.out.println(r);
       }
       
        
       
       if(ontology.isEntityOWLClass(activity)) {
    	   System.out.println("A OWLClass");
    	   //System.out.println(ontology.getCommentOfEntity(activity)); 
       } else {
    	   System.out.println("Not a OWLClass");
       }

	}
	
	
	@FXML
	private void onShowOntologyEntityDescription() {
		String strSelItem = cbDocumentedEnties.getSelectionModel().getSelectedItem();
		if( ! strSelItem.isEmpty() ) {
			OWLEntity entity = ontology.getEntityByName(strSelItem);
			taOntDescription.setText(ontology.getCommentOfEntity(entity));
		}
	}
	
	
	@FXML
	private void onLoadBPMN() {

		try {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Load BPMN2.0 Process Modell");
			
			chooser.getExtensionFilters().add(new ExtensionFilter("BPMN 2.0", "*.bpmn"));
			chooser.getExtensionFilters().add(new ExtensionFilter("XML-BPMN","*.xml"));
			
			//Handle selected file
			File selectedFile = chooser.showOpenDialog(null);
			//TODO: Test:
			//File selectedFile = new File("resource/ExampleProcessModel.bpmn")
			
			if(selectedFile != null) {
				BPMNModelHandler bpmnModelHandler = new BPMNModelHandler();
				BPMNModel model = bpmnModelHandler.readModelFromFile(selectedFile);

				appendLog("=== Read BPMN-File <" + selectedFile.getAbsolutePath() + "> ===");
				
		        //Show Collaboration-Infos
		        ExtensionElements e = model.getCollaboration().getExtensionElements();
		        appendLog(" - Extension-Elements: " + e.getElements().size());
		        for(Participant p : model.getCollaboration().getParticipants())
		        {
		        	appendLog(" - Participant: " +p.getName());
		        }
		        		        
		        //Show processes of model
		        List<Process> procList = model.getProcesses();            
		        appendLog("Found Processes in Model (" + procList.size()+ "):");            
		        for(int i = 0; i < procList.size(); i++)
		        {
		        	Process p = procList.get(i);
		        	appendLog("Process (" + (i+1) +")");
		        	appendLog("  Name: " +p.getName());
		        	List<StartEvent> starts = (List<StartEvent>) p.getChildElementsByType(StartEvent.class);
		        	List<EndEvent> ends = (List<EndEvent>) p.getChildElementsByType(EndEvent.class);
		        	appendLog("  Elements in Prozess:" + p.getDomElement().getChildElements().size());
		        	appendLog("  Start: " + starts.get(0).getName());
		        	appendLog("  End(s): " +ends.toString());
		
		        	if( !(i == procList.size()-1) )
		        		appendLog("-----");
		        }
			}
		} catch(Exception e) 
		{
			appendLog("ERROR - Failed to load File");
		}
	}
	 
	public void appendLog(String text) {
		taLog.appendText(dateFormater.format(new Date()) +":\t" + text + "\n");
	}

}
