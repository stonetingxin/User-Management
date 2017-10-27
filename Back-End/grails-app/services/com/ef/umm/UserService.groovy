package com.ef.umm

import grails.transaction.Transactional
import grails.web.context.ServletContextHolder
import org.apache.commons.io.FilenameUtils

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE

@Transactional
class UserService {

    def CCSettingsService
    def updateProfilePic(agentId, file) {
        def webDirectory = ServletContextHolder?.servletContext?.getRealPath("/")
        File destinationFile = new File(webDirectory + "/base/assets1/images/agents/", agentId + "." + FilenameUtils.getExtension(file?.getOriginalFilename()?.toLowerCase()))
        file.transferTo(destinationFile)
        return [status: 200]
    }

    def getProfilePic(agentId) {
        def directory = ServletContextHolder?.servletContext?.getRealPath("/")
        File profilePic = new File(directory + '/base/assets1/images/agents/' + agentId + '.jpg')
        return profilePic.exists()

    }

    def deleteProfilePic(agentId) {
        def directory = ServletContextHolder?.servletContext?.getRealPath("/")
        File profilePic = new File(directory + '/base/assets1/images/agents/' + agentId + '.jpg')
        if (profilePic?.exists()) {
            if (profilePic?.delete())
                return [status: 200]
            else
                return [status: 500]
        } else
            return [status: 204]

    }

    def getAgentTeams(agentId){
        def url, id
        def teamsMap = new ArrayList();
        def agent = getAgent(agentId)
        try {

            if (agent?.get("type") == 2) {
                def primary = agent?.get("primarySupervisorOf")?.get("supervisorOfTeamName")
                primary?.each {
                    url = it?.get("refURL")
                    id = url?.split('/')?.last()
                    teamsMap.add([name: it?.get("@name"), id: id, type: "p"])
                }

            }

        } catch (Exception e) {
            log.error("Error occurred while fetching team(Primary) for agent ${agentId}")
        }
        try {
            def secondary = agent?.get("secondarySupervisorOf")?.get("supervisorOfTeamName")
            secondary?.each {
                url = it?.get("refURL")
                id = url?.split('/')?.last()
                teamsMap.add([name: it?.get("@name"), id: id, type: "s"])
            }
        } catch (Exception ex) {
            log.error("Error occurred while fetching team(Secondary) for agent ${agentId}")
        }

        return teamsMap
    }

    def getAgent(id) {
        AuthenticationBean authenticationBean = new AuthenticationBean()
        authenticationBean?.setUsername(CCSettingsService?.primaryUsername)
        authenticationBean?.setPassword(CCSettingsService?.primaryPassword)
        authenticationBean?.setBaseUrl(CCSettingsService?.webRequest+"://"+CCSettingsService?.primaryIp+"/adminapi")

        def result = CCSettingsService.callAPI(authenticationBean, "GET", "/resource/"+id, null, null, null)
        if(result.available){
            return result.data
        }else{
            authenticationBean?.setUsername(CCSettingsService?.secondaryUsername)
            authenticationBean?.setPassword(CCSettingsService?.secondaryPassword)
            authenticationBean?.setBaseUrl(CCSettingsService?.webRequest+"://"+CCSettingsService?.secondaryIp+"/adminapi")
            if (result.available) {
                return result.data
            } else {
                return [status: SERVICE_UNAVAILABLE]
            }
        }
    }
}
