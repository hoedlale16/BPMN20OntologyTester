package at.fh.BPMN20OntologyTester.model;

import java.util.List;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Collaboration;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;

/**
 * A BPMNModel is the objectional representation of a .bpmn File(XML)
 *  
 * @author Alexander Hoedl
 *
 */
public class BPMNModel{
 	
	private final BpmnModelInstance model; 
	
	public BPMNModel(BpmnModelInstance model) {
		this.model = model;
	}
	
	/**
	 * Return the Collaboration of the model which represents the head information
	 * in XLM this is represented by TAG 'collaboration' and occur once
	 * @return
	 */
	public Collaboration getCollaboration() {
		List<Collaboration> collList = (List<Collaboration>) model.getModelElementsByType(Collaboration.class);
		return collList.get(0);
	}
	
	/**
	 * Returns all Processes of the Model.
	 * in XML this is represented by TAG 'process' and can occur more than once
	 * @return
	 */
	public List<Process> getProcesses() {		
		return  (List<Process>) model.getModelElementsByType(Process.class);
	}
	
	/**
	 * Return the BPMNDiagram of the model which represents graphical representation
	 * in XLM this is represented by TAG 'bpmndi:BPMNDiagram' and occur once
	 * @return
	 */
	public BpmnDiagram getBPMNDiagram() {
		List<BpmnDiagram> dia = (List<BpmnDiagram>) model.getModelElementsByType(BpmnDiagram.class);
		return dia.get(0);
	}
	
	/**
	 * Returns the ModelInstance itself
	 * @return
	 */
	public BpmnModelInstance getModel() {

		return model;
	}
}
