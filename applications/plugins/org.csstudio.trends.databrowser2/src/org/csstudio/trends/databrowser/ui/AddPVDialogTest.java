package org.csstudio.trends.databrowser.ui;

import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

/** [Headless] JUnit Plug-in demo of the AddPVDialog
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class AddPVDialogTest
{
    @Test
    public void dialogTest()
    {
        final Shell shell = new Shell();
        final String names[] = new String[] { "Fred", "Jane" };
        final AddPVDialog dlg = new AddPVDialog(shell, names);
        if (dlg.open() == AddPVDialog.OK)
        {
            System.out.println("Name  : " + dlg.getName());
            System.out.println("Period: " + dlg.getScanPeriod());
        }
    }
}
