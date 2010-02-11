package org.csstudio.trends.databrowser.propsheet;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

/** Base class for TableViewerColumn EditingSupport,
 *  in most cases only leaving getValue()/setValue() to be implemented.
 *  @author Kay Kasemir
 */
abstract public class EditSupportBase extends EditingSupport
{
    /** Initialize
     *  @param table_viewer Always require a TableViewer
     */
    public EditSupportBase(final TableViewer table_viewer)
    {
        super(table_viewer);
    }

    /** Column is always editable */
    @Override
    protected boolean canEdit(final Object element)
    {
        return true;
    }
    
    /** Default to TextCellEditor */
    @Override
    protected CellEditor getCellEditor(final Object element)
    {
        return new TextCellEditor(((TableViewer)getViewer()).getTable());
    }
}
