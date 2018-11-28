/**
 * 
 */
package at.fh.BPMN20OntologyTester.model;

import java.util.Optional;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A Restriction class defines an owl:restriction Enttrie. There might be more
 * than one restriction on a class or property(e.g. Min/Max)
 * 
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class OWLClassRestriction {

	// Determines which Value is affected. Either it is a XML-Child-Tag or an
	// XML-Attribute
	private OWLProperty onProperty;

	// Cardinality defines how often the property must occurs. According the Type it
	// is a range or a exact value
	private int cardinality;
	private CardinalityTypeEnum cardinalityType = CardinalityTypeEnum.Unkown;

	// Optionally set if Restriction affects an attribute. It defines the expected
	// data-type for the property:
	private DataRangeEnum onDataRange = DataRangeEnum.Unkown;

	// Defines the affected OWL-Class.
	// It explizit set with 'onClass', then restriction this OWL-Class, otherwise
	// restriction affects OWL-Class where it is defined (This are usually
	// DataProperty Restrictions).

	// onClass attribute not necessary to check in XML because XML has
	// to fulfill the restriction anyway.It doesn't matter where the restriction
	// comes from but good to know in GUI.
	private OWLClass onClass;

	public enum CardinalityTypeEnum {
		MinCardinality, MaxCardinality, ExactCardinality, Unkown;
	}

	public enum DataRangeEnum {
		DataRangeString, DataRangeBoolean, DataRangeInteger, Unkown
	}

	public enum AffectedXMLPartEnum {
		ChildNodeRestriction, AttributeRestriction, Unkown
	}

	/**
	 * Creates an OWLClassRestriction with given data of subClassOf-DomElement
	 * 
	 * @param domElement
	 *            domElement of XML-Tag 'subClassOf of ontology
	 * @param owlClassOfRestriction
	 *            OWL-Class where domElement(restricion) is found
	 * @param ontology
	 * 				ontology where restriciton is found from to create links for onClass/onProperty
	 * @throws Exception
	 */
	public OWLClassRestriction(Element domElement, OWLClass owlClassOfRestriction, OWLModel ontology) throws Exception {
		try {
			parseDomElement(domElement, owlClassOfRestriction, ontology);
		} catch (Exception e) {
			throw new Exception("OWLClassRestriction failed: Error while parsing domElement");
		}

		// Validate data
		if (onProperty == null) {
			throw new Exception("OWLClassRestriciton requires link to an OWLProperty!");
		}

		if (cardinalityType.equals(CardinalityTypeEnum.Unkown)) {
			throw new Exception("OWLClass requires valid CardinalityTypeEnum");
		}

		if (onClass == null) {
			// Element 'onClass' not found, restriction is directly associated to given
			// OWLClass
			this.onClass = owlClassOfRestriction;
		}
	}

	/**
	 * Helper method to parse given DOm-Object to get a restriction
	 * 
	 * @param domElement
	 * @throws Exception
	 */
	private void parseDomElement(Element domElement, OWLClass owlClassOfRestriction, OWLModel ontology) throws Exception {
		NodeList childs = domElement.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {

			Node childNode = childs.item(i);
			switch (childNode.getNodeName()) {
			case "owl:onProperty":
				String propertyIRI = ((Element) childNode).getAttribute("rdf:resource");
				Optional<OWLProperty> optProp = ontology.getPropertyByIRI(propertyIRI);
				if (optProp.isPresent())
					onProperty = optProp.get();
				break;
			case "owl:minCardinality":
			case "owl:minQualifiedCardinality":
				cardinalityType = CardinalityTypeEnum.MinCardinality;
				cardinality = Integer.parseInt(childNode.getTextContent());
				break;
			case "owl:maxCardinality":
			case "owl:maxQualifiedCardinality":
				cardinalityType = CardinalityTypeEnum.MaxCardinality;
				cardinality = Integer.parseInt(childNode.getTextContent());
				break;
			case "owl:qualifiedCardinality":
				cardinalityType = CardinalityTypeEnum.ExactCardinality;
				cardinality = Integer.parseInt(childNode.getTextContent());
				break;

			case "owl:onDataRange":
				// BPMN2.0 Ontology currently defines DataRanges in
				// - http://www.w3.org/2001/XMLSchema#string
				// - http://www.w3.org/2001/XMLSchema#boolean
				// - http://www.w3.org/2001/XMLSchema#integer

				String value = ((Element) childNode).getAttribute("rdf:resource");
				switch (value) {
				case "http://www.w3.org/2001/XMLSchema#string":
					onDataRange = DataRangeEnum.DataRangeString;
					break;
				case "http://www.w3.org/2001/XMLSchema#boolean":
					onDataRange = DataRangeEnum.DataRangeBoolean;
					break;
				case "http://www.w3.org/2001/XMLSchema#integer":
					onDataRange = DataRangeEnum.DataRangeInteger;
					break;
				default:
					System.out.println("DataType <" + value + ">");
					onDataRange = DataRangeEnum.Unkown;

				}
				break;
			case "owl:onClass":
				String onClassIRI = ((Element) childNode).getAttribute("rdf:resource");

				Optional<OWLClass> optClass = ontology.getOWLClassByIRI(onClassIRI);
				if (optClass.isPresent())
					onClass = optClass.get();
				break;
			case "#text":
				// No glue where this comes from, ignore it...
				break;
			default:
				System.out.println("Unsupported child <" + childNode.getNodeName() + ">");

			}
		}
	}

	public OWLProperty getOnProperty() {
		return onProperty;
	}

	public int getCardinality() {
		return cardinality;
	}

	public CardinalityTypeEnum getCardinalityType() {
		return cardinalityType;
	}

	public DataRangeEnum getOnDataRange() {
		return onDataRange;
	}

	public OWLClass getOnClass() {
		return onClass;
	}

	public String toFormattedToString() {
		StringBuilder sb = new StringBuilder();

		sb.append("OWLClassRestriction [").append("onProperty=").append(onProperty.getIRI().getShortForm()).append(", ")
				.append("cardinality=").append(cardinality).append(", ").append("cardinalityType=")
				.append(cardinalityType);

		if (onDataRange != DataRangeEnum.Unkown) {
			sb.append(", onDataRage=").append(onDataRange);
		}

		if (onClass != null) {
			sb.append(", onClass=").append(onClass.getIRI().getShortForm());
		}

		sb.append("]");
		return sb.toString();
	}

	@Override
	public String toString() {
		return "OWLClassRestriction [onProperty=" + onProperty + ", cardinality=" + cardinality + ", cardinalityType="
				+ cardinalityType + ", onDataRange=" + onDataRange + ", onClass=" + onClass + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cardinality;
		result = prime * result + ((cardinalityType == null) ? 0 : cardinalityType.hashCode());
		result = prime * result + ((onClass == null) ? 0 : onClass.hashCode());
		result = prime * result + ((onDataRange == null) ? 0 : onDataRange.hashCode());
		result = prime * result + ((onProperty == null) ? 0 : onProperty.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OWLClassRestriction other = (OWLClassRestriction) obj;
		if (cardinality != other.cardinality)
			return false;
		if (cardinalityType != other.cardinalityType)
			return false;
		if (onClass == null) {
			if (other.onClass != null)
				return false;
		} else if (!onClass.equals(other.onClass))
			return false;
		if (onDataRange != other.onDataRange)
			return false;
		if (onProperty == null) {
			if (other.onProperty != null)
				return false;
		} else if (!onProperty.equals(other.onProperty))
			return false;
		return true;
	}

}