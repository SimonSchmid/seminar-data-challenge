package org.bof;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.knip.base.data.img.ImgPlusCell;


/**
 * This is the model implementation of HistogrammCreator.
 * Creates a histrogram of a column in a specific range by counting all occurences of the values in this column. Also includes missing values in the histogram.
 *
 * @author David Kolb
 */
public class HistogrammCreatorNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(HistogrammCreatorNodeModel.class);
        
    
    /** The config key for the range. */ 
	static final String CFGKEY_RANGE = "range";
	/** The config key for the selected column. */
    static final String CFGKEY_COLUMN_NAME = "columnName";
    /** The config key for the pattern. */
    static final String CFGKEY_PATTERN = "pattern";

    /** initial default count value. */
    static final int DEFAULT_COUNT = 1;
	public static final int IN_PORT = 0;

    // example value: the models count variable filled from the dialog 
    // and used in the models execution method. The default components of the
    // dialog work with "SettingsModels".
    
    // the settings model for the range 
    private final SettingsModelIntegerBounded m_range =
        new SettingsModelIntegerBounded(HistogrammCreatorNodeModel.CFGKEY_RANGE,
                    HistogrammCreatorNodeModel.DEFAULT_COUNT,
                    1, Integer.MAX_VALUE);
    
    // the settings model storing the selected column
 	private final SettingsModelString m_column = new SettingsModelString(
 			HistogrammCreatorNodeModel.CFGKEY_COLUMN_NAME, "");
 	
 	// the settings model storing the pattern
  	private final SettingsModelString m_pattern = new SettingsModelString(
  			HistogrammCreatorNodeModel.CFGKEY_PATTERN, "");
    

    /**
     * Constructor for the node model.
     */
    protected HistogrammCreatorNodeModel() {
    
        // TODO one incoming port and one outgoing port is assumed
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO do something here
        logger.info("Node Model Stub... this is not yet implemented !");       
        
        DataTableSpec outputSpec = createOutputColumnSpec();
        
        // the execution context will provide us with storage capacity, in this
        // case a data container to which we will add rows sequentially
        // Note, this container can also handle arbitrary big data tables, it
        // will buffer to disc if necessary.
        BufferedDataContainer container = exec.createDataContainer(outputSpec);
        
        int[] histoCount = new int[m_range.getIntValue()];      
        
        int colIndexCluster = inData[IN_PORT].getDataTableSpec().findColumnIndex(
                m_column.getStringValue());
        
        int colIndexImage = inData[IN_PORT].getDataTableSpec().findColumnIndex(
                "Image");
        
        ArrayList<DataRow> distinctImg = new ArrayList<DataRow>();
        ArrayList< ArrayList<DataRow> > distinctImgs = new ArrayList< ArrayList<DataRow> >();    
        long oldHash = 0;
        
        int rowCount = 1;
        for (DataRow currRow : inData[IN_PORT]) {
        	ImgPlusCell<?> img = (ImgPlusCell<?>)currRow.getCell(colIndexImage);
        	long newHash = img.hashCode();
        	
        	//end of table reached
        	if( rowCount == inData[IN_PORT].getRowCount() ){
        		distinctImg.add(currRow);
        		distinctImgs.add(new ArrayList<DataRow>(distinctImg));
        	}
        	//curr img equal to img before
        	else if( oldHash == 0 || newHash == oldHash  ){
        		distinctImg.add(currRow);
        	}        	
        	//curr img not equal to img before
        	else{
        		distinctImgs.add(new ArrayList<DataRow>(distinctImg));
        		distinctImg.clear();
        		distinctImg.add(currRow);
        	}  	
        	oldHash = newHash;
        	rowCount++;
        }
       
        
        int rowCurser = 0;
        for(ArrayList<DataRow> img : distinctImgs){
        	for (DataRow currRow : img) {
            	StringCell currCell = (StringCell)currRow.getCell(colIndexCluster);
            	String currValue = currCell.getStringValue();
            	
            	String prePattern = currValue.substring(0, m_pattern.getStringValue().length());
            	String postPattern = currValue.substring(m_pattern.getStringValue().length());
            	    	
            	if (prePattern.equals(m_pattern.getStringValue()) == false) {
                    throw new InvalidSettingsException(
                            "wrong pattern specified: " 
                            + m_pattern.getStringValue() 
                            + " . Please (re-)configure the node.");
                }
            	else{
            		Integer currHistoCount = new Integer(postPattern);
            		histoCount[currHistoCount]++;
            	}
            }
            
            
            RowKey key = new RowKey("Row " + rowCurser); 
                
            DataCell[] cells = new DataCell[m_range.getIntValue() + 1];
            for(int k = 0 ; k < m_range.getIntValue() ; k++){
                cells[k] = new IntCell(histoCount[k]);
            }
            
            ImgPlusCell<?> imgForId = (ImgPlusCell<?>)img.get(0).getCell(colIndexImage);
            String imgId = imgForId.getStringValue();		
            
            cells[m_range.getIntValue()] = new StringCell(imgId);
            DataRow row = new DefaultRow(key, cells);
            container.addRowToTable(row);
                
            //clear histoCount Array for next iteration
            for(int j = 0 ; j < m_range.getIntValue() ; j++){
                histoCount[j] = 0;
            }
                
            // check if the execution monitor was canceled
            exec.checkCanceled();
            exec.setProgress(rowCurser / (double)m_range.getIntValue(), 
                "Adding row " + rowCurser);
            
            rowCurser++;
        }
        
        
        // once we are done, we close the container and return its table
        container.close();
        BufferedDataTable out = container.getTable();
        return new BufferedDataTable[]{out};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Code executed on reset.
        // Models build during execute are cleared here.
        // Also data handled in load/saveInternals will be erased here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        
    	// first of all validate the incoming data table spec
        
       
        boolean containsName = false;
        for (int i = 0; i < inSpecs[IN_PORT].getNumColumns(); i++) {
            DataColumnSpec columnSpec = inSpecs[IN_PORT].getColumnSpec(i);           
            // if the column name is set  
            // it must be contained in the data table spec
            if (m_column != null 
                    && columnSpec.getName().equals(
                    m_column.getStringValue())) {
                containsName = true;
            }
            
        }        
        
        if (!containsName) {
            throw new InvalidSettingsException(
                    "Input table contains not the column " 
                    + m_column.getStringValue() 
                    + " . Please (re-)configure the node.");
        }

    	
        // and the DataTableSpec for the appended part
        DataTableSpec appendedSpec = createOutputColumnSpec();
                     
        return new DataTableSpec[]{appendedSpec};
    }

    
    private DataTableSpec createOutputColumnSpec() {
    	DataColumnSpec[] allColSpecs = new DataColumnSpec[m_range.getIntValue() + 1];
        for(int i = 0 ; i < m_range.getIntValue() ; i++ ){
        	allColSpecs[i] = new DataColumnSpecCreator("cluster_" + i, IntCell.TYPE).createSpec();
        }      
        allColSpecs[m_range.getIntValue()] = new DataColumnSpecCreator("imgToString", StringCell.TYPE).createSpec();
        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
        
        return outputSpec;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        // TODO save user settings to the config object.        
        m_range.saveSettingsTo(settings);
        m_column.saveSettingsTo(settings);
        m_pattern.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO load (valid) settings from the config object.
        // It can be safely assumed that the settings are valided by the 
    	// method below.        
        m_range.loadSettingsFrom(settings);
        m_column.loadSettingsFrom(settings);
        m_pattern.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO check if the settings could be applied to our model
        // e.g. if the count is in a certain range (which is ensured by the
        // SettingsModel).
        // Do not actually set any values of any member variables.
        m_range.validateSettings(settings);
        m_column.validateSettings(settings);
        m_pattern.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        
        // TODO load internal data. 
        // Everything handed to output ports is loaded automatically (data
        // returned by the execute method, models loaded in loadModelContent,
        // and user settings set through loadSettingsFrom - is all taken care 
        // of). Load here only the other internals that need to be restored
        // (e.g. data used by the views).

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       
        // TODO save internal models. 
        // Everything written to output ports is saved automatically (data
        // returned by the execute method, models saved in the saveModelContent,
        // and user settings saved through saveSettingsTo - is all taken care 
        // of). Save here only the other internals that need to be preserved
        // (e.g. data used by the views).

    }

}

