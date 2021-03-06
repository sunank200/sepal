/**
 * @author Mino Togna
 */

require( './user.scss' )

var moment = require( 'moment' )

var EventBus = require( '../event/event-bus' )
var Events   = require( '../event/events' )
var Sepal    = require( '../main/sepal' )

var template          = require( './user.html' )
var html              = $( template( {} ) )
// UI components
var sessionsSection   = null
var rowSessionSection = null
var resourcesSection  = null

var init = function () {
    var appSection = $( '#app-section' ).find( '.user' )
    
    if ( appSection.children().length <= 0 ) {
        appSection.append( html )
        
        sessionsSection   = html.find( '.sessions' )
        rowSessionSection = html.find( '.row-session-placeholder' )
        resourcesSection  = html.find( '.resources' )
    }
    
}

var setSessions = function ( sessions ) {
    sessionsSection.find( '.row-session' ).remove()
    
    $.each( sessions, function ( i, session ) {
        var row = rowSessionSection.clone()
        row.removeClass( 'row-session-placeholder' ).addClass( 'row-session ' + session.id )
        
        row.find( '.type' ).html( session.instanceType.name )
        var creationTimeFromNow = moment( session.creationTime, "YYYY-MM-DD[T]HH:mm:ss" ).fromNow()
        row.find( '.time' ).html( creationTimeFromNow )
        row.find( '.cost' ).html( session.costSinceCreation + " USD " )

        row.find( '.btn-remove' ).click( function ( e ) {
            EventBus.dispatch( Events.SECTION.USER.REMOVE_SESSION, null, session.id )
        } )

        sessionsSection.append( row )
        setTimeout( function () {
            row.fadeIn( 200 )
        }, i * 100 )
    } )
}

var removeSession = function ( sessionId ) {
    var sessionRow = sessionsSection.find( '.' + sessionId )
    sessionRow.fadeOut( {
        complete: function () {
            sessionRow.remove()
        }
    } )
}

var setSpending = function ( spending ) {
    resourcesSection.find( '.monthlyInstanceBudget' ).html( spending.monthlyInstanceBudget + " USD" )
    resourcesSection.find( '.monthlyInstanceSpending' ).html( spending.monthlyInstanceSpending + " USD" )
    resourcesSection.find( '.monthlyStorageBudget' ).html( spending.monthlyStorageBudget + " USD" )
    resourcesSection.find( '.monthlyStorageSpending' ).html( spending.monthlyStorageSpending + " USD" )
    resourcesSection.find( '.storageQuota' ).html( spending.storageQuota + " GB" )
    resourcesSection.find( '.storageUsed' ).html( (spending.storageUsed).toFixed( 2 ) + " GB" )
}

module.exports = {
    init           : init
    , setSessions  : setSessions
    , setSpending  : setSpending
    , removeSession: removeSession
}