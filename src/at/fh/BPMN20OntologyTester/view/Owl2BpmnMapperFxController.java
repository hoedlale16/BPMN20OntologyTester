/**
 * 
 */
package at.fh.BPMN20OntologyTester.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Optional;
import java.util.Properties;

import at.fh.BPMN20OntologyTester.controller.FxController;
import at.fh.BPMN20OntologyTester.controller.XmlElement2OWLClassesMapper;
import at.fh.BPMN20OntologyTester.view.dto.Owl2BpmnTableData;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;

/**
 * @author Alexander Hoedl IMA16 - Information Management (BSc) University of
 *         applied Sciences FH JOANNEUM
 *
 */
public class Owl2BpmnMapperFxController implements FxController {

	@FXML
	private TableView<Owl2BpmnTableData> tableOWL2BPMN;

	private ObservableList<Owl2BpmnTableData> data = FXCollections.observableArrayList();

	@FXML
	private TableColumn<Owl2BpmnTableData, String> tcOWLNaming, tcBPMNNaming;

	@FXML
	private TableColumn<Owl2BpmnTableData, Boolean> tcModified, tcDuplicate;

	/**
	 * Returns the new mappings which need to be stored in mapper 
	 * @return
	 */
	public Properties getNewMappings() {
		
		Properties properties = new Properties();
		for(Owl2BpmnTableData d: data) {
			//Key: BPMN-Name
			//Value: OWL-Class/Property
			properties.setProperty(d.getBpmnName(),d.getOwlName());
		}
		
		return properties;
	}
	
	private boolean pressedOK = false;
	public boolean isPressedOK() {
		return pressedOK;
	}
	
	public Owl2BpmnMapperFxController() {

		// COnvert Data from Mapper to List for GUI and changes
		prepareData(XmlElement2OWLClassesMapper.getInstance().getLoadedMapping());
	}

	private void prepareData(Properties propMappings) {
		data.clear();
		for (String key : propMappings.stringPropertyNames()) {
			String value = propMappings.getProperty(key);

			//key = OWL-Entry, value = XML-Entry
			Owl2BpmnTableData d = new Owl2BpmnTableData(key, value);
			data.add(d);
		}
	}
	
	/**
	 * Helper method to checks if table contains modified data or not
	 * @return
	 */
	private boolean containsModifiedData() {
		
		//Check for added or deleted entries
		if(data.size() != XmlElement2OWLClassesMapper.getInstance().getLoadedMapping().size())
			return true;
		
		//Check for real modifikations
		for(Owl2BpmnTableData d : data) {
			if (d.isModified())
		    	return true;
		};
		
		return false;
	}
	
	/**
	 * Helper method to checks if table contains duplications
	 * @return
	 */
	private boolean containsDuplicateData() {
		boolean duplicates = false;
		for(Owl2BpmnTableData d : data) {
			if (d.isDuplicate()) {
				duplicates = true;
		    	break;
		    }
		};
		
		return duplicates;
	}

	/**
	 * Initialize Controller and setup environment
	 */
	@FXML
	private void initialize() {

		tableOWL2BPMN.setItems(data);
		setupColumns();

		// Make Table and Columns editable
		tableOWL2BPMN.setEditable(true);
		tableOWL2BPMN.getSelectionModel().cellSelectionEnabledProperty().set(true);
	}

	/**
	 * Set CellFactories to show data nicely
	 */
	private void setupColumns() {
		tcOWLNaming.setCellFactory(TextFieldTableCell.forTableColumn());
		tcBPMNNaming.setCellFactory(TextFieldTableCell.forTableColumn());

		tcDuplicate.setCellValueFactory(
				new Callback<CellDataFeatures<Owl2BpmnTableData, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(CellDataFeatures<Owl2BpmnTableData, Boolean> param) {
						return param.getValue().isDuplicateProperty();
					}
				});

		tcDuplicate.setCellFactory(CheckBoxTableCell.forTableColumn(tcDuplicate));

		tcModified.setCellValueFactory(
				new Callback<CellDataFeatures<Owl2BpmnTableData, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(CellDataFeatures<Owl2BpmnTableData, Boolean> param) {
						return param.getValue().isModifiedProperty();
					}
				});

		tcModified.setCellFactory(CheckBoxTableCell.forTableColumn(tcModified));
	}

	/**
	 * React when user pressed a key and a table cell is selected. Either we have to
	 * switch in edit mode or select the next cell
	 * 
	 * @param event
	 */
	@FXML
	private void onTableKeyPressed(KeyEvent event) {
		
		if(event.getCode() == KeyCode.DELETE) {
			deleteSelectedEntry();
		}
		
		if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
			// User entered key for editing.(alphabet or number key)
			@SuppressWarnings("unchecked")
			final TablePosition<Owl2BpmnTableData, String> focusedCell = tableOWL2BPMN.focusModelProperty().get()
					.focusedCellProperty().get();
			// Trigger Edit
			tableOWL2BPMN.edit(focusedCell.getRow(), focusedCell.getTableColumn());

		} else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.TAB) {
			// Just select the next one
			tableOWL2BPMN.getSelectionModel().selectNext();
			event.consume();
		} else if (event.getCode() == KeyCode.LEFT) {
			// work around due to
			// TableView.getSelectionModel().selectPrevious() due to a bug
			// stopping it from working on
			// the first column in the last row of the table
			selectPreviousCell();
			event.consume();
		}
	}

	@SuppressWarnings("unchecked")
	// Found on https://dzone.com/articles/editable-tables-in-javafx (2018-11-28)
	private void selectPreviousCell() {
		if (tableOWL2BPMN.getSelectionModel().isCellSelectionEnabled()) {
			// in cell selection mode, we have to wrap around, going from
			// right-to-left, and then wrapping to the end of the previous line
			TablePosition<Owl2BpmnTableData, String> pos = tableOWL2BPMN.getFocusModel().getFocusedCell();
			if (pos.getColumn() - 1 >= 0) {
				// go to previous row
				tableOWL2BPMN.getSelectionModel().select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
			} else if (pos.getRow() < tableOWL2BPMN.getItems().size()) {
				// wrap to end of previous row
				tableOWL2BPMN.getSelectionModel().select(pos.getRow() - 1,
						tableOWL2BPMN.getVisibleLeafColumn(tableOWL2BPMN.getVisibleLeafColumns().size() - 1));
			}
		} else {
			int focusIndex = tableOWL2BPMN.getFocusModel().getFocusedIndex();
			if (focusIndex == -1) {
				tableOWL2BPMN.getSelectionModel().select(tableOWL2BPMN.getItems().size() - 1);
			} else if (focusIndex > 0) {
				tableOWL2BPMN.getSelectionModel().select(focusIndex - 1);
			}
		}
	}

	private TableColumn<Owl2BpmnTableData, ?> getTableColumn(final TableColumn<Owl2BpmnTableData, String> column,
			int offset) {
		int columnIndex = tableOWL2BPMN.getVisibleLeafIndex(column);
		int newColumnIndex = columnIndex + offset;
		return tableOWL2BPMN.getVisibleLeafColumn(newColumnIndex);
	}
	/**
	 * Deletes the current selected Entry from mapping 
	 */
	private void deleteSelectedEntry() {
		Owl2BpmnTableData selData =tableOWL2BPMN.getSelectionModel().getSelectedItem();
		if(selData != null) {
			
			//Remove possible previous duplicate issues
			data.forEach(d -> {
				if (d.getBpmnName().equals(selData.getBpmnName())) {
					d.setDuplicate(false);
				}
			});
			
			data.remove(selData);
		}
	}

	@FXML
	private void onBPMNNameChanged(CellEditEvent<Owl2BpmnTableData, String> event) {

		boolean valueChange = false;
		String value = event.getOldValue();
		if (event.getNewValue() != null) {
			value = event.getNewValue();
			valueChange = true;
		}

		Owl2BpmnTableData affectedData = ((Owl2BpmnTableData) event.getTableView().getItems()
				.get(event.getTablePosition().getRow()));

		// Check if owl-entry with new name already exists. The must be unique
		if (valueChange) {

			//Remove possible previous duplicate issues
			data.forEach(d -> {
				if (d.getBpmnName().equals(event.getOldValue())) {
					d.setDuplicate(false);
				}
			});
			
			// Iterate over current data and mark as duplicated (BPMN-Name must be unique)
			data.forEach(d -> {
				if (d.getBpmnName().equals(event.getNewValue())) {
					d.setDuplicate(true);
					affectedData.setDuplicate(true);
				}
			});
			

			// Mark as modified and set new value
			affectedData.setBpmnName(value);
			tableOWL2BPMN.refresh();
			tableOWL2BPMN.getSelectionModel().selectBelowCell();
		}
	}

	@FXML
	private void onOWLNameChanged(CellEditEvent<Owl2BpmnTableData, String> event) {
		boolean valueChange = false;
		String value = event.getOldValue();
		if (event.getNewValue() != null) {
			value = event.getNewValue();
			valueChange = true;
		}

		Owl2BpmnTableData affectedData = ((Owl2BpmnTableData) event.getTableView().getItems()
				.get(event.getTablePosition().getRow()));

		// Check if owl-entry with new name already exists. The must be unique
		if (valueChange) {
			affectedData.setOwlName(value);
			tableOWL2BPMN.refresh();
			tableOWL2BPMN.getSelectionModel().selectBelowCell();
		}
	}

	@FXML
	private void onNewEntry() {
		data.add(new Owl2BpmnTableData("", ""));
	}

	@FXML
	private void onDeleteSelected() {
		deleteSelectedEntry();
	}

	@FXML
	private void onApplyAndClose(ActionEvent event) {
		// close Dialog
		if(containsModifiedData()) {
			
			//Check for duplicate Data!
			if(containsDuplicateData()) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error Dialog");
				alert.setHeaderText("Duplication detected");
				alert.setContentText("Ooops, there are duplications. XML-Entries must be unique - Please fix it first!");
				alert.showAndWait();
			} else {
				//Apply changes and close
				XmlElement2OWLClassesMapper.getInstance().replaceMapping(getNewMappings());
				((Node)(event.getSource())).getScene().getWindow().hide();
			}
			
		} else {
			//No changes, nothing to do - just close
			((Node)(event.getSource())).getScene().getWindow().hide();
		}
	}

	@FXML
	private void onCancel(ActionEvent event) {
			
		if(containsModifiedData()) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Modified Data detected");
			alert.setHeaderText("Modified Data detected\"");
			alert.setContentText("There are changed data. Are you shure to withdraw changes?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				((Node)(event.getSource())).getScene().getWindow().hide();
			} 
		} else {
			((Node)(event.getSource())).getScene().getWindow().hide();
		}	
	}

	@FXML
	private void onExportMapping() {
		try {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Export OWL <-> BPMN2.0 Property Mapping");

			chooser.getExtensionFilters().add(new ExtensionFilter("Properties", "*.properties"));

			// Handle selected file
			File selectedFile = chooser.showSaveDialog(null);

			if (selectedFile != null) {			
				Properties properties = getNewMappings();
				
				properties.store(new FileOutputStream(selectedFile),"Mapping OWL <-> BPMN");
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Export successfull");
				alert.setHeaderText("Successfully exported mapping file");
				alert.showAndWait();
			}
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error!");
			alert.setHeaderText("Error while exporting Mapping file");
			alert.setContentText("ERROR - Failed to export mapping to  File <" + e.getMessage() + ">");
			alert.showAndWait();
		} 
		
	}

	@FXML
	private void onLoadMapping() {
		try {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Load OWL <-> BPMN2.0 Property Mapping");

			chooser.getExtensionFilters().add(new ExtensionFilter("Properties", "*.properties"));

			// Handle selected file
			File selectedFile = chooser.showOpenDialog(null);

			if (selectedFile != null) {			
				Properties properties = new Properties();
				properties.load(new FileInputStream(selectedFile));
				
				prepareData(properties);
			}
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error!");
			alert.setHeaderText("Error while loading Mapping file");
			alert.setContentText("ERROR - Failed to load File <" + e.getMessage() + ">");
			alert.showAndWait();
		}
	}
}
