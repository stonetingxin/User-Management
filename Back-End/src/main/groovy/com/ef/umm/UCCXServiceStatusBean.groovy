package com.ef.umm

/**
 * Created by saqib ahmad on 10/19/2017.
 */
class UCCXServiceStatusBean {
    boolean primaryServerStatus
    boolean secondaryServerStatus

    private  AuthenticationBean authenticationBeanPrimary = new AuthenticationBean();
    private  AuthenticationBean authenticationBeanSecondary = new AuthenticationBean();


    boolean getPrimaryServerStatus() {
        return primaryServerStatus
    }

    void setPrimaryServerStatus(boolean primaryServerStatus) {
        this.primaryServerStatus = primaryServerStatus
    }

    boolean getSecondaryServerStatus() {
        return secondaryServerStatus
    }

    void setSecondaryServerStatus(boolean secondaryServerStatus) {
        this.secondaryServerStatus = secondaryServerStatus
    }
    AuthenticationBean getAuthenticationBeanPrimary() {
        return authenticationBeanPrimary
    }

    void setAuthenticationBeanPrimary(AuthenticationBean authenticationBeanPrimary) {
        this.authenticationBeanPrimary = authenticationBeanPrimary
    }

    AuthenticationBean getAuthenticationBeanSecondary() {
        return authenticationBeanSecondary
    }

    void setAuthenticationBeanSecondary(AuthenticationBean authenticationBeanSecondary) {
        this.authenticationBeanSecondary = authenticationBeanSecondary
    }

    @Override
    public String toString() {
        return "UCCXServiceStatusBean{" +
                "primaryServerStatus=" + primaryServerStatus +
                ", secondaryServerStatus=" + secondaryServerStatus +
                ", authenticationBeanPrimary=" + authenticationBeanPrimary +
                ", authenticationBeanSecondary=" + authenticationBeanSecondary +
                '}';
    }
}
