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
            text description: 'Enviar los stages separados por ";"... Vacío si necesita todos los stages', name: 'stages'
    }
    stages {
        stage("pipeline"){
            steps {
                script{
                  switch(params.compileTool)
                    {
                        case 'maven':
                            //def ejecucion = load 'maven.groovy'
                            //ejecucion.call()
			    maven.call(params.stages)
                        break;
                        case 'gradle':
                            //def ejecucion = load 'gradle.groovy'
                            //ejecucion.call()
			    gradle.call(params.stages)
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

return this;
