package at.fh.BPMN20OntologyTester.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import at.fh.BPMN20OntologyTester.model.OWLModel;

/**
 * Handler to deal with an Ontology(create out of file, ...) 
 * 
 * @author Alexander Hödl
 * IMA16 - Information Management
 * University of applied Sciences FH JOANNEUM
 *
 */
public class OntologyHandler {

	// This Object represents the famous BPMN20Ontology
	private OWLModel bpmn20Ontology = null;
	
	

	//Design-Pattern Singleton:
	//Take care that only one instance of the Ontology exists in the whole application
	private static OntologyHandler theInstance = null;

	public static OntologyHandler getInstance() throws OWLOntologyCreationException, FileNotFoundException {
		if (theInstance == null)
			theInstance = new OntologyHandler();
		return theInstance;
	}


	private OntologyHandler() throws OWLOntologyCreationException, FileNotFoundException {
		//Create Ontology reading from resource File as Stream
		//This is the initial initializiation of the ontology on startup!
		setBpmn20Ontology(loadBPMN20Ontology(getClass().getResourceAsStream("/resource/bpmn/BPMN20.owl")));
	}

	/**
	 * Method to load and initialize the BPMN20 Ontology from given file
	 * 
	 * @param file
	 *            - OWL-File to load which represents the BPMN20 Ontology
	 * @return Ontology as object
	 * @throws OWLOntologyCreationException
	 */
	public OWLModel loadBPMN20Ontology(File file) throws OWLOntologyCreationException {
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
		return new OWLModel(ontology);
	}
	
	/**
	 * Method to load and initialize the BPMN20 Ontology from an input stream.
	 * 
	 * @param stream - OWL to load from stream which represents the BPMN20 Ontology
	 * @return Ontology as object
	 * @throws OWLOntologyCreationException
	 */
	private OWLModel loadBPMN20Ontology(InputStream stream) throws OWLOntologyCreationException {
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(stream);
		return new OWLModel(ontology);
	}

	/**
	 * Sets a new Ontology for the application
	 * @param newOntology
	 */
	public void setBpmn20Ontology(OWLModel newOntology) {
		this.bpmn20Ontology = newOntology;
	}
	
	/**
	 * Return the BPMN20Ontology Object which is initialized on creation of this
	 * Handler
	 * 
	 * @return
	 */
	public OWLModel getBpmn20Ontology() {
		return bpmn20Ontology;
	}

}
