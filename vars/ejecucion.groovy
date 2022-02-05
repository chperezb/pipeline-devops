/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/

def call(){
  
pipeline {
    agent any
    environment {
        NEXUS_USER         = credentials('NEXUS-USER')
        NEXUS_PASSWORD     = credentials('NEXUS-PASS')
    }
    parameters {
            choice choices: ['maven', 'gradle'], description: 'Seleccione una herramienta para preceder a compilar', name: 'compileTool'
    }
    stages {
        stage("pipeline"){
            steps {
                script{
		  def is_ci_or_cd = verifyBranchName()
                  figlet is_ci_or_cd;
                  switch(params.compileTool)
                    {
                        case 'maven':
                            //def ejecucion = load 'maven.groovy'
                            //ejecucion.call()
			    maven.call(checkBranch())
                        break;
                        case 'gradle':
                            //def ejecucion = load 'gradle.groovy'
                            //ejecucion.call()
			    gradle.call(checkBranch())
                        break;
                    }
                }
            }
            post{
                success{
                    slackSend color: 'good', message: "[Cristian Perez B] [${JOB_NAME}] [${BUILD_TAG}] Ejecucion Exitosa", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-jenkins'
                }
                failure{
                    slackSend color: 'danger', message: "[Cristian Perez B] [${env.JOB_NAME}] [${BUILD_TAG}] Ejecucion fallida en stage [${env.TAREA}]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-jenkins'
                }
            }
        }
    }
}

}

def checkBranch(){
	if(env.GIT_BRANCH.contains('feature') || env.GIT_BRANCH.contains('develop')) {
		return 'CI'
	} else {
		return 'CD'
	}
}

return this;
