package org.csstudio.trends.databrowser.propsheet;

import org.csstudio.swt.xygraph.undo.IUndoableCommand;
import org.csstudio.swt.xygraph.undo.OperationsManager;
import org.csstudio.trends.databrowser.Messages;
import org.csstudio.trends.databrowser.model.ModelItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

/** Undo-able command to change item's name
 *  @author Kay Kasemir
 */
public class ChangeNameCommand implements IUndoableCommand
{
    final private Shell shell;
    final private ModelItem item;
    final private String old_name, new_name;

    /** Register and perform the command
     *  @param shell Shell used for error dialogs
     *  @param operations_manager OperationsManager where command will be reg'ed
     *  @param item Model item to configure
     *  @param new_name New value
     */
    public ChangeNameCommand(final Shell shell,
            final OperationsManager operations_manager,
            final ModelItem item, final String new_name)
    {
        this.shell = shell;
        this.item = item;
        this.old_name = item.getName();
        this.new_name = new_name;
        try
        {
            item.setName(new_name);
        }
        catch (Exception ex)
        {
            MessageDialog.openError(shell, Messages.Error,
                    NLS.bind(Messages.ChangeNameErrorFmt,
                            new Object[] { old_name, new_name, ex.getMessage()}));
            // Exit before registering for undo because there's nothing to undo
            return;
        }
        operations_manager.addCommand(this);
    }

    /** {@inheritDoc} */
    public void redo()
    {
        try
        {
            item.setName(new_name);
        }
        catch (Exception ex)
        {
            MessageDialog.openError(shell, Messages.Error,
                    NLS.bind(Messages.ChangeNameErrorFmt,
                            new Object[] { old_name, new_name, ex.getMessage()}));
        }
    }

    /** {@inheritDoc} */
    public void undo()
    {
        try
        {
            item.setName(old_name);
        }
        catch (Exception ex)
        {
            MessageDialog.openError(shell, Messages.Error,
                    NLS.bind(Messages.ChangeNameErrorFmt,
                            new Object[] { new_name, old_name, ex.getMessage()}));
        }
    }
    
    /** @return Command name that appears in undo/redo menu */
    @Override
    public String toString()
    {
        return Messages.ItemName;
    }
}
