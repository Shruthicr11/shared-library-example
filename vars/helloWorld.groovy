/*def call(Map config = [:]) {
    sh "echo Hello ${config.name}. Today is ${config.dayOfWeek}."
}*/

def call(string name, string dayOfWeek) {
    sh "echo this is ${name} and today is ${dayOfWeek}"
}
