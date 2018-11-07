/**
 * 
 */
package at.fh.BPMN20OntologyTester.controller;

import java.util.HashSet;
import java.util.Set;

import at.fh.BPMN20OntologyTester.model.BPMNModel;
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

}
