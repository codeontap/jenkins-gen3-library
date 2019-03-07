#!/usr/bin/env groovy

def call( String propertiesFile ) {

    skipDefaultCheckout(true)
    
    def environmentVariables = []
    def siteProperties = readProperties interpolate: true, file: cot.siteProperties();
    environmentVariables += siteProperties.collect {/$it.key=$it.value/ }

    withEnv ( environmentVariables ) {
        sh '''#!/bin/bash
            trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
            ${AUTOMATION_BASE_DIR}/setContext.sh
            RESULT=$?
        '''
    }

    def contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {
        sh '''#!/bin/bash
            trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM
            ${AUTOMATION_DIR}/validateAcceptReleaseParameters.sh
            RESULT=$?
        ''' 
    }

    contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {
        sh '''#!/bin/bash
            trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM
            ${AUTOMATION_DIR}/constructTree.sh -c ${RELEASE_TAG} -i ${RELEASE_TAG}
            RESULT=$?
        '''
    }

    contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {
        sh '''#!/bin/bash
            trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM
            ${AUTOMATION_DIR}/acceptReleaseSetup.sh
            RESULT=$?
        '''
    }

    contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {
        sh '''#!/bin/bash
            trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM
            ${AUTOMATION_DIR}/acceptRelease.sh
            RESULT=$?
        '''
    }
}