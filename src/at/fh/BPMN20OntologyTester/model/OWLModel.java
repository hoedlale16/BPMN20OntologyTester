package at.fh.BPMN20OntologyTester.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.search.EntitySearcher;

public class OWLModel {

	private final OWLOntology ontology;

	public OWLModel(OWLOntology ontology) {
		this.ontology = ontology;
	}

	/**
	 * Return the plain full Ontology object
	 * 
	 * @return
	 */
	public OWLOntology getOntology() {
		return ontology;
	}

	public Set<OWLClass> getClasses() {
		Set<OWLClass> classes = new HashSet<OWLClass>();

		// Read all Classes from ontology and store in Set
		ontology.classesInSignature().forEach(c -> classes.add(c));

		return classes;
	}

	public Set<OWLObjectProperty> getObjectProperties() {
		Set<OWLObjectProperty> objProperties = new HashSet<OWLObjectProperty>();

		// Read all Object-properties from ontology and store in Set
		ontology.objectPropertiesInSignature().forEach(o -> objProperties.add(o));

		return objProperties;
	}

	public Set<OWLDataProperty> getDataProperties() {
		Set<OWLDataProperty> dataProperties = new HashSet<OWLDataProperty>();

		// Read all Data-Properties from ontology and store in Set
		ontology.dataPropertiesInSignature().forEach(o -> dataProperties.add(o));

		
		
		return dataProperties;
	}

	/**
	 * Return the full bench of Axioms of the OWL. Axioms are the object which
	 * contains all infomration about classes and properties
	 * 
	 * https://www.w3.org/TR/2002/WD-owl-semantics-20021108/syntax.html#2.3 The
	 * biggest differences between OWL Lite and the full abstract syntax show up in
	 * the axioms, which are used to provide information about classes and
	 * properties. As it is the smaller language, OWL Lite axioms are given first,
	 * and then the full abstract syntax is given as additions to OWL Lite.
	 * 
	 * Axioms are used to associate class and property IDs with either partial or
	 * complete specifications of their characteristics, and to give other logical
	 * information about classes and properties. Axioms used to be called
	 * definitions, but they are not all definitions in the common sense of the
	 * term, as has been made evident in several discussions in the WG, and thus a
	 * more-neutral name has been chosen.
	 * 
	 * @return
	 */
	public Set<OWLEntity> getAllEntities() {
		Set<OWLEntity> entities = new HashSet<OWLEntity>();

		entities.addAll(this.getClasses());
		entities.addAll(this.getObjectProperties());
		entities.addAll(this.getDataProperties());

		return entities;
	}

	/**
	 * Return a single OWLEntity by name
	 * 
	 * @param name
	 * @return
	 */
	public OWLEntity getEntityByName(String name) {
		for (OWLEntity a : getAllEntities()) {
			if (a.getIRI().getShortForm().equals(name))
				return a;
		}

		return null;
	}

	/**
	 * Returns the description of the requested entity. If the Entity has a
	 * description, the description will returned. Otherwhise an empty string will
	 * returned
	 * 
	 * @param entity
	 * @return
	 */
	public String getCommentOfEntity(OWLEntity entity) {
		StringBuilder sb = new StringBuilder();
		OWLAnnotationProperty prop = OWLManager.getOWLDataFactory().getRDFSComment();

		List<OWLAnnotation> annotations = EntitySearcher.getAnnotations(entity, ontology, prop)
				.collect(Collectors.toList());
		for (OWLAnnotation a : annotations) {
			OWLAnnotationValue val = a.getValue();
			if (val instanceof OWLLiteral) {
				sb.append(val);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a list of Entities which does not have a 'rdfs:comment' annotation
	 * 
	 * @return
	 */
	public Set<OWLEntity> getUnDocumentedEntities() {
		Set<OWLEntity> undocumentedEntities = new HashSet<OWLEntity>();

		for (OWLEntity e : getAllEntities()) {
			if (this.getCommentOfEntity(e).isEmpty()) {
				undocumentedEntities.add(e);
			}
		}

		return undocumentedEntities;
	}
	
	/**
	 * Return all Entities which have a documentation (rdfs:comment annotation)
	 * @return
	 */
	public Set<OWLEntity> getDocumentedEntities() {
		Set<OWLEntity> documentedEntities = getAllEntities();		
		documentedEntities.removeAll(getUnDocumentedEntities());
		
		return documentedEntities;
	}
	
	/**
	 * Tetermines if an given Entity is from a specific type
	 * @param entity
	 * @return
	 */
	public boolean isEntityOWLClass(OWLEntity entity) {
		return entity.isOWLClass();
	}

	/**
	 * Returns the set of Restrictions which exists for the given OWL-Class
	 * 
	 * @param owlClass
	 * @return
	 */
	public Set<OWLClassAxiom> getRestrictionsOfClass(OWLClass owlClass) {
		// Read restrictions
		//TODO: zusammenfassen auf eine OWLRestriction mit min/max usw
		Set<OWLClassAxiom> restrictions = ontology.axioms(owlClass).collect(Collectors.toSet());

		return restrictions;
	}

}
