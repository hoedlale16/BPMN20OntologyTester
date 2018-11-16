/**
 * 
 */
package at.fh.BPMN20OntologyTester.model;

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.model.xml.instance.DomElement;

/**
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class BPMNElement {
	
	private final DomElement bpmnDomElement;
	private final Set<FailedOWLClassRestriction> failedRestrictions;
	private final String guiDisplayName;
	/**
	 * @param bpmnDomElement
	 * @param failedRestrictions
	 */
	public BPMNElement(DomElement bpmnDomElement, Set<FailedOWLClassRestriction> failedRestrictions, String guiDisplayName) {
		super();
		this.bpmnDomElement = bpmnDomElement;
		this.failedRestrictions = failedRestrictions;
		this.guiDisplayName = guiDisplayName;
	}
	public BPMNElement(DomElement bpmnDomElement, Set<FailedOWLClassRestriction> failedRestrictions) {
		super();
		this.bpmnDomElement = bpmnDomElement;
		this.failedRestrictions = failedRestrictions;
		this.guiDisplayName = bpmnDomElement.getLocalName();
	}
	
	public BPMNElement(DomElement bpmnDomElement,String guiDisplayName) {
		super();
		this.bpmnDomElement = bpmnDomElement;
		this.failedRestrictions = new HashSet<FailedOWLClassRestriction>();
		this.guiDisplayName = guiDisplayName;
	}
	
	public BPMNElement(DomElement bpmnDomElement) {
		super();
		this.bpmnDomElement = bpmnDomElement;
		this.failedRestrictions = new HashSet<FailedOWLClassRestriction>();
		this.guiDisplayName = bpmnDomElement.getLocalName();
	}
	
	
	public DomElement getBpmnDomElement() {
		return bpmnDomElement;
	}
	public Set<FailedOWLClassRestriction> getFailedRestrictions() {
		return failedRestrictions;
	}
	
	public String getGUIDisplayName() {
		return guiDisplayName;
	}
	
	
	public void addFailedRestriction(FailedOWLClassRestriction r) {
		failedRestrictions.add(r);
	}
	
	public void addFailedRestrictions(Set<FailedOWLClassRestriction> restrictions) {
		failedRestrictions.addAll(restrictions);
	}
	
	
	@Override
	public String toString() {
		return guiDisplayName;
	}
	
	
	

}
