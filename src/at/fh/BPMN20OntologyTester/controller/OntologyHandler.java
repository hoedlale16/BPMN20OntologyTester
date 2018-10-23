package at.fh.BPMN20OntologyTester.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import at.fh.BPMN20OntologyTester.model.OWLModel;

public class OntologyHandler {
	
	private static OntologyHandler theInstance = null;
	
	public static OntologyHandler getInstance() 
	throws OWLOntologyCreationException,FileNotFoundException {
		if (theInstance == null)
			theInstance = new OntologyHandler();
		return theInstance;
	}
	
	//This Object represents the famous BPMN20Ontology
	private final OWLModel bpmn20Ontology;
	
	private OntologyHandler()
	throws OWLOntologyCreationException, FileNotFoundException
	{
		//Load file from resource folder
		File file = new File("resource/bpmn/BPMN20.owl");	
		if (! file.exists())
			throw new FileNotFoundException("BPMN20.owl not found in resource folder!");
			
		//Initialize Ontology object with file.
		bpmn20Ontology = loadBPMN20Ontology(file);
	}
	
	/**
	 * Method to load and initialize the BPMN20 Ontology 
	 * @param file - OWL-File to load which represents the BPMN20 Ontology
	 * @return Ontology as object
	 * @throws OWLOntologyCreationException 
	 */
	private OWLModel loadBPMN20Ontology(File file) throws OWLOntologyCreationException {
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
		return new OWLModel(ontology);
	}	

	/**
	 * Return the BPMN20Ontology Object which is initialized on creation of this Handler
	 * @return
	 */
	public OWLModel getBpmn20Ontology() {
		return bpmn20Ontology;
	}
	
	

}
