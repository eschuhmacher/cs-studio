package org.csstudio.trends.databrowser.archive;

import java.util.ArrayList;

import org.csstudio.archivereader.ArchiveReader;
import org.csstudio.trends.databrowser.Messages;
import org.csstudio.trends.databrowser.model.ArchiveDataSource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

/** Eclipse background job for searching names on archive data server
 *  @author Kay Kasemir
 */
abstract public class SearchJob extends Job
{
    final private ArchiveReader reader;
    final private ArchiveDataSource archives[];
    final private String pattern;
    final boolean pattern_is_glob;
    
    /** Create job that connects to given URL, then notifies view when done. */
    public SearchJob(final ArchiveReader reader, final ArchiveDataSource archives[],
            final String pattern, final boolean pattern_is_glob)
    {
        super(NLS.bind(Messages.SearchChannelFmt, pattern));
        this.reader = reader;
        this.archives = archives;
        this.pattern = pattern;
        this.pattern_is_glob = pattern_is_glob;
    }

    /** {@inheritDoc} */
    @Override
    protected IStatus run(final IProgressMonitor monitor)
    {
        final ArrayList<ChannelInfo> channels = new ArrayList<ChannelInfo>();
        monitor.beginTask(reader.getServerName(), archives.length);
        for (ArchiveDataSource archive : archives)
        {
            try
            {
                monitor.subTask(archive.getName());
                final String[] names;
                if (pattern_is_glob)
                    names = reader.getNamesByPattern(archive.getKey(), pattern);
                else
                    names = reader.getNamesByRegExp(archive.getKey(), pattern);
                for (String name : names)
                    channels.add(new ChannelInfo(archive, name));
                monitor.worked(1);
            }
            catch (final Exception ex)
            {
                monitor.setCanceled(true);
                monitor.done();
                archiveServerError(reader.getURL(), ex);
                return Status.CANCEL_STATUS;
            }
        }
        monitor.done();
        receivedChannelInfos(
            (ChannelInfo[]) channels.toArray(new ChannelInfo[channels.size()]));
        return Status.OK_STATUS;
    }

    /** Invoked when the job located names on the server
     *  @param channels List of names found on server
     */
    abstract protected void receivedChannelInfos(ChannelInfo channels[]);

    /** Invoked when the job failed
     *  @param url ArchiveServer URL that resulted in error
     *  @param ex Exception
     */
    abstract protected void archiveServerError(String url, Exception ex);
}