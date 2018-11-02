/**
 * 
 */
package at.fh.BPMN20OntologyTester;

import java.io.FileNotFoundException;

import org.semanticweb.owlapi.model.OWLClass;

import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.OWLModel;

/**
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class MyTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			

			OWLModel ontology = OntologyHandler.getInstance().getBpmn20Ontology();
			
			OWLClass owlClass = (OWLClass) ontology.getEntityByShortName("SubProcess");
			ontology.getAllRestrictionsOfClass(owlClass);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
