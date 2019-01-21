/**
 * 
 */
package at.fh.BPMN20OntologyTester.view.dto;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction;

/**
 * Represents a simple BPMN-Element (e.g. startEvent, task, process)
 * 
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class BPMNElementV2 {
	
	private final Element bpmnElement;
	private final Set<FailedOWLClassRestriction> failedRestrictions;
	private String guiDisplayName;
	
	/**
	 * @param bpmnElement
	 * @param failedRestrictions
	 */
	public BPMNElementV2(Element bpmnElement, Set<FailedOWLClassRestriction> failedRestrictions, String guiDisplayName) {
		super();
		this.bpmnElement = bpmnElement;
		this.failedRestrictions = failedRestrictions;
		this.guiDisplayName = guiDisplayName;
	}
	public BPMNElementV2(Element bpmnElement, Set<FailedOWLClassRestriction> failedRestrictions) {
		super();
		this.bpmnElement = bpmnElement;
		this.failedRestrictions = failedRestrictions;
		this.guiDisplayName = bpmnElement.getLocalName();

	}

	
	
	public BPMNElementV2(Element bpmnElement,String guiDisplayName) {
		super();
		this.bpmnElement = bpmnElement;
		this.failedRestrictions = new HashSet<FailedOWLClassRestriction>();
		this.guiDisplayName = guiDisplayName;
	}
	
	public BPMNElementV2(Element bpmnElement) {
		super();
		this.bpmnElement = bpmnElement;
		this.failedRestrictions = new HashSet<FailedOWLClassRestriction>();
		this.guiDisplayName = bpmnElement.getLocalName();
	}
	
	
	/**
	 * @param string
	 */
	public BPMNElementV2(String guiDisplayName) {
		super();
		this.bpmnElement = null;
		this.failedRestrictions = new HashSet<FailedOWLClassRestriction>();
		this.guiDisplayName = guiDisplayName;
	}
	
	
	public Element getBpmnElement() {
		return bpmnElement;
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
		return bpmnElement.getLocalName();
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
		//To compare two BPMNElements for us at bpmnElement just the localname is important
		result = prime * result + ((bpmnElement == null) ? 0 : bpmnElement.getLocalName().hashCode());
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
		BPMNElementV2 other = (BPMNElementV2) obj;
		if (bpmnElement == null) {
			if (other.bpmnElement != null)
				return false;
		} else if (!bpmnElement.getLocalName().equals(other.bpmnElement.getLocalName()))
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
