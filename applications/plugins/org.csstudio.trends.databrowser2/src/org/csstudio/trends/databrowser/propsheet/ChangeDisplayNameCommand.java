package org.csstudio.trends.databrowser.propsheet;

import org.csstudio.swt.xygraph.undo.IUndoableCommand;
import org.csstudio.swt.xygraph.undo.OperationsManager;
import org.csstudio.trends.databrowser.Messages;
import org.csstudio.trends.databrowser.model.ModelItem;

/** Undo-able command to change item's display name
 *  @author Kay Kasemir
 */
public class ChangeDisplayNameCommand implements IUndoableCommand
{
    final private ModelItem item;
    final private String old_name, new_name;

    /** Register and perform the command
     *  @param operations_manager OperationsManager where command will be reg'ed
     *  @param item Model item to configure
     *  @param new_name New value
     */
    public ChangeDisplayNameCommand(final OperationsManager operations_manager,
            final ModelItem item, final String new_name)
    {
        this.item = item;
        this.old_name = item.getDisplayName();
        this.new_name = new_name;
        operations_manager.addCommand(this);
        redo();
    }

    /** {@inheritDoc} */
    public void redo()
    {
        item.setDisplayName(new_name);
    }

    /** {@inheritDoc} */
    public void undo()
    {
        item.setDisplayName(old_name);
    }
    
    /** @return Command name that appears in undo/redo menu */
    @Override
    public String toString()
    {
        return Messages.TraceDisplayName;
    }
}
