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
 * @author Alexander Hödl
 * IMA16 - Information Management
 * University of applied Sciences FH JOANNEUM
 *
 */
public class BPMNModelHandler {
	
	public BPMNModelHandler() {
	}
	
	/**
	 * Reads .bpmn file(.XML) which represents a process model.
	 *  - Validates model against BPMN20.xsd schema
	 *  - If valid creates an Object out of it and return this object
	 * @param modelFile - File where to read
	 * @return Object representing process
	 * @throws VerifyException -  Thrown if validation against xsd('resouce/BPMN20.xsd) failed
	 * @throws BpmnModelException - Thrown if error occurred while reading process model
	 */ 
	public BPMNModel readModelFromFile(File modelFile) 
	  throws VerifyException, BpmnModelException,ModelValidationException
	  {
		//Check if given File is valid to BPMN-XSD from https://www.omg.org/spec/BPMN/2.0/
		/*try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance("XMLConstants.W3C_XML_SCHEMA_NS_URI");
			File xsdFile = new File("resource/BPMN20.xsd");
			Validator bpmnXSD = schemaFactory.newSchema(xsdFile).newValidator();	
			bpmnXSD.validate(new StreamSource(modelFile));
		}
		catch(Exception e){
		  e.printStackTrace();
		  throw new VerifyException("Given File is not a valid BPMN2.0 model. Validation against XSD failed!");	
		}*/
		
		//Read and validate Model (XSD)
		BpmnModelInstance modelInst = Bpmn.readModelFromFile(modelFile);
		Bpmn.validateModel(modelInst);
		
		//Create Object and return it
		return new BPMNModel(modelInst);
		
	}
	

}
