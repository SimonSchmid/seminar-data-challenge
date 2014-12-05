package org.bof;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

/**
 * <code>NodeDialog</code> for the "HistogrammCreator" Node.
 * Creates a histrogram of a column in a specific range by counting all occurences of the values in this column. Also includes missing values in the histogram.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author David Kolb
 */
public class HistogrammCreatorNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring HistogrammCreator node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected HistogrammCreatorNodeDialog() {
        super();
        
        //nr of bins control element
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                	HistogrammCreatorNodeModel.CFGKEY_RANGE,
                	HistogrammCreatorNodeModel.DEFAULT_COUNT,
                    1, Integer.MAX_VALUE),
                    "Range:", /*step*/ 1));
        
        //nr of bins control element
        addDialogComponent(new DialogComponentString(
                new SettingsModelString(
                	HistogrammCreatorNodeModel.CFGKEY_PATTERN,
                	""),
                    "Pattern:"));
        
        // column to bin
        addDialogComponent(new DialogComponentString(
                new SettingsModelString(
                	HistogrammCreatorNodeModel.CFGKEY_COLUMN_NAME,
                	""),
                    "column name:"));               
                    
    }
}

