#!/usr/bin/env groovy

def call( 
        String      environment, 
        String      segment, 
        String      deploymentMode,
        String      comment ,  
        String[]    levelsList,
        String[]    segmentUnits,
        String[]    solutionUnits,
        String[]    applicationUnits,
        Boolean     runIdSignificant = false ) {

    skipDefaultCheckout(true)

    def environmentVariables = []

    environmentVariables['ENVIRONMENT'] = environment ?: env.ENVIRONMENT
    environmentVariables['SEGMENT'] = segment ?: env.SEGMENT
    environmentVariables['deploymentMode'] = deploymentMode ?: env.DEPLOYMENT_MODE
    environmentVariables['comment'] = deploymentMode ?: env.comment
    environmentVariables['levelsList'] = levelsList ?: env.LEVELS_LIST
    environmentVariables['segmentUnits'] = segmentUnits?join(',') ?: env.SEGMENT_UNITS
    environmentVariables['solutionUnits'] = solutionUnits?join(',') ?: env.SOLUTION_UNITS
    environmentVariables['applicationUnits'] = applicationUnits?join(',') ?: env.APPLICATION_UNITS

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