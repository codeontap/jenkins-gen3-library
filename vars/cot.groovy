#!/usr/bin/env groovy

def siteProperties() {

    propertiesFile 
    return env.SITE_PROPERITES ?: '/var/opt/codeontap/site.properties'
}