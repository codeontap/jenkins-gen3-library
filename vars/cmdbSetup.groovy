// Clone the CodeOnTap CMDB and collect information about the environment context the script is being run in
def call(
    String cmdbTag = ''
) {

    script {
        env['CMDB_TAG'] = cmdbTag
    }

    setContext()

    sh '''#!/usr/bin/env bash
    trap 'exit ${RESULT:-1}' EXIT SIGHUP SIGINT SIGTERM

    EXTRA_ARGS=""
    if [[ -n "${CMDB_TAG}" ]]; then
        EXTRA_ARGS="${EXTRA_ARGS} -c ${CMDB_TAG} -i ${CMDB_TAG}"
    fi

    ${AUTOMATION_DIR}/constructTree.sh ${EXTRA_ARGS}
    RESULT=$?
    '''

    script {
        def contextProperties = readProperties interpolate: true, file: "${WORKSPACE}/context.properties";
        contextProperties.each{ k, v -> env["${k}"] ="${v}" }
    }
}
