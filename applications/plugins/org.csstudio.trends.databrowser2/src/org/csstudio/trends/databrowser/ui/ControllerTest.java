package org.csstudio.trends.databrowser.ui;

import org.csstudio.platform.data.ITimestamp;
import org.csstudio.trends.databrowser.model.ArchiveDataSource;
import org.csstudio.trends.databrowser.model.FormulaInput;
import org.csstudio.trends.databrowser.model.FormulaItem;
import org.csstudio.trends.databrowser.model.Model;
import org.csstudio.trends.databrowser.model.ModelItem;
import org.csstudio.trends.databrowser.model.PVItem;
import org.csstudio.trends.databrowser.model.PlotSample;
import org.csstudio.trends.databrowser.model.PlotSamples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

/** (Headless) JUnit Plug-in demo of Controller for Plot and Model.
 * 
 *  Creates Model, Plot and Controller, showing most of the functionality
 *  but does not have a workbench. 
 *
 *  Must run as plug-in test to load XY Graph icons etc.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ControllerTest
{
    private boolean run = true;
    
    private Model model;
    private Plot plot;

    private Controller controller;
    
    private void createModel() throws Exception
    {
        model = new Model();

        ModelItem item;
        
        item = new PVItem("sim://sine(-1, 1, 20, 0.25)", 1);
        item.setDisplayName("Sine (scanned)");
        model.addItem(item);

        item = new FormulaItem("math", "sine*0.5+2",
                new FormulaInput[] { new FormulaInput(item, "sine") });
        
        item = new PVItem("sim://ramp(0, 2, 40, 0.5)", 0);
        item.setDisplayName("Ramp (monitored)");
        item.setAxis(1);
        model.addItem(item);

        final ArchiveDataSource archive = new ArchiveDataSource("jdbc:oracle:thin:sns_reports/sns@(DESCRIPTION=(ADDRESS_LIST=(LOAD_BALANCE=OFF)(ADDRESS=(PROTOCOL=TCP)(HOST=172.31.75.138)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=172.31.75.141)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=ics_prod_lba)))",
                1, "rdb");
        item = new PVItem("CCL_LLRF:IOC1:Load", 0);
        ((PVItem)item).addArchiveDataSource(archive);
        item.setDisplayName("CCL 1 CPU Load (monitored)");
        item.setAxis(2);
        model.addItem(item);

        item = new PVItem("DTL_LLRF:IOC1:Load", 1.0);
        ((PVItem)item).addArchiveDataSource(archive);
        item.setDisplayName("DTL 1 CPU Load (1 sec)");
        item.setAxis(2);
        model.addItem(item);

        item = new FormulaItem("calc", "dtl-10",
                new FormulaInput[] { new FormulaInput(item, "dtl") });
        item.setDisplayName("Lessened Load");
        item.setAxis(2);
        model.addItem(item);
    }

    private void createGUI(final Composite parent)
    {
        final GridLayout layout = new GridLayout(2, false);
        parent.setLayout(layout);
        
        // Canvas that holds the graph
        final Canvas plot_box = new Canvas(parent, 0);
        plot_box.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, layout.numColumns, 1));

        // [Debug] button
        final Button debug = new Button(parent, SWT.PUSH);
        debug.setText("Debug");
        debug.setLayoutData(new GridData(SWT.RIGHT, 0, true, false));
        debug.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                debug();
            }
        });
        
        // [Done] button to end demo
        final Button ok = new Button(parent, SWT.PUSH);
        ok.setText("Done");
        ok.setLayoutData(new GridData(SWT.RIGHT, 0, false, false));
        ok.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                run = false;
            }
        });

        plot = new Plot(plot_box);
    }

    protected void debug()
    {
        for (int i=0; i<model.getItemCount(); ++i)
        {
            final ModelItem item = model.getItem(i);
            if (! (item instanceof PVItem))
                continue;
            System.out.println("\n" + item.getName() + ":");
            final PlotSamples samples = item.getSamples();
            synchronized (samples)
            {
                if (samples.getSize() <= 0)
                    continue;
                ITimestamp last = samples.getSample(0).getTime();
                for (int s=0; s<samples.getSize(); ++s)
                {
                    final PlotSample sample = samples.getSample(s);
                    System.out.println(sample);
                    final ITimestamp time = sample.getTime();
                    if (time.isLessThan(last))
                    {
                        System.out.println("Time sequence error!");
                        break;
                    }
                    last = time;
                }
            }
        }
    }

    @Test
    public void controllerDemo() throws Exception
    {
        final Display display = Display.getDefault();

        final Shell shell = new Shell();
        shell.setSize(600, 500);

        createModel();
        createGUI(shell);
        controller = new Controller(shell, model, plot);
        controller.start();
        
        shell.open();
        
        while (run  &&  !shell.isDisposed())
        {
          if (!display.readAndDispatch())
            display.sleep();
        }
        
        controller.stop();
        System.out.println("Controller stopped, waiting a little");
        // Wait a few seconds to see if more events happen after controller is stopped
        Thread.sleep(2000);
        shell.dispose();
    }
}
