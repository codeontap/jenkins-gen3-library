#!/usr/bin/env groovy

def call( Map params = [:] ) {

    skipDefaultCheckout(true)

    levelsList          = (params.get('levelsList', [])).join(',')
    segmentUnits        = (params.get('segmentUnits', [])).join(',')
    solutionUnits       = (params.get('solutionUnits', [])).join(',')
    applicationUnits    = (params.get('applicationUnits', [])).join(',')

    def environmentVariables = []

    environmentVariables += [ "ENVIRONMENT=${params.get('environment', '')}" ]
    environmentVariables += [ "SEGMENT=${params.get('segment', '')}" ]
    environmentVariables += [ "DEPLOYMENT_MODE=${params.get('deploymentMode', '')}" ]
    environmentVariables += [ "COMMENT=${params.get('comment', '')}" ]
    environmentVariables += [ "TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT=${params.get('runIdSignificant', '')}" ]

    environmentVariables += [ "LEVELS_LIST=${levelsList}" ]
    environmentVariables += [ "LEVELS_LIST=${segmentUnits}" ]
    environmentVariables += [ "LEVELS_LIST=${solutionUnits}" ]
    environmentVariables += [ "LEVELS_LIST=${applicationUnits}" ]

    echo "${environmentVariables}"

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
        trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
        ${AUTOMATION_DIR}/constructTree.sh
        RESULT=$?
        ''' 
    }

    contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {
        sh '''#!/bin/bash
        trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
        ${AUTOMATION_DIR}/manageEnvironment.sh
        RESULT=$?
        '''
    }
}