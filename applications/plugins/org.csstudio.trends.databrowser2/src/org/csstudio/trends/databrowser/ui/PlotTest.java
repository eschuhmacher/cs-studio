package org.csstudio.trends.databrowser.ui;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.csstudio.platform.data.IValue;
import org.csstudio.platform.model.IArchiveDataSource;
import org.csstudio.trends.databrowser.model.ModelItem;
import org.csstudio.trends.databrowser.model.PlotSampleArray;
import org.csstudio.trends.databrowser.model.PlotSamples;
import org.csstudio.trends.databrowser.model.TestSampleBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

/** (Headless) JUnit Plug-in demo of Plot
 *  
 *  Simply displays the plot. Static data, no controller.
 *  
 *  Must run as plug-in test to load XY Graph icons etc.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PlotTest implements PlotListener
{
    private boolean run = true;
    
    // PlotListener
    public void scrollRequested(final boolean enable_scrolling)
    {
        System.out.println("Scroll enabled: " + enable_scrolling);
        
    }

    // PlotListener
    public void timeAxisChanged(final long start_ms, final long end_ms)
    {
        System.out.println("Time axis: " + start_ms + " ... " + end_ms);
    }
    
    // PlotListener
    public void valueAxisChanged(int index, double lower, double upper)
    {
        System.out.println("Value axis " + index + ": " + lower + " ... " + upper);
    }

    // PlotListener
    public void droppedName(final String name)
    {
        System.out.println("Name dropped: " + name);
    }
    
    // PlotListener
    public void droppedPVName(final String name, final IArchiveDataSource archive)
    {
        System.out.println("PV Name dropped: " + name);
    }

    private void createGUI(final Composite parent)
    {
        final GridLayout layout = new GridLayout(1, false);
        parent.setLayout(layout);
        
        // Canvas that holds the graph
        final Canvas plot_box = new Canvas(parent, 0);
        plot_box.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, layout.numColumns, 1));

        // [Done] button to end demo
        final Button ok = new Button(parent, SWT.PUSH);
        ok.setText("Done");
        ok.setLayoutData(new GridData(SWT.RIGHT, 0, true, false));
        ok.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                run = false;
            }
        });

        final Plot plot = new Plot(plot_box);
        plot.addListener(this);

        // Create demo samples
        final ArrayList<IValue> values = new ArrayList<IValue>();
        for (int i=1; i<10; ++i)
            values.add(TestSampleBuilder.makeValue(i));
        values.add(TestSampleBuilder.makeError(15, "Disconnected"));
        // TODO This case is not handled: There is a value. Line should continue
        //      until the following 'disconnect', but since it's only one sample,
        //      the plot doesn't show it at all.
        values.add(TestSampleBuilder.makeValue(17));
        values.add(TestSampleBuilder.makeError(18, "Disconnected"));
        
        for (int i=20; i<30; ++i)
            values.add(TestSampleBuilder.makeValue(i));

        final PlotSampleArray samples = new PlotSampleArray();
        samples.set("Demo", values);
        
        // Add item with demo samples
        ModelItem item = new ModelItem("Demo")
        {
            @Override
            public PlotSamples getSamples()
            {
                return samples;
            }

            @Override
            public void write(PrintWriter writer)
            {
            }
        };
        item.setColor(new RGB(0, 0, 255));
        plot.addTrace(item);
        
        plot.setTimeRange(samples.getSample(0).getValue().getTime(),
                samples.getSample(samples.getSize()-1).getValue().getTime());
    }

    @Test
    public void plotDemo()
    {
        final Shell shell = new Shell();
        shell.setSize(600, 500);

        createGUI(shell);
        shell.open();
        
        final Display display = Display.getDefault();
        while (run  &&  !shell.isDisposed())
        {
          if (!display.readAndDispatch())
            display.sleep();
        }    
    }
}
