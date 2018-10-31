/**
 * 
 */
package at.fh.BPMN20OntologyTester.model;

/**
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class OWLClassRestriction {

	private final OWLClassRestrictionEnum type;
	private final int cardinality;

	/**
	 * 
	 */
	public OWLClassRestriction(OWLClassRestrictionEnum restrictionType, int cardinality) {
		this.type = restrictionType;
		this.cardinality = cardinality;
		
	}

	private enum OWLClassRestrictionEnum {
		RESTRICTION_MIN, RESTRICTION_MAX, RESTRICTION_EQL
	}
}
