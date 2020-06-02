// Set the codeontap context
def call(
    String environment = '',
    String segment = 'default',
    String deploymentMode = '',
    String releaseMode = '',
    String releaseIdentifier = ''
) {
    script {
        env['ENVIRONMENT'] = environment
        env['SEGMENT'] = segment
        env['GENERATION_CONTEXT_DEFINED'] = ''
        env['ACCOUNT'] = ''
        env['DEPLOYMET_MODE'] = deploymentMode
        env['RELEASE_MODE'] = releaseMode
        env['RELEASE_IDENTIFIER'] = releaseIdentifier
    }

    sh '''#!/usr/bin/env bash

    EXTRA_ARGS=""

    if [[ -n "${RELEASE_MODE}" ]]; then
        EXTRA_ARGS="${EXTRA_ARGS} -r ${RELEASE_MODE}"
    fi

    if [[ -n "${DEPLOYMENT_MODE}" ]]; then
        EXTRA_ARGS="${EXTRA_ARGS} -d ${DEPLOYMENT_MODE}"
    fi

    ${AUTOMATION_BASE_DIR}/setContext.sh ${EXTRA_ARGS} || exit $?
    '''

    script {
        def contextProperties = readProperties interpolate: true, file: "${WORKSPACE}/context.properties";
        contextProperties.each{ k, v -> env["${k}"] ="${v}" }
    }
}
