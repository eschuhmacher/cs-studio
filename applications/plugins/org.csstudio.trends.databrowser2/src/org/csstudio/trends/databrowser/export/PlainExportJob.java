package org.csstudio.trends.databrowser.export;

import java.io.PrintStream;

import org.csstudio.archivereader.ValueIterator;
import org.csstudio.platform.data.ITimestamp;
import org.csstudio.platform.data.IValue;
import org.csstudio.trends.databrowser.Messages;
import org.csstudio.trends.databrowser.model.Model;
import org.csstudio.trends.databrowser.model.ModelItem;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

/** Eclipse Job for exporting data from Model to file
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PlainExportJob extends ExportJob
{
    final protected ValueFormatter formatter;
    
    public PlainExportJob(final Model model, 
            final ITimestamp start, final ITimestamp end, final Source source,
            final int optimize_count, final ValueFormatter formatter,
            final String filename,
            final ExportErrorHandler error_handler)
    {
        super("# ", model, start, end, source, optimize_count, filename, error_handler);
        this.formatter = formatter;
    }

    /** {@inheritDoc} */
    @Override
    protected void printExportInfo(final PrintStream out)
    {
        super.printExportInfo(out);
        out.println(comment + "Format     : " + formatter.toString());
        out.println(comment);
        out.println(comment + "Data is in TAB-delimited columns, should import into e.g. Excel");
        out.println();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void performExport(final IProgressMonitor monitor,
                                 final PrintStream out) throws Exception
    {
        for (int i=0; i<model.getItemCount(); ++i)
        {
            final ModelItem item = model.getItem(i);
            // Item header
            if (i > 0)
                out.println();
            printItemInfo(out, item);
            // Get data
            monitor.subTask(NLS.bind("Fetching data for {0}", item.getName()));
            final ValueIterator values = createValueIterator(item);
            // Dump all values
            out.println(comment + Messages.TimeColumn + Messages.Export_Delimiter + formatter.getHeader());
            long line_count = 0;
            while (values.hasNext()  &&  !monitor.isCanceled())
            {
                final IValue value = values.next();
                out.println(value.getTime() + Messages.Export_Delimiter + formatter.format(value));
                ++line_count;
                if (++line_count % PROGRESS_UPDATE_LINES == 0)
                    monitor.subTask(NLS.bind("{0}: Wrote {1} samples", item.getName(), line_count));
            }
        }
    }
}
