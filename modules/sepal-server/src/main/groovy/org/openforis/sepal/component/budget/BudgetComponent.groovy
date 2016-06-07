package org.openforis.sepal.component.budget

import org.openforis.sepal.component.AbstractComponent
import org.openforis.sepal.component.budget.adapter.JdbcBudgetRepository
import org.openforis.sepal.component.budget.api.HostingService
import org.openforis.sepal.component.budget.command.*
import org.openforis.sepal.component.budget.internal.InstanceSpendingService
import org.openforis.sepal.component.budget.internal.StorageUseService
import org.openforis.sepal.component.budget.query.GenerateUserSpendingReport
import org.openforis.sepal.component.budget.query.GenerateUserSpendingReportHandler
import org.openforis.sepal.event.HandlerRegistryEventDispatcher
import org.openforis.sepal.transaction.SqlConnectionManager
import org.openforis.sepal.user.JdbcUserRepository
import org.openforis.sepal.user.UserRepository
import org.openforis.sepal.util.Clock
import org.openforis.sepal.util.SystemClock

import javax.sql.DataSource

import static java.util.concurrent.TimeUnit.MINUTES

class BudgetComponent extends AbstractComponent {
    BudgetComponent(HostingService hostingService, DataSource dataSource) {
        this(
                dataSource,
                hostingService,
                new JdbcUserRepository(new SqlConnectionManager(dataSource)),
                new HandlerRegistryEventDispatcher(),
                new SystemClock()
        )
    }

    BudgetComponent(
            DataSource dataSource,
            HostingService hostingService,
            UserRepository userRepository,
            HandlerRegistryEventDispatcher eventDispatcher,
            Clock clock) {
        super(dataSource, eventDispatcher)

        def connectionManager = new SqlConnectionManager(dataSource)
        def budgetRepository = new JdbcBudgetRepository(connectionManager, clock)
        def instanceSpendingService = new InstanceSpendingService(budgetRepository, hostingService, clock)
        def storageUseService = new StorageUseService(budgetRepository, hostingService, clock)

        command(CheckUserInstanceSpending, new CheckUserInstanceSpendingHandler(instanceSpendingService, budgetRepository, eventDispatcher))
        command(CheckUserStorageUse, new CheckUserStorageUseHandler(storageUseService, budgetRepository, eventDispatcher))
        command(UpdateBudget, new UpdateBudgetHandler(budgetRepository))
        command(DetermineUserStorageUsage, new DetermineUserStorageUsageHandler(storageUseService, userRepository))

        query(GenerateUserSpendingReport,
                new GenerateUserSpendingReportHandler(instanceSpendingService, storageUseService, budgetRepository))
    }

    void onStart() {
        schedule(1, MINUTES, new DetermineUserStorageUsage())
    }
}