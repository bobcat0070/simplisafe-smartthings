/**
 *  SimpliSafe integration for SmartThings
 *
 *  Copyright 2015 Felix Gorodishter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
preferences {
	input(name: "username", type: "text", title: "Username", required: "true", description: "Your SimpliSafe username")
	input(name: "password", type: "password", title: "Password", required: "true", description: "Your SimpliSafe password")
	input(name: "locationID", type: "text", title: "Location ID", required: "true", description: "Your SimpliSafe location ID")
	input(name: "devicename", type: "text", title: "Device name", defaultValue: "SmartThings", required: "true", description: "Friendly name for SimpliSafe Login Info")
	input(name: "deviceuuid", type: "text", title: "Device UID", required: "true", description: "Device ID for SimpliSafe Login Info")
}
 
metadata {
	// Automatically generated. Make future change here.
	definition (name: "SimpliSafe", namespace: "bobcat0070", author: "Felix Gorodishter") {
		capability "Alarm"
		capability "Polling"
        capability "Contact Sensor"
		capability "Presence Sensor"
		capability "Carbon Monoxide Detector"
		capability "Smoke Detector"
        capability "Temperature Measurement"
        capability "Water Sensor"
		command "home"
		command "away"
		command "off"
		attribute "recent_alarm", "string" 
		attribute "recent_fire", "string" 
		attribute "recent_co", "string" 
		attribute "recent_flood", "string" 
		attribute "freeze", "string" 
		attribute "warnings", "string"        
	}

	simulator {
		// TODO: define status and reply messages here
	}

	
tiles(scale: 2) {
    multiAttributeTile(name:"alarm", type: "generic", width: 6, height: 4){
        tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
            attributeState "Off", label:'${name}', icon: "st.security.alarm.off", backgroundColor: "#ffffff"
            attributeState "Home", label:'${name}', icon: "st.security.alarm.on", backgroundColor: "#00E6E6"
            attributeState "Away", label:'${name}', icon: "st.security.alarm.on", backgroundColor: "#E60000"
        }
    }	
		standardTile("recent_alarm", "device.contact", inactiveLabel: false, width: 2, height: 2) {
			state "closed", label:'Alarm', icon: "st.security.alarm.clear", backgroundColor: "#79b821"
			state "open", label:'ALARM', icon: "st.security.alarm.alarm", backgroundColor: "#E60000"
		}
		standardTile("freeze", "device.freeze_status", inactiveLabel: false, width: 2, height: 2) {
			state "no alert", label:'${new_freeze}°', icon: "st.alarm.temperature.normal", backgroundColor: "#79b821"
			state "alert", label:'${new_freeze}°', icon: "st.alarm.temperature.freeze", backgroundColor: "#E60000"
		}
		standardTile("recent_fire", "device.smoke", inactiveLabel: false, width: 2, height: 2) {
			state "clear", label:'Fire', icon: "st.alarm.smoke.clear", backgroundColor: "#79b821"
			state "detected", label:'FIRE', icon: "st.alarm.smoke.smoke", backgroundColor: "#E60000"
		}
		standardTile("recent_co", "device.carbonMonoxide", inactiveLabel: false, width: 2, height: 2) {
			state "clear", label:'CO2', icon: "st.alarm.carbon-monoxide.clear", backgroundColor: "#79b821"
			state "detected", label:'CO2', icon: "st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor: "#E60000"
		}
		standardTile("recent_flood", "device.water", inactiveLabel: false, width: 2, height: 2) {
			state "dry", label:'Flood', icon: "st.alarm.water.dry", backgroundColor: "#79b821"
			state "wet", label:'FLOOD', icon: "st.alarm.water.wet", backgroundColor: "#E60000"
		}
		standardTile("warnings", "device.warnings_status", inactiveLabel: false, width: 2, height: 2) {
			state "no alert", label:'OK', backgroundColor: "#79b821"
			state "alert", label:'ERROR', backgroundColor: "#E60000"
		}
		standardTile("refresh", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}
		
		main "alarm"
		details(["alarm", "recent_alarm", "freeze", "recent_fire", "recent_co", "recent_flood", "warnings", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'alarm' attribute

}

// handle commands
def off() {
	log.debug "Executing 'off'"
	
	setAlarmMode('Off')
}

def strobe() {
	log.debug "Executing 'strobe'"
	// TODO: handle 'strobe' command
}

def siren() {
	log.debug "Executing 'siren'"
	// TODO: handle 'siren' command
}

def both() {
	log.debug "Executing 'both'"
	// TODO: handle 'both' command
}

def poll() {
	log.debug "Executing 'poll'"

	api('status', []) { response ->
		log.debug "Status request $response.status $response.data"
		
		if (response.data.return_code < 1) {
			return
		}
		
		def location = response.data.location
				
		def new_state = location.system.state
		def old_state = device.currentValue("alarm")
		def state_changed = new_state != old_state
		
        def new_recent_alarm = location.monitoring.recent_alarm.text
		def old_recent_alarm = device.currentValue("recent_alarm")
		def recent_alarm_changed = new_recent_alarm != old_recent_alarm
        
        def new_recent_fire = location.monitoring.recent_fire.text
		def old_recent_fire = device.currentValue("recent_fire")
		def recent_fire_changed = new_recent_fire != old_recent_fire
		
        def new_recent_co = location.monitoring.recent_co.text
		def old_recent_co = device.currentValue("recent_co")
		def recent_co_changed = new_recent_co != old_recent_co

		def new_recent_flood = location.monitoring.recent_flood.text
		def old_recent_flood = device.currentValue("recent_flood")
		def recent_flood_changed = new_recent_flood != old_recent_flood
		
        def new_freeze = location.monitoring.freeze.temp
		def old_freeze = device.currentValue("freeze")
		def freeze_changed = new_freeze != old_freeze

        def new_warnings = location.monitoring.warnings
		def old_warnings = device.currentValue("warnings")
		def warnings_changed = new_warnings != old_warnings
		
        
		def alarm_presence = ['off':'present', 'home':'present', 'away':'not present']
		def presence_state_changed = device.currentValue("presence") != alarm_presence.getAt(new_state)

		log.debug "Alarm State: $new_state"
		log.debug "Alarm: $new_recent_alarm"
		log.debug "Fire: $new_recent_fire"
		log.debug "CO2: $new_recent_co"
		log.debug "Flood: $new_recent_flood"
		log.debug "Freeze: $new_freeze"
		log.debug "Warnings: $new_warnings"

		sendEvent(name: 'presence', value: alarm_presence.getAt(new_state), displayed: presence_state_changed, isStateChange: presence_state_changed)
		sendEvent(name: 'alarm', value: new_state, displayed: state_changed, isStateChange: state_changed)
		sendEvent(name: 'recent_alarm', value: new_recent_alarm, displayed: recent_alarm_changed, isStateChange: recent_alarm_changed)
		sendEvent(name: 'recent_fire', value: new_recent_fire, displayed: recent_fire_changed, isStateChange: recent_fire_changed)
		sendEvent(name: 'recent_co', value: new_recent_co, displayed: recent_co_changed, isStateChange: recent_co_changed)
		sendEvent(name: 'recent_flood', value: new_recent_flood, displayed: recent_flood_changed, isStateChange: recent_flood_changed)
		sendEvent(name: 'freeze', value: new_freeze, displayed: freeze_changed, isStateChange: freeze_changed)
		sendEvent(name: 'warnings', value: new_warnings, displayed: warnings_changed, isStateChange: warnings_changed)
		
	if (new_recent_alarm != "No Alert") { 
	sendEvent(name: 'contact', value: "open", displayed: recent_alarm_changed, isStateChange: recent_alarm_changed) }
		else {
	sendEvent(name: 'contact', value: "closed", displayed: recent_alarm_changed, isStateChange: recent_alarm_changed)
		}
		
	if (new_recent_fire != "No Alert") { 
	sendEvent(name: 'smoke', value: "detected", displayed: recent_fire_changed, isStateChange: recent_fire_changed) }
		else {
	sendEvent(name: 'smoke', value: "clear", displayed: recent_fire_changed, isStateChange: recent_fire_changed)
		}
		
	if (new_recent_co != "No Alert") { 
	sendEvent(name: 'carbonMonoxide', value: "detected", displayed: recent_co_changed, isStateChange: recent_co_changed) }
		else {
	sendEvent(name: 'carbonMonoxide', value: "clear", displayed: recent_co_changed, isStateChange: recent_co_changed)
		}
		
	if (new_recent_flood != "No Alert") { 
	sendEvent(name: 'water', value: "wet", displayed: recent_flood_changed, isStateChange: recent_flood_changed) }
		else {
	sendEvent(name: 'water', value: "dry", displayed: recent_flood_changed, isStateChange: recent_flood_changed)
		}

	if (new_freeze <= "42" || new_freeze == "?") { 
	sendEvent(name: 'freeze_status', value: "alert", displayed: freeze_changed, isStateChange: freeze_changed) 
	sendEvent(name: 'temperature', value: new_freeze, displayed: freeze_changed, isStateChange: freeze_changed) }
		else {
	sendEvent(name: 'freeze_status', value: "no alert", displayed: freeze_changed, isStateChange: freeze_changed)
	sendEvent(name: 'temperature', value: new_freeze, displayed: freeze_changed, isStateChange: freeze_changed)	
		}
		
	if (new_warnings != null) { 
	sendEvent(name: 'warnings_status', value: "alert", displayed: warnings_changed, isStateChange: warnings_changed) }
		else {
	sendEvent(name: 'warnings_status', value: "no alert", displayed: warnings_changed, isStateChange: warnings_changed)
		}
		
	}
}

def home() {
	log.debug "Executing 'home'"
	// TODO: handle 'home' command
	
	setAlarmMode('Home')
}

def away() {
	log.debug "Executing 'away'"
	// TODO: handle 'away' command
	
	setAlarmMode('Away')
}

def setAlarmMode(mode) {
	// TODO
}

def api(method, args = [], success = {}) {
	log.debug "Executing 'api'"
	
	if(!isLoggedIn()) {
		log.debug "Need to login"
		login(method, args, success)
		return
	}
	
	// SimpliSafe requires this funkiness
	def required_payload = [
		no_persist: 1,
		XDEBUG_SESSION_START: 'session_name'
	]
	
	// append it to the args
	args = required_payload
	
	def methods = [
		'status': [uri: "https://simplisafe.com/mobile/$state.auth.uid/sid/$locationID/dashboard", type: 'post']
	]
	
	def request = methods.getAt(method)
	
	log.debug "Starting $method : $args"
	doRequest(request.uri, args, request.type, success)
}
 
// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
	log.debug "Calling $type : $uri : $args"
	
	def params = [
		uri: uri,
		headers: [
			'Cookie': state.cookiess
		],
		body: args
	]
	
	log.debug params
	
	try {
		if (type == 'post') {
			httpPostJson(params, success)
		} else if (type == 'get') {
			httpGet(params, success)
		}
		
	} catch (e) {
		log.debug "something went wrong: $e"
	}
}

def login(method = null, args = [], success = {}) { 
	log.debug "Executing 'login'"
	def params = [
		uri: 'https://simplisafe.com/mobile/login/',
		body: [
			name: settings.username, 
			pass: settings.password, 
			device_name: settings.devicename, 
			device_uuid: settings.deviceuuid,
			version: 1200,
			no_persist: 1,
			XDEBUG_SESSION_START: 'session_name'
		]
	]
	
	state.cookiess = ''
	
	httpPost(params) {response ->
		log.debug "Request was successful, $response.status"
		log.debug response.headers
		
		state.auth = response.data
		
		// set the expiration to 10 minutes
		state.auth.expires_at = new Date().getTime() + 600000;
		
		response.getHeaders('Set-Cookie').each {
			String cookie = it.value.split(';|,')[0]
			log.debug "Adding cookie to collection: $cookie"
			state.cookiess = state.cookiess+cookie+';'
		}
		log.debug "cookies: $state.cookiess"
		
		api(method, args, success)

	}
}

def isLoggedIn() {
	if(!state.auth) {
		log.debug "No state.auth"
		return false
	}

	log.debug state.auth.uid

	def now = new Date().getTime();
	log.debug now
	log.debug state.auth.expires_at
	return state.auth.expires_at > now
}
