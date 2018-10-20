package at.fh.BPMN20OntologyTester.view.fxcontroller;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Participant;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainSceneFxController {
	
	@FXML private TextArea taLog;
	@FXML private Button btLoadBPMN;
	
	
	public MainSceneFxController() {
		// TODO Auto-generated constructor stub
	}
	
	
	@FXML
	private void initialize()
	{       
		appendText("=== Read and initialize Ontology: ===");
		try {
			OWLOntology ontology = OntologyHandler.getInstance().getBpmn20Ontology();
			appendText("  Ontology-Size: " +ontology.getAxiomCount());
	        List<OWLAxiom> classes = ontology.axioms().filter(ax -> ax.getAxiomType() == AxiomType.CLASS_ASSERTION).collect(Collectors.toList());	
	        appendText("  Ontology-Classes: " +classes.size());
	        for(OWLAxiom a: classes)
	        {
	        	appendText(a.toString());
	        }
		} catch (Exception e) {
			appendText("Error while loading Ontology: " + e.getMessage());
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

				appendText("=== Read BPMN-File <" + selectedFile.getAbsolutePath() + "> ===");
				
		        //Show Collaboration-Infos
		        ExtensionElements e = model.getCollaboration().getExtensionElements();
		        appendText(" - Extension-Elements: " + e.getElements().size());
		        for(Participant p : model.getCollaboration().getParticipants())
		        {
		        	appendText(" - Participant: " +p.getName());
		        }
		        		        
		        //Show processes of model
		        List<Process> procList = model.getProcesses();            
		        appendText("Found Processes in Model (" + procList.size()+ "):");            
		        for(int i = 0; i < procList.size(); i++)
		        {
		        	Process p = procList.get(i);
		        	appendText("Process (" + (i+1) +")");
		        	appendText("  Name: " +p.getName());
		        	List<StartEvent> starts = (List<StartEvent>) p.getChildElementsByType(StartEvent.class);
		        	List<EndEvent> ends = (List<EndEvent>) p.getChildElementsByType(EndEvent.class);
		        	appendText("  Elements in Prozess:" + p.getDomElement().getChildElements().size());
		        	appendText("  Start: " + starts.get(0).getName());
		        	appendText("  End(s): " +ends.toString());
		
		        	if( !(i == procList.size()-1) )
		        		appendText("-----");
		        }
			}
		} catch(Exception e) 
		{
			appendText("ERROR - Failed to load File");
		}
	}
	 
	public void appendText(String text) {
		//TODO: Aktuelles Datum hinzufuegen
		taLog.appendText(text+"\n");
	}

}
