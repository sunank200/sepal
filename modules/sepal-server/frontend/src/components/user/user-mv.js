/**
 * @author Mino Togna
 */

var EventBus = require( '../event/event-bus' )
var Events   = require( '../event/events' )
var Loader   = require( '../loader/loader' )

var View  = require( './user-v' )
var Model = require( './user-m' )

var requestSandboxReport = function () {
    var params = {
        url      : '/api/sandbox/report'
        , success: function ( response ) {
            Model.setUserSandboxReport( response )

            View.setSessions( Model.getSessions() )
            View.setSpending( Model.getSpending() )
        }
    }
    
    EventBus.dispatch( Events.AJAX.REQUEST, null, params )
}

var show = function ( e, type ) {
    if ( type == 'user' ) {
        View.init()

        requestSandboxReport()
    }
}

var removeSession = function ( evt, sessionId ) {
    var session = Model.getSessionById( sessionId )

    var params = {
        url         : '/api/' + session.path
        , method    : 'DELETE'
        , beforeSend: function () {
            Loader.show()
        }
        , success   : function ( response ) {
            View.removeSession( sessionId )
            // requestSandboxReport()
            Loader.hide( { delay: 200 } )
        }
    }

    EventBus.dispatch( Events.AJAX.REQUEST, null, params )
}

EventBus.addEventListener( Events.SECTION.SHOW, show )
EventBus.addEventListener( Events.SECTION.USER.REMOVE_SESSION, removeSession )