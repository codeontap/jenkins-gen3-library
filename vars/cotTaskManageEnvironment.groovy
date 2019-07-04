#!/usr/bin/env groovy

def call( Map params = [:] ) {

    skipDefaultCheckout(true)

    levelsList          = params.get('levelsList', []).join(',')
    segmentUnits        = params.get('segmentUnits', []).join(',')
    solutionUnits       = params.get('solutionUnits', []).join(',')
    applicationUnits    = params.get('applicationUnits', []).join(',')

    def environmentVariables = []

    environmentVariables['ENVIRONMENT']                             = params.get('environment', '')
    environmentVariables['SEGMENT']                                 = params.get('segment', '')
    environmentVariables['deploymentMode']                          = params.get('deploymentMode', '') 
    environmentVariables['comment']                                 = params.get('comment', '')
    environmentVariables['LEVELS_LIST']                             = params.get('levelsList', '')
    environmentVariables['SEGMENT_UNITS_LIST']                      = params.get('segmentUnits', '')
    environmentVariables['SOLUTION_UNITS_LIST']                     = params.get('solutionUnits', '')
    environmentVariables['APPLICATION_UNITS_LIST']                  = params.get('applicationUnits', '') 
    environmentVariables['TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT'] = params.get('runIdSignificant', '')

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