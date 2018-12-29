package at.fh.BPMN20OntologyTester.controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.OWLModel;

/**
 * Handler to deal with an Ontology(create out of file, ...)
 * 
 * @author Alexander Hödl IMA16 - Information Management University of applied
 *         Sciences FH JOANNEUM
 *
 */
public class OntologyHandler {

	// This Object represents the current loaded ontology
	private OWLModel loadedOntology = null;

	// Design-Pattern Singleton to take care that there is just one OntologyHandler:
	private static OntologyHandler theInstance = null;

	public static OntologyHandler getInstance() {
		if (theInstance == null)
			theInstance = new OntologyHandler();
		return theInstance;
	}

	private OntologyHandler() {
		/**
		 * To load an ontology call the loadOntolgy methods which reads the given
		 * ontoloy, and sets it 'loadedOntolgy' To get the ontolgy, call the method
		 * getLoadedOntolgy To set an external created new ontology call method
		 * setOntology
		 */
	}

	/**
	 * Method to load and create a BPMN20 Ontology from given file and sets loaded
	 * ontology as currently loaded one To get the loaded ontology, just call method
	 * getLoadedOntolgy which returns the loaded ontology or Optional.empty if not
	 * set
	 * 
	 * @param file
	 *            - OWL-File to load which represents the BPMN20 Ontology
	 * @throws OWLOntologyCreationException
	 */
	public void loadOntology(File file) throws OWLOntologyCreationException {
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
		setOntology(new OWLModel(ontology, file));
	}

	/**
	 * Method to load and create a BPMN20 Ontology from given inputstream and sets loaded
	 * ontology as currently loaded one To get the loaded ontology, just call method
	 * getLoadedOntolgy which returns the loaded ontology or Optional.empty if not
	 * set
	 * 
	 * @param stream
	 *            - OWL to load from stream which represents the BPMN20 Ontology
	 * @throws OWLOntologyCreationException
	 */
	public void loadOntology(InputStream stream) throws OWLOntologyCreationException {
		try {
			// Parse content to a temporary File to store it for the DOM and parse it for
			// the OWL Manager as well
			File file = File.createTempFile("ontology", null);
			file.deleteOnExit();
			Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

			OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
			setOntology(new OWLModel(ontology, file));
		} catch (Exception e) {
			throw new OWLOntologyCreationException("Unable to load ontology from inputstream", e);
		}
	}

	/**
	 * Sets a new Ontology for the application
	 * 
	 * @param newOntology
	 */
	public void setOntology(OWLModel newOntology) {
		this.loadedOntology = newOntology;
	}

	/**
	 * Return the BPMN20Ontology Object which is initialized on creation of this
	 * Handler
	 * 
	 * @return
	 */
	public Optional<OWLModel> getLoadedOntology() {
		if (loadedOntology == null) {
			return Optional.empty();
		} else {
			return Optional.of(loadedOntology);
		}
	}
	
	
	/**
	 * Converts given process model to an ontology based on current loaded one.
	 * @param processModel
	 * @return
	 */
	public OWLOntology convertToOntology(BPMNModel processModel) throws Exception {
		Optional<OWLModel> optloadedOWL = getLoadedOntology();
		if(optloadedOWL.isPresent()) {
			OWLOntology baseOntology = optloadedOWL.get().getOntology();
			
			//Create a new empty Ontology
			OWLOntology newOntology = OWLManager.createOWLOntologyManager().createOntology();
			
			
			//Get all Elements of process Model 
			Set<String> elementNames = processModel.getAllElementsOfModel(true).
										stream().map(d -> d.getLocalName()).collect(Collectors.toSet());
			
			//For each element, load all Axioms(Propertys,Classes,...) and add to new Ontology
			for(String elem : elementNames) {
				OWLEntity owlEntity = optloadedOWL.get().getEntityByShortName(elem);
				if(owlEntity != null) {
					Stream<OWLAxiom> axioms =baseOntology.referencingAxioms(owlEntity);
					if(axioms != null)
						newOntology.addAxioms(axioms);
				}
			}
			
			return newOntology;
			
			
		} else {
			throw new Exception("No base Ontology found!");
		}
	}
	
	public void saveOntology(File file, OWLOntology ontology) throws OWLOntologyStorageException {
		//Create the manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		//Use the same format as the base(loaded) ontology
		OWLOntology baseOntology = getLoadedOntology().get().getOntology();
		OWLDocumentFormat format = manager.getOntologyFormat(baseOntology);
		if(format == null) {
			format = new OWLXMLDocumentFormat();
		}
		manager.saveOntology(ontology, format, IRI.create(file.toURI()));
	}

}
