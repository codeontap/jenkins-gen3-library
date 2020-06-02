// Sets the desired count of an Ec2 based ECS cluster to 1 if its current state is set to 0
// This is used to get around issues with ECS autoscaling and the current jenkins ECS agent plugin
// It doesn't support capacity providers which allow for ondeamnd resource provisionig for tasks
def call( String ecsDeploymentUnit ) {

    script {
        env['DEPLOYMENT_UNIT'] = ecsDeploymentUnit
    }

    getDeploymentUnitBuildBlueprint( "${env['DEPLOYMENT_UNIT']}")

    sh '''#!/usr/bin/env bash
        . "${GENERATION_BASE_DIR}/execution/common.sh"
        . "${AUTOMATION_BASE_DIR}/common.sh"

        # Get the generation context so we can run template generation
        . "${GENERATION_BASE_DIR}/execution/setContext.sh"

        BUILD_BLUEPRINT="${AUTOMATION_DATA_DIR}/build_blueprint-${DEPLOYMENT_UNIT}-config.json"

        ECS_CLUSTER_ARN="$( jq -r '.Occurrence.State.Attributes.ARN | select (.!=null)' < "${BUILD_BLUEPRINT}" || exit $?)"
        ECS_CAPACITY_PROVIDER="$(aws ecs describe-clusters --clusters "${ECS_CLUSTER_ARN}" --query 'clusters[0].defaultCapacityProviderStrategy[0].capacityProvider' --output text)"
        ECS_ASG_ARN="$(aws ecs describe-capacity-providers --capacity-providers "${ECS_CAPACITY_PROVIDER}" --query 'capacityProviders[0].autoScalingGroupProvider.autoScalingGroupArn' --output text)"

        ECS_ASG_NAME="$( aws autoscaling describe-auto-scaling-groups | jq -r --arg asgArn "${ECS_ASG_ARN}" '.AutoScalingGroups[] | select(.AutoScalingGroupARN==$asgArn) | .AutoScalingGroupName')"
        ECS_ASG_DESIRED_CAPACITY="$( aws autoscaling describe-auto-scaling-groups | jq -r --arg asgArn "${ECS_ASG_ARN}" '.AutoScalingGroups[] | select(.AutoScalingGroupARN==$asgArn ) | .DesiredCapacity')"

        echo "ClusterArn: ${ECS_CLUSTER_ARN} - CpacityProvider: ${ECS_CAPACITY_PROVIDER} - ASGArn: ${ECS_ASG_ARN} - ASGName: ${ECS_ASG_NAME} - Desired ${ECS_ASG_DESIRED_CAPACITY}"

        # If the ECS cluster has no instances than Jenkins can't start a task
        # This will add one instance and allow Jenkins to start
        # ECS autoscaling will manage the scale down activity to ensure that there are no running tasks
        if [[ "${ECS_ASG_DESIRED_CAPACITY}" == 0 ]]; then
            info "Waking up ECS Cluster"
            aws autoscaling set-desired-capacity --auto-scaling-group-name "${ECS_ASG_NAME}" --desired-capacity 1
        else
            info "ECS Cluster ready to go"
        fi

    '''
}
