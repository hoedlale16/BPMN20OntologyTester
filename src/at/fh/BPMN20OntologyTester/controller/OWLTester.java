/**
 * 
 */
package at.fh.BPMN20OntologyTester.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.semanticweb.owlapi.model.OWLClass;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.fh.BPMN20OntologyTester.model.BPMNElement;
import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLModel;
import at.fh.BPMN20OntologyTester.model.enums.OWLConformanceClassEnum;
import at.fh.BPMN20OntologyTester.model.enums.OWLRestrictionFailingLevelEnum;

/**
 * Controller to compare loaded Ontology with process model.
 * 
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class OWLTester {

	/**
	 * Simple test if given ontology contains all model elements(xml-nodes) as OWL
	 * Class. For the naming mapping (OWl-Class/Properties and XML-Node/Attribute
	 * Names the owl2bpmnmapper is used. Returns a list of Elements which ontology
	 * does not understand.
	 * 
	 * @param ontology
	 *            - Ontology to test
	 * @param model
	 *            - Process modell to test against ontolgy
	 * @param owl2bpmnMapper
	 *            - Mapper to map use correct names of each part for mapping
	 * @param ignoreExtensionElements
	 *            - flag if elements in exteionElements should be ignored. These
	 *            elements are customized element according the BPMN-2.0 Standard
	 * @return
	 */
	public static Set<BPMNElement> testXMLNodesExsistAsOWLClasses(OWLModel ontology, BPMNModel model,
			Owl2BpmnNamingMapper owl2bpmnMapper) {
		Set<BPMNElement> notFoundInOWL = new HashSet<BPMNElement>();

		// Each XML-Node must exist as OWL-Class in the Ontology.
		// Each Attribute of an XML-Test must exist as OWL-Property in the Ontology
		Set<DomElement> elements = model.getAllElementsOfModel();
		for (DomElement element : elements) {

			String mappedOWLname = owl2bpmnMapper.getMappedNameFor(element.getLocalName(), true);

			if (!ontology.existsOWLClassWithName(mappedOWLname)) {
				notFoundInOWL.add(new BPMNElement(element));
			}
		}
		return notFoundInOWL;
	}

	/**
	 * Simple test if given ontology contains all for all attributes of each model
	 * elements(xml-nodes) as OWLProperty. For the naming mapping
	 * (OWl-Class/Properties and XML-Node/Attribute Names the owl2bpmnmapper is
	 * used. Returns a list of Elements which ontology does not understand.
	 * 
	 * @param ontology
	 *            - Ontology to test
	 * @param model
	 *            - Process modell to test against ontolgy
	 * @param owl2bpmnMapper
	 *            - Mapper to map use correct names of each part for mapping
	 * @param ignoreExtensionElements
	 *            - flag if elements in exteionElements should be ignored. These
	 *            elements are customized element according the BPMN-2.0 Standard
	 * @return
	 */
	public static Set<String> testXMLAttributesExsistAsOWLProperties(OWLModel ontology, BPMNModel model,
			Owl2BpmnNamingMapper owl2bpmnMapper) {
		Set<String> notFoundInOWL = new HashSet<String>();

		for (String xmlAttr : model.getAllAttributesOfModel()) {
			String mappedOWLname = owl2bpmnMapper.getMappedNameFor(xmlAttr, true);
			if (!ontology.existsOWLPropertyWithName(mappedOWLname)) {
				notFoundInOWL.add(xmlAttr);
			}
		}
		return notFoundInOWL;
	}

	/**
	 * Tests if Elements in Process model meet all defined Restrictions. Returns a
	 * map build in following way: - Key: Represent the process where the Element
	 * with failing Restrictions exists - Value: List of Elements wich failed
	 * Restrictions
	 * 
	 * @param ontology
	 * @param model
	 * @param owl2bpmnMapper
	 * @param ignoreWarningRestrictions
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public static Map<Element, Set<FailedOWLClassRestriction>> testXMLNodesMeedOWLClassRestrictions(OWLModel ontology,
			BPMNModel model, Owl2BpmnNamingMapper owl2bpmnMapper, boolean ignoreWarningRestrictions) throws Exception {

		Map<Element, Set<FailedOWLClassRestriction>> failedNodes = new HashMap<Element, Set<FailedOWLClassRestriction>>();

		//Return a List of all Elements from the document
		NodeList nodeList = model.getRawDOMDocument().getElementsByTagName("*");
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == node.ELEMENT_NODE) {
				Element element = (Element) node;
				// Get mapped OWL name for XML-Node
				String elementName = element.getTagName().substring(element.getTagName().indexOf(":") + 1);
				
				String mappedOWLname = owl2bpmnMapper.getMappedNameFor(elementName, true);
				Optional<OWLClass> owlClass = ontology.getOWLClassByShortNameIgnoreCase(mappedOWLname);
				if (owlClass.isPresent()) {
					Set<OWLClassRestriction> restrictions = ontology
							.getAllOWLClassRestrictionOfOWLClass(owlClass.get());

					Set<FailedOWLClassRestriction> elementFailedRestrictions = testXMLElement(element, restrictions,
							owl2bpmnMapper);
					if (!elementFailedRestrictions.isEmpty()) {
						failedNodes.put(element, elementFailedRestrictions);
					}
				} else {
					System.out.println("No OWL-Class found for XML-Element <" +elementName + ">");
				}
			}
		}
		return failedNodes;
	}

	/**
	 * Helper method to test a single XML Element
	 * @param element
	 * @param restrictions
	 * @param owl2bpmnMapper
	 * @return
	 */
	private static Set<FailedOWLClassRestriction> testXMLElement(Element element,
			Set<OWLClassRestriction> restrictions, Owl2BpmnNamingMapper owl2bpmnMapper) {

		Set<FailedOWLClassRestriction> failedRestrictions = new HashSet<FailedOWLClassRestriction>();
		
		Optional<FailedOWLClassRestriction> optFCR;
		for(OWLClassRestriction restriction: restrictions) {
			if(restriction.isFulfilledByAttribute()) {
				optFCR = testXMLAttributeRestrictions(element, restriction, owl2bpmnMapper);
			} else {
				optFCR = testXMLChildRestrictions(element, restriction, owl2bpmnMapper);
			}
			
			if(optFCR.isPresent()) {
				failedRestrictions.add(optFCR.get());
			}
			
		}


		return failedRestrictions;
	}

	/**
	 * Helper to test if element fulfill all restrictoins based on attributes
	 * 
	 * @param element
	 * @param attributeRestrictions
	 * @param owl2bpmnMapper
	 * @return
	 */
	private static Optional<FailedOWLClassRestriction> testXMLAttributeRestrictions(Element element,
			OWLClassRestriction attRestriction, Owl2BpmnNamingMapper owl2bpmnMapper) {

		String originalOWLPropertyName = attRestriction.getOnProperty().getIRI().getShortForm();
		String affectedXMLAttribute = owl2bpmnMapper.getMappedNameFor(originalOWLPropertyName, false);

		// Attribute just can appear 0 or 1...
		int bpmnOccurance = element.hasAttribute(affectedXMLAttribute) ? 1 : 0;
		if (!isRestrictionCardinaltyMet(bpmnOccurance, attRestriction)) {
			String errMsg = "Restriction expected XML-Attribute <" + affectedXMLAttribute + "> at Element <" 
						+ element.getTagName().substring(element.getTagName().indexOf(":")+1) + "> <"
						+ attRestriction.getCardinality() + "> times with Matchtype <"
						+ attRestriction.getCardinalityType() + "> but element occured in BPMN <" + bpmnOccurance
						+ "> times.";

			return Optional.of(
						new FailedOWLClassRestriction(OWLRestrictionFailingLevelEnum.ERROR, errMsg, attRestriction));
		}
		
		//Test Datatype
		if(bpmnOccurance > 0 ) {
			String rawValue = element.getAttribute(affectedXMLAttribute);
			if ( ! isRestrictionDataTypeMet(rawValue,attRestriction) ) {
				String errMsg = "Restriction expected XML-Attribute <" + affectedXMLAttribute + "> at Element <" 
						+ element.getTagName().substring(element.getTagName().indexOf(":")+1) + "> with datatype <"
						+ attRestriction.getCardinalityType() + "but element found raw value <"
						+ rawValue + ">";

			return Optional.of(
						new FailedOWLClassRestriction(OWLRestrictionFailingLevelEnum.ERROR, errMsg, attRestriction));
			}

		}

		return Optional.empty();
	}

	/**
	 * Helper to test if element fullfill all restrictions based on XML-Childs
	 * 
	 * @param element
	 * @param xmlElementRestrictions
	 * @param owl2bpmnMapper
	 * @return
	 */
	private static Optional<FailedOWLClassRestriction> testXMLChildRestrictions(Element element,
			OWLClassRestriction childRestriction, Owl2BpmnNamingMapper owl2bpmnMapper) {
	
		String originalOWLPropertyName = childRestriction.getOnProperty().getIRI().getShortForm();
		String affectedXMLElement = owl2bpmnMapper.getMappedNameFor(originalOWLPropertyName, false);

		// Calculate amount of appeared XMl-Childs with name
		int bpmnOccurance = element.getElementsByTagName(affectedXMLElement).getLength();
		if (!isRestrictionCardinaltyMet(bpmnOccurance, childRestriction)) {
			String errMsg = "Restriction expected XML-Child-Element <" + affectedXMLElement + "> at Element <" 
					+  element.getTagName().substring(element.getTagName().indexOf(":")+1) + "> <"
					+ childRestriction.getCardinality() + "> times with Matchtype <"
					+ childRestriction.getCardinalityType() + "> but element occured in BPMN <" + bpmnOccurance
					+ "> times.";
			return Optional.of(
					new FailedOWLClassRestriction(OWLRestrictionFailingLevelEnum.ERROR, errMsg, childRestriction));
		}

		return Optional.empty();
	}

	/**
	 * Returns List of Elements assigned to conformance class according ontology
	 * 
	 * @param ontology
	 * @param model
	 * @param owl2bpmnMapper
	 * @return
	 */
	public static Map<OWLConformanceClassEnum, List<BPMNElement>> getConformanceClassOfElements(OWLModel ontology,
			BPMNModel model, Owl2BpmnNamingMapper owl2bpmnMapper) {

		Map<OWLConformanceClassEnum, List<BPMNElement>> conformanceClasses = new HashMap<OWLConformanceClassEnum, List<BPMNElement>>();

		// Collect all conformance Classes of model
		for (Process proc : model.getProcesses()) {
			for (DomElement elem : model.getProcessElementsAsDomElements(proc)) {
				String mappedOWLname = owl2bpmnMapper.getMappedNameFor(elem.getLocalName(), true);
				Optional<OWLClass> owlClass = ontology.getOWLClassByShortNameIgnoreCase(mappedOWLname);
				if (owlClass.isPresent()) {

					OWLConformanceClassEnum conformanceClass = ontology.getConformanceClassOfOWLClass(owlClass.get());

					if (conformanceClasses.containsKey(conformanceClass)) {
						conformanceClasses.get(conformanceClass).add(new BPMNElement(elem));
					} else {
						ArrayList<BPMNElement> hs = new ArrayList<BPMNElement>();
						hs.add(new BPMNElement(elem));
						conformanceClasses.put(conformanceClass, hs);
					}
				}
			}
		}

		return conformanceClasses;
		// Determine the highest one
		// return
		// OwlConformanceClassHandler.getInstance().getHighestConformanceClass(conformanceClasses);
	}

	

	private static boolean isRestrictionCardinaltyMet(int bpmnOccurance, OWLClassRestriction restriction) {
		switch (restriction.getCardinalityType()) {
		case ExactCardinality:
			return (bpmnOccurance == restriction.getCardinality());
		case MinCardinality:
			return (bpmnOccurance >= restriction.getCardinality());
		case MaxCardinality:
			return (bpmnOccurance <= restriction.getCardinality());
		default:
			return false;
		}
	}

	private static boolean isRestrictionDataTypeMet(String bpmnRawValue, OWLClassRestriction rest) {
		switch (rest.getOnDataRange()) {
		case DataRangeString:
			return true; // String is always true...
		case DataRangeBoolean:
			return ("true".equalsIgnoreCase(bpmnRawValue) || "false".equalsIgnoreCase(bpmnRawValue));
		case DataRangeInteger: {
			try {
				Integer.parseInt(bpmnRawValue);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
		default:
			return false;
		}
	}

}
