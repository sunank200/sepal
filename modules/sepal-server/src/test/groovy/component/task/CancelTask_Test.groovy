package component.task

import org.openforis.sepal.command.ExecutionFailed

import static org.openforis.sepal.component.task.api.Task.State.CANCELED

class CancelTask_Test extends AbstractTaskTest {
    def 'Given one passive task, when canceling it, task is canceled, work is not canceled, and session is closed'() {
        def task = pendingTask()

        when:
        cancelTask(task)

        then:
        oneTaskIs CANCELED
        workerGateway.canceledNone()
        sessionManager.closedOne()
    }

    def 'Given one active task, when canceling it, task is canceled, work is canceled, and session is closed'() {
        def task = activeTask()

        when:
        cancelTask(task)

        then:
        oneTaskIs CANCELED
        workerGateway.canceledOne()
        sessionManager.closedOne()
    }

    def 'Given two active tasks, when canceling one, task is canceled, work is canceled, and session is not closed'() {
        activeTask()
        def task = activeTask()

        when:
        cancelTask(task)

        then:
        oneTaskIs CANCELED
        workerGateway.canceledOne()
        sessionManager.closedNone()
    }

    def 'Given two task in different instance types, when canceling one, task is canceled, and session is closed'() {
        def task = pendingTask()
        pendingTask(instanceType: 'another-instance-type')

        when:
        cancelTask(task)

        then:
        oneTaskIs CANCELED
        sessionManager.closedOne()
    }

    def 'Given two tasks for different users, when canceling one, task is canceled, and session is closed'() {
        def task = pendingTask()
        pendingTask(username: 'another-username')

        when:
        cancelTask(task)

        then:
        oneTaskIs CANCELED
        sessionManager.closedOne()
    }

    def 'Given task, when other user cancel task, task is not canceled, and execution fails'() {
        def task = pendingTask([username: 'another-username'])

        when:
        cancelTask(task)

        then:
        noTaskIs CANCELED
        thrown ExecutionFailed
    }

    def 'Given a canceled task, when canceling task, execution fails'() {
        def task = canceledTask()

        when:
        cancelTask(task)

        then:
        thrown ExecutionFailed
    }
}
