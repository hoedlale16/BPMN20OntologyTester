package at.fh.BPMN20OntologyTester.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Collaboration;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.DomElement;

/**
 * Representation of an .bpmn File(XML) (BPMN-Model)
 * 
 * @author Alexander Hödl
 * IMA16 - Information Management
 * University of applied Sciences FH JOANNEUM
 *
 */
public class BPMNModel {

	private final BpmnModelInstance model;

	public BPMNModel(BpmnModelInstance model) {
		this.model = model;
	}

	/**
	 * Return the Collaboration of the model which represents the head information
	 * in XLM this is represented by TAG 'collaboration' and occur once
	 * 
	 * @return
	 */
	public Collaboration getCollaboration() {
		List<Collaboration> collList = (List<Collaboration>) model.getModelElementsByType(Collaboration.class);
		return collList.get(0);
	}

	/**
	 * Returns all Processes of the Model. in XML this is represented by TAG
	 * 'process' and can occur more than once
	 * 
	 * @return
	 */
	public List<Process> getProcesses() {
		return (List<Process>) model.getModelElementsByType(Process.class);
	}

	/**
	 * Return process object for given name
	 * @param name
	 * @return
	 */
	public Process getProcessByName(String name) {
		for (Process p : getProcesses()) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * Returns a list of StartEvents forgiven Process
	 * @return
	 */
	public List<StartEvent> getStartEventsForProcess(Process p) {
		return (List<StartEvent>) p.getChildElementsByType(StartEvent.class);
	}
	
	/**
	 * Returns a list of EndEvents for given Process
	 * @param p
	 * @return
	 */
	public List<EndEvent> getEndEventsForProcess(Process p) {
		return (List<EndEvent>) p.getChildElementsByType(EndEvent.class);
	}

	/**
	 * Return the BPMNDiagram of the model which represents graphical representation
	 * in XLM this is represented by TAG 'bpmndi:BPMNDiagram' and occur once. If the
	 * element can not found, method will return null.
	 * 
	 * @return
	 */
	public BpmnDiagram getBPMNDiagram() {
		List<BpmnDiagram> dia = (List<BpmnDiagram>) model.getModelElementsByType(BpmnDiagram.class);

		if (dia == null || dia.isEmpty())
			return null;
		return dia.get(0);
	}

	/**
	 * Returns the process description of the BPMN Model
	 * 
	 * @return
	 */
	public Optional<String> getProcessDescription() {
		BpmnDiagram dia = this.getBPMNDiagram();
		if (dia != null) {
			return Optional.of(dia.getDocumentation());
		}

		return Optional.empty();
	}

	/**
	 * Returns the ModelInstance itself
	 * 
	 * @return
	 */
	public BpmnModelInstance getModel() {

		return model;
	}

	/**
	 * Returns the main Definitions Tag from an .bpmn file which contains -
	 * Namespace - Modelname - ...
	 * 
	 * @return
	 */
	public Definitions getModelDefinitions() {
		Definitions def = model.getDefinitions();
		return def;
	}

	/**
	 * Returns a Set of Elements which occure in the model. Required to check if the
	 * ontolgy has an entry for all of this elements
	 * 
	 * @return
	 */
	public Set<String> getAllElementsOfModel(boolean ignoreFromExtensionElementsField) {
		Set<String> elements = new HashSet<String>();

		DomDocument doc = model.getDocument();
		addElementToSet(doc.getRootElement(), elements, ignoreFromExtensionElementsField);

		return elements;
	}

	/**
	 * Helper Method to add an Element and his childs to the element Set in a
	 * recursive way
	 * 
	 * @param element
	 * @param elementSet
	 */
	private void addElementToSet(DomElement element, Set<String> elementSet,boolean ignoreExtenionElements) {
		elementSet.add(element.getLocalName());

		if(! "extensionElements".equals(element.getLocalName())) {
			
			// Iterate over all Childs and add them now...
			for (DomElement e : element.getChildElements()) {
				// calls this method for all recursivle to add all the children which is Element
				addElementToSet(e, elementSet,ignoreExtenionElements);
			}
		} else {
			//Check if we are allowed to add elemets from extensionElementsFiled as well
			if (! ignoreExtenionElements) {
				for (DomElement e : element.getChildElements()) {	
					addElementToSet(e, elementSet,ignoreExtenionElements);
				}
			}
		}
	}
}
