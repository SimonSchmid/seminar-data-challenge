package org.bof;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "HistogrammCreator" Node.
 * Creates a histrogram of a column in a specific range by counting all occurences of the values in this column. Also includes missing values in the histogram.
 *
 * @author David Kolb
 */
public class HistogrammCreatorNodeFactory 
        extends NodeFactory<HistogrammCreatorNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public HistogrammCreatorNodeModel createNodeModel() {
        return new HistogrammCreatorNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<HistogrammCreatorNodeModel> createNodeView(final int viewIndex,
            final HistogrammCreatorNodeModel nodeModel) {
        return new HistogrammCreatorNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new HistogrammCreatorNodeDialog();
    }

}

