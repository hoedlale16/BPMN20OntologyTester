/**
 * 
 */
package at.fh.BPMN20OntologyTester.model;

/**
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class OwlSparqlQueryFactory {
	
	private static String getPrefixes() {
		StringBuilder sb = new StringBuilder();
		
		//Append prefix			
		sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>").append("\r\n")
		  .append("PREFIX bpmnOwl: <http://www.reiter.at/ontology/bpmn2.0#>").append("\r\n")
		  .append("PREFIX owl: <http://www.w3.org/2002/07/owl#>").append("\r\n");
		
		return sb.toString();
	}
	
	public static String getAttributesWithRestrictions(String owlClass) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(getPrefixes());
		sb.append("SELECT ?onProperty").append("\r\n")
		  .append("WHERE {").append("\r\n")
		  .append("  bpmnOwl:" ).append(owlClass).append("\r\n")
		  .append("  rdfs:subClassOf ?subClasses . ").append("\r\n")
		  .append("    ?subClasses (owl:Restriction)* ?restriction . ").append("\r\n")
		  .append("      ?restriction owl:onProperty ?onProperty . ").append("\r\n")
		  .append("}");
		
		
		return sb.toString();
	}
	
	public static String getAllInhertiedClasses(String owlClass) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(getPrefixes());		
		sb.append("SELECT ?superClass").append("\r\n")
		  .append("WHERE {").append("\r\n")
		  .append("  bpmnOwl:" ).append(owlClass).append("\r\n")
		  .append("  rdfs:subClassOf ?superClass .").append("\r\n")
		  .append("}");
		
		return sb.toString();
				
	}

}
