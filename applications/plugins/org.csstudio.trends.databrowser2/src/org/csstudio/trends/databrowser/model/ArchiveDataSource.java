package org.csstudio.trends.databrowser.model;

import org.csstudio.platform.model.AbstractControlSystemItem;
import org.csstudio.platform.model.IArchiveDataSource;

/** Archive data source
 *  @author Kay Kasemir
 */
public class ArchiveDataSource extends AbstractControlSystemItem implements
        IArchiveDataSource
{
    /** URL of the archive data server. */
    private String url;

    /** Key of the archive under the url. */
    private int key;

    /** Description of the data source. */
    private String description;
    
    /** Initialize
     *  @param url Data server URL.
     *  @param key Archive key.
     *  @param name Archive name, derived from key.
     */
    public ArchiveDataSource(final String url, final int key, final String name)
    {
        this(url, key, name, name);
    }

    /** Initialize
     *  @param url Data server URL.
     *  @param key Archive key.
     *  @param name Archive name, derived from key.
     *  @param description Description, up to archive data server
     */
    public ArchiveDataSource(final String url, final int key, final String name,
            final String description)
    {
        super(name);
        this.url = url;
        this.key = key;
        this.description = description;
    }

    /** Initialize this IArchiveDataSource from generic interface */
    public ArchiveDataSource(final IArchiveDataSource archive)
    {
        this(archive.getUrl(), archive.getKey(), archive.getName());
    }

    /** {@inheritDoc} */
    public final String getTypeId()
    {
        return TYPE_ID;
    }

    /** {@inheritDoc} */
    public final String getUrl()
    {
        return url;
    }

    /** {@inheritDoc} */
    public final int getKey()
    {
        return key;
    }

    /** @return Description */
    public final String getDescription()
    {
        return description;
    }

    /** Compare ArchiveDataSource by URL and key, ignoring the description
     *  {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (! (obj instanceof ArchiveDataSource))
            return false;
        if (obj == this)
            return true;
        final ArchiveDataSource other = (ArchiveDataSource) obj;
        return key == other.key && url.equals(other.url);
    }

    /** Debug string representation */
    @SuppressWarnings("nls")
    @Override
    public final String toString()
    {
        return "Archive '" + url + "' (" + key + ", '" + getName() + "')";
    }
}
