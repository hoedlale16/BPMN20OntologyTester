/**
 * 
 */
package at.fh.BPMN20OntologyTester.view;

import at.fh.BPMN20OntologyTester.controller.Owl2BPMNMapper;

/**
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class Owl2BpmnMapperFxController {

	private Owl2BPMNMapper mapping;
	
	public Owl2BpmnMapperFxController(Owl2BPMNMapper owl2bpmnmaping) {
		this.mapping = owl2bpmnmaping;
		System.out.println("TODO: show mapping: " + mapping.getAllMappings().size());
	}
}
