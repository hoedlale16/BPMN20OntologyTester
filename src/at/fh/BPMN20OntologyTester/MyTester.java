/**
 * 
 */
package at.fh.BPMN20OntologyTester;

import java.io.File;

import org.semanticweb.owlapi.model.OWLClass;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLModel;

/**
 * This is a not producitve main to test implementations in a fast way without loading a gUI and stuff
 * 
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
			//Initialize Ontology nd model
			OWLModel ontology = OntologyHandler.getInstance().getBpmn20Ontology();
			BPMNModel model = BPMNModelHandler.readModelFromFile(new File("/path/to/your/bpmn/file.bpmn"));
			
			
			
			//Test methods
			OWLClass owlClass = (OWLClass) ontology.getEntityByShortName("SubProcess");
			for(OWLClassRestriction r: ontology.getOWLClassRestrictionOfOWLClass(owlClass,true)) {
				System.out.println(r);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
