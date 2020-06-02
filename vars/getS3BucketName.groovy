def call(String deploymentUnit ) {

    script {
        env['DEPLOYMENT_UNIT'] = deploymentUnit
    }

    getDeploymentUnitBuildBlueprint( "${env['DEPLOYMENT_UNIT']}")

    sh '''#!/usr/bin/env bash
    . "${GENERATION_BASE_DIR}/execution/common.sh"
    . "${AUTOMATION_BASE_DIR}/common.sh"

    BUILD_BLUEPRINT="${AUTOMATION_DATA_DIR}/build_blueprint-${DEPLOYMENT_UNIT}-config.json"
    if [[ -f "${BUILD_BLUEPRINT}" ]]; then
        BUCKET_NAME="$( jq -r '.Occurrence.State.Attributes.NAME | select (.!=null)' < "${BUILD_BLUEPRINT}" )"

        info "Bucket: ${BUCKET_NAME}"
        save_context_property BUCKET_NAME "${BUCKET_NAME}"
    else
        fatal "Could not find staging bucket details" && exit 255
    fi
    '''

    script {
        def contextProperties = readProperties interpolate: true, file: "${WORKSPACE}/context.properties";
        contextProperties.each{ k, v -> env["${k}"] ="${v}" }
    }
}
