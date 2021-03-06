/**
 * @author Mino Togna
 */

var EventBus       = require( '../event/event-bus' )
var Events         = require( '../event/events' )
var Loader         = require( '../loader/loader' )
var View           = require( './search-retrieve-v' )
var SceneAreaModel = require( '../scenes-selection/scenes-selection-m' )
var SearchForm     = require( '../search/search-form' )

View.init()
View.hide( { delay: 0, duration: 0 } )

var show     = false
var appShown = true

var appShow   = function ( e, section ) {
    View.hide()
    appShown = true
}
var appReduce = function ( e, section ) {
    appShown = false
    if ( show ) {
        View.show()
    }
}

var getRequestData = function () {
    var data        = {}
    data.countryIso = SearchForm.countryCode()
    
    var scenes = []
    // console.log( "request data: ", SceneAreaModel )
    $.each( SceneAreaModel.areasSelection(), function ( i, k ) {
        $.each( SceneAreaModel.getSceneAreaSelectedImages( k ), function ( j, img ) {
            scenes.push( { sceneId: img.sceneId, sensor: img.sensor } )
        } )
    } )
    data.scenes = JSON.stringify( scenes )
    
    return data
}

var getRequestParams = function ( url ) {
    var data   = getRequestData()
    var params = {
        url         : url
        , data      : data
        , type      : "POST"
        , beforeSend: function () {
            Loader.show()
        }
        , success   : function () {
            Loader.hide( { delay: 300 } )
            EventBus.dispatch( Events.SECTION.TASK_MANAGER.CHECK_STATUS )
        }
    }
    return params
}

var retrieve = function () {
    // '/data/scenes/retrieve') 
//  { countryIso:ITA, scenes:[ {sceneId: 'LC81900302015079LGN00', sensor: 'LC8'}, ... ] }
    var params = getRequestParams( '/api/data/scenes/retrieve' )
    EventBus.dispatch( Events.AJAX.REQUEST, null, params )
}

var mosaic = function () {
    var params = getRequestParams( '/api/data/scenes/mosaic' )
    EventBus.dispatch( Events.AJAX.REQUEST, null, params )
}

var sceneAreasLoaded = function () {
    show = true
    if ( appShown == false ) {
        appReduce()
    }
}

EventBus.addEventListener( Events.SECTION.SHOW, appShow )
EventBus.addEventListener( Events.SECTION.REDUCE, appReduce )

EventBus.addEventListener( Events.SECTION.SEARCH.RETRIEVE, retrieve )
EventBus.addEventListener( Events.SECTION.SEARCH.MOSAIC, mosaic )

EventBus.addEventListener( Events.SECTION.SEARCH.SCENE_AREAS_LOADED, sceneAreasLoaded )