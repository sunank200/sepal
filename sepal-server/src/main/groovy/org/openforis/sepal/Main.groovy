package org.openforis.sepal

import org.openforis.sepal.command.HandlerRegistryCommandDispatcher
import org.openforis.sepal.endpoint.Endpoints
import org.openforis.sepal.geoserver.GeoServerLayerMonitor
import org.openforis.sepal.metadata.ConcreteMetadataProviderManager
import org.openforis.sepal.metadata.JDBCUsgsDataRepository
import org.openforis.sepal.metadata.crawling.EarthExplorerMetadataCrawler
import org.openforis.sepal.sandbox.*
import org.openforis.sepal.sandboxwebproxy.SandboxWebProxy
import org.openforis.sepal.scene.management.*
import org.openforis.sepal.scene.retrieval.SceneRetrievalComponent
import org.openforis.sepal.transaction.SqlConnectionManager
import org.openforis.sepal.user.JDBCUserRepository
import org.openforis.sepal.util.HttpResourceLocator

class Main {

    def static dataSetRepository
    def static connectionManager

    static void main(String[] args) {
        def propertiesLocation = args.length == 1 ? args[0] : "/etc/sepal/sepal.properties"
        SepalConfiguration.instance.setConfigFileLocation(propertiesLocation)

        deployEndpoints()
        startSceneManager()
        startLayerMonitor()
        startCrawling()
    }

    static startCrawling() {
        def metadataProviderManager = new ConcreteMetadataProviderManager(dataSetRepository)
        metadataProviderManager.registerCrawler(new EarthExplorerMetadataCrawler(new JDBCUsgsDataRepository(connectionManager), new HttpResourceLocator()))
        metadataProviderManager.start();
    }

    static startLayerMonitor() {
        GeoServerLayerMonitor.start()
    }

    static startSceneManager() {
        def scenesDownloadRepo = new JdbcScenesDownloadRepository(
                new SqlConnectionManager(
                        SepalConfiguration.instance.dataSource
                )
        )
        def retrievalComponent = new SceneRetrievalComponent()
        def sceneManager = new SceneManager(
                retrievalComponent.sceneProvider,
                retrievalComponent.sceneProcessor,
                retrievalComponent.scenePublisher,
                scenesDownloadRepo)

        retrievalComponent.register(scenesDownloadRepo, sceneManager)
        sceneManager.start()
    }

    static deployEndpoints() {
        connectionManager = new SqlConnectionManager(SepalConfiguration.instance.dataSource)
        def scenesDownloadRepo = new JdbcScenesDownloadRepository(connectionManager)
        def commandDispatcher = new HandlerRegistryCommandDispatcher(connectionManager)

        def daemonURI = SepalConfiguration.instance.dockerDaemonURI
        def imageName = SepalConfiguration.instance.dockerImageName
        def sandboxManager = new DockerSandboxManager(
                new JDBCUserRepository(connectionManager),
                new DockerRESTClient(daemonURI),
                imageName
        )

        new SandboxWebProxy(9191, ['rstudio-server': 8787], sandboxManager).start()

        dataSetRepository = new JdbcDataSetRepository(connectionManager)
        Endpoints.deploy(
                dataSetRepository,
                commandDispatcher,
                new RequestScenesDownloadCommandHandler(scenesDownloadRepo),
                new ScenesDownloadEndPoint(commandDispatcher, scenesDownloadRepo),
                scenesDownloadRepo,
                new RemoveRequestCommandHandler(scenesDownloadRepo),
                new RemoveSceneCommandHandler(scenesDownloadRepo),
                new SandboxManagerEndpoint(commandDispatcher),
                new ObtainUserSandboxCommandHandler(sandboxManager),
                new ReleaseUserSandboxCommandHandler(sandboxManager)
        )
    }

}