/*def call(Map config = [:]) {
    sh "echo Hello ${config.name}. Today is ${config.dayOfWeek}."
}*/

def call(String name, String dayOfWeek) {
    sh "echo this is ${name} and today is ${dayOfWeek}"
}
