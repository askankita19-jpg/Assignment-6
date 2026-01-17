def call() {

    def config = readYaml(text: libraryResource('config.yml'))

    pipeline {
        agent any

        options {
            skipDefaultCheckout(true)
        }

        stages {

            stage('Clone') {
                steps {
                    echo "Cloning repository..."
                    git branch: 'main', url: config.GIT_REPO
                }
            }

            stage('User Approval') {
                when {
                    expression { config.KEEP_APPROVAL_STAGE }
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
                        pwd
                        ls -l
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
        }
    }
}
