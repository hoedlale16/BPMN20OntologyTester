package at.fh.BPMN20OntologyTester.controller;

import java.io.File;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.xml.ModelValidationException;

import com.google.common.base.VerifyException;

import at.fh.BPMN20OntologyTester.model.BPMNModel;

/**
 * Handler to create a create a new BPMN2.0 Model object
 * 
 * @author Alexander Hödl IMA16 - Information Management University of applied
 *         Sciences FH JOANNEUM
 *
 */
public class BPMNModelHandler {

	private BPMNModelHandler() {
	}

	/**
	 * Reads .bpmn file(.XML) which represents a process model. - Validates model
	 * against BPMN20.xsd schema - If valid creates an Object out of it and return
	 * this object
	 * 
	 * @param modelFile
	 *            - File where to read
	 * @return Object representing process
	 * @throws Exception 
	 */
	public static BPMNModel readModelFromFile(File modelFile)
			throws Exception {
		// Read and validate Model (XSD)
		BpmnModelInstance modelInst = Bpmn.readModelFromFile(modelFile);
		Bpmn.validateModel(modelInst);

		// Create Object and return it
		return new BPMNModel(modelInst, modelFile);

	}

}
