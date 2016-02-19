package integration.dataprovider.earthexplorer

import fake.SynchronousJobExecutor
import org.openforis.sepal.SepalConfiguration
import org.openforis.sepal.scene.DataSet
import org.openforis.sepal.scene.SceneReference
import org.openforis.sepal.scene.SceneRequest
import org.openforis.sepal.scene.retrieval.FileSystemSceneRepository
import org.openforis.sepal.scene.retrieval.provider.FileSystemSceneContextProvider
import org.openforis.sepal.scene.retrieval.provider.earthexplorer.EarthExplorerSceneProvider
import org.openforis.sepal.scene.retrieval.provider.earthexplorer.RestfulEarthExplorerClient
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class EarthExplorerSceneProviderTest extends Specification {
//    def tmpDir = File.createTempDir()
//    def workingDir = new File(tmpDir, 'workingDir')
//    def homeDir = new File(tmpDir, 'home')
//    EarthExplorerSceneProvider provider
//
//    def setup() {
//        workingDir.mkdir()
//        homeDir.mkdir()
//        def config = SepalConfiguration.instance
//        config.setConfigFileLocation('/Users/wiell/Documents/Dump/sepal/sepal.properties')
//        this.provider = new EarthExplorerSceneProvider(
//                new RestfulEarthExplorerClient(),
//                new SynchronousJobExecutor(),
//                new FileSystemSceneContextProvider(new FileSystemSceneRepository(workingDir, homeDir.absolutePath)))
//    }
//
//    def 'Test'() {
//        when:
//        def requests = provider.retrieve([new SceneRequest(1, new SceneReference('LC81920292016048LGN00', DataSet.LANDSAT_8), 'some-user')])
//
//        then:
//        false
//    }
}
