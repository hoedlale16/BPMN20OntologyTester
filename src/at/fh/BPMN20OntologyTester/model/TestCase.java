/**
 * 
 */
package at.fh.BPMN20OntologyTester.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.camunda.bpm.model.bpmn.instance.Process;

import at.fh.BPMN20OntologyTester.controller.OWLTester;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.controller.Owl2BpmnNamingMapper;
import at.fh.BPMN20OntologyTester.controller.OwlConformanceClassHandler;
import at.fh.BPMN20OntologyTester.model.enums.OWLConformanceClassEnum;
import at.fh.BPMN20OntologyTester.model.enums.TestCaseEnum;

/**
 * Represents a OWL<->ProcessModel Testcase and holds the testresults
 *
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 */
public class TestCase {

	private final OWLModel ontology;
	private final BPMNModel processModel;
	private final Owl2BpmnNamingMapper owl2bpmnMapping;

	// Holds the testresults
	private final Set<BPMNElement> resultsXmlNodesWithoutOWLClass = new HashSet<BPMNElement>();
	private final Set<String> resultsXmlAttributesWithoutOWLProperty = new HashSet<String>();
	private final Map<Process, List<BPMNElement>> resultsXmlNodesFailOWLRestrictions = new HashMap<Process, List<BPMNElement>>();

	private boolean ignoreTcSpecificData = false;

	

	/**
	 * @param ontology
	 * @param processModel
	 * @param owl2bpmnMapping
	 * @throws Exception 
	 */
	public TestCase(OWLModel ontology, BPMNModel processModel, Owl2BpmnNamingMapper owl2bpmnMapping) throws Exception {
		this.ontology = ontology;
		this.processModel = processModel;
		this.owl2bpmnMapping = owl2bpmnMapping;
		
		if (this.ontology == null) {
			throw new Exception ("Ontology not loaded - unable to create testcase!");
		} 
		if(this.processModel == null) {
			throw new Exception("No process model given! - unable to create testcase!");
		}
	}

	public TestCase(OWLModel ontology, BPMNModel processModel, Owl2BpmnNamingMapper owl2bpmnMapping,
			boolean ignoreSpecificData)  throws Exception {
		this.ontology = ontology;
		this.processModel = processModel;
		this.owl2bpmnMapping = owl2bpmnMapping;
		this.ignoreTcSpecificData = ignoreSpecificData;
		
		if (this.ontology == null) {
			throw new Exception ("Ontology not loaded - unable to create testcase!");
		} 
		if(this.processModel == null) {
			throw new Exception("No process model given! - unable to create testcase!");
		}
	}
	
	/**
	 * Creates a TestCase with given proessModel. 
	 * Uses loaded Ontology and Mapping file to create Testcase
	 * @param processModel
	 * @throws Exception - Throws exception if ontology or process model is null
	 */
	public TestCase(BPMNModel processModel) throws Exception {

		Optional<OWLModel> optOntology = OntologyHandler.getInstance().getLoadedOntology();
		if (! optOntology.isPresent()) {
			throw new Exception ("Ontology not loaded - unable to create testcase!");
		} 
		if(processModel == null) {
			throw new Exception("No process model given! - unable to create testcase!");
		}
		
		this.ontology = optOntology.get();
		this.processModel = processModel;
		this.owl2bpmnMapping = Owl2BpmnNamingMapper.getInstance();
	}

	public OWLModel getOntology() {
		return ontology;
	}

	public BPMNModel getProcessModel() {
		return processModel;
	}

	public Owl2BpmnNamingMapper getOwl2bpmnMapping() {
		return owl2bpmnMapping;
	}

	public boolean isIgnoreTcSpecificData() {
		return ignoreTcSpecificData;
	}

	public void setIgnoreTcSpecificData(boolean ignoreTcSpecificData) {
		this.ignoreTcSpecificData = ignoreTcSpecificData;
	}

	public Set<BPMNElement> getResultsXmlNodesWithoutOWLClass() {
		return resultsXmlNodesWithoutOWLClass;
	}

	public Set<String> getResultsXmlAttributesWithoutOWLProperty() {
		return resultsXmlAttributesWithoutOWLProperty;
	}

	public Map<Process, List<BPMNElement>> getResultsXmlNodesFailOWLRestrictions() {
		return resultsXmlNodesFailOWLRestrictions;
	}

	public void executeTest(TestCaseEnum testcase) {
		switch (testcase) {
		case XMLElementsAsOWLClasses: {
			resultsXmlNodesWithoutOWLClass.clear();
			resultsXmlNodesWithoutOWLClass.addAll(OWLTester.testXMLNodesExsistAsOWLClasses(ontology, processModel,
					owl2bpmnMapping, ignoreTcSpecificData));
			break;
		}
		case XMlAttributesAsOWLProperties: {
			resultsXmlAttributesWithoutOWLProperty.clear();
			resultsXmlAttributesWithoutOWLProperty.addAll(OWLTester.testXMLAttributesExsistAsOWLProperties(ontology,
					processModel, owl2bpmnMapping, ignoreTcSpecificData));
			break;
		}
		case XMLElementFailOWLClassRestrictions: {
			resultsXmlNodesFailOWLRestrictions.clear();
			resultsXmlNodesFailOWLRestrictions.putAll(OWLTester.testXMLNodesMeedOWLClassRestrictions(ontology,
					processModel, owl2bpmnMapping, ignoreTcSpecificData));
			break;
		}
		}
	}

	public void executeAllTests() {
		executeTest(TestCaseEnum.XMLElementsAsOWLClasses);
		executeTest(TestCaseEnum.XMlAttributesAsOWLProperties);
		executeTest(TestCaseEnum.XMLElementFailOWLClassRestrictions);
		
		determineConformanceClassOfModel();
	}
	
	public void determineConformanceClassOfModel() {
		processModel.setConformanceClass(getConformanceClassesOfModel());
	}
	
	public OWLConformanceClassEnum getConformanceClassesOfModel() {
		OwlConformanceClassHandler confClassHandler = OwlConformanceClassHandler.getInstance();
		return confClassHandler.getHighestConformanceClass(getConformanceClasses());
	}
	
	public Map<OWLConformanceClassEnum, List<BPMNElement>> getConformanceClasses() {
		return OWLTester.getConformanceClassOfElements(ontology, processModel, owl2bpmnMapping);
	}

	
	public String getTestResultReport() {
		StringBuilder sb = new StringBuilder();
		int totalFailedOWLClassRestrictions = 0;
		for(Process p : resultsXmlNodesFailOWLRestrictions.keySet()) {
			totalFailedOWLClassRestrictions += resultsXmlNodesFailOWLRestrictions.get(p).size();
		}
		
		sb.append("Testreport ob Ontology").append("\n").append("\n");
		sb.append("General OWL Data:").append("\n")
		  .append(" - OWL-Classes: ").append(ontology.getOWLClasses().size()).append("\n")
		  .append(" - OWL-DataProperties: ").append(ontology.getDataProperties().size()).append("\n")
		  .append(" - OWL-ObjectProperties: ").append(ontology.getObjectProperties().size()).append("\n\n");
		
		sb.append("General OWL Issues:").append("\n")
		  .append(" - Processmodel Conformance Class: ").append(processModel.getConformanceClass()).append("\n")
		  .append(" - Number of XML-Nodes without OWL-Class: ").append(resultsXmlNodesWithoutOWLClass.size()).append("\n")
		  .append(" - Number of XML-Attributes without OWL-Propertiy: ").append(resultsXmlAttributesWithoutOWLProperty.size()).append("\n")
		  .append(" - Number of XML-Nodes which failed the OWL-Class Restriction: ").append(totalFailedOWLClassRestrictions).append("\n\n");

		sb.append("Detailed List of XML-Nodes without OWL-Classes:").append("\n");
		for(BPMNElement e: resultsXmlNodesWithoutOWLClass) {
			sb.append(" - ").append(e.getDomLocalName()).append("\n");
		}
		sb.append("\n");
		sb.append("Detailed List of XML-Attributes without OWL-Property").append("\n");
		for(String attr: resultsXmlAttributesWithoutOWLProperty) {
			sb.append(" - ").append(attr).append("\n");
		}
		sb.append("\n");
		sb.append("Detailed List of XML-Nodes which failed OWL-Class Restriction:").append("\n");
		for(Process proc: resultsXmlNodesFailOWLRestrictions.keySet()) {
			sb.append(" - ").append(proc.getDomElement().getLocalName()).append("\n");
			for(BPMNElement node: resultsXmlNodesFailOWLRestrictions.get(proc)) {
				sb.append("   - ").append(node.getDomLocalName()).append("\n");
				for(FailedOWLClassRestriction fr : node.getFailedRestrictions()) {
					sb.append("     - ").append(fr.getFormattedFailingReason()).append("\n");
				}
			}
		}
		
		return sb.toString();
	}
	
}
