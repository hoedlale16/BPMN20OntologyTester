/**
 * 
 */
package at.fh.BPMN20OntologyTester.model;

import java.util.Optional;

import org.semanticweb.owlapi.model.OWLCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataRestriction;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectRestriction;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLRestriction;

import at.fh.BPMN20OntologyTester.model.enums.OWLRestrictionCardinalityTypeEnum;
import at.fh.BPMN20OntologyTester.model.enums.OWLRestrictionDataRangeEnum;

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
	private OWLRestrictionCardinalityTypeEnum cardinalityType = OWLRestrictionCardinalityTypeEnum.Unkown;

	// Optionally set if Restriction affects an attribute. It defines the expected
	// data-type for the property:
	private OWLRestrictionDataRangeEnum onDataRange = OWLRestrictionDataRangeEnum.Unkown;

	// Defines the affected OWL-Class.
	// It explizit set with 'onClass', then restriction this OWL-Class, otherwise
	// restriction affects OWL-Class where it is defined (This are usually
	// DataProperty Restrictions).

	// onClass attribute not necessary to check in XML because XML has
	// to fulfill the restriction anyway.It doesn't matter where the restriction
	// comes from but good to know in GUI.
	private OWLClass onClass;
	
	
	// Determines if Restriction must fulfilled by XML-Attribute
	// or XML-Child-Element
	private final boolean fulfilledbyXMLAttribute;

	public OWLClassRestriction(OWLRestriction owlRestriction,OWLModel ontology) throws Exception {
		
		parseOWLRestriction(owlRestriction);
		if (onClass == null) {
			this.fulfilledbyXMLAttribute = true;
		} else {
			this.fulfilledbyXMLAttribute = false;
		}
		
		// Validate data
		if (onProperty == null) {
			throw new Exception("OWLClassRestriciton for onClass <" + 
								onClass.getIRI().getShortForm() +
								"> in OWL-Class<" + owlRestriction.getProperty().toString() +
								"> has no link to an OWLProperty! " + 
								"Either property does not exit in ontology or 'onProperty' not set!");
		}

		if (cardinalityType.equals(OWLRestrictionCardinalityTypeEnum.Unkown)) {
			throw new Exception("OWLClassRestriciton for onClass <" + 
								onClass.getIRI().getShortForm() +
								"> in OWL-Class<" + owlRestriction.getProperty().toString() +
								"> requires valid Cardinality type!");
		}
		
		if(onClass != null && onDataRange != OWLRestrictionDataRangeEnum.Unkown) {
			throw new Exception("OWLClassRestriciton for onClass <" + 
					onClass.getIRI().getShortForm() +
					"> in OWL-Class<" + owlRestriction.getProperty().toString() +
					"> affects an XML-Attribute and XML-Element!");
		}
	}

	@SuppressWarnings("rawtypes")
	private void parseOWLRestriction(OWLRestriction owlRestriction) throws Exception{
		try {
			// Get Cardinality and Cardinality type
			if(owlRestriction instanceof OWLCardinalityRestriction) {
				this.cardinality = ((OWLCardinalityRestriction) owlRestriction).getCardinality();		
				 
				//Determine cardinality type
				this.cardinalityType =  OWLRestrictionCardinalityTypeEnum.Unkown;
				if( owlRestriction instanceof OWLDataExactCardinality ||  
					owlRestriction instanceof OWLObjectExactCardinality ) {
						this.cardinalityType =  OWLRestrictionCardinalityTypeEnum.ExactCardinality;
				} 
				if( owlRestriction instanceof OWLDataMinCardinality ||  
					owlRestriction instanceof OWLObjectMinCardinality ) {
					this.cardinalityType =  OWLRestrictionCardinalityTypeEnum.MinCardinality;
				}
				if( owlRestriction instanceof OWLDataMaxCardinality ||  
					owlRestriction instanceof OWLObjectMaxCardinality ) {
					this.cardinalityType =  OWLRestrictionCardinalityTypeEnum.MaxCardinality;
				}
			}
			
			if(owlRestriction.isDataRestriction()) {
				OWLDataRestriction dr = (OWLDataRestriction)owlRestriction;
				dr.datatypesInSignature().forEach( d -> {
					this.onDataRange = determineDataRangeEnum(d.toString());
				});
				
				this.onProperty = owlRestriction.getProperty().asOWLDataProperty();
				
				
			}
			if(owlRestriction.isObjectRestriction()) {
				OWLObjectRestriction or = (OWLObjectRestriction)owlRestriction;
				or.classesInSignature().forEach( c -> {
					this.onClass = c.asOWLClass();
				});
				
				this.onProperty = owlRestriction.getProperty().asOWLObjectProperty();
			}
					
			} catch (Exception e) {
			throw new Exception("OWLClassRestriction failed: Error while parsing domElement");
		}
	}
	
	/**
	 * BPMN2.0 Ontology currently defines DataRanges in
	 *  - http://www.w3.org/2001/XMLSchema#string
	 *  - http://www.w3.org/2001/XMLSchema#boolean
 	 *  - http://www.w3.org/2001/XMLSchema#integer
	 * @param value
	 * @return
	 */
	private OWLRestrictionDataRangeEnum determineDataRangeEnum(String value) {
		switch (value) {
		case "xsd:string":
		case "http://www.w3.org/2001/XMLSchema#string": 
			return OWLRestrictionDataRangeEnum.DataRangeString;
		case "xsd:boolean":
		case "http://www.w3.org/2001/XMLSchema#boolean":
			return OWLRestrictionDataRangeEnum.DataRangeBoolean;
		case "xsd:integer":
		case "http://www.w3.org/2001/XMLSchema#integer":
			return OWLRestrictionDataRangeEnum.DataRangeInteger;
		case "xsd:double":
		case "http://www.w3.org/2001/XMLSchema#double":
			return OWLRestrictionDataRangeEnum.DataRangeDouble;
		default:
			return OWLRestrictionDataRangeEnum.Unkown;
		}
	}

	public OWLProperty getOnProperty() {
		return onProperty;
	}

	public int getCardinality() {
		return cardinality;
	}

	public OWLRestrictionCardinalityTypeEnum getCardinalityType() {
		return cardinalityType;
	}

	public OWLRestrictionDataRangeEnum getOnDataRange() {
		return onDataRange;
	}

	public Optional<OWLClass> getOnClass() {
		if(onClass != null)
			return Optional.of(onClass);
		
		return Optional.empty();
	}

	public boolean isFulfilledByAttribute() {
		return this.fulfilledbyXMLAttribute;
	}
	
	public String toFormattedToString() {
		StringBuilder sb = new StringBuilder();

		sb.append("OWLClassRestriction musst fulfilled by ");
		if(this.fulfilledbyXMLAttribute) {
			sb.append("XML-Attribute").append(" [");
		} else {
			sb.append("XML-Element").append(" [");
		}
		sb.append("onProperty=").append(onProperty.getIRI().getShortForm()).append(", ")
		  .append("cardinality=").append(cardinality).append(", ").append("cardinalityType=")
		  .append(cardinalityType);

		if (onDataRange != OWLRestrictionDataRangeEnum.Unkown) {
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