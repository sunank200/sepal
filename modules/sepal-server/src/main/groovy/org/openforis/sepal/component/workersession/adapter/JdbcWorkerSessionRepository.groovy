package org.openforis.sepal.component.workersession.adapter

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.openforis.sepal.component.workersession.api.Timeout
import org.openforis.sepal.component.workersession.api.WorkerInstance
import org.openforis.sepal.component.workersession.api.WorkerSession
import org.openforis.sepal.component.workersession.api.WorkerSessionRepository
import org.openforis.sepal.transaction.SqlConnectionManager
import org.openforis.sepal.util.Clock

import java.sql.Timestamp

import static org.openforis.sepal.component.workersession.api.WorkerSession.State.ACTIVE
import static org.openforis.sepal.component.workersession.api.WorkerSession.State.PENDING

class JdbcWorkerSessionRepository implements WorkerSessionRepository {
    private final SqlConnectionManager connectionManager
    private final Clock clock

    JdbcWorkerSessionRepository(SqlConnectionManager connectionManager, Clock clock) {
        this.connectionManager = connectionManager
        this.clock = clock
    }

    void insert(WorkerSession session) {
        sql.executeInsert('''
                INSERT INTO worker_session(state, username, worker_type, instance_type, instance_id, host, creation_time, update_time, id)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)''', [
                session.state.name(), session.username, session.workerType, session.instanceType, session.instance.id,
                session.instance.host, session.creationTime, session.updateTime, session.id
        ])
    }

    void update(WorkerSession session) {
        sql.executeUpdate('''
                UPDATE worker_session
                SET state = ?, update_time = ?
                WHERE id = ?''', [session.state.name(), clock.now(), session.id])
    }

    WorkerSession getSession(String sessionId) {
        def row = sql.firstRow('''
                SELECT id, state, username, worker_type, instance_type, instance_id, host, creation_time, update_time
                FROM worker_session
                WHERE id = ?''',
                [sessionId])
        if (!row)
            throw new IllegalStateException("Non-existing worker session: $sessionId")
        toSession(row)
    }

    List<WorkerSession> userSessions(String username, List<WorkerSession.State> states, String workerType = null, String instanceType = null) {
        def query = '''
                SELECT id, state, username, worker_type, instance_type, instance_id, host, creation_time, update_time
                FROM worker_session
                WHERE username = ?'''
        def params = [username]
        if (workerType) {
            query += """
                AND worker_type = ?"""
            params.add(workerType)
        }

        if (states)
            query += """
                AND state IN (${(['?'] * states.size()).join(', ')})"""
        params.addAll(states.collect { it.name() })
        if (instanceType) {
            query += """
                AND instance_type = ?"""
            params << instanceType
        }
        sql.rows(query as String, params).collect { toSession(it) }
    }

    List<WorkerSession> pendingOrActiveSessions() {
        sql.rows('''
                SELECT id, state, username, worker_type, instance_type, instance_id, host, creation_time, update_time
                FROM worker_session
                WHERE state IN (?, ?)''', [PENDING.name(), ACTIVE.name()]
        ).collect { toSession(it) }
    }

    List<WorkerSession> timedOutSessions() {
        def now = clock.now()
        sql.rows('''
                SELECT id, state, username, worker_type, instance_type, instance_id, host, creation_time, update_time
                FROM worker_session
                WHERE (state = ? AND update_time < ?)
                OR (state = ? AND update_time < ?)''', [
                PENDING.name(), Timeout.PENDING.lastValidUpdate(now),
                ACTIVE.name(), Timeout.ACTIVE.lastValidUpdate(now)]
        ).collect { toSession(it) }
    }

    WorkerSession pendingSessionOnInstance(String instanceId) {
        def row = sql.firstRow('''
                SELECT id, state, username, worker_type, instance_type, instance_id, host, creation_time, update_time
                FROM worker_session
                WHERE instance_id = ? AND state = ?''', [instanceId, PENDING.name()])
        if (row)
            return toSession(row)
        return null
    }

    private WorkerSession toSession(GroovyRowResult row) {
        def state = row.state as WorkerSession.State
        new WorkerSession(
                id: row.id,
                state: state,
                username: row.username,
                workerType: row.worker_type,
                instanceType: row.instance_type,
                instance: new WorkerInstance(id: row.instance_id, host: row.host),
                creationTime: toDate(row.creation_time),
                updateTime: toDate(row.update_time)
        )
    }

    private Date toDate(date) {
        new Date((date as Timestamp).time)
    }

    private Sql getSql() {
        connectionManager.sql
    }

}
