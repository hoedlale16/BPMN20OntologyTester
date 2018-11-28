/**
 * 
 */
package at.fh.BPMN20OntologyTester.view;

import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.Owl2BPMNMapper;

/**
 * @author Alexander Hoedl
 * IMA16 - Information Management (BSc)
 * University of applied Sciences FH JOANNEUM
 *
 */
public class Owl2BpmnMapperFxController implements FxController {

	private Owl2BPMNMapper mapping;
	
	public Owl2BpmnMapperFxController() {
		this.mapping = Owl2BPMNMapper.getInstance();
		System.out.println("TODO: show mapping: " + mapping.getLoadedMapping().size());
	}
}
