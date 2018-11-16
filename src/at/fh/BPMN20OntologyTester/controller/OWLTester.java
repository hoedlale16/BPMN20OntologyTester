/**
 * 
 */
package at.fh.BPMN20OntologyTester.controller;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.camunda.bpm.model.xml.instance.DomElement;
import org.semanticweb.owlapi.model.OWLClass;

import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLModel;

/**
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class OWLTester {

	/**
	 * Simple test if given ontology contians all model elements as OWL Class
	 * Returns a list of Elements which ontology does not understand.
	 * 
	 * @param ontology
	 * @param model
	 * @param ignoreExtensionElements
	 * @return
	 */
	public static Set<String> testBPMNElementsExsistAsOWLClasses(OWLModel ontology, BPMNModel model, boolean ignoreExtensionElements) {
		Set<String> notFoundInOWL = new HashSet<String>();

		Set<String> elements = model.getAllElementsOfModel(ignoreExtensionElements);
		for (String s : elements) {
			if (!ontology.existsClassForEntity(s))
				notFoundInOWL.add(s);
		}
		return notFoundInOWL;
	}
	
	/**
	 * Test method to check if Restrictions defined in the OWL are fullfiled by the BPMN-Element
	 * @param bpmnElement - Element to test
	 * @param owlClassRestrictions - List of Restrictions found for this element
	 * @return
	 */
	@SuppressWarnings("incomplete-switch")
	public static Set<FailedOWLClassRestriction> testBPMNElementMeetOWLRestrictions(DomElement bpmnElement ,OWLModel ontology) throws Exception{

		Set<FailedOWLClassRestriction> failedRestrictions = new HashSet<FailedOWLClassRestriction>();

		//Try to get OWLClass for given DOM-Element of ontology
		OWLClass owlClass = ontology.getOWLClassByShortNameIgnoreCase(bpmnElement.getLocalName());
		if(owlClass == null) {
			//If we have no OWL-Class we can not test against restrictions.
			//This error must already reported in test 'testBPMNElementExistsAsOWLClass, so we can ignore it here!
			return failedRestrictions;
		}
		
		//We've found an OWL-Class, load restrictions and start checking process for each restriction
		for(OWLClassRestriction r : ontology.getOWLClassRestrictionOfOWLClass(owlClass, true)) {
			switch(r.getAffectedXMLPart()) {
				case ChildNodeRestriction: {
					//TODO: further look into childs
					Optional<FailedOWLClassRestriction> fr = testChildNodeRestrictions(r,bpmnElement);
					if(fr.isPresent()) {
						failedRestrictions.add(fr.get());
					}
					
					//Here we have to to some mapping i guess
					break;
				}
				case AttributeRestriction: {
					
					Optional<FailedOWLClassRestriction> fr = testAttributeRestrictions(r,bpmnElement);
					if(fr.isPresent()) {
						failedRestrictions.add(fr.get());
					}
					
					break;
				}
			}
		}
		
		return failedRestrictions;
	}
	
	private static Optional<FailedOWLClassRestriction> testChildNodeRestrictions(OWLClassRestriction restriction,DomElement bpmnElement) {
		String affectedProperty = restriction.getOnProperty().getIRI().getShortForm();
		
		//Test cardinality
		System.out.println("TODO - Implementation to test Child Node Restrictions missing!!!!");
		
		//No error found
		return Optional.empty();
		
	}
	
	private static Optional<FailedOWLClassRestriction> testAttributeRestrictions(OWLClassRestriction restriction,DomElement bpmnElement) {
		
		String affectedProperty = restriction.getOnProperty().getIRI().getShortForm();

		//Test Cardinality
		int bpmnOccurance = 0;
		if (bpmnElement.hasAttribute(affectedProperty) ) {
			bpmnOccurance = 1;
		}
		
		if ( ! isRestrictionCardinaltyMet(bpmnOccurance, restriction)) {
			String errMsg = "Restriction expected element <" + 
					restriction.getOnProperty().getIRI().getShortForm() +"> <" + 
					restriction.getCardinality() + "> times with Matchtype <" +
					restriction.getCardinalityType() +"> but element occured in BPMN <" + bpmnOccurance +"> times.";
			
			return Optional.of(new FailedOWLClassRestriction(errMsg, restriction));
		} 
		
		//Check DataType if Element occurs in BPMN-DOM-Element
		if (bpmnElement.hasAttribute(affectedProperty)) {			
			String bpmnRawValue = bpmnElement.getAttribute(affectedProperty);
			if( ! isRestrictionDataTypeMet(bpmnRawValue, restriction)) {
				String errMsg = "Restriction expected dataType <" +  restriction.getCardinalityType() +"> but found <" +bpmnRawValue + ">" ;
				return Optional.of(new FailedOWLClassRestriction(errMsg, restriction));
			}
		}
		
		//No error found
		return Optional.empty();
		
		
	}
	
	private static boolean isRestrictionCardinaltyMet(int bpmnOccurance, OWLClassRestriction restriction) {
		switch (restriction.getCardinalityType()) {
			case ExactCardinality: return (bpmnOccurance == restriction.getCardinality());
			case MinCardinality: return (bpmnOccurance >= restriction.getCardinality());
			case MaxCardinality: return (bpmnOccurance <= restriction.getCardinality());
			default: return false;
		}
	}
	
	private static boolean isRestrictionDataTypeMet(String bpmnRawValue, OWLClassRestriction rest) {
		switch (rest.getOnDataRange()) {
			case DataRangeString: return true; //String is always true...
			case DataRangeBoolean: return ("true".equalsIgnoreCase(bpmnRawValue) || "false".equalsIgnoreCase(bpmnRawValue));
			case DataRangeInteger: {
				try {
					Integer.parseInt(bpmnRawValue);
					return true;
				} catch(NumberFormatException e) {
					return false;
				}
			}
			default: return false;
		}
	}

}
