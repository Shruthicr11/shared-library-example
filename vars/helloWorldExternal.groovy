def call(Map config = [:]) {
    loadLinuxScript(name: 'helloworld.sh')
    sh 'ls -ltr'
    sh "sh helloworld.sh ${config.name} ${config.dayOfWeek}"
}