package at.fh.BPMN20OntologyTester.model;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectRestriction;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Representation of an .owl File(XML) (Ontology)
 * 
 * @author Alexander Hödl IMA16 - Information Management University of applied
 *         Sciences FH JOANNEUM
 *
 */
public class OWLModel {

	// Represent the Ontology as OWL-API Object
	private final OWLOntology ontology;

	// Represents the File from which the OWL-API Object was create out
	private final Document ontologyAsDOMDocument;

	public OWLModel(OWLOntology ontology, File ontologyFile) throws OWLOntologyCreationException {
		try {
			this.ontology = ontology;
			this.ontologyAsDOMDocument = praseXMlFile(ontologyFile);
		} catch (Exception e) {
			// Simplify Exception Handling
			throw new OWLOntologyCreationException(e);
		}
	}

	/**
	 * Parses given XML-File and creates an DOM-Document out of it
	 * 
	 * @param file
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private Document praseXMlFile(File file) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(file);
		return doc;
	}

	/**
	 * Return the plain full Ontology object
	 * 
	 * @return
	 */
	public OWLOntology getOntology() {
		return ontology;
	}

	/**
	 * Returns ths Ontology as simple DOM Document
	 * 
	 * @return
	 */
	public Document getOntologyAsDOMDocument() {
		return ontologyAsDOMDocument;
	}

	/**
	 * Returns the Reasoner Object for the Ontolgy to query for sub-classes and
	 * stuff
	 * 
	 * @return
	 */
	public OWLReasoner getOWLReasoner() {
		OWLReasonerFactory rf = new ReasonerFactory();
		return rf.createReasoner(ontology);
	}

	/**
	 * Returns all avaliable Classes from Ontology
	 * 
	 * @return
	 */
	public Set<OWLClass> getOWLClasses() {
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
	 * Returns all OWLClasses, ObjectProperties and DataPropeties of the ontology
	 * 
	 * @return
	 */
	public Set<OWLEntity> getAllEntities() {
		Set<OWLEntity> entities = new HashSet<OWLEntity>();

		entities.addAll(this.getOWLClasses());
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
	public OWLEntity getEntityByShortName(String name) {
		for (OWLEntity a : getAllEntities()) {
			if (a.getIRI().getShortForm().equals(name))
				return a;
		}

		return null;
	}

	/**
	 * Returns class for given name ignoring case sensitivity
	 * 
	 * @param name
	 * @return
	 */
	public OWLClass getOWLClassByShortNameIgnoreCase(String name) {
		for (OWLClass c : getOWLClasses()) {
			if (c.getIRI().getShortForm().equalsIgnoreCase(name)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Returns an OWLEntity(Class or Property) for given IRI)
	 * 
	 * @param entityIRI
	 * @return
	 */
	public OWLEntity getEntityByIRI(String entityIRI) {
		for (OWLEntity a : getAllEntities()) {
			if (entityIRI.equalsIgnoreCase(a.getIRI().toString()))
				return a;
		}

		return null;
	}

	/**
	 * Returns an Data or ObjectProperty forgiven IRI
	 * 
	 * @param propertyIRI
	 * @return
	 */
	public Optional<OWLProperty> getPropertyByIRI(String propertyIRI) {
		Set<OWLEntity> props = new HashSet<OWLEntity>();
		props.addAll(getObjectProperties());
		props.addAll(getDataProperties());

		for (OWLEntity a : props) {
			if (propertyIRI.equalsIgnoreCase(a.getIRI().toString()))
				return Optional.of((OWLProperty) a);
		}

		return Optional.empty();
	}

	public Optional<OWLClass> getOWLClassByIRI(String classIRI) {
		for (OWLClass c : getOWLClasses()) {
			if (classIRI.equalsIgnoreCase(c.getIRI().toString()))
				return Optional.of(c);
		}

		return Optional.empty();
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

	public boolean existsOWLClassWithName(String name) {
		for (OWLEntity e : this.getOWLClasses()) {
			// in Model the names might be in lower case, so ignore case sensitive
			if (e.getIRI().getShortForm().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}
	
	public boolean existsOWLPropertyWithName(String name) {
		Set<OWLProperty> props = new HashSet<OWLProperty>();
		props.addAll(this.getDataProperties());
		props.addAll(this.getObjectProperties());
		
		for (OWLProperty p : props) {
			// in Model the names might be in lower case, so ignore case sensitive
			if (p.getIRI().getShortForm().equalsIgnoreCase(name))
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
	 * Determines if an given Entity is a OWLClass
	 * 
	 * @param entity
	 * @return
	 */
	public boolean isEntityOWLClass(OWLEntity entity) {
		return entity.isOWLClass();
	}

	/**
	 * Returns the set of Restrictions which exists for the given OWL-Class.
	 * 
	 * Description: All subClassOf Tags in the ontology represent an inheritance.
	 * -If the subclassOf Tag has no childTag(owl:restriction) it is a simple
	 * inheritance and linked with attribut 'rdf:about' -If the subclassOf Tag has a
	 * child owl:restriciotn, it handles a restriction for the inherited class which
	 * is linked in 'onClass' For the XML it is unnecessary if the restriction
	 * affects the inherited class because it needs to be fullfilled by the XML
	 * anyway.
	 * 
	 * Workflow: In the fist step collect all inherited classes. get all
	 * subClassOf-Elements of inherited class and collect them in a set. This is
	 * done in an recursive way.
	 * 
	 * In the second step iterate over all found classes and get the restrictions of
	 * the class. It doesn't matter if this is a directly asssosiated restriciton or
	 * based on inheritance. In case of an 'inheridated restriction' the tag
	 * 'onClass' is not necessary anymore.
	 * 
	 * @param owlClass
	 *            - Class
	 * @param includeAllInheritedClasses
	 *            - True if Restrictions of inherited classes should loaded as well
	 * @return
	 */
	public Set<OWLClassRestriction> getAllOWLClassRestrictionOfOWLClass(OWLClass owlClass,
			boolean includeAllInheritedClasses) throws Exception {

		Set<OWLClassRestriction> restrictions = new HashSet<OWLClassRestriction>();
		Set<OWLClass> inheritedClasses = new HashSet<OWLClass>();

		// 1. Add given class and collect all inherited classes for given owlClass
		inheritedClasses = getAllInheritedClassesOfClass(owlClass, inheritedClasses, includeAllInheritedClasses);

		// 2. Check for Restriction in each class and add them to list
		// Due to the inheritance restrictions in subClassOfs are required to
		// be fullfilled by given owlClass as well
		for (OWLClass oc : inheritedClasses) {
			restrictions.addAll(getOWLClassRestrictionOfOWLClass(oc));
		}
		return restrictions;
	}

	/**
	 * Helper class to collect all inherited Classes of given class. Goes through
	 * all subclasess of given classes and calls the method recursively to return
	 * the total inherited classes of given class
	 * 
	 * @param owlClass
	 * @param inheritedclasses
	 * @param includeAllInheritedClasses
	 * @return
	 */
	private Set<OWLClass> getAllInheritedClassesOfClass(OWLClass owlClass, Set<OWLClass> inheritedclasses,
			boolean includeAllInheritedClasses) {
		inheritedclasses.add(owlClass);

		// Check if inherited classes are required
		if (includeAllInheritedClasses) {
			for (OWLClass oc : getSubClassOf(owlClass)) {

				// Recursive call to add inherited classes. Just add them if they're not in the
				// list yet
				if (!inheritedclasses.contains(oc)) {
					inheritedclasses = getAllInheritedClassesOfClass(oc, inheritedclasses, includeAllInheritedClasses);
				}
			}
		}

		return inheritedclasses;
	}

	/**
	 * Helper class which returns all directlry associated
	 * restrictions(DataRestricitons) to given class.
	 * 
	 * @param owlClass
	 * @return
	 */
	private Set<OWLClassRestriction> getOWLClassRestrictionOfOWLClass(OWLClass owlClass) throws Exception {
		Set<OWLClassRestriction> restrictions = new HashSet<OWLClassRestriction>();

		// Retrieve the raw DOM Element of given OWL Class
		Element owlClassElement = getDOMElementOfClass(owlClass);
		if (owlClassElement != null) {

			// Iterate over all subclassOf Nodes to get owl:Restriction childs
			NodeList subClassOfNL = owlClassElement.getChildNodes();
			for (int x = 0; x < subClassOfNL.getLength(); x++) {

				// We're just interested in Nodes with name rdfs:subClassOf which contains the
				// restrictions
				if (isNodeElementAndHasNameAndChilds(subClassOfNL.item(x), "rdfs:subClassOf", true)) {

					NodeList restrictionNL = subClassOfNL.item(x).getChildNodes();
					for (int y = 0; y < restrictionNL.getLength(); y++) {
						Node restrcitionNode = restrictionNL.item(y);
						// We're just interested in Child-Nodes with name owl:Restriction
						if (isNodeElementAndHasNameAndChilds(restrcitionNode, "owl:Restriction", true)) {
							// JIHAAA we've found an restriction - Create an OWLCLassRestriction Object
							OWLClassRestriction restriction = new OWLClassRestriction((Element) restrcitionNode,
									owlClass,this);
							restrictions.add(restriction);

						}
					}
				}
			}
		}

		return restrictions;
	}

	/**
	 * Returns the raw DOM-Element for given OWLClass
	 * 
	 * @param owlClass
	 * @return
	 */
	public Element getDOMElementOfClass(OWLClass owlClass) {
		// Get all Elements of typ 'owl:Class'
		NodeList nl = getOntologyAsDOMDocument().getElementsByTagName("owl:Class");

		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);

			// Filter on given className
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& ((Element) node).getAttribute("rdf:about").equalsIgnoreCase(owlClass.getIRI() + "")) {

				return (Element) node;
			}
		}
		return null;
	}

	/**
	 * Helper Method to determine if an given Node is from Type Element, contains
	 * given name and optionally has child-Nodes
	 * 
	 * @param node
	 * @param name
	 * @param requireChilds
	 * @return
	 */
	private boolean isNodeElementAndHasNameAndChilds(Node node, String name, boolean requireChilds) {

		if (node.getNodeType() == Node.ELEMENT_NODE && name.equalsIgnoreCase(node.getNodeName())
				&& (node.hasChildNodes() == requireChilds)) {
			return true;
		}

		return false;

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
			// Handle Subclass which is a direct link to a OWLClass
			if (a.getSuperClass() instanceof OWLClass) {
				OWLClass c = a.getSuperClass().asOWLClass();
				subClasses.add(c);
			}

			// Handle subClass which is hidden in owl:Restriciton Element
			// (=ObjectRestriction)
			if (a.getSuperClass() instanceof OWLObjectRestriction) {
				OWLObjectRestriction r = (OWLObjectRestriction) a.getSuperClass();
				r.signature().forEach(s -> {
					if (s instanceof OWLClass) {
						subClasses.add(s.asOWLClass());
					}
				});
			}

		}

		return subClasses;
	}

}
