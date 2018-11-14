/**
 * 
 */
package at.fh.BPMN20OntologyTester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.semanticweb.owlapi.model.OWLClass;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.OWLTester;
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
			
		//Get OWLClass for DOM-Element
		OWLClass seOWLClass = ontology.getOWLClassByShortNameIgnoreCase(element.getLocalName());
		if(seOWLClass == null) {
			System.out.println("ERROR no OWLClass found for Element <" + element.getLocalName() + ">");
			return;
		}
		
		Set<OWLClassRestriction> restrictions = ontology.getOWLClassRestrictionOfOWLClass(seOWLClass, true);
				

		System.out.println("Test Element <" + element.getLocalName() + "> with Restrictions: "  + restrictions.size());
		Set<OWLClassRestriction> failedRestrictions = OWLTester.testBPMNElementMeetOWLRestrictions(element, restrictions);
		System.out.println("Found FailedRestrictions: " + failedRestrictions.size());
		/*for(OWLClassRestriction fr: failedRestrictions) {
				System.out.println(fr);
		}*/
			
		//show valid restrictions
		restrictions.removeAll(failedRestrictions); 
		System.out.println("Passed Restrictions: " + restrictions.size());
		/*for(OWLClassRestriction pr: restrictions) {
				System.out.println(pr);
		}*/
		System.out.println("Done");
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
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
