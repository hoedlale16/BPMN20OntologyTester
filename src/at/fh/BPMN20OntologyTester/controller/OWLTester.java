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

import org.semanticweb.owlapi.model.OWLClass;
import org.w3c.dom.Element;

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
	 * @param mapper
	 *            - Mapper to map use correct names of each part for mapping
	 * @param ignoreExtensionElements
	 *            - flag if elements in exteionElements should be ignored. These
	 *            elements are customized element according the BPMN-2.0 Standard
	 * @return
	 */
	public static Set<BPMNElement> testXMLElementsExistsAsOWLClasses(OWLModel ontology, BPMNModel model,
			XmlElement2OWLClassesMapper mapper) {
		Set<BPMNElement> notFoundInOWL = new HashSet<BPMNElement>();

		// Each XML-Node must exist as OWL-Class in the Ontology.
		// Each Attribute of an XML-Test must exist as OWL-Property in the Ontology
		Set<Element> elements = model.getAllElementsOfModel();
		for (Element element : elements) {
			String mappedOWLname = mapper.getMappedNameFor(element.getLocalName(), true);
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
	 * @param mapper
	 *            - Mapper to map use correct names of each part for mapping
	 * @param ignoreExtensionElements
	 *            - flag if elements in exteionElements should be ignored. These
	 *            elements are customized element according the BPMN-2.0 Standard
	 * @return
	 */
	public static Set<String> testXMLAttributesExsistAsOWLProperties(OWLModel ontology, BPMNModel model,
			XmlAttribute2OWLPropertyMapper mapper) {
		Set<String> notFoundInOWL = new HashSet<String>();

		for (String xmlAttr : model.getAllAttributesOfModel()) {
			String mappedOWLname = mapper.getMappedNameFor(xmlAttr, true);
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
	 * @param xmlElem2owlClassMapper
	 * @param ignoreWarningRestrictions
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public static Map<Element, Set<FailedOWLClassRestriction>> testXMLElementsMeetOWLClassRestrictions(OWLModel ontology,
			BPMNModel model, XmlElement2OWLClassesMapper xmlElem2owlClassMapper,
			XmlAttribute2OWLPropertyMapper xmlAttr2owlPropMapper) throws Exception {

		Map<Element, Set<FailedOWLClassRestriction>> failedNodes = new HashMap<Element, Set<FailedOWLClassRestriction>>();

		for(Element element: model.getAllElementsOfModel()) {			
			// Get mapped OWL name for XML-Node
			String mappedOWLname = xmlElem2owlClassMapper.getMappedNameFor(element.getLocalName(), true);
			Optional<OWLClass> owlClass = ontology.getOWLClassByShortNameIgnoreCase(mappedOWLname);
			if (owlClass.isPresent()) {
				Set<OWLClassRestriction> restrictions = ontology
						.getAllOWLClassRestrictionOfOWLClass(owlClass.get());

				Set<FailedOWLClassRestriction> elementFailedRestrictions = testXMLElementRestrictions(element, restrictions,
						xmlElem2owlClassMapper, xmlAttr2owlPropMapper);
				if (!elementFailedRestrictions.isEmpty()) {
					failedNodes.put(element, elementFailedRestrictions);
				}
			} else {
				//Can ignored because already shown at "testXMLElementsExistsAsOWLClasses"
			}
		}
		return failedNodes;
	}

	/**
	 * Helper method to test a single XML Element
	 * @param element
	 * @param restrictions
	 * @param mapper
	 * @return
	 */
	private static Set<FailedOWLClassRestriction> testXMLElementRestrictions(Element element,
			Set<OWLClassRestriction> restrictions, 
			XmlElement2OWLClassesMapper xmlElem2owlClassMapper,
			XmlAttribute2OWLPropertyMapper xmlAttr2owlPropMapper) {

		Set<FailedOWLClassRestriction> failedRestrictions = new HashSet<FailedOWLClassRestriction>();
		
		Optional<FailedOWLClassRestriction> optFCR;
		for(OWLClassRestriction restriction: restrictions) {
			if(restriction.isFulfilledByAttribute()) {
				optFCR = testXMLAttributeRestrictions(element, restriction, xmlAttr2owlPropMapper);
			} else {
				optFCR = testXMLChildRestrictions(element, restriction, xmlElem2owlClassMapper);
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
	 * @param mapper
	 * @return
	 */
	private static Optional<FailedOWLClassRestriction> testXMLAttributeRestrictions(Element element,
			OWLClassRestriction attRestriction, XmlAttribute2OWLPropertyMapper mapper) {

		String originalOWLPropertyName = attRestriction.getOnProperty().getIRI().getShortForm();
		String affectedXMLAttribute = mapper.getMappedNameFor(originalOWLPropertyName, false);

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
						+ attRestriction.getOnDataRange() + "> but element found raw value <"
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
			OWLClassRestriction childRestriction, XmlElement2OWLClassesMapper owl2bpmnMapper) {
	
		String originalOWLPropertyName = childRestriction.getOnClass().get().getIRI().getShortForm();
		String affectedXMLElement = owl2bpmnMapper.getMappedNameFor(originalOWLPropertyName, false);

		
		// Calculate amount of appeared XMl-Childs with name. 
		int bpmnOccurance = element.getElementsByTagNameNS("*", affectedXMLElement).getLength();
		
		//Test restriction
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
			BPMNModel model, XmlElement2OWLClassesMapper owl2bpmnMapper) {

		Map<OWLConformanceClassEnum, List<BPMNElement>> conformanceClasses = new HashMap<OWLConformanceClassEnum, List<BPMNElement>>();

		// Collect all conformance Classes of model
		for (Element elem : model.getAllElementsOfModel()) {
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
		case DataRangeDouble: {
			try {
				Double.parseDouble(bpmnRawValue);
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
