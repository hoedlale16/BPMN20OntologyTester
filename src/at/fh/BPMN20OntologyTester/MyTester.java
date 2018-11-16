/**
 * 
 */
package at.fh.BPMN20OntologyTester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.semanticweb.owlapi.model.OWLClass;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.OWLTester;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction;
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
	
	
	private static BPMNModel createBPMNModel() throws IOException {
		File file = File.createTempFile("model", null);
		file.deleteOnExit();
		
		InputStream stream = new MyTester().getClass().getResourceAsStream("/resource/bpmn/ExampleProcessModel.bpmn");
		
		Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);	
		
		return BPMNModelHandler.readModelFromFile(file);
	}
	
	
	private static void showRestrictionsOfElement(String elementName, OWLModel ontology) throws Exception {
		System.out.println("All Restrictions of Class <"+elementName+">");
		
		OWLClass owlClass = ontology.getOWLClassByShortNameIgnoreCase(elementName);
		for(OWLClassRestriction r : ontology.getOWLClassRestrictionOfOWLClass(owlClass, true)) {
			System.out.println(r.toFormattedToString());

		}
		System.out.println("Done");
	}
	
	
	private static void testRestrictionofElement(DomElement element, BPMNModel model, OWLModel ontology) throws Exception {
		System.out.println("Test restrictions of Class <"+element.getLocalName()+">");

		Set<FailedOWLClassRestriction> failedRestrictions = OWLTester.testBPMNElementMeetOWLRestrictions(element, ontology);
		//Just output for Elements which have failed restrictions
		if( failedRestrictions.size() > 0) {
			OWLClass owlClass = ontology.getOWLClassByShortNameIgnoreCase(element.getLocalName());
			int totalRestrictions = ontology.getOWLClassRestrictionOfOWLClass(owlClass, true).size();
			
			System.out.println("  - Total Restrictions:  " + totalRestrictions);
			System.out.println("  - Failed Restrictions: " + failedRestrictions.size());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			//Initialize Ontology and model
			OWLModel ontology = OntologyHandler.getInstance().getBpmn20Ontology();			
			BPMNModel model = createBPMNModel();
			
			//RESTRICITON TESTS;
			String elementName = "startEvent";
			//Show Restrictions of element
			showRestrictionsOfElement(elementName,ontology);
		
			//Test restrictions of DOM-Element 			
			Process proc = model.getProcessByName("Posteingang");
			for(DomElement d: model.getProcessElementsAsDomElements(proc)) {
				testRestrictionofElement(d,model,ontology);
			}

			/*for (DomElement startEvent : model.getProcessElementsAsDomElements(proc)) {
				if(startEvent.getLocalName().equals("startEvent")) {
					testRestrictionofElement(startEvent, model, ontology);					
				}
			}*/

			
			
			System.out.println("Done");	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
