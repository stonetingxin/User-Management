package com.ef.umm

import grails.transaction.Transactional
import grails.web.context.ServletContextHolder
import org.apache.commons.io.FilenameUtils

@Transactional
class UserService {

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
}
