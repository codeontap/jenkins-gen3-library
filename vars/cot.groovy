#!/usr/bin/env groovy

def siteProperties() {
    return env.SITE_PROPERITES ?: '/var/opt/codeontap/site.properties'
}