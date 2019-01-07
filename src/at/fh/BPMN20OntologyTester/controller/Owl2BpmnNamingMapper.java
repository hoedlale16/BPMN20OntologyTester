/**
 * 
 */
package at.fh.BPMN20OntologyTester.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLEntity;

import at.fh.BPMN20OntologyTester.model.OWLModel;

/**
 * The OWL2BPMN Mapping file is for tokens which need to have different names in
 * OWL and BPMN Document. If there2 is no difference, no entry in mapping file
 * is required, then a 1:1 mapping for the tokens is done The key is the
 * OWL-Entry, the value is the BPMN-Processmodel entry
 * 
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class Owl2BpmnNamingMapper {

	private final Properties owl2xmlMapping;

	private static Owl2BpmnNamingMapper theInstance;

	public static Owl2BpmnNamingMapper getInstance() {
		if (theInstance == null)
			theInstance = new Owl2BpmnNamingMapper();

		return theInstance;
	}

	private Owl2BpmnNamingMapper() {
		// Initialize property mappings. Muss filled via load-Methods
		this.owl2xmlMapping = new Properties();
	}
	
	/**
	 * Replaces current stored mapping with given new one
	 * @param newMapping
	 */
	public void replaceMapping(Properties newMapping) {
		this.owl2xmlMapping.clear();

		for(Object k : newMapping.keySet()) {
			String key = (String) k;
			String value = newMapping.getProperty(key);
			owl2xmlMapping.setProperty(key, value);
		}
	}

	/**
	 * Initialize Properties for mapping by reading given file. 
	 * Creates mapping properties out of it. to get the mapping call getter methods
	 * 
	 * @param stream
	 * @throws Exception
	 */
	public void loadMappingFromFile(File file) throws Exception {
		loadMappingFromStream(new FileInputStream(file));
	}

	/**
	 * Initialize Properties for mapping by reading given stream. 
	 * Creates mapping properties out of it. to get the mapping call getter methods
	 * 
	 * @param stream
	 * @throws Exception
	 */
	public void loadMappingFromStream(InputStream stream) throws Exception {
		try {
			if (stream != null) {
				this.owl2xmlMapping.load(stream);
			} else {
				throw new Exception("Given input is null - unable to load properties!");
			}

		} catch (IOException ex) {
			throw ex;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	/**
	 * Initialize a new Mapping which is a 1:1 mapping of each OWL-Entry. Overwrites
	 * the existing mapping. To get the mapping call method 'getMapping()'
	 * 
	 * @param ontology
	 */
	public void generateNewMappingFile(OWLModel ontology) {

		// Clears all previous stored properties
		owl2xmlMapping.clear();

		// Iterate over all OWL-Entities and generate a 1:1 mapping
		for (OWLEntity e : ontology.getAllEntities()) {
			owl2xmlMapping.setProperty(e.getIRI().getShortForm(), e.getIRI().getShortForm());
		}
	}

	/**
	 * Sets or adds a new mapping to the properties, depending if xmlElementName
	 * already exists or not
	 * 
	 * @param xmlElementName
	 * @param owlEntityName
	 */
	public void setMapping(String xmlElementName,String owlEntityName ) {
		owl2xmlMapping.setProperty(xmlElementName,owlEntityName);
	}

	/**
	 * Returns the mapped Processmodelle(XML) Element(Attribute or Node) to given
	 * OWL-Entity Name(Class or Property)
	 * 
	 * @param owlEntityName
	 * @return
	 */
	public Optional<String> getXmlElementName(String owlEntityName) {
		
		for(String key: getKeys()) {
			String value = owl2xmlMapping.getProperty(key);
			if(value.equals(owlEntityName))
				return Optional.of(key);
		}

		return Optional.empty();
	}

	/**
	 * Returns the mapped OWLEntity name for given Processmodel(XML) Element name
	 * (Attribute or Node)
	 * 
	 * @param xmlElementName
	 * @return
	 */
	public Optional<String> getOwlEntityName(String xmlElementName) {
		String xmlElement = owl2xmlMapping.getProperty(xmlElementName);

		if (xmlElement == null) {
			return Optional.empty();
		}

		return Optional.of(xmlElement);
		
	}
	
	/**
	 * Returns the mapped name for given name-token. If there is a
	 * mapping returns the mapping string otherwise the original name
	 * 
	 * @param originalName - Token to look up
	 * @param getForBPMNElement - true if token is from BPMN and OWL-Token is expected
	 * 							- false if token is from OWL and BPMN-Token is expected
	 * @return
	 */
	public String getMappedNameFor(String originalName, boolean getForBPMNElement) {	
		
		//Lookup mapped token. 
		Optional<String> optMappedName = Optional.empty();
		if(getForBPMNElement)
			optMappedName = getOwlEntityName(originalName);
		else
			optMappedName = getXmlElementName(originalName);
		
		//Check if we have an mapping key or not and return value
		if (optMappedName.isPresent()) 
			return optMappedName.get();
		
		return originalName;
	}
	

	/**
	 * Returns the current Mapping between OWL and ProcessModel(XML)
	 * 
	 * @return
	 */
	public Properties getLoadedMapping() {
		return owl2xmlMapping;
	}
	
	public Set<String> getKeys() {
		
		return owl2xmlMapping.keySet().stream().map( Object::toString).collect(Collectors.toSet());
	}
		
	/**
	 * Return true if mappings exists or false if no mappings exists
	 * @return
	 */
	public boolean hasMappings() {
		return !owl2xmlMapping.isEmpty();
	}

	/**
	 * Save mappings to given file
	 * 
	 * @param file
	 * @throws Exception
	 */
	public void save(File file) throws Exception {
		save(file, "Mapping OWL <-> ProcessModell(XML)");
	}

	/**
	 * Save mapping to given file and adds given comment to file
	 * 
	 * @param file
	 * @param comment
	 * @throws Exception
	 */
	public void save(File file, String comment) throws Exception {
		owl2xmlMapping.store(new FileOutputStream(file), comment);
	}
}
