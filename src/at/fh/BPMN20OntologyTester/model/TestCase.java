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

import org.w3c.dom.Element;

import at.fh.BPMN20OntologyTester.controller.OWLTester;
import at.fh.BPMN20OntologyTester.controller.OntologyHandler;
import at.fh.BPMN20OntologyTester.controller.XmlElement2OWLClassesMapper;
import at.fh.BPMN20OntologyTester.controller.OwlConformanceClassHandler;
import at.fh.BPMN20OntologyTester.controller.XmlAttribute2OWLPropertyMapper;
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
	private final XmlElement2OWLClassesMapper xmlElem2owlClassMapper;
	private final XmlAttribute2OWLPropertyMapper xmlAttr2owlPropMapper;

	// Holds the testresults
	private final Set<BPMNElement> resultsXmlNodesWithoutOWLClass = new HashSet<BPMNElement>();
	private final Set<String> resultsXmlAttributesWithoutOWLProperty = new HashSet<String>();
	private final Map<Element,Set<FailedOWLClassRestriction>> resultsXmlNodesFailOWLRestrictions = new HashMap<Element,Set<FailedOWLClassRestriction>>();

	/**
	 * @param ontology
	 * @param processModel
	 * @param owl2bpmnMapping
	 * @throws Exception 
	 */
	public TestCase(OWLModel ontology,  BPMNModel processModel, 
					XmlElement2OWLClassesMapper xmlElem2owlClassMapper,
					XmlAttribute2OWLPropertyMapper xmlAttr2owlPropMapper) throws Exception {
		this.ontology = ontology;
		this.processModel = processModel;
		this.xmlElem2owlClassMapper = xmlElem2owlClassMapper;
		this.xmlAttr2owlPropMapper = xmlAttr2owlPropMapper;
		
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
		this.xmlElem2owlClassMapper = XmlElement2OWLClassesMapper.getInstance();
		this.xmlAttr2owlPropMapper = XmlAttribute2OWLPropertyMapper.getInstance();
	}

	public OWLModel getOntology() {
		return ontology;
	}

	public BPMNModel getProcessModel() {
		return processModel;
	}
	
	public String getFileNameOfProcessMOdelCreatedOf() {
		return processModel.getFileFromWhomModelWasCreated().getName();
	}

	public XmlElement2OWLClassesMapper getOwl2bpmnMapping() {
		return xmlElem2owlClassMapper;
	}


	public Set<BPMNElement> getResultsXmlNodesWithoutOWLClass() {
		return resultsXmlNodesWithoutOWLClass;
	}

	public Set<String> getResultsXmlAttributesWithoutOWLProperty() {
		return resultsXmlAttributesWithoutOWLProperty;
	}

	public Map<Element,Set<FailedOWLClassRestriction>> getResultsXmlNodesFailOWLRestrictions() {
		return resultsXmlNodesFailOWLRestrictions;
	}

	public void executeTest(TestCaseEnum testcase) throws Exception {
		switch (testcase) {
		case XMLElementsAsOWLClasses: {
			resultsXmlNodesWithoutOWLClass.clear();
			resultsXmlNodesWithoutOWLClass.addAll(OWLTester.testXMLElementsExistsAsOWLClasses(ontology, processModel,
					xmlElem2owlClassMapper));
			break;
		}
		case XMlAttributesAsOWLProperties: {
			resultsXmlAttributesWithoutOWLProperty.clear();
			resultsXmlAttributesWithoutOWLProperty.addAll(OWLTester.testXMLAttributesExsistAsOWLProperties(ontology,
					processModel, xmlAttr2owlPropMapper));
			break;
		}
		case XMLElementFailOWLClassRestrictions: {
			resultsXmlNodesFailOWLRestrictions.clear();
			resultsXmlNodesFailOWLRestrictions.putAll(OWLTester.testXMLElementsMeetOWLClassRestrictions(ontology,
					processModel, xmlElem2owlClassMapper, xmlAttr2owlPropMapper));
			break;
		}
		}
	}

	public void executeAllTests() throws Exception {
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
		return OWLTester.getConformanceClassOfElements(ontology, processModel, xmlElem2owlClassMapper);
	}

	
	public String getTestResultReport() {
		StringBuilder sb = new StringBuilder();
		int totalFailedOWLClassRestrictions = 0;
		for(Element e : resultsXmlNodesFailOWLRestrictions.keySet()) {
			totalFailedOWLClassRestrictions += resultsXmlNodesFailOWLRestrictions.get(e).size();
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
		for(Element e: resultsXmlNodesFailOWLRestrictions.keySet()) {
			sb.append(" - ").append(e.getLocalName()).append("\n");
			for(FailedOWLClassRestriction fcr: resultsXmlNodesFailOWLRestrictions.get(e)) {
					sb.append("   - ").append(fcr.getFormattedFailingReason()).append("\n");
			}
		}
		
		return sb.toString();
	}
	
}
