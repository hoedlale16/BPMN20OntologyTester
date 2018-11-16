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
	/**
	 * @param failingReason
	 * @param restriction
	 */
	public FailedOWLClassRestriction(String failingReason, OWLClassRestriction restriction) {
		super();
		this.failingReason = failingReason;
		this.restriction = restriction;
	}
	
	
	public String getFailingReason() {
		return failingReason;
	}
	
	public OWLClassRestriction getRestriction() {
		return restriction;
	}	
}
