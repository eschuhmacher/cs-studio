package org.csstudio.trends.databrowser.preferences;

import org.csstudio.trends.databrowser.Activator;
import org.csstudio.trends.databrowser.Messages;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/** Preference Page, registered in plugin.xml
 *  @author Kay Kasemir
 */
public class PreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage
{
    /** Initialize */
    public PreferencePage()
    {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(new ScopedPreferenceStore(new InstanceScope(), Activator.PLUGIN_ID));
        setMessage(Messages.PrefPage_Title);
    }
    
    /** {@inheritDoc} */
    public void init(IWorkbench workbench)
    {
        // NOP
    }

    /** {@inheritDoc} */
    @Override
    protected void createFieldEditors()
    {
        final Composite parent = getFieldEditorParent();

        // Time span: 60 seconds .. 1 month
        final IntegerFieldEditor timespan = new IntegerFieldEditor(Preferences.TIME_SPAN,
                Messages.PrefPage_TimeRange, parent);
        timespan.setValidRange(60, 60*60*24*30);
        addField(timespan);        

        // Scan period: >=0
        final StringFieldEditor scanperiod = new StringFieldEditor(Preferences.SCAN_PERIOD,
                Messages.PrefPage_ScanPeriod, parent)
        {
            @Override
            protected boolean checkState()
            {
                final Text text = getTextControl();
                if (text == null)
                    return false;
                try
                {
                    final double period = Double.parseDouble(text.getText().trim());
                    if (period < 0)
                    {
                        showErrorMessage(Messages.ScanPeriodTT);
                        return false;
                    }
                    clearErrorMessage();
                    return true;
                }
                catch (Exception ex)
                {
                    showErrorMessage(Messages.ScanPeriodTT);
                    return false;
                }
            }
        };
        addField(scanperiod);        
        
        // Live sample buffer: 0 ... max int
        final IntegerFieldEditor buffersize = new IntegerFieldEditor(Preferences.BUFFER_SIZE,
                Messages.PrefPage_LiveBufferSize, parent);
        buffersize.setValidRange(0, Integer.MAX_VALUE);
        addField(buffersize);        
        
        // Refresh period: >0 seconds
        final StringFieldEditor updateperiod = new StringFieldEditor(Preferences.UPDATE_PERIOD,
                Messages.PrefPage_UpdatePeriod, parent)
        {
            @Override
            protected boolean checkState()
            {
                final Text text = getTextControl();
                if (text == null)
                    return false;
                try
                {
                    final double period = Double.parseDouble(text.getText().trim());
                    if (period <= 0)
                    {
                        showErrorMessage(Messages.UpdatePeriodTT);
                        return false;
                    }
                    clearErrorMessage();
                    return true;
                }
                catch (Exception ex)
                {
                    showErrorMessage(Messages.UpdatePeriodTT);
                    return false;
                }
            }
        };
        addField(updateperiod);        
        
        // Line Width: Some pixel range
        final IntegerFieldEditor linewidth = new IntegerFieldEditor(Preferences.LINE_WIDTH,
                Messages.PrefPage_TraceLineWidth, parent);
        linewidth.setValidRange(0, 100);
        addField(linewidth);        
        
        // Archive fetch delay:  0.1 .. 10 seconds
        final IntegerFieldEditor fetch_delay = new IntegerFieldEditor(Preferences.ARCHIVE_FETCH_DELAY,
                Messages.PrefPage_ArchiveFetchDelay, parent);
        fetch_delay.setValidRange(100, 10000);
        addField(fetch_delay);        
        
        // Plot bins: 10 ... one bin per second for a year
        final IntegerFieldEditor plotbins = new IntegerFieldEditor(Preferences.PLOT_BINS,
                Messages.PrefPage_PlotBins, parent);
        plotbins.setValidRange(10, 365*24*60*60);
        addField(plotbins);        

        // Server URLs
        final StringTableFieldEditor urls = new StringTableFieldEditor(
                parent, Preferences.URLS, Messages.PrefPage_DataServerURLs,
                new String[] { Messages.URL },
                new boolean[] { true },
                new int[] { 500 },
                new ArchiveURLEditor(parent.getShell()));
        addField(urls);

        // Default archives
        final StringTableFieldEditor archives = new StringTableFieldEditor(
                parent, Preferences.ARCHIVES, Messages.PrefPage_Archives,
                new String[] { Messages.ArchiveName, Messages.ArchiveKey, Messages.URL },
                new boolean[] { true, true, true },
                new int[] { 100, 50, 500 },
                new ArchiveDataSourceEditor(parent.getShell()));
        addField(archives);
    }
}
