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

import org.semanticweb.owlapi.model.OWLEntity;

import at.fh.BPMN20OntologyTester.model.OWLModel;

/**
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class Owl2BPMNMapper {
	
	private final Properties owl2xmlMapping;
	
	private static Owl2BPMNMapper theInstance;
	public static Owl2BPMNMapper getInstance() {
		if(theInstance == null)
			theInstance = new Owl2BPMNMapper();
		
		return theInstance;
	}
	
	private Owl2BPMNMapper() {
		//Initialize empty mapping
		this.owl2xmlMapping = new Properties();
	}

	public void initializeMapping(InputStream stream) throws Exception {	    	
    	try {
    		if(stream != null) {
    			owl2xmlMapping.load(stream);
    		} else {
    			throw new Exception("Given input is null - unable to load properties!");
    		}
 
    	} catch (IOException ex) {
    		throw ex;
        } finally{
        	if(stream!=null){
        		try {
        			stream.close();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	}
        }
	}
	
	public void initializeMapping(File file) throws Exception {
		initializeMapping (new FileInputStream(file));
	}
	
	/**
	 * Initialize a new Mapping which is a 1:1 mapping of each OWL-Entry.
	 * Overwrites the existing mapping.
	 * To get the mapping call method 'getMapping()'
	 * @param ontology
	 */
	public void generateNewMappingFile(OWLModel ontology) {
		
		//Clears all previous stored properties
		owl2xmlMapping.clear();
		
		//Iterate over all OWL-Entites and generate a 1:1 mapping
		for(OWLEntity e: ontology.getAllEntities()) {
			owl2xmlMapping.setProperty(e.getIRI().getShortForm(), e.getIRI().getShortForm());
		}
	}
	
	public void setMapping(String owlEntityName, String xmlElementName) {
		owl2xmlMapping.setProperty(owlEntityName, xmlElementName);
	}
	
	/**
	 * Returns the mapped Processmodelle(XML) Element(Attribute or Node) to given OWL-Entity Name(Class or Property)
	 * @param owlEntityName
	 * @return
	 */
	public Optional<String> getXmlElementName(String owlEntityName) {
		String xmlElement = owl2xmlMapping.getProperty(owlEntityName);
		
		if(xmlElement == null) {
			return Optional.empty();
		}
		
		return Optional.of(xmlElement);
	}
	
	/**
	 * Returns the mapped OWLEntity name for given Processmodel(XML) Element name (Attribute or Node)
	 * @param xmlElementName
	 * @return
	 */
	public Optional<String> getOwlEntityName(String xmlElementName) {
		if (owl2xmlMapping.containsValue(xmlElementName)) {
			for(Object v: owl2xmlMapping.values()) {
				String key = (String)v;
				if (key.endsWith(xmlElementName))
					return Optional.of(key);
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * Returns the current Mapping between OWL and ProcessModel(XML)
	 * @return
	 */
	public Properties getAllMappings() {
		return owl2xmlMapping;
	}
	
	public void save(File file) throws Exception {
		save(file, "Mapping OWL <-> ProcessModell(XML)");
	}
	
	public void save(File file,String comment) throws Exception {
		owl2xmlMapping.store(new FileOutputStream(file), comment);
	}
	
	public void saveAsXML(File file) throws Exception {
		owl2xmlMapping.storeToXML(new FileOutputStream(file), null);
	}

}
