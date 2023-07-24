def call(Map config = [:]) {
    loadLinuxScript(name: 'helloworld.sh')
    sh 'ls -ltr'
    sh "bash helloworld.sh ${config.name} ${config.dayOfWeek}"
}