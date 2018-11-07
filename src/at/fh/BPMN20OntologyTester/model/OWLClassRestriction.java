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
	private double cardinality;
	private CardinalityTypeEnum cardinalityType = CardinalityTypeEnum.Unkown;
	private OWLEntity onClass;
	private String onDataRange;
	
	public enum CardinalityTypeEnum {
	    MinCardinality,
	    MaxCardinality,
		ExactCardinality,
		Unkown;
	}
	
	public OWLClassRestriction(Element domElement) {
		try {
			parseDomElement(domElement);
		}catch(Exception e) {
			e.printStackTrace();
		}
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
					cardinality = Double.parseDouble(childNode.getTextContent());					
					break;
				case "owl:maxCardinality":
				case "owl:maxQualifiedCardinality":
					cardinalityType = CardinalityTypeEnum.MaxCardinality;
					cardinality = Double.parseDouble(childNode.getTextContent());					
					break;
				case "owl:qualifiedCardinality":
					cardinalityType = CardinalityTypeEnum.ExactCardinality;
					cardinality = Double.parseDouble(childNode.getTextContent());					
					break;
					
				case "owl:onDataRange":
					//Deprecated in OWL2.0 -> https://www.w3.org/TR/owl2-rdf-based-semantics/
					//Note: The use of the IRI owl:DataRange has been deprecated as of OWL 2. The IRI rdfs:Datatype SHOULD be used instead. 
					setOnDataRange(((Element)childNode).getAttribute("rdf:resource"));
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


	public double getCardinality() {
		return cardinality;
	}


	public void setCardinality(double cardinality) {
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

	public String getOnDataRange() {
		return onDataRange;
	}


	public void setOnDataRange(String onDataRange) {
		this.onDataRange = onDataRange;
	}
	
	/**
	 * Determine if Cardinality is from type boolean
	 * @return
	 */
	public boolean isCardinalityBoolean() {
		if("http://www.w3.org/2001/XMLSchema#boolean".equals(onDataRange)) 
			return true;
		return false;
	}
	
	public boolean convertCardinalityForDataRangeBoolean() {
			//0: means false, 1 means true
			return (cardinality > 0);
	}

	
	public String toFormattedToString() {
		StringBuilder sb = new StringBuilder();

		sb.append("OWLClassRestriction [");
		
		if(onProperty != null) {
			sb.append("onProperty=").append( onProperty.getIRI().getShortForm() ).append(", ");
		}
		
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
		temp = Double.doubleToLongBits(cardinality);
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
		if (Double.doubleToLongBits(cardinality) != Double.doubleToLongBits(other.cardinality))
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