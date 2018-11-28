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

import org.camunda.bpm.model.xml.instance.DomElement;
import org.semanticweb.owlapi.model.OWLClass;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.OWLTester;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.controller.Owl2BPMNMapper;
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
		for(OWLClassRestriction r : ontology.getAllOWLClassRestrictionOfOWLClass(owlClass, true)) {
			System.out.println(r.toFormattedToString());

		}
		System.out.println("Done");
	}
	
	
	private static void testRestrictionofElement(DomElement element, BPMNModel model, OWLModel ontology) throws Exception {
		System.out.println("Test restrictions of Class <"+element.getLocalName()+">");
		//Get OWL2XML-Naming Mappig
		Owl2BPMNMapper owl2bpmnMapper = Owl2BPMNMapper.getInstance();
		
		Set<FailedOWLClassRestriction> failedRestrictions = OWLTester.testBPMNElementMeetOWLRestrictions(element, ontology,owl2bpmnMapper,false);
		//Just output for Elements which have failed restrictions
		if( failedRestrictions.size() > 0) {
			OWLClass owlClass = ontology.getOWLClassByShortNameIgnoreCase(element.getLocalName());
			int totalRestrictions = ontology.getAllOWLClassRestrictionOfOWLClass(owlClass, true).size();
			
			System.out.println("  - Total Restrictions:  " + totalRestrictions);
			System.out.println("  - Failed Restrictions: " + failedRestrictions.size());
		}
	}
	
	private static void generateNewMappingFile(OWLModel ontology) throws Exception {

		Owl2BPMNMapper.getInstance().generateNewMappingFile(ontology);
		File mappingFile = new File("C:/Users/Alexander/Desktop/OWL2XMLmapping.properties");
		Owl2BPMNMapper.getInstance().save(mappingFile);
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		try {
			System.out.println("---- Start -----");	
			//Initialize Ontology
			OntologyHandler owlHandler = OntologyHandler.getInstance();
			owlHandler.loadOntology(MyTester.class.getResourceAsStream("/resource/owl/BPMN20.owl"));
			OWLModel ontology = OntologyHandler.getInstance().getLoadedOntology().get();
			
			//Initialize Model
			BPMNModel model = createBPMNModel();
			
			generateNewMappingFile(ontology);
			
			//RESTRICITON TESTS;
			/*
			String elementName = "startEvent";
			OWLClass owlClass = ontology.getOWLClassByShortNameIgnoreCase(elementName);
			Set<OWLClassRestriction> x = ontology.getAllOWLClassRestrictionOfOWLClass(owlClass, true);
			System.out.println(x.size());
			System.out.println("--");
			Set<OWLClassRestriction> x1 = ontology.getAllOWLClassRestrictionOfOWLClass(owlClass, true);
			System.out.println(x1.size());
			 */
			//Show Restrictions of element
			//showRestrictionsOfElement(elementName,ontology);

			//Test restrictions of DOM-Element 			
			/*Process proc = model.getProcessByName("Posteingang");
			for(DomElement d: model.getProcessElementsAsDomElements(proc)) {
				testRestrictionofElement(d,model,ontology);
			}*/

			/*for (DomElement startEvent : model.getProcessElementsAsDomElements(proc)) {
				if(startEvent.getLocalName().equals("startEvent")) {
					testRestrictionofElement(startEvent, model, ontology);					
				}
			}*/

			
			
			System.out.println("---- Done -----");	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
