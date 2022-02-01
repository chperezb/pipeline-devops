import utilities.*

def call(stages){

    //def stagesList = stages.split(";")
    // stagesList.each{
    //     println("===>${it}")
    //     "${it}"()

    // }

   def listStagesOrder = [
        'build': 'stageCleanBuildTest',
        'sonar': 'stageSonar',
        'curl_spring': 'stageRunSpringCurl',
        'upload_nexus': 'stageUploadNexus',
        'download_nexus': 'stageDownloadNexus',
        'run_jar': 'stageRunJar',
        'curl_jar': 'stageCurlJar'
    ]

    def arrayUtils = new array.arrayExtentions();
    def stagesArray = []
        stagesArray = arrayUtils.searchKeyInArray(stages, ";", listStagesOrder)

    if (stagesArray.isEmpty()) {
        echo 'El pipeline se ejecutará completo'
        allStages()
    } else {
        echo 'Stages a ejecutar :' + stages
        stagesArray.each{ stageFunction ->//variable as param
            echo 'Ejecutando ' + stageFunction
            "${stageFunction}"()
        }
    }

//     if (stages.isEmpty()) {
//         echo 'El pipeline se ejecutará completo'
//         allStages()
//     } else {
//         echo 'Stages a ejecutar :' + stages
//         listStagesOrder.each { stageName, stageFunction ->
//             stagesList.each{ stageToExecute ->//variable as param
//                 if(stageName.equals(stageToExecute)){
//                 echo 'Ejecutando ' + stageFunction
//                 "${stageFunction}"()
//                 }
//             }
//         }
// 
//     }
}

def allStages(){
    stageCleanBuildTest()
    stageSonar()
    stageRunSpringCurl()
    stageUploadNexus()
    stageDownloadNexus()
    stageRunJar()
    stageCurlJar()
}

def stageCleanBuildTest(){
    env.TAREA = "Paso 1: Build && Test"
    stage("$env.TAREA"){
        sh "echo 'Build && Test!'"
        sh "gradle clean build"
        // code
    }
}

def stageSonar(){
    env.TAREA="Paso 2: Sonar - Análisis Estático"
    stage("$env.TAREA"){
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh "echo 'Calling sonar by ID!'"
            // Run Maven on a Unix agent to execute Sonar.
            sh './gradlew sonarqube -Dsonar.projectKey=job-github-sonar-1 -Dsonar.java.binaries=build'
        }
    }
}

def stageRunSpringCurl(){
    env.TAREA="Paso 3: Curl Springboot Gradle sleep 20"
    stage("$env.TAREA"){
        sh "gradle bootRun&"
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

def stageUploadNexus(){
    env.TAREA="Paso 4: Subir Nexus"
    stage("$env.TAREA"){
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: 'jar',
                    filePath: 'build/libs/DevOpsUsach2020-0.0.1.jar'
                ]
            ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
        ]
    }
}
def stageDownloadNexus(){
    // env.TAREA="Paso 3: Curl Springboot Gradle sleep 20"
    // stage("$env.TAREA"){
    stage("Paso 5: Descargar Nexus"){

        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
}
def stageRunJar(){
      // env.TAREA="Paso 3: Curl Springboot Gradle sleep 20"
    // stage("$env.TAREA"){
    stage("Paso 6: Levantar Artefacto Jar"){
        sh 'nohup java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
}
def stageCurlJar(){
      // env.TAREA="Paso 3: Curl Springboot Gradle sleep 20"
    // stage("$env.TAREA"){
    stage("Paso 7: Testear Artefacto - Dormir(Esperar 20sg) "){
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}
return this;
