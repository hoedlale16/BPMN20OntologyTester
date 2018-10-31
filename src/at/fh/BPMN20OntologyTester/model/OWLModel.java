package at.fh.BPMN20OntologyTester.model;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Representation of an .owl File(XML) (Ontology)
 * 
 * @author Alexander Hödl IMA16 - Information Management University of applied
 *         Sciences FH JOANNEUM
 *
 */
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
	 * Checks if an Entity with given name exsits or not
	 * 
	 * @param name
	 * @return
	 */
	public boolean existsEntity(String name) {
		for (OWLEntity e : this.getAllEntities()) {
			// in Model the names might be in lower case, so ignore case sensitive
			if (e.getIRI().getShortForm().equalsIgnoreCase(name))
				return true;
		}
		return false;
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
				sb.append(((OWLLiteral) val).getLiteral());
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
	 * 
	 * @return
	 */
	public Set<OWLEntity> getDocumentedEntities() {
		Set<OWLEntity> documentedEntities = getAllEntities();
		documentedEntities.removeAll(getUnDocumentedEntities());

		return documentedEntities;
	}

	/**
	 * Tetermines if an given Entity is from a specific type
	 * 
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
	// public Set<OWLClassAxiom> getRestrictionsOfClass(OWLClass owlClass) {
	public void getRestrictionsOfClass(OWLClass owlClass) {

		Set<OWLAxiom> restrictions = new HashSet<OWLAxiom>();
		
		//First add all restrictions of given class
		restrictions.addAll(getRestrictionAxiomOfSingleClass(owlClass));
		
		//Second iterate over all sub classes and add restrictions which need to be fullfilled by class as well
		//System.out.println("The real sub class of <" + owlClass.getIRI().getShortForm() + ">");
		for (OWLClass c : getSubClassOf(owlClass)) {
			restrictions.addAll(getRestrictionAxiomOfSingleClass(c));
		}
		
		//Now we have all restrictions:
		System.out.println("Following restrictions required for class <" + owlClass.getIRI().getShortForm() + ">");
		for(OWLAxiom rest: restrictions) {
			System.out.println(rest);
		}

	}

	/**
	 * Helper method to return all Axioms which contains an owl:restriction in given Class 
	 * @param owlClass
	 * @return
	 */
	private Set<OWLAxiom> getRestrictionAxiomOfSingleClass(OWLClass owlClass) {
		Set<OWLAxiom> restrictions = new HashSet<OWLAxiom>();

		// Iterate over all Axioms of Type SUBCLASS_OF of given Class
		List<OWLClassAxiom> axioms = ontology.axioms(owlClass)
				.filter(a -> a.getAxiomType().equals(AxiomType.SUBCLASS_OF)).collect(Collectors.toList());
		for (OWLClassAxiom a : axioms) {
			// Now just show restrictions which belongs directly to given class and not
			// refer to an external one
			for (OWLClassExpression nce : a.nestedClassExpressions()
					.filter(x -> !x.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS))
					.collect(Collectors.toList())) {
				
				//TODO: Create a OWLClassRestriction Object depending on what it is
				//	DataExcatCardinality => boolean vlaues
				//	ObjectMin/Max => 
				boolean isDataProperty = false; //(nce.dataPropertiesInSignature().collect(Collectors.toSet()).size() > 0);
				boolean isObjProperty = false; //nce.getObjectPropertiesInSignature().size() > 0;
				String affectedEntity = null;
						
				for(OWLDataProperty dp: nce.getDataPropertiesInSignature()) {
					isDataProperty = true;
					affectedEntity = dp.getIRI().getShortForm();
				}
				
				for(OWLObjectProperty op: nce.getObjectPropertiesInSignature()) {
					isObjProperty = true;
					affectedEntity = op.getIRI().getShortForm();
					
				}
				
				
				
				System.out.println("Entity <"+  affectedEntity + "> DataProp <" + isDataProperty +"> - ObjProp <" + isObjProperty+"> - value <>");
				restrictions.add(a);
			}
		}

		return restrictions;
	}

	/**
	 * Return all Sub-Classes(rdfs:subClass) which are OWLClass of given Class
	 * 
	 * @param owlClass
	 * @return
	 */
	public Set<OWLClass> getSubClassOf(OWLClass owlClass) {

		Set<OWLClass> subClasses = new HashSet<OWLClass>();

		// Read all axioms of type SubClass and filter on given Class
		Set<OWLSubClassOfAxiom> subClassAxioms = ontology.axioms(AxiomType.SUBCLASS_OF)
				.filter(a -> a.getSubClass().equals(owlClass)).collect(Collectors.toSet());
		for (OWLSubClassOfAxiom a : subClassAxioms) {
			if (a.getSuperClass() instanceof OWLClass) {
				subClasses.add(a.getSuperClass().asOWLClass());
			}
		}

		return subClasses;
	}

	public Set<String> testElementsExsistInOntology(BPMNModel model) {
		Set<String> notFoundInOWL = new HashSet<String>();

		Set<String> elements = model.getAllElementsOfModel();
		for (String s : elements) {
			// System.out.println(s);
			if (!existsEntity(s))
				notFoundInOWL.add(s);
		}
		return notFoundInOWL;
	}
}
