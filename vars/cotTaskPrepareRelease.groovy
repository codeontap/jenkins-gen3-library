#!/usr/bin/env groovy

def call( String propertiesFile ) {

    def environmentVariables = []
    def productProperties = readProperties interpolate: true, file: propertiesFile;
    environmentVariables += productProperties.collect {/$it.key=$it.value/ }

    sh 'env'
    sh 'unset GIT_COMMIT'
    sh 'env'

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
        sh 'env'
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