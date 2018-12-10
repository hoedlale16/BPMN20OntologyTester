/**
 * 
 */
package at.fh.BPMN20OntologyTester.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;

import at.fh.BPMN20OntologyTester.model.BPMNElement;
import at.fh.BPMN20OntologyTester.model.enums.OWLConformanceClassEnum;

/**
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class OwlConformanceClassHandler {
	
	private static OwlConformanceClassHandler theInstance = null;
	public static OwlConformanceClassHandler getInstance() {
		if(theInstance == null)
			theInstance = new OwlConformanceClassHandler();
		return theInstance;
	}
	
	private final Properties conformanceClasses;
	
	private OwlConformanceClassHandler() {
		this.conformanceClasses = new Properties();
		
	}
	
	/**
	 * Initialize Conformance classe from given file
	 */
	public void load(File file) throws Exception {
		load(new FileInputStream(file));
	}

	public void load(InputStream stream) throws Exception {
		try {
			if (stream != null) {
				this.conformanceClasses.load(stream);
			} else {
				throw new Exception("Given input is null - unable to load conformance classes!");
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
	
	public boolean hasMappings() {
		return !conformanceClasses.isEmpty();
	}
	
	public boolean isOWLClassConformanceClass(OWLClass c) {
		if(c != null) {
			return conformanceClasses.keySet().contains(c.getIRI().getShortForm());
		}
		return false;
	}
	
	/**
	 * Return priority of Conformance Class
	 * 0 .. UNKOWN
	 * 1 .. DESCRIPTIVE
	 * 2 .. ANALYTIC
	 * 3 .. EXECUTIVE
	 * @param conformanceClass
	 * @return
	 */
	public int getOWLClassConformancePriorioty(OWLConformanceClassEnum conformance) {
		switch(conformance) {
			case DescriptiveConformance: return 1;
			case AnalyticConformance: return 2;
			case ExecutiveConformance: return 3;
			case FullConformance: return 4;
			default: return 0;
		}
	}
	
	/**
	 * Maps given OWLCLass to OWLClassconformanceEnum
	 * @param oc
	 * @return
	 */
	public OWLConformanceClassEnum getMappedEnumForOWLConformanceClass(OWLClass oc) {
		if (isOWLClassConformanceClass(oc)) {
			return OWLConformanceClassEnum.valueOf(conformanceClasses.getProperty(oc.getIRI().getShortForm()));
		}
		return OWLConformanceClassEnum.Unkown;
	}
	
	/**
	 * Compares given current confidence Class with given OWLClass.
	 * If the given OWLCLass is from a higher confidence class return the confidence class of OWLClass
	 * Otherwise stick to the current confidence class and return that one.
	 * @param currConfClass
	 * @param owlClass
	 * @return
	 */
	public OWLConformanceClassEnum getHigherConfClass(OWLConformanceClassEnum currConfClass, OWLClass owlClass) {
		OWLConformanceClassEnum confClassOfOWLClass = getMappedEnumForOWLConformanceClass(owlClass);
		
		if(isNewConformanceClassHigher(currConfClass,confClassOfOWLClass))
			return confClassOfOWLClass;
		else
			return currConfClass;
		
	}
	
	public OWLConformanceClassEnum getHighestConformanceClass(Map<OWLConformanceClassEnum, List<BPMNElement>> conformanceClasses) {
		OWLConformanceClassEnum highestConfClass = OWLConformanceClassEnum.Unkown;
		for(OWLConformanceClassEnum c: conformanceClasses.keySet()) {
			if(isNewConformanceClassHigher(highestConfClass, c))
				highestConfClass = c;
		}
		
		return highestConfClass;
	}
	
	private boolean isNewConformanceClassHigher(OWLConformanceClassEnum currConfClass, OWLConformanceClassEnum newConfClass) {
		int currPriorityofConfclass = getOWLClassConformancePriorioty(currConfClass);
		int priorityOfGivenOWLClass = getOWLClassConformancePriorioty(newConfClass);
		
		if(currPriorityofConfclass < priorityOfGivenOWLClass)
			return true;
		else
			return false;
	}

}
