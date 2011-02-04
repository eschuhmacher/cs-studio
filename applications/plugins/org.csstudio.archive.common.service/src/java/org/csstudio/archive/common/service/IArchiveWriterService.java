/*
 * Copyright (c) 2010 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */
package org.csstudio.archive.common.service;

import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.csstudio.archive.common.service.adapter.IValueWithChannelId;
import org.csstudio.platform.data.ITimestamp;
import org.csstudio.platform.data.IValue;

/**
 * Archive engine writer methods.
 *
 * TODO (bknerr): all database access methods should definitely return explicit immutables.
 *                Note guavas immutable collections implement 'mutable' interfaces with
 *                throwing UOEs. mmh.
 *
 * @author bknerr
 * @since 12.11.2010
 */
public interface IArchiveWriterService {

    /**
     * Retrieves the time stamp of the latest sample for the given channel.
     *
     * @param name the identifying channel name.
     * @return the time stamp of the latest sample
     * @throws ArchiveServiceException if the retrieval of the latest time stamp failed
     */
    @CheckForNull
    ITimestamp getLatestTimestampForChannel(@Nonnull final String name) throws ArchiveServiceException;

    /**
     * Retrieves the channel id for a given channel name.
     * @param name the name of the channel
     * @return the id
     * @throws ArchiveServiceException
     */
    int getChannelId(@Nonnull final String name) throws ArchiveServiceException;

    /**
     * Writes the samples to the archive.
     *
     * @param samples the samples to be archived with their channel id
     * @return true, if the samples have been persisted
     * @throws ArchiveServiceException
     */
    boolean writeSamples(@Nonnull final Collection<IValueWithChannelId> samples) throws ArchiveServiceException;

    /**
     * Transfers the sample information to the persistence layer.
     * Has to be followed by {@link IArchiveWriterService#flush()} to actually persist the
     * information.
     * @param channelId
     * @param value
     * @throws ArchiveServiceException
     */
    void submitSample(int channelId, IValue value) throws ArchiveServiceException;

    /**
     * TODO (kasemir) : committing or persisting?
     * Commits AND directly writes the metadata information out of the given value for this channel to the persistence
     * layer.
     * Does NOT have to be followed by {@link IArchiveWriterService#flush()}.
     *
     * @param channelName the name of the channel
     * @param sample the current sample
     * @throws ArchiveServiceException if the writing of meta data failed
     */
    void writeMetaData(@Nonnull final String channelName, @Nonnull final IValue sample) throws ArchiveServiceException;

    /**
     * Triggers the persistence layer to actually persist the committed information.
     *
     * @return true, if the committed information had been successfully persisted.
     * @throws ArchiveServiceException
     */
    boolean flush() throws ArchiveServiceException;
}