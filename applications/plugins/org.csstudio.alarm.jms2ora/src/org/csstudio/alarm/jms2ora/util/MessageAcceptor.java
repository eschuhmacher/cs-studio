
/* 
 * Copyright (c) 2009 Stiftung Deutsches Elektronen-Synchrotron, 
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

package org.csstudio.alarm.jms2ora.util;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.csstudio.alarm.jms2ora.IMessageConverter;
import org.csstudio.alarm.jms2ora.Jms2OraPlugin;
import org.csstudio.alarm.jms2ora.VersionInfo;
import org.csstudio.alarm.jms2ora.preferences.PreferenceConstants;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Markus Moeller
 *
 */
public class MessageAcceptor implements MessageListener {
    
    /** The class logger */
    private static final Logger LOG = LoggerFactory.getLogger(MessageAcceptor.class);

    /** Class that collects statistic informations. Query it via XMPP. */
    private StatisticCollector collector;

    /** The class converts the RAWMessage objects to ArchiveMessage objects */
    private IMessageConverter messageConverter;

    /** Array of message receivers */
    private JmsMessageReceiver[] receivers;

    /** Indicates if the application was initialized or not */
    private boolean initialized;

    public MessageAcceptor(IMessageConverter converter, StatisticCollector stat) {
        
        messageConverter = converter;
        collector = stat;

        IPreferencesService prefs = Platform.getPreferencesService();
        String urls = prefs.getString(Jms2OraPlugin.PLUGIN_ID,
                                      PreferenceConstants.JMS_PROVIDER_URLS,
                                      "", null);
        String topics = prefs.getString(Jms2OraPlugin.PLUGIN_ID,
                                        PreferenceConstants.JMS_TOPIC_NAMES,
                                        "", null);
        String factoryClass = prefs.getString(Jms2OraPlugin.PLUGIN_ID,
                                              PreferenceConstants.JMS_CONTEXT_FACTORY_CLASS,
                                              "", null);

        String[] urlList = this.getUrlList(urls);
        String[] topicList = this.getTopicList(topics);

        receivers = new JmsMessageReceiver[urlList.length];

        String hostName = Hostname.getInstance().getHostname();
        
        for(int i = 0;i < urlList.length;i++) {
            
            try {
                receivers[i] = new JmsMessageReceiver(factoryClass, urlList[i], topicList);
                receivers[i].startListener(this, VersionInfo.NAME + "@" + hostName + "_" + this.hashCode());
                initialized = true;
            } catch(Exception e) {
                LOG.error("*** Exception *** : " + e.getMessage());
                initialized = false;
            }
        }
        
        initialized = (initialized == true) ? true : false;
    }
    
    public void closeAllReceivers() {
        
        LOG.info("closeAllReceivers(): Closing all receivers.");
        
        if(receivers != null) {
            for(int i = 0;i < receivers.length;i++) {
                receivers[i].stopListening();
            }
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(Message message) {
        
        if(message instanceof MapMessage) {

            RawMessage rm = new RawMessage((MapMessage) message);
            
            messageConverter.putRawMessage(rm);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("onMessage(): {}", message.toString());
            }
            
            collector.incrementReceivedMessages();
            
        } else {
            LOG.warn("Received a non MapMessage object: {}", message.toString());
            LOG.warn("Discarding invalid message.");
        }        
    }
    
    /**
     * Returns a String array containing the URL's
     * 
     * @param urls - Comma seperated list of JMS URL's
     * @return Array of String
     */
    private String[] getUrlList(String urls) {
        
        String[] result = null;
        
        if(urls.length() > 0) {
            result = urls.split(",");
            for(int i = 0;i < result.length;i++) {
                LOG.info("[" + result[i] + "]");
            }
        } else {
            result = new String[0];
        }

        return result;
    }
    
    /**
     * Returns a String array containing the topic names
     * 
     * @param topics - Comma seperated list of topic names
     * @return Array of String
     */
    private String[] getTopicList(String topics) {
        
        String[] result = null;
        
        if(topics.length() > 0) {
            result = topics.split(",");
            for(int i = 0;i < result.length;i++) {
                LOG.info("[" + result[i] + "]");
            }
        } else {
            result = new String[0];
        }

        return result;
    }
}
