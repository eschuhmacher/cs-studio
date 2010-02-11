package org.csstudio.trends.databrowser.export;

import org.csstudio.trends.databrowser.Messages;

/** From where to export data
 *  @author Kay Kasemir
 */
public enum Source
{
    /** Use data from plot */
    PLOT(Messages.ExportSource_Plot),
    /** Fetch raw archive data */
    RAW_ARCHIVE(Messages.ExportSource_RawArchive),
    /** Get optimized (reduced) archive data */
    OPTIMIZED_ARCHIVE(Messages.ExportSource_OptimizedArchive);
    
    final private String name;
    
    private Source(final String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
