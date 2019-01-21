/**
 * 
 */
package at.fh.BPMN20OntologyTester.model;

import at.fh.BPMN20OntologyTester.model.enums.OWLRestrictionFailingLevelEnum;

/**
 * This Class represents an failed Restriction between BPMN-Model and
 * OWL-Restriction. During the test the affected restriction was not able to met
 * because of given reason
 * 
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class FailedOWLClassRestriction {

	private final String failingReason;
	// Represents the OWLClass which failed
	private final OWLClassRestriction restriction;
	private final OWLRestrictionFailingLevelEnum failingLevel;

	
	/**
	 * @param failingReason
	 * @param restriction
	 */
	public FailedOWLClassRestriction(String failingReason, OWLClassRestriction restriction) {
		super();
		this.failingReason = failingReason;
		this.restriction = restriction;
		this.failingLevel = OWLRestrictionFailingLevelEnum.ERROR;

	}

	public FailedOWLClassRestriction(OWLRestrictionFailingLevelEnum failLevel, String failingReason,
			OWLClassRestriction restriction) {
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
		if(restriction.getOnClass().isPresent()) {
			sb.append("[OWL-Class: ").append(restriction.getOnClass().get().getIRI().getShortForm()).append("] - ");
		} else {
			sb.append("[OWL-Property: ").append(restriction.getOnProperty().getIRI().getShortForm()).append("] - ");
		}

		switch (failingLevel) {
		case ERROR:
			sb.append("ERR: ");
			break;
		case WARNING:
			sb.append("WRN: ");
			break;
		}
		sb.append(failingReason);

		return sb.toString();
	}

	public OWLClassRestriction getRestriction() {
		return restriction;
	}

	public OWLRestrictionFailingLevelEnum getFailingLevel() {
		return failingLevel;
	}

	public boolean isErrorFailure() {
		if (failingLevel == OWLRestrictionFailingLevelEnum.ERROR) {
			return true;
		}

		return false;
	}

}
