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
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction.DataRangeEnum;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction.RestrictionFailingLevelEnum;

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
	public static Set<String> testAllBPMNElementsExsistAsOWLClasses(OWLModel ontology, BPMNModel model, boolean ignoreExtensionElements) {
		Set<String> notFoundInOWL = new HashSet<String>();

		
		//Each XML-Element(Tag) must exist as OWL-Class in the Ontology.
		//Each Attribute of an XML-Test must exist as OWL-Property in the Ontology
		Set<String> elements = model.getAllElementsOfModel(ignoreExtensionElements);
		for (String s : elements) {
			if (!ontology.existsClassForEntity(s))
				notFoundInOWL.add(s);
		}
		return notFoundInOWL;
	}
	
	//public static Set<BPMNElement> testBPMNElementAttributExists
	
	/**
	 * Test method to check if Restrictions defined in the OWL are fullfiled by the BPMN-Element
	 * @param bpmnElement - Element to test
	 * @param owlClassRestrictions - List of Restrictions found for this element
	 * @return
	 */
	@SuppressWarnings("incomplete-switch")
	public static Set<FailedOWLClassRestriction> testBPMNElementMeetOWLRestrictions(DomElement bpmnElement ,OWLModel ontology, boolean ignoreWarningRestrictions) throws Exception{

		Set<FailedOWLClassRestriction> failedRestrictions = new HashSet<FailedOWLClassRestriction>();

		//Try to get OWLClass for given DOM-Element of ontology
		OWLClass owlClass = ontology.getOWLClassByShortNameIgnoreCase(bpmnElement.getLocalName());
		if(owlClass == null) {
			//If we have no OWL-Class we can not test against restrictions.
			//This error must already reported in test 'testBPMNElementExistsAsOWLClass, so we can ignore it here!
			return failedRestrictions;
		}
		
		//We've found an OWL-Class, load restrictions and start checking process for each restriction
		for(OWLClassRestriction r : ontology.getAllOWLClassRestrictionOfOWLClass(owlClass, true)) {	
			Optional<FailedOWLClassRestriction> fr = testRestriction(r,bpmnElement,ignoreWarningRestrictions);
			if(fr.isPresent()) {
				failedRestrictions.add(fr.get());
			}
		}
		
		return failedRestrictions;
	}
		
	private static int getOccuredXmlTagswithName(String expectedXmlTagName, DomElement bpmnElement, int currentOccurance ) {	
		//Check if current given element matches
		if(bpmnElement.getLocalName().equals(expectedXmlTagName)) {
			currentOccurance++;
		}
		
		//Check for children of given element
		for (DomElement child : bpmnElement.getChildElements()) {
			//Recursive call to check if children have it... 
			currentOccurance = getOccuredXmlTagswithName(expectedXmlTagName,child,currentOccurance);
		}
		return currentOccurance;
	}
	
	private static Optional<FailedOWLClassRestriction> testRestriction(OWLClassRestriction restriction,DomElement bpmnElement, boolean ignoreWarningRestrictions) {
		
		String affectedProperty = restriction.getOnProperty().getIRI().getShortForm();
		boolean foundAsAttributeOrXmlTag = false;
		//TODO: properly we need a mapping between PropertyNames of OWL and XMl-Tags/Attribute Names
		
		//Determine the occurred cardinality in XML-Element. 
		int bpmnOccurance = 0;
		if (bpmnElement.hasAttribute(affectedProperty) ) {
			//If it affects an attribute it can just occur once
			bpmnOccurance = 1;
			foundAsAttributeOrXmlTag = true;
		} else {
			//If it affects an XML-Tag try to get amount with of child-tags with name
			bpmnOccurance = getOccuredXmlTagswithName(affectedProperty,bpmnElement,bpmnOccurance);

			if ( bpmnOccurance != 0)  {
				foundAsAttributeOrXmlTag = true;
			}
		}
		
		//If we are not allowed to ignore warnings check them too!
		//A warning is when the property of the restriction is not found as attribute or XML-Tag in the bpmnElement
		//but cardinality of restriction is 0 as well. In this case there could be an mapping problem as well.
		if ( ! ignoreWarningRestrictions) { 
			//Check if restriction does not explicit requires attribute or XML Tag.
			//When not it might be a problem in the OWL-Naming as well
			if ( (! foundAsAttributeOrXmlTag) && restriction.getCardinality() == 0) {
				String errMsg = "Restriction did not expect element <" + 
					restriction.getOnProperty().getIRI().getShortForm() +"> and element not found in BPMN. This might be a mapping error as well!";
				return Optional.of(new FailedOWLClassRestriction(RestrictionFailingLevelEnum.WARNING ,errMsg, restriction));
			}
		} 
		
		//We've found an XML-Element - check if cardinality of restriction is met 
		if ( ! isRestrictionCardinaltyMet(bpmnOccurance, restriction)) {
			String errMsg = "Restriction expected element <" + 
					restriction.getOnProperty().getIRI().getShortForm() +"> <" + 
					restriction.getCardinality() + "> times with Matchtype <" +
					restriction.getCardinalityType() +"> but element occured in BPMN <" + bpmnOccurance +"> times.";
			
			return Optional.of(new FailedOWLClassRestriction(RestrictionFailingLevelEnum.ERROR, errMsg, restriction));
		} 
		
		//Additional check for DataType if restriction affects an attribute in bpmnElement and DataRange is defined in OWL
		if (restriction.getOnDataRange() != DataRangeEnum.Unkown && bpmnElement.hasAttribute(affectedProperty)) {			
			String bpmnRawValue = bpmnElement.getAttribute(affectedProperty);
			if( ! isRestrictionDataTypeMet(bpmnRawValue, restriction)) {
				String errMsg = "Restriction expected dataType <" +  
						restriction.getOnDataRange() +"> for xml-attribut <" +
						restriction.getOnProperty().getIRI().getShortForm() + 
						"> but found <" +bpmnRawValue + ">" ;
				return Optional.of(new FailedOWLClassRestriction(RestrictionFailingLevelEnum.ERROR, errMsg, restriction));
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
