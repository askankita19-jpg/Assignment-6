def call(Map config) {

    pipeline {
        agent any

        stages {

            stage('Clone') {
                steps {
                    echo "Cloning repository..."
                    git url: config.GIT_REPO
                }
            }

            stage('User Approval') {
                when {
                    expression { config.KEEP_APPROVAL_STAGE == true }
                }
                steps {
                    input message: "Approve deployment to ${config.ENVIRONMENT}?",
                          ok: "Deploy"
                }
            }

            stage('Playbook Execution') {
                steps {
                    echo "Executing Ansible Playbook..."
                    sh """
                        cd ${config.CODE_BASE_PATH}
                        ansible-playbook ${config.PLAYBOOK_NAME}
                    """
                }
            }
        }

        post {
            success {
                slackSend(
                    channel: config.SLACK_CHANNEL_NAME,
                    message: "SUCCESS: ${config.ACTION_MESSAGE}"
                )
            }
            failure {
                slackSend(
                    channel: config.SLACK_CHANNEL_NAME,
                    message: "FAILED: Deployment to ${config.ENVIRONMENT}"
                )
            }
        }
    }
}
