/**
 * 
 */
package at.fh.BPMN20OntologyTester.model.enums;

/**
 * Represents possoible Conformance classes. If a new one is added, take case that Enums is handeled in
 * 'getOWLClassConformancePriorioty of OwlConfomrnaceClassHandler.
 * 
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public enum OWLConformanceClassEnum {
	DescriptiveConformance,
	AnalyticConformance,
	ExecutiveConformance,
	FullConformance,
	Unkown
}
