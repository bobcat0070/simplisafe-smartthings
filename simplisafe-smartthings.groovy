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
	input(name: "username", type: "text", title: "Username", description: "Your SimpliSafe username (usually an email address)")
	input(name: "password", type: "password", title: "Password", description: "Your SimpliSafe password")
}
 
metadata {
	// Automatically generated. Make future change here.
	definition (name: "SimpliSafe", namespace: "bobcat0070", author: "Felix Gorodishter") {
		capability "Alarm"
		capability "Polling"

		command "home"
		command "away"
		command "off"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("alarmMode", "device.alarmMode", width: 2, height: 2,
					 canChangeIcon: true) {
			state "Off", label: '${name}',
				  icon: "st.security.alarm.off", backgroundColor: "#ffffff"
			state "Home", label: '${name}',
				  icon: "st.security.alarm.on", backgroundColor: "#00E6E6"
			state "Away", label: '${name}',
				  icon: "st.security.alarm.on", backgroundColor: "#E60000"
				  
		}
		standardTile("refresh", "device.alarmMode", inactiveLabel: false, decoration: "flat") {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}

		main "alarmMode"
		details(["alarmMode", "refresh"])
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
	// TODO: handle 'poll' command
	
	api('status', []) { response ->
		log.debug "Status request $response.status $response.data"
		
		if (response.data.num_locations < 1) {
			return
		}
		
		
		def locations = response.data.locations
		def location = locations.keySet()[0]
		log.debug location

		def new_state = locations."$location".system_state
		def old_state = device.currentValue("alarmMode")
		def state_changed = new_state != old_state
		
		log.debug new_state
		log.debug old_state
		log.debug state_changed
				
		sendEvent(name: 'alarmMode', value: new_state, displayed: state_changed, isStateChange: state_changed)
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
		'status': [uri: "https://simplisafe.com/mobile/$state.auth.uid/locations", type: 'post']
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
			device_name: 'Android', 
			device_uuid: '21611e80-0000-0000-0000-0800200c9a66',
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
