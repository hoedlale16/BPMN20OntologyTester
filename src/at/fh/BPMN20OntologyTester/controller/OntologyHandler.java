package at.fh.BPMN20OntologyTester.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import at.fh.BPMN20OntologyTester.model.OWLModel;

/**
 * Handler to deal with an Ontology(create out of file, ...)
 * 
 * @author Alexander Hödl IMA16 - Information Management University of applied
 *         Sciences FH JOANNEUM
 *
 */
public class OntologyHandler {

	// This Object represents the famous BPMN20Ontology
	private OWLModel bpmn20Ontology = null;

	// Design-Pattern Singleton:
	// Take care that only one instance of the Ontology exists in the whole
	// application
	private static OntologyHandler theInstance = null;

	public static OntologyHandler getInstance() throws OWLOntologyCreationException, FileNotFoundException {
		if (theInstance == null)
			theInstance = new OntologyHandler();
		return theInstance;
	}

	private OntologyHandler() throws OWLOntologyCreationException, FileNotFoundException {
		/*
		 * Just called one due to design pattern singleton. Creates Ontology by reading
		 * from resource File as Stream as initialization on startup If an other
		 * ontology is required, overwrite loaded one with method loadOntolgy or
		 * setOntology.
		 */
		setOntology(loadOntology(getClass().getResourceAsStream("/resource/owl/BPMN20.owl")));
	}

	/**
	 * Method to load and create a BPMN20 Ontology from given file. Must set
	 * explizit via method 'setBpmn20Ontology' for usage
	 * 
	 * @param file
	 *            - OWL-File to load which represents the BPMN20 Ontology
	 * @return Ontology as object
	 * @throws OWLOntologyCreationException
	 */
	public OWLModel loadOntology(File file) throws OWLOntologyCreationException {
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
		return new OWLModel(ontology, file);
	}

	/**
	 * Method to load and initialize the BPMN20 Ontology from an input stream.
	 * 
	 * @param stream
	 *            - OWL to load from stream which represents the BPMN20 Ontology
	 * @return Ontology as object
	 * @throws OWLOntologyCreationException
	 */
	private OWLModel loadOntology(InputStream stream) throws OWLOntologyCreationException {
		try {
			// Parse content to a temporary File to store it for the DOM and parse it for
			// the OWL Manager as well
			File file = File.createTempFile("ontology", null);
			file.deleteOnExit();
			Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

			OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);

			return new OWLModel(ontology, file);
		} catch (Exception e) {
			throw new OWLOntologyCreationException(e);
		}
	}

	/**
	 * Sets a new Ontology for the application
	 * 
	 * @param newOntology
	 */
	public void setOntology(OWLModel newOntology) {
		this.bpmn20Ontology = newOntology;
	}

	/**
	 * Return the BPMN20Ontology Object which is initialized on creation of this
	 * Handler
	 * 
	 * @return
	 */
	public OWLModel getOntology() {
		return bpmn20Ontology;
	}

}
