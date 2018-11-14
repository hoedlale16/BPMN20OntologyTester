/**
 * 
 */
package at.fh.BPMN20OntologyTester.model;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLProperty;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.fh.BPMN20OntologyTester.controller.OntologyHandler;

/**
 * A Restriction class defines an owl:restriction Enttrie. There might be more than one restriction on a class or property(e.g. Min/Max)
 * 
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class OWLClassRestriction {

	private OWLProperty onProperty;
	private int cardinality;
	private CardinalityTypeEnum cardinalityType = CardinalityTypeEnum.Unkown;
	
	//IF onClass stays null, Restriction does affect an attribute of DOM-Element of BPMN Model
	//If onClass is not null, link to sub-Node inside the DOM-Element of BPMN Model is linked
	private OWLEntity onClass;
	
	//Just set if Restriction affects an attribute and defines the expected data-type for cardinality:
	private DataRangeEnum onDataRange;
	
	public enum CardinalityTypeEnum {
	    MinCardinality,
	    MaxCardinality,
		ExactCardinality,
		Unkown;
	}
	
	public enum DataRangeEnum {
		DataRangeString,
		DataRangeBoolean,
		DataRangeInteger,
		Unkown
	}
	
	public OWLClassRestriction(Element domElement) 
	throws Exception {
		try {
			parseDomElement(domElement);
		}catch(Exception e) {
			throw new Exception("OWLClassRestriction failed: Error while parsing domElement");
		}
		
		//Validate data
		if(onProperty == null) {
			throw new Exception("OWLClassRestriciton require link to an OWLProperty!");
		}
		
		if (cardinalityType.equals(CardinalityTypeEnum.Unkown)) {
			throw new Exception("OWLClass required valid CardinalityTypeEnum");
		}
		
		if ( onDataRange != null &&  onDataRange.equals(DataRangeEnum.Unkown)) {
			throw new Exception("Restriction affects a not supported DataRange");
		}
	}
	
	
	/**
	 * Checks if OWlRestriction affets an attribute of the BPMN-DOM-Node or a child node which is defined in 'onClass'
	 * @return
	 */
	public boolean affectsChildNode() {
		return (onClass != null);
	}

	/**
	 * Helper method to parse given DOm-Object to get a restriction
	 * 
	 * @param domElement
	 * @throws Exception
	 */
	private void parseDomElement(Element domElement) throws Exception {
		NodeList childs = domElement.getChildNodes();
		for(int i= 0; i <childs.getLength(); i++ ) {
		
			Node childNode = childs.item(i);
			switch(childNode.getNodeName()) {
				case "owl:onProperty":  
					String propertyIRI = ((Element)childNode).getAttribute("rdf:resource");
					onProperty = OntologyHandler.getInstance().getBpmn20Ontology().getPropertyByIRI(propertyIRI);
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
					DataRangeEnum dataRange = DataRangeEnum.Unkown;
					
					//BPMN2.0 Ontology currently defines DataRanges in
					//  - http://www.w3.org/2001/XMLSchema#string
					//	- http://www.w3.org/2001/XMLSchema#boolean
					//	- http://www.w3.org/2001/XMLSchema#integer
					
					String value = ((Element)childNode).getAttribute("rdf:resource");
					switch ( value ) {
						case "http://www.w3.org/2001/XMLSchema#string":
							dataRange = DataRangeEnum.DataRangeString; break;
						case "http://www.w3.org/2001/XMLSchema#boolean":
							dataRange = DataRangeEnum.DataRangeBoolean; break;
						case "http://www.w3.org/2001/XMLSchema#integer":
							dataRange = DataRangeEnum.DataRangeInteger; break;

					}
					
					setOnDataRange(dataRange);
					break;
				case "owl:allValuesFrom":
					break;
				case "owl:someValuesFrom":
					break;
				case "owl:hasValue":
					break;
				case "owl:onClass":
					String resource = ((Element)childNode).getAttribute("rdf:resource");
					onClass = OntologyHandler.getInstance().getBpmn20Ontology().getEntityByIRI(resource);
					break;
				case "#text":
					//No glue where this comes frome, ignore it...
					break;
				default:
					System.out.println("Unsupported child <" + childNode.getNodeName() +">");
					
			}
		}
	}


	public OWLProperty getOnProperty() {
		return onProperty;
	}


	public void setOnProperty(OWLProperty onProperty) {
		this.onProperty = onProperty;
	}


	public int getCardinality() {
		return cardinality;
	}

	
	public String getCardinalityAsString() {
		return cardinality + "";
	}
	
	public boolean getCardinalityAsBoolean() {
			//0: means false, 1 means true
			return (cardinality > 0);
	}
	

	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}


	public CardinalityTypeEnum getCardinalityType() {
		return cardinalityType;
	}


	public void setCardinalityType(CardinalityTypeEnum cardinalityType) {
		this.cardinalityType = cardinalityType;
	}


	public OWLEntity getOnClass() {
		return onClass;
	}


	public void setOnClass(OWLEntity onClass) {
		this.onClass = onClass;
	}

	public DataRangeEnum getOnDataRange() {
		return onDataRange;
	}


	public void setOnDataRange(DataRangeEnum onDataRange) {
		this.onDataRange = onDataRange;
	}
	
	public String toFormattedToString() {
		StringBuilder sb = new StringBuilder();

		sb.append("OWLClassRestriction [");
		
		//A Restrictions is required to have a link to an property
		sb.append("onProperty=").append( onProperty.getIRI().getShortForm() ).append(", ");
		
		sb.append("cardinality=").append(cardinality).append(", ");
		sb.append("cardinalityType=").append(cardinalityType).append(", ");
		
		if(onClass != null) {
			sb.append("onClass=").append( onClass.getIRI().getShortForm()).append(", ");
		}
		
		sb.append("onDataRange=").append(onDataRange).append("]");
		
		return sb.toString();
	}	
		
	@Override
	public String toString() {	 
		return "OWLClassRestriction [onProperty=" + onProperty + ", cardinality=" + cardinality + ", cardinalityType="
				+ cardinalityType + ", onClass=" + onClass +", onDataRage= " + onDataRange +"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Integer.toUnsignedLong(cardinality);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (onDataRange == null) {
			if (other.onDataRange != null)
				return false;
		} else if (!onDataRange.equals(other.onDataRange))
			return false;
		if (onProperty == null) {
			if (other.onProperty != null)
				return false;
		} else if (!onProperty.equals(other.onProperty))
			return false;
		return true;
	}
	
	
}