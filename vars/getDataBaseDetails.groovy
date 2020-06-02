// Using the dataset switch DynamoDb table get the inactive/active database and the connection details for the Db
def call( String dataset, String datasetInstance ) {


    script {
        env['DATASET'] = dataset
        env['DATASET_INSTANCE'] = datasetInstance
    }

    getDataSetState( "${env["DATASET"]}", "${env["DATASET_INSTANCE"]}" )

    sh '''#!/usr/bin/env bash

        . "${GENERATION_BASE_DIR}/execution/common.sh"
        . "${AUTOMATION_BASE_DIR}/common.sh"

        # Get the generation context so we can run template generation
        . "${GENERATION_BASE_DIR}/execution/setContext.sh"

        RDSID="$( echo "${DATASET_ITEM}" | jq -r '.DB_INSTANCEID.S' )"

        PGHOST="$( echo "${DATASET_ITEM}" | jq -r '.DB_FQDN.S' )"
        PGPORT="$( echo "${DATASET_ITEM}" | jq -r '.DB_PORT.S' )"
        PGUSER="$( echo "${DATASET_ITEM}" | jq -r '.DB_USERNAME.S' )"
        PGDATABASE="$( echo "${DATASET_ITEM}" | jq -r '.DB_NAME.S' )"

        POSTGRES_ENCRYPTED_PASSWORD="$( echo "${DATASET_ITEM}" | jq -r '.DB_PASSWORD.S' )"
        ENCRYPTED_PASSWORD_PREFIX="base64:"
        PGPASSWORD="$( ${GENERATION_DIR}/manageCrypto.sh -b -v -d -t "${POSTGRES_ENCRYPTED_PASSWORD#$ENCRYPTED_PASSWORD_PREFIX}" || exit $? )"

        info "RDS Instance Id: ${RDSID}"
        info "PGHOST: ${PGHOST} - PGPORT: ${PGPORT}"

        if [[ -n "${PGPASSWORD}" ]]; then
            info "Password Decrypted"
        else
            info "Password Empty"
            exit 255
        fi

        save_context_property RDSID "${RDSID}"
        save_context_property PGHOST "${PGHOST}"
        save_context_property PGPORT "${PGPORT}"
        save_context_property PGUSER "${PGUSER}"
        save_context_property PGPASSWORD "${PGPASSWORD}"
        save_context_property PGDATABASE "${PGDATABASE}"

    '''

    script {
        def contextProperties = readProperties interpolate: true, file: "${WORKSPACE}/context.properties";
        contextProperties.each{ k, v -> env["${k}"] ="${v}" }
    }
}
