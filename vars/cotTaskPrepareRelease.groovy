#!/usr/bin/env groovy

def call( ) {

    def environmentVariables = []
    def siteProperties = readProperties interpolate: true, file: cot.siteProperties();
    environmentVariables += siteProperties.collect {/$it.key=$it.value/ }

    // Override builtin variables
    environmentVariables += [ 'GIT_COMMIT=' ]

    withEnv ( environmentVariables ) {
        sh '''#!/bin/bash
            trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM
            ${AUTOMATION_BASE_DIR}/setContext.sh -r selective
            RESULT=$?
        '''
    }

    def contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {
        sh '''#!/bin/bash
            trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
            ${AUTOMATION_DIR}/constructTree.sh
            RESULT=$?
        ''' 
    }

    contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {
        sh '''#!/bin/bash
            trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM
            ${AUTOMATION_DIR}/confirmBuilds.sh
            RESULT=$?
        '''
    }

    contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {
        sh '''#!/bin/bash
            trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM
            ${AUTOMATION_DIR}/prepareReleaseSetup.sh
            RESULT=$?
        '''
    }

    contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {
        sh '''#!/bin/bash
            trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM
            ${AUTOMATION_DIR}/prepareRelease.sh
            RESULT=$?
        '''
    }
}