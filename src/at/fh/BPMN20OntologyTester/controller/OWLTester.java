/**
 * 
 */
package at.fh.BPMN20OntologyTester.controller;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.camunda.bpm.model.xml.instance.DomElement;
import org.semanticweb.owlapi.model.OWLClass;

import at.fh.BPMN20OntologyTester.model.BPMNModel;
import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.FailedOWLClassRestriction.RestrictionFailingLevelEnum;
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction;
import at.fh.BPMN20OntologyTester.model.OWLClassRestriction.DataRangeEnum;
import at.fh.BPMN20OntologyTester.model.OWLModel;

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
	public static Set<String> testAllBPMNElementsExsistAsOWLClasses(OWLModel ontology, BPMNModel model,
			Owl2BPMNMapper owl2bpmnMapper, boolean ignoreExtensionElements) {
		Set<String> notFoundInOWL = new HashSet<String>();

		// Each XML-Node must exist as OWL-Class in the Ontology.
		// Each Attribute of an XML-Test must exist as OWL-Property in the Ontology
		Set<DomElement> elements = model.getAllElementsOfModel(ignoreExtensionElements);
		for (DomElement s : elements) {

			String mappedOWLname = getMappedNameForBPMNElement(s.getLocalName(), owl2bpmnMapper);

			if (!ontology.existsOWLClassWithName(mappedOWLname)) {
				notFoundInOWL.add(s.getLocalName());
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
	public static Set<String> testAllBPMNAttributesExsistAsOWLProperties(OWLModel ontology, BPMNModel model,
			Owl2BPMNMapper owl2bpmnMapper, boolean ignoreExtensionElements) {
		Set<String> notFoundInOWL = new HashSet<String>();

		// Each Attribute of an XML-Test must exist as OWL-Property in the Ontology
		Set<DomElement> elements = model.getAllElementsOfModel(ignoreExtensionElements);
		for (DomElement domElement : elements) {
			// Get all attributes of current element and check if
			// for(Attribute attribute: domElement.getAttriubtes()) {
			// TODO: Hier gehts weiter!
			String attributeName = "TODO";

			String mappedOWLname = getMappedNameForBPMNElement(attributeName, owl2bpmnMapper);

			if (!ontology.existsOWLPropertyWithName(mappedOWLname)) {
				notFoundInOWL.add(attributeName);
			}
		}
		return notFoundInOWL;
	}

	// public static Set<BPMNElement> testBPMNElementAttributExists

	/**
	 * Test method to check if Restrictions defined in the OWL are fullfiled by the
	 * BPMN-Element
	 * 
	 * @param bpmnElement
	 *            - Element of process modell to test
	 * @param ontology
	 *            - Ontolgy to test against. required to load restrictions for
	 *            element
	 * @param owl2bpmnMapper
	 *            - Mapper for correct naming match between OWL and BPMN-Model
	 * @param ignoreWarningRestrictions
	 *            - Flag if warnings should ignored or not. A warning is when the
	 *            restriction has cardinality 0 and the element is not found in the
	 *            model. In this case it could be correct but the mapping might be
	 *            wrong too. - List of Restrictions found for this element
	 * @return
	 */
	@SuppressWarnings("incomplete-switch")
	public static Set<FailedOWLClassRestriction> testBPMNElementMeetOWLRestrictions(DomElement bpmnElement,
			OWLModel ontology, Owl2BPMNMapper owl2bpmnMapper, boolean ignoreWarningRestrictions) throws Exception {

		Set<FailedOWLClassRestriction> failedRestrictions = new HashSet<FailedOWLClassRestriction>();

		// Get mapped OWL name for bpmnElement
		String mappedOWLname = getMappedNameForBPMNElement(bpmnElement.getLocalName(), owl2bpmnMapper);

		OWLClass owlClass = ontology.getOWLClassByShortNameIgnoreCase(mappedOWLname);
		if (owlClass == null) {
			// If we have no OWL-Class we can not test against restrictions.
			// This error must already reported in test 'testBPMNElementExistsAsOWLClass, so
			// we can ignore it here!
			return failedRestrictions;
		}

		// We've found an OWL-Class, load restrictions and start checking process for
		// each restriction
		for (OWLClassRestriction r : ontology.getAllOWLClassRestrictionOfOWLClass(owlClass, true)) {
			Optional<FailedOWLClassRestriction> fr = testRestriction(r, bpmnElement, owl2bpmnMapper,
					ignoreWarningRestrictions);
			if (fr.isPresent()) {
				failedRestrictions.add(fr.get());
			}
		}

		return failedRestrictions;
	}

	/**
	 * Helper class to test a single restriction. Test correct amount of occurence
	 * and optionally datatype
	 * 
	 * @param restriction
	 * @param bpmnElement
	 * @param owl2bpmnMapper
	 * @param ignoreWarningRestrictions
	 * @return
	 */
	private static Optional<FailedOWLClassRestriction> testRestriction(OWLClassRestriction restriction,
			DomElement bpmnElement, Owl2BPMNMapper owl2bpmnMapper, boolean ignoreWarningRestrictions) {

		// Map OWL-Name to XML-Element name
		String originalOWLPropertyName = restriction.getOnProperty().getIRI().getShortForm();
		String affectedXMLElementName = getMappedNameForOWLElement(originalOWLPropertyName, owl2bpmnMapper);

		boolean foundAsAttributeOrXmlTag = false;
		// TODO: properly we need a mapping between PropertyNames of OWL and
		// XMl-Tags/Attribute Names

		// Determine the occurred cardinality in XML-Element.
		int bpmnOccurance = 0;
		if (bpmnElement.hasAttribute(affectedXMLElementName)) {
			// If it affects an attribute it can just occur once
			bpmnOccurance = 1;
			foundAsAttributeOrXmlTag = true;
		} else {
			// If it affects an XML-Tag try to get amount with of child-tags with name
			bpmnOccurance = getOccuredXmlNodesWithName(affectedXMLElementName, bpmnElement, bpmnOccurance);

			if (bpmnOccurance != 0) {
				foundAsAttributeOrXmlTag = true;
			}
		}

		// If we are not allowed to ignore warnings check them too!
		// A warning is when the property of the restriction is not found as attribute
		// or XML-Tag in the bpmnElement
		// but cardinality of restriction is 0 as well. In this case there could be an
		// mapping problem as well.
		if (!ignoreWarningRestrictions) {
			// Check if restriction does not explicit requires attribute or XML Tag.
			// When not it might be a problem in the OWL-Naming as well
			if ((!foundAsAttributeOrXmlTag) && restriction.getCardinality() == 0) {
				String errMsg = "Restriction did not expect element <"
						+ restriction.getOnProperty().getIRI().getShortForm()
						+ "> and element not found in BPMN. This might be a mapping error as well!";
				return Optional
						.of(new FailedOWLClassRestriction(RestrictionFailingLevelEnum.WARNING, errMsg, restriction));
			}
		}

		// We've found an XML-Element - check if cardinality of restriction is met
		if (!isRestrictionCardinaltyMet(bpmnOccurance, restriction)) {
			String errMsg = "Restriction expected element <" + restriction.getOnProperty().getIRI().getShortForm()
					+ "> <" + restriction.getCardinality() + "> times with Matchtype <"
					+ restriction.getCardinalityType() + "> but element occured in BPMN <" + bpmnOccurance + "> times.";

			return Optional.of(new FailedOWLClassRestriction(RestrictionFailingLevelEnum.ERROR, errMsg, restriction));
		}

		// Additional check for DataType if restriction affects an attribute in
		// bpmnElement and DataRange is defined in OWL
		if (restriction.getOnDataRange() != DataRangeEnum.Unkown && bpmnElement.hasAttribute(affectedXMLElementName)) {
			String bpmnRawValue = bpmnElement.getAttribute(affectedXMLElementName);
			if (!isRestrictionDataTypeMet(bpmnRawValue, restriction)) {
				String errMsg = "Restriction expected dataType <" + restriction.getOnDataRange()
						+ "> for xml-attribut <" + restriction.getOnProperty().getIRI().getShortForm() + "> but found <"
						+ bpmnRawValue + ">";
				return Optional
						.of(new FailedOWLClassRestriction(RestrictionFailingLevelEnum.ERROR, errMsg, restriction));
			}
		}

		// No error found
		return Optional.empty();
	}

	/**
	 * Returns the number of occurred XML Nodes with given name in given bpmnelement
	 * 
	 * @param expectedXmlTagName
	 * @param bpmnElement
	 * @param currentOccurance
	 * @return
	 */
	private static int getOccuredXmlNodesWithName(String expectedXmlTagName, DomElement bpmnElement,
			int currentOccurance) {
		// Check if current given element matches
		if (bpmnElement.getLocalName().equals(expectedXmlTagName)) {
			currentOccurance++;
		}

		// Check for children of given element
		for (DomElement child : bpmnElement.getChildElements()) {
			// Recursive call to check if children have it...
			currentOccurance = getOccuredXmlNodesWithName(expectedXmlTagName, child, currentOccurance);
		}
		return currentOccurance;
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

	/**
	 * Returns the mapped name for given BPMN-Element-Name. If there is a mapping
	 * returns the mapping string otherwise the original name
	 * 
	 * @param originalName
	 * @param owl2bpmnMapping
	 * @return
	 */
	private static String getMappedNameForBPMNElement(String originalName, Owl2BPMNMapper owl2bpmnMapping) {
		// Try to get OWLClass for given DOM-Element of ontology
		String mappedOWLName = originalName;
		// Check if we have an mapping key. Then we have to overwrite the raw xmlTagName
		Optional<String> optMappedName = owl2bpmnMapping.getOwlEntityName(originalName);
		if (optMappedName.isPresent()) {
			mappedOWLName = optMappedName.get();
		}
		;
		return mappedOWLName;
	}

	/**
	 * Returns the mapped name for given OWL-Class/Property-Name. If there is a
	 * mapping returns the mapping string otherwise the original name
	 * 
	 * @param originalName
	 * @param owl2bpmnMapping
	 * @return
	 */
	private static String getMappedNameForOWLElement(String originalName, Owl2BPMNMapper owl2bpmnMapping) {
		// Try to get OWLClass for given DOM-Element of ontology
		String mappedOWLName = originalName;
		// Check if we have an mapping key. Then we have to overwrite the raw xmlTagName
		Optional<String> optMappedName = owl2bpmnMapping.getXmlElementName(originalName);
		if (optMappedName.isPresent()) {
			mappedOWLName = optMappedName.get();
		}
		;
		return mappedOWLName;
	}

}
