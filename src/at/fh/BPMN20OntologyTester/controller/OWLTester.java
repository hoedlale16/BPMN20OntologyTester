/**
 * 
 */
package at.fh.BPMN20OntologyTester.controller;

import java.util.HashSet;
import java.util.Set;

import javax.print.DocFlavor.STRING;

import org.camunda.bpm.model.xml.instance.DomElement;
import org.semanticweb.HermiT.model.DataRange;

import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction.DataRangeEnum;
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
			if (!ontology.existsEntity(s))
				notFoundInOWL.add(s);
		}
		return notFoundInOWL;
	}
	
	public static Set<OWLClassRestriction> testBPMNElementMeetOWLRestrictions(DomElement bpmnElement ,Set<OWLClassRestriction> owlClassRestrictions) {
		Set<OWLClassRestriction> failedRestrictions = new HashSet<OWLClassRestriction>();
		
		
		for(OWLClassRestriction r : owlClassRestrictions) {
			
			//Get Name which node(Child-Node or attribute) is effected
			String affectedProperty = r.getOnProperty().getIRI().getShortForm();
			
			if(r.affectsChildNode()) {
				//TODO: further look into childs
				//Here we have to to some mapping i guess
			} else {
				//Restriction affects DOm-Attribute
				String bpmnRawValue = bpmnElement.getAttribute(affectedProperty);
				
				switch (r.getOnDataRange()) {
					case DataRangeBoolean:
						if ( ! checkAttributeRestriction(r, Boolean.parseBoolean(bpmnRawValue)) ) {
							failedRestrictions.add(r);
						}
						break;
					case DataRangeInteger:
						if ( ! checkAttributeRestriction(r,  Integer.parseInt(bpmnRawValue)) ) {
							failedRestrictions.add(r);
						}
						break;
					case DataRangeString:
						if ( ! checkAttributeRestriction(r, bpmnRawValue) ) {
							failedRestrictions.add(r);
						}
						break;
					default:
						//Not supported DataRange
						failedRestrictions.add(r);
						break;
				}
				
			}
			
			
			
			
		}
		
		return failedRestrictions;
	}
	
	
	

	/**
	 * Tests if restriction is met. Returns true if restriction is met otherwhise false
	 * @param r
	 * @param bpmnValue
	 */
	private static boolean checkAttributeRestriction(OWLClassRestriction r, int bpmnValue) {
		switch (r.getCardinalityType()) {
			case ExactCardinality: return (bpmnValue == r.getCardinality());
			case MinCardinality: return (bpmnValue > r.getCardinality());
			case MaxCardinality: return (bpmnValue <= r.getCardinality());
			default: {
				//If this happens, some crazy shit is going on
				return false;
			}
		}
	}
	
	private static boolean checkAttributeRestriction(OWLClassRestriction r, boolean bpmnValue) {
		switch (r.getCardinalityType()) {
			//In boolean case there must be an exact match!
			case ExactCardinality: return (bpmnValue == r.getCardinalityAsBoolean());
			default: {
				//If this happens, some crazy shit is going on
				return false;
			}
		}
	}
	
	private static boolean checkAttributeRestriction(OWLClassRestriction r, String bpmnValue) {
		switch (r.getCardinalityType()) {
			//String Compare must be exact!
			case ExactCardinality: return (bpmnValue.equals(r.getCardinalityAsString()));
			default: {
				//If this happens, some crazy shit is going on
				return false;
			}
		}
	}

}
