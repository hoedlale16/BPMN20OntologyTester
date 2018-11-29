/**
 * 
 */
package at.fh.BPMN20OntologyTester.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.model.bpmn.instance.Process;

import at.fh.BPMN20OntologyTester.controller.OWL2BPMNMapper;
import at.fh.BPMN20OntologyTester.controller.OWLTester;

/**
 * Represents a OWL<->ProcessModel Testcase and holds the testresults
 *
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 */
public class TestCase {

	private final OWLModel ontology;
	private final BPMNModel processModel;
	private final OWL2BPMNMapper owl2bpmnMapping;

	// Holds the testresults
	private final Set<BPMNElement> resultsXmlNodesWithoutOWLClass = new HashSet<BPMNElement>();
	private final Set<String> resultsXmlAttributesWithoutOWLProperty = new HashSet<String>();
	private final Map<Process, List<BPMNElement>> resultsXmlNodesFailOWLRestrictions = new HashMap<Process, List<BPMNElement>>();

	private boolean ignoreTcSpecificData = false;

	public enum TestCaseEnum {
		XMLNodesAsOWLClass, XMlAttributesAsOWLProperties, XMLNodeFailOWLClassRestrictions,
	}

	/**
	 * @param ontology
	 * @param processModel
	 * @param owl2bpmnMapping
	 */
	public TestCase(OWLModel ontology, BPMNModel processModel, OWL2BPMNMapper owl2bpmnMapping) {
		this.ontology = ontology;
		this.processModel = processModel;
		this.owl2bpmnMapping = owl2bpmnMapping;
	}

	public TestCase(OWLModel ontology, BPMNModel processModel, OWL2BPMNMapper owl2bpmnMapping,
			boolean ignoreSpecificData) {
		this.ontology = ontology;
		this.processModel = processModel;
		this.owl2bpmnMapping = owl2bpmnMapping;
		this.ignoreTcSpecificData = ignoreSpecificData;
	}

	public OWLModel getOntology() {
		return ontology;
	}

	public BPMNModel getProcessModel() {
		return processModel;
	}

	public OWL2BPMNMapper getOwl2bpmnMapping() {
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
		case XMLNodesAsOWLClass: {
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
		case XMLNodeFailOWLClassRestrictions: {
			resultsXmlNodesFailOWLRestrictions.clear();
			resultsXmlNodesFailOWLRestrictions.putAll(OWLTester.testXMLNodesMeedOWLClassRestrictions(ontology,
					processModel, owl2bpmnMapping, ignoreTcSpecificData));
			break;
		}
		}
	}

	public void executeAllTets() {
		executeTest(TestCaseEnum.XMLNodesAsOWLClass);
		executeTest(TestCaseEnum.XMlAttributesAsOWLProperties);
		executeTest(TestCaseEnum.XMLNodeFailOWLClassRestrictions);
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
