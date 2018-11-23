/**
 * 
 */
package at.fh.BPMN20OntologyTester.model;

import org.camunda.bpm.model.xml.instance.DomElement;

/**
 * This Class represents an failed Restriction  between BPMN-Model and OWL-Restriction.
 * During the test the affected restriction was not able to met because of given reason
 * 
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class FailedOWLClassRestriction {

	
	private final String failingReason;
	//Represents the OWLClass which failed
	private final OWLClassRestriction restriction;
	private final RestrictionFailingLevelEnum failingLevel;
	
	public enum RestrictionFailingLevelEnum {
			WARNING,
			ERROR;
	}
	/**
	 * @param failingReason
	 * @param restriction
	 */
	public FailedOWLClassRestriction(String failingReason, OWLClassRestriction restriction) {
		super();
		this.failingReason = failingReason;
		this.restriction = restriction;
		this.failingLevel = RestrictionFailingLevelEnum.ERROR;
		
	}
	
	public FailedOWLClassRestriction(RestrictionFailingLevelEnum failLevel, String failingReason, OWLClassRestriction restriction) {
		super();
		this.failingReason = failingReason;
		this.restriction = restriction;
		this.failingLevel = failLevel;
		
	}
	
	public String getFailingReason() {
		return failingReason;
	}
	
	public String getFormattedFailingReason() {
		StringBuilder sb = new StringBuilder();
		sb.append("[OWL-Class: ").append(restriction.getOnClass().getIRI().getShortForm()).append("] - ");
		
		switch(failingLevel) {
			case ERROR:   sb.append("ERR: "); break;
			case WARNING: sb.append("WRN: "); break;
		}
		sb.append(failingReason);
		
		return sb.toString();
	}
	
	public OWLClassRestriction getRestriction() {
		return restriction;
	}

	public RestrictionFailingLevelEnum getFailingLevel() {
		return failingLevel;
	}	
	
	public boolean isErrorFailure() {
		if (failingLevel == RestrictionFailingLevelEnum.ERROR) {
			return true;
		}
		
		return false;
	}
	
}
