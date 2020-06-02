def call(
    ArrayList levels,
    ArrayList segmentUnits=[],
    ArrayList solutionUnits=[],
    ArrayList applicationUnits=[],
    String deploymentMode = 'update',
    boolean treatRunIdAsSignificant = true
)   {

    script {
        env['DEPLOYMENT_MODE'] = "${deploymentMode}"

        env['LEVELS_LIST'] = "${levels.join(',')}"
        env['SEGMENT_UNITS_LIST'] = "${segmentUnits.join(',') }"
        env['SOLUTION_UNITS_LIST'] = "${solutionUnits.join(',') }"
        env['APPLICATION_UNITS_LIST'] = "${applicationUnits.join(',')}"
        env['TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT'] ='true'

        env['AUTOMATION_JOB_IDENTIFIER'] = env['BUILD_TAG']
    }

    // Update the database to use the new build reference
    echo "Refreshing product config repo ${env["SEGMENT_BUILDS_DIR"]}"
    dir("${env["SEGMENT_BUILDS_DIR"]}" ) {
        sh 'git pull --rebase'
    }

    sh'''#!/bin/bash
        ${AUTOMATION_DIR}/manageEnvironment.sh || exit $?
    '''

    script {
        def contextProperties = readProperties interpolate: true, file: "${WORKSPACE}/context.properties";
        contextProperties.each{ k, v -> env["${k}"] ="${v}" }
    }
}
