import com.ef.umm.MyAccessTokenJsonRenderer
/// / Place your Spring DSL code here
beans = {
    accessTokenJsonRenderer(com.ef.umm.MyAccessTokenJsonRenderer){bean ->
        bean.autowire = true
    }

}
