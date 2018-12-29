/**
 * 
 */
package at.fh.BPMN20OntologyTester;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import at.fh.BPMN20OntologyTester.controller.BPMNModelHandler;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.controller.Owl2BpmnNamingMapper;
import at.fh.BPMN20OntologyTester.controller.OwlConformanceClassHandler;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.OWLModel;

/**
 * This is a not testing environment to run fast integration tests without loading GUI(VIEW part)
 * 
 * @author Alexander Hoedl IMA16 
 * Information Management (BSc) 
 * University of applied Sciences FH JOANNEUM
 *
 */
public class MyDevelopmentTester {
	
	private static BPMNModel createBPMNModel(String resourcePath) throws Exception {
		File file = File.createTempFile("model", null);
		file.deleteOnExit();
		InputStream stream = new MyDevelopmentTester().getClass().getResourceAsStream(resourcePath);
		Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

		return BPMNModelHandler.readModelFromFile(file);
	}
	
	private static OWLModel createOntology(String resourcePath) throws Exception {
		// Initialize Ontology
		OntologyHandler owlHandler = OntologyHandler.getInstance();
		owlHandler.loadOntology(MyDevelopmentTester.class.getResourceAsStream(resourcePath));
		
		return OntologyHandler.getInstance().getLoadedOntology().get();
	}
	
	private static void initAdditionalAppData() throws Exception {
		Owl2BpmnNamingMapper owl2xmlMapper = Owl2BpmnNamingMapper.getInstance();
		owl2xmlMapper.loadMappingFromStream(
				MyDevelopmentTester.class.getResourceAsStream("/resource/owl/OWL2BPMNmapping.properties"));

		// Initialize ConformanceClassMapping
		OwlConformanceClassHandler ccHandler = OwlConformanceClassHandler.getInstance();
		ccHandler.load(MyDevelopmentTester.class.getResourceAsStream("/resource/owl/OWLconformanceClasses.properties"));

	}

	
	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		try {
			System.out.println("---- Start -----");
			// Initialize all required data
			initAdditionalAppData();		
			OWLModel ontology = createOntology("/resource/owl/BPMN20.owl");
			BPMNModel model = createBPMNModel("/resource/bpmn/ExampleProcessModel1.bpmn");
			
			//-- RUN developer tests here!---
			System.out.println("Run some Tests");
		
			System.out.println("---- Done -----");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
