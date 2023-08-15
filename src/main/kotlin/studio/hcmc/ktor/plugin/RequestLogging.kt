package studio.hcmc.ktor.plugin

import io.ktor.server.application.*
import io.ktor.server.request.*
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

class RequestLoggingConfiguration {
    var level = Level.TRACE
    var logger = LoggerFactory.getLogger("RequestLogger")
    var format: suspend (call: ApplicationCall, body: String) -> String = { _, _ -> throw NotImplementedError() }
}

val RequestLogging = createApplicationPlugin("RequestLogging", ::RequestLoggingConfiguration) {
    val print: (msg: String) -> Unit = when (pluginConfig.level) {
        Level.ERROR -> pluginConfig.logger::error
        Level.WARN -> pluginConfig.logger::warn
        Level.INFO -> pluginConfig.logger::info
        Level.DEBUG -> pluginConfig.logger::debug
        Level.TRACE -> pluginConfig.logger::trace
    }

    onCall { call ->
        print(pluginConfig.format(call, call.receiveText()))
    }
}