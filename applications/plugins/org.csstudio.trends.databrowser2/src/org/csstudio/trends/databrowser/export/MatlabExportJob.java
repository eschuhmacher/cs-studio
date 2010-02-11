package org.csstudio.trends.databrowser.export;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.csstudio.archivereader.ValueIterator;
import org.csstudio.platform.data.ITimestamp;
import org.csstudio.platform.data.IValue;
import org.csstudio.platform.data.ValueUtil;
import org.csstudio.trends.databrowser.model.Model;
import org.csstudio.trends.databrowser.model.ModelItem;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

/** Eclipse Job for exporting data from Model to Matlab-format file
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class MatlabExportJob extends ExportJob
{
    public MatlabExportJob(final Model model, final ITimestamp start,
            final ITimestamp end, final Source source,
            final int optimize_count, final String filename,
            final ExportErrorHandler error_handler)
    {
        super("% ", model, start, end, source, optimize_count, filename, error_handler);
    }

    /** {@inheritDoc} */
    @Override
    protected void printExportInfo(final PrintStream out)
    {
        super.printExportInfo(out);
        out.println(comment);
        out.println(comment + "This file can be loaded into Matlab");
        out.println(comment);
        out.println(comment + "It defines a 'Time Series' object for each channel");
        out.println(comment + "which can be displayed via the 'plot' command.");
        out.println(comment + "Time series can be analyzed further with the Matlab");
        out.println(comment + "Time Series Tools, see Matlab manual.");
        out.println();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void performExport(final IProgressMonitor monitor,
                                 final PrintStream out) throws Exception
    {
        final DateFormat date_format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
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
            MatlabQualityHelper qualities = new MatlabQualityHelper();
            long line_count = 0;
            out.println("clear t;");
            out.println("clear v;");
            out.println("clear q;");
            while (values.hasNext()  &&  !monitor.isCanceled())
            {
                final IValue value = values.next();
                ++line_count;
                // t(1)='03/15/2010 13:30:10';
                out.println("t{" + line_count + "}='" +
                    date_format.format(value.getTime().toCalendar().getTime()) + "';");
                // v(1)=4.125;
                final double num = ValueUtil.getDouble(value);
                if (Double.isNaN(num) || Double.isInfinite(num))
                    out.println("v(" + line_count + ")=NaN;");
                else
                    out.println("v(" + line_count + ")=" + num +";");
                // q(1)=0;
                out.println("q(" + line_count + ")=" + qualities.getQualityCode(value.getSeverity(), value.getStatus()) +";");
                if (line_count % PROGRESS_UPDATE_LINES == 0)
                    monitor.subTask(NLS.bind("{0}: Wrote {1} samples", item.getName(), line_count));
            }
            // channel1 = timeseries(v', t, q', 'Name', 'ThePVName');
            // Patch "_" in name because Matlab plot will interprete it as LaTeX sub-script
            final String channel_name = item.getDisplayName().replace("_", "\\_");
            out.println("channel"+i+"=timeseries(v', t, q', 'Name', '"+channel_name+"');");
            out.println("clear t;");
            out.println("clear v;");
            out.println("clear q;");

            out.print("channel"+i+".QualityInfo.Code=[");
            for (int q=0; q<qualities.getNumCodes(); ++q)
                out.print(" " + q);
            out.println(" ];");

            out.print("channel"+i+".QualityInfo.Description={");
            for (int q=0; q<qualities.getNumCodes(); ++q)
                out.print(" '" + qualities.getQuality(q) + "'");
            out.println(" };");
            
            out.println();
        }
        out.println(comment + "Example for plotting the data");
        for (int i=0; i<model.getItemCount(); ++i)
        {
            out.println("subplot(1, " + model.getItemCount() + ", " + (i+1) + ");");
            out.println("plot(channel" + i + ");");
        }
    }
}
