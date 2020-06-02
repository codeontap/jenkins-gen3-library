// Builds an rdssnapshot from based on the provided deploymentUnit and deploymentMode
// Deployment Unit is the deployment unit of the dataset component which holds the build details of the dataet
// Deploymen Mode specifies which database to use as the build sourc
def call( String deploymentUnit, String deploymentMode ) {
    script {
        env['IMAGE_FORMAT'] = 'rdssnapshot'
        env['DEPLOYMENT_UNIT'] = "${deploymentUnit}"
        env['DEPLOYMENT_MODE'] = "${deploymentMode}"
        env['GENERATION_CONTEXT_DEFINED'] = ''
        env['ACCOUNT'] = ''
    }

    sh '''#!/usr/bin/env bash
    trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM

    ${AUTOMATION_BASE_DIR}/setContext.sh
    RESULT=$?
    '''

    script {
        def contextProperties = readProperties interpolate: true, file: "${WORKSPACE}/context.properties";
        contextProperties.each{ k, v -> env["${k}"] ="${v}" }
    }

    sh '''#!/usr/bin/env bash
        . "${GENERATION_BASE_DIR}/execution/common.sh"
        . "${AUTOMATION_BASE_DIR}/common.sh"

        # Get the generation context so we can run template generation
        . "${GENERATION_BASE_DIR}/execution/setContext.sh"

        echo "Segment dir ${SEGMENT_SOLUTIONS_DIR}"
        cd "${SEGMENT_SOLUTIONS_DIR}"

        echo "Details for environment - ${ENVIRONMENT} ${SEGMENT} ${ACCOUNT}"
    '''

    script {
        def contextProperties = readProperties interpolate: true, file: "${WORKSPACE}/context.properties";
        contextProperties.each{ k, v -> env["${k}"] ="${v}" }
    }

    sh '''#!/usr/bin/env bash
        trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM

        ${AUTOMATION_DIR}/buildSetup.sh
        RESULT=$?
    '''

    script {
        def contextProperties = readProperties interpolate: true, file: "${WORKSPACE}/context.properties";
        contextProperties.each{ k, v -> env["${k}"] ="${v}" }
    }

    sh '''#!/usr/bin/env bash
        trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM

        ${AUTOMATION_DIR}/buildRDSSnapshot.sh
        RESULT=$?
    '''

    script {
        def contextProperties = readProperties interpolate: true, file: "${WORKSPACE}/context.properties";
        contextProperties.each{ k, v -> env["${k}"] ="${v}" }
    }

    sh '''#!/usr/bin/env bash
        trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM

        ${AUTOMATION_DIR}/manageImages.sh
        RESULT=$?
    '''

    script {
        def contextProperties = readProperties interpolate: true, file: "${WORKSPACE}/context.properties";
        contextProperties.each{ k, v -> env["${k}"] ="${v}" }

        def chainProperties = readProperties interpolate: true, file: "${WORKSPACE}/chain.properties";
        chainProperties.each{ k, v -> env["${k}"] ="${v}" }
    }
}
