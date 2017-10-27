import com.ef.umm.MyAccessTokenJsonRenderer
/// / Place your Spring DSL code here
beans = {
    accessTokenJsonRenderer(com.ef.umm.MyAccessTokenJsonRenderer){bean ->
        userService = ref('userService')
        bean.autowire = true
    }

}
