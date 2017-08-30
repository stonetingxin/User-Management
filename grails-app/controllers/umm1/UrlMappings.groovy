package umm1

import com.ef.umm.*

class UrlMappings {

    static mappings = {

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
