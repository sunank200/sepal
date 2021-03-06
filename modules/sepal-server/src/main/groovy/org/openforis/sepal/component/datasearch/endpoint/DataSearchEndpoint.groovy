package org.openforis.sepal.component.datasearch.endpoint

import groovymvc.Controller
import org.openforis.sepal.command.CommandDispatcher
import org.openforis.sepal.component.datasearch.SceneArea
import org.openforis.sepal.component.datasearch.SceneMetaData
import org.openforis.sepal.component.datasearch.SceneQuery
import org.openforis.sepal.component.datasearch.query.FindSceneAreasForAoi
import org.openforis.sepal.component.datasearch.query.FindScenesForSceneArea
import org.openforis.sepal.query.QueryDispatcher
import org.openforis.sepal.user.UserRepository
import org.openforis.sepal.util.DateTime

import static groovy.json.JsonOutput.toJson

class DataSearchEndpoint {
    private final QueryDispatcher queryDispatcher
    private final CommandDispatcher commandDispatcher
    private final UserRepository userRepository

    DataSearchEndpoint(QueryDispatcher queryDispatcher,
                       CommandDispatcher commandDispatcher,
                       UserRepository userRepository) {
        this.queryDispatcher = queryDispatcher
        this.commandDispatcher = commandDispatcher
        this.userRepository = userRepository
    }

    void registerWith(Controller controller) {
        controller.with {

            get('/data/sceneareas') {
                response.contentType = "application/json"
                def sceneAreas = queryDispatcher.submit(new FindSceneAreasForAoi(aoiId: params.countryIso))
                def data = sceneAreas.collect { [sceneAreaId: it.id, polygon: polygonData(it)] }
                send(toJson(data))
            }

            get('/data/sceneareas/{sceneAreaId}') {
                response.contentType = "application/json"
                def query = new SceneQuery(
                        sceneAreaId: params.sceneAreaId,
                        fromDate: DateTime.parseDateString(params.startDate as String),
                        toDate: DateTime.parseDateString(params.endDate as String),
                        targetDay: params.targetDay
                )
                def scenes = queryDispatcher.submit(new FindScenesForSceneArea(query))
                def data = scenes.collect { sceneData(it, query) }
                send(toJson(data))
            }
        }
    }

    Map sceneData(SceneMetaData scene, SceneQuery query) {
        [
                sceneId          : scene.id,
                sensor           : scene.sensorId,
                browseUrl        : scene.browseUrl as String,
                acquisitionDate  : DateTime.toDateString(scene.acquisitionDate),
                cloudCover       : scene.cloudCover,
                sunAzimuth       : scene.sunAzimuth,
                sunElevation     : scene.sunElevation,
                daysFromTargetDay: DateTime.daysFromDayOfYear(scene.acquisitionDate, query.targetDay)
        ]
    }

    List polygonData(SceneArea sceneArea) {
        sceneArea.polygon.path.collect { [it.lat, it.lng] }
    }
}
