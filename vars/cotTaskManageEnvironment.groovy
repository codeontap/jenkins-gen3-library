#!/usr/bin/env groovy

def call( Map params = [:] ) {

    skipDefaultCheckout(true)

    levelsList          = params.get(levelsList, []).join(',')
    segmentUnits        = params.get(segmentUnits, []).join(',')
    solutionUnits       = params.get(solutionUnits, []).join(',')
    applicationUnits    = params.get(applicationUnits, []).join(',')

    def environmentVariables = []

    environmentVariables['ENVIRONMENT']                             = params.get(environment, env.ENVIRONMENT) 
    environmentVariables['SEGMENT']                                 = params.get(segment, env.SEGMENT)
    environmentVariables['deploymentMode']                          = params.get(deploymentMode,env.DEPLOYMENT_MODE)
    environmentVariables['comment']                                 = params.get(comment, env.COMMENT)
    environmentVariables['LEVELS_LIST']                             = params.get(levelsList, env.LEVELS_LIST)
    environmentVariables['SEGMENT_UNITS_LIST']                      = params.get(segmentUnits, env.SEGMENT_UNITS)
    environmentVariables['SOLUTION_UNITS_LIST']                     = params.get(solutionUnits, env.SOLUTION_UNITS)
    environmentVariables['APPLICATION_UNITS_LIST']                  = params.get(applicationUnits, env.APPLICATION_UNITS)
    environmentVariables['TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT'] = params.get(runIdSignificant, env.TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT)

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