// Gets the CodeOnTap build blueprint for a deploymentUnit
// This file can be used to get deployment specifc attributes of the components deployed for an environment
def call( String deploymentUnit ) {
    script {
        env['DEPLOYMENT_UNIT'] = deploymentUnit
    }

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

        info "Details for environment - ${ENVIRONMENT} ${SEGMENT} ${ACCOUNT} ${DEPLOYMENT_UNIT}"

        # Create build blueprint
        ${GENERATION_DIR}/createBuildblueprint.sh -u "${DEPLOYMENT_UNIT}" -o "${AUTOMATION_DATA_DIR}" >/dev/null || exit $?

        BUILD_BLUEPRINT="${AUTOMATION_DATA_DIR}/build_blueprint-${DEPLOYMENT_UNIT}-config.json"

        info "Blueprint saved to ${BUILD_BLUEPRINT}"

        save_context_property BUILD_BLUEPRINT "${BUILD_BLUEPRINT}"
    '''

    script {
        def contextProperties = readProperties interpolate: true, file: "${WORKSPACE}/context.properties";
        contextProperties.each{ k, v -> env["${k}"] ="${v}" }
    }
}
