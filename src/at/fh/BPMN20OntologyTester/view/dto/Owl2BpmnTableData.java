/**
 * 
 */
package at.fh.BPMN20OntologyTester.view.dto;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class Owl2BpmnTableData {

	private SimpleStringProperty owlName, bpmnName;
	private SimpleBooleanProperty  modified, duplicate;

	public Owl2BpmnTableData(String bpmnName,String owlName) {

		this.owlName = new SimpleStringProperty(owlName);
		this.bpmnName = new SimpleStringProperty(bpmnName);
		this.modified = new SimpleBooleanProperty(false);
		this.duplicate = new SimpleBooleanProperty(false);

	}

	public boolean isModified() {
		return modified.get();
	}
	
	public SimpleBooleanProperty isModifiedProperty() {
		return modified;
	}

	public String getOwlName() {
		return owlName.get();
	}

	public String getBpmnName() {
		return bpmnName.get();
	}

	public boolean isDuplicate() {
		return duplicate.get();
	}

	public SimpleBooleanProperty isDuplicateProperty() {
		return duplicate;
	}
	
	public void setModified(boolean modified) {
		this.modified.set(modified);
	}

	public void setOwlName(String owlName) {
		this.owlName.set(owlName);
		setModified(true);
	}

	public void setBpmnName(String bpmnName) {
		this.bpmnName.set(bpmnName);
		setModified(true);
	}

	public void setDuplicate(boolean duplicate) {
		this.duplicate.set(duplicate);
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bpmnName == null) ? 0 : bpmnName.hashCode());
		result = prime * result + ((duplicate == null) ? 0 : duplicate.hashCode());
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
		Owl2BpmnTableData other = (Owl2BpmnTableData) obj;
		if (bpmnName == null) {
			if (other.bpmnName != null)
				return false;
		} else if (!bpmnName.equals(other.bpmnName))
			return false;
		if (duplicate == null) {
			if (other.duplicate != null)
				return false;
		} else if (!duplicate.equals(other.duplicate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Owl2BpmnTableData [bpmnName=" + bpmnName + ", owlName=" + owlName + ", modified=" + modified
				+ ", duplicate=" + duplicate + "]";
	}

}
