/**
 *  wayne5w-room_me.groovy
 *
 *  https://raw.githubusercontent.com/wayne5w/Hubitat/master/drivers/wayne5w-room_me.groovy
 *
 *  Copyright 2020 Wayne Williams 
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2020-01-30  Wayne Williams Original Creation
 *	
 */
 
metadata {
	definition (name: "RoomMe", namespace: "wayne5w", author: "Wayne Williams", importUrl: "raw.githubusercontent.com/wayne5w/Hubitat/master/drivers/wayne5w-room_me.groovy") {
        capability "Refresh"
        capability "Pushable Button"
        capability "Holdable Button"
        capability "Signal Strength"
        capability "Presence Sensor"  
        
        command "sendData", ["string"]
        //command "deleteAllChildDevices"
	}

	preferences {
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

// parse events into attributes
def parse(String data) {
	if (logEnable) log.debug "description= '${data}'"
    def msg = parseLanMessage(data)
  	def headerString = msg.header
    def mac = msg.mac  //needed for backwards compatability
    
    if (!headerString) {
        log.debug "headerstring was null for some reason :("
    }
    def JSON = msg.body

    if (JSON) {
        if (logEnable) log.debug "msg= $JSON"
    }
 
      catch (e) {
        log.error "Error in parse() routine, error = ${e}"
      }

}

private getHostAddress() {
    def ip = settings.ip
    def port = settings.port

	if (logEnable) log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}

def sendData(message) {
    sendEthernet(message) 
}

def sendEthernet(message) {
    if (message.contains(" ")) {
        def parts = message.split(" ")
        def name  = parts.length>0?parts[0].trim():null
        def value = parts.length>0?parts[1].trim():null
        message = name + "%20" + value
    }
	if (logEnable) log.debug "Executing 'sendEthernet' ${message}"
	if (settings.ip != null && settings.port != null) {
    	new hubitat.device.HubAction(
    		method: "POST",
    		path: "/${message}?",
    		headers: [ HOST: "${getHostAddress()}" ]
		)
    }
    else {
    	log.warn "Parent HubDuino Ethernet Device: Please verify IP address and Port are configured."    
    }
}

def refresh() {
	if (logEnable) log.debug "Executing 'refresh()'"
	sendEthernet("refresh")
}

def installed() {
	log.info "Executing 'installed()'"
    state.numButtons = 0
    sendEvent(name: "numberOfButtons", value: state.numButtons)
}

def uninstalled() {
    log.info "Executing 'uninstalled()'"
}

def initialize() {
	log.info "Executing 'initialize()'"
}


private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex.toUpperCase()

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport.toUpperCase()
}
