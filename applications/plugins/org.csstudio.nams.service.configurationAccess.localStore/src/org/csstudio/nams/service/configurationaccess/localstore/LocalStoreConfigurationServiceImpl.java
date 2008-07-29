package org.csstudio.nams.service.configurationaccess.localstore;

import java.util.Collection;
import java.util.List;

import org.csstudio.nams.service.configurationaccess.localstore.declaration.AlarmbearbeiterDTO;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.AlarmbearbeiterGruppenDTO;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.Configuration;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.FilterDTO;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.HistoryDTO;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.LocalStoreConfigurationService;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.NewAMSConfigurationElementDTO;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.ReplicationStateDTO;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.TopicDTO;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.exceptions.InconsistentConfigurationException;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.exceptions.StorageError;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.exceptions.StorageException;
import org.csstudio.nams.service.configurationaccess.localstore.declaration.exceptions.UnknownConfigurationElementError;
import org.csstudio.nams.service.configurationaccess.localstore.internalDTOs.DefaultFilterTextDTO;
import org.csstudio.nams.service.configurationaccess.localstore.internalDTOs.FilterConditionDTO;
import org.csstudio.nams.service.configurationaccess.localstore.internalDTOs.RubrikDTO;
import org.csstudio.nams.service.configurationaccess.localstore.internalDTOs.filterConditionSpecifics.HasManuallyJoinedElements;
import org.csstudio.nams.service.logging.declaration.Logger;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

/**
 * Implementation für Hibernate.
 * 
 * TODO Rename to ConfigurationStoreServiceHibernateImpl
 */
class LocalStoreConfigurationServiceImpl implements
		LocalStoreConfigurationService {

	private final Logger logger;
	private final SessionFactory sessionFactory;

	/**
	 * Für Tests.
	 */
	SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	/**
	 * 
	 * @param session
	 *            The session to work on; the session will be treated as
	 *            exclusive instance and be closed on finalization of this
	 *            service instance.
	 * @param logger
	 */
	public LocalStoreConfigurationServiceImpl(
			final SessionFactory sessionFactory, final Logger logger) {
		this.sessionFactory = sessionFactory;
		this.logger = logger;

		transactionProcessor = new TransactionProcessor(sessionFactory, logger);
	}

	public void deleteDTO(final NewAMSConfigurationElementDTO dto)
			throws StorageError, StorageException,
			InconsistentConfigurationException {
		
		UnitOfWork<Object> loadEntireConfigurationWork = new UnitOfWork<Object>() {
			public Object doWork(Mapper mapper) throws Throwable {
				mapper.delete(dto);
				return dto;
			}
		};

		try {
			this.transactionProcessor
					.doInTransaction(loadEntireConfigurationWork);
		} catch (InterruptedException e) {
			logger.logWarningMessage(this,
					"Delete of DTO interrupted", e);
			throw new StorageException(
					"Delete of DTO interrupted", e);
		}
	}

	public ReplicationStateDTO getCurrentReplicationState()
			throws StorageError, StorageException,
			InconsistentConfigurationException {
		ReplicationStateDTO result = null;
		Session session = null;
		try {
			session = this.openNewSession();
			final Transaction newTransaction = session.beginTransaction();
			newTransaction.begin();
			final List<?> messages = session.createQuery(
					"from ReplicationStateDTO r where r.flagName = '"
							+ ReplicationStateDTO.DB_FLAG_NAME + "'").list();

			if (!messages.isEmpty()) {
				result = (ReplicationStateDTO) messages.get(0);
			}
			newTransaction.commit();
		} catch (final Throwable t) {
			new StorageError("Failed to write replication flag", t);
		} finally {
			closeSession(session);
		}

		if (result == null) {
			throw new InconsistentConfigurationException(
					"Replication state unavailable.");
		}

		return result;
	}

	public Configuration getEntireConfiguration() throws StorageError,
			StorageException, InconsistentConfigurationException {

		Configuration result = null;

		UnitOfWork<Configuration> loadEntireConfigurationWork = new UnitOfWork<Configuration>() {
			public Configuration doWork(Mapper mapper) throws Throwable {
				Configuration resultOfUnit = null;

				Collection<RubrikDTO> alleRubriken = mapper.loadAll(RubrikDTO.class, true); // FIXME Bei Joined hinzufuegen fuer entsprechende Elemente zuordnen!!!!

				Collection<AlarmbearbeiterDTO> alleAlarmbarbeiter = mapper.loadAll(AlarmbearbeiterDTO.class, true);
				Collection<TopicDTO> alleAlarmtopics = mapper.loadAll(TopicDTO.class, true);
				Collection<AlarmbearbeiterGruppenDTO> alleAlarmbearbeiterGruppen = mapper.loadAll(AlarmbearbeiterGruppenDTO.class, true);
				Collection<FilterConditionDTO> allFilterConditions = mapper.loadAll(FilterConditionDTO.class, true);
				Collection<FilterDTO> allFilters = mapper.loadAll(FilterDTO.class, true);
				Collection<DefaultFilterTextDTO> allDefaultFilterTextDTO = mapper.loadAll(DefaultFilterTextDTO.class, true);


				resultOfUnit = new Configuration(alleAlarmbarbeiter,
						alleAlarmtopics, alleAlarmbearbeiterGruppen,
						allFilters, allFilterConditions, alleRubriken,
						allDefaultFilterTextDTO);

				return resultOfUnit;
			}
		};

		try {
			result = this.transactionProcessor
					.doInTransaction(loadEntireConfigurationWork);
		} catch (InterruptedException e) {
			logger.logWarningMessage(this,
					"Load of entire configuration interrupted", e);
			throw new StorageException(
					"Load of entire configuration interrupted", e);
		}

		return result;
	}

	public void prepareSynchonization() throws StorageError, StorageException,
			InconsistentConfigurationException {
		// TODO Hier die Syn-Tabellen anlegen / Datgen kopieren / GGf. über ein
		// HSQL-Statement.
		throw new UnsupportedOperationException("not implemented yet.");
	}

	public void saveCurrentReplicationState(
			final ReplicationStateDTO currentState) throws StorageError,
			StorageException, UnknownConfigurationElementError {
		Transaction newTransaction = null;
		Session session = null;
		try {
			session = this.openNewSession();
			newTransaction = session.beginTransaction();
			newTransaction.begin();
			session.saveOrUpdate(currentState);
			newTransaction.commit();
		} catch (final Throwable t) {
			if (newTransaction != null) {
				newTransaction.rollback();
			}
			throw new StorageException("unable to save replication state", t);
		} finally {
			closeSession(session);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends NewAMSConfigurationElementDTO> List<T> loadAll(
			Session session, Class<T> clasz) {
		List<T> list = session.createCriteria(clasz).list();

		return list;
	}

	public void saveDTO(final NewAMSConfigurationElementDTO dto)
			throws StorageError, StorageException,
			InconsistentConfigurationException {

		UnitOfWork<NewAMSConfigurationElementDTO> saveWork = new UnitOfWork<NewAMSConfigurationElementDTO>() {
			public NewAMSConfigurationElementDTO doWork(Mapper mapper)
					throws Throwable {

				mapper.save(dto); // performs "deep" save

				return dto;
			}
		};

		try {
			this.transactionProcessor.doInTransaction(saveWork);
		} catch (InterruptedException e) {
			logger.logWarningMessage(this, "save has been interrupted", e);
			throw new StorageException("save has been interrupted", e);
		}
	}

	public void saveHistoryDTO(final HistoryDTO historyDTO)
			throws StorageError, StorageException,
			InconsistentConfigurationException {
		Transaction newTransaction = null;
		Session session = null;
		try {
			session = this.openNewSession();
			newTransaction = session.beginTransaction();
			newTransaction.begin();
			session.saveOrUpdate(historyDTO);
			newTransaction.commit();
		} catch (final Throwable t) {
			if (newTransaction != null) {
				newTransaction.rollback();
			}
			throw new StorageException("unable to save history element", t);
		} finally {
			closeSession(session);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		this.sessionFactory.close();
	}


	private Session sessionWorkingOn = null;
	private TransactionProcessor transactionProcessor;

	private void closeSession(Session session) throws HibernateException {
		if (session != null && session.isOpen()) {
			try {
				session.flush();
				// session.close();
			} catch (final HibernateException he) {
				sessionWorkingOn.close();
				sessionWorkingOn = null;
				throw new StorageError("session could not be closed", he);
			}
		}
	}

	private Session openNewSession() throws HibernateException {
		Session result = null;
		if (sessionWorkingOn == null) {
			result = this.sessionFactory.openSession();
			result.setCacheMode(CacheMode.IGNORE);
			result.setFlushMode(FlushMode.COMMIT);
		} else {
			result = sessionWorkingOn;
		}
		return result;
	}


}
