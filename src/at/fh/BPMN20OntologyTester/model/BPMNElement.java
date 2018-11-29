/**
 * 
 */
package at.fh.BPMN20OntologyTester.model;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.camunda.bpm.model.xml.instance.DomElement;

/**
 * Represents a simple BPMN-Element (e.g. startEvent, task, process)
 * 
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class BPMNElement {
	
	private final DomElement bpmnDomElement;
	private final Set<FailedOWLClassRestriction> failedRestrictions;
	private String guiDisplayName;
	
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
	
	/**
	 * Returns all FailedOWLClassRestrictions
	 * @return
	 */
	public Set<FailedOWLClassRestriction> getFailedRestrictions() {
		return failedRestrictions;
	}
	
	/**
	 * Returns FailedRestrictions with or without filtering warnings
	 * @param ignoreWarnings
	 * @return
	 */
	public Set<FailedOWLClassRestriction> getFailedRestrictions(boolean ignoreWarnings) {
		if (ignoreWarnings)
			return failedRestrictions.stream().filter(r -> r.isErrorFailure()).collect(Collectors.toSet());
		
		return failedRestrictions;
			
	}
	
	
	public Optional<FailedOWLClassRestriction> getFailedRestrictionWithErrorText(String errText) {
		for(FailedOWLClassRestriction fr : failedRestrictions) {
			if(fr.getFormattedFailingReason().equals(errText)) {
				return Optional.of(fr);
			}
		}
		
		return Optional.empty();
	}
	
	public String getGUIDisplayName() {
		return guiDisplayName;
	}
	
	public void setGUIDisplayName(String guiDisplayName) {
		this.guiDisplayName = guiDisplayName;
	}
	
	public String getDomLocalName() {
		return bpmnDomElement.getLocalName();
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		//To compare two BPMNElements for us at bpmnDomElement just the localname is important
		result = prime * result + ((bpmnDomElement == null) ? 0 : bpmnDomElement.getLocalName().hashCode());
		result = prime * result + ((failedRestrictions == null) ? 0 : failedRestrictions.hashCode());
		result = prime * result + ((guiDisplayName == null) ? 0 : guiDisplayName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BPMNElement other = (BPMNElement) obj;
		if (bpmnDomElement == null) {
			if (other.bpmnDomElement != null)
				return false;
		} else if (!bpmnDomElement.getLocalName().equals(other.bpmnDomElement.getLocalName()))
			return false;
		if (failedRestrictions == null) {
			if (other.failedRestrictions != null)
				return false;
		} else if (!failedRestrictions.equals(other.failedRestrictions))
			return false;
		if (guiDisplayName == null) {
			if (other.guiDisplayName != null)
				return false;
		} else if (!guiDisplayName.equals(other.guiDisplayName))
			return false;
		return true;
	}
	
	
	

}
