
const key = 'debug_trigger.msg'
const message = `An example message from ${KubePackages.getMetadata('debug_trigger').displayName()}`

KubePackages.sharedData.put(key, message)
console.info(`message put into shared data for startup: ${message}`)

console.info(`debug_trigger.msg in current script type: ${KubePackages.sharedData.get(key)}`)
console.info(`debug_trigger.msg in startup: ${KubePackages.sharedData.getForType('startup', key)}`)
console.info(`debug_trigger.msg in server: ${KubePackages.sharedData.getForType('server', key)}`)
