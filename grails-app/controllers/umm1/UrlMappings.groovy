package umm1

import com.ef.umm.*

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
        "/$micro/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
        "/"(redirect:"/base/index.html")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
