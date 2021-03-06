package org.openforis.sepal.component.workersession.command

import org.openforis.sepal.command.AbstractCommand
import org.openforis.sepal.command.CommandHandler
import org.openforis.sepal.command.InvalidCommand
import org.openforis.sepal.command.Unauthorized
import org.openforis.sepal.component.workersession.api.WorkerSession
import org.openforis.sepal.component.workersession.api.WorkerSessionRepository
import org.openforis.sepal.util.annotation.Data

import static org.openforis.sepal.component.workersession.api.WorkerSession.State.ACTIVE
import static org.openforis.sepal.component.workersession.api.WorkerSession.State.PENDING

@Data(callSuper = true)
class Heartbeat extends AbstractCommand<WorkerSession> {
    String sessionId
}

class HeartbeatHandler implements CommandHandler<WorkerSession, Heartbeat> {
    private final WorkerSessionRepository sessionRepository

    HeartbeatHandler(WorkerSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository
    }

    WorkerSession execute(Heartbeat command) {
        def session = sessionRepository.getSession(command.sessionId)
        if (command.username && command.username != session.username)
            throw new Unauthorized("Session not owned by user: $session", command)
        if (![ACTIVE, PENDING].contains(session.state))
            throw new InvalidCommand('Only active and pending sessions can receive heartbeats', command)
        if (session.active)
            sessionRepository.update(session)
        return session
    }
}
