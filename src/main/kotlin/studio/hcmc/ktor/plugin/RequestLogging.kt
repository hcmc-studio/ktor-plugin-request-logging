package studio.hcmc.ktor.plugin

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import studio.hcmc.ktor.serialization.toJsonObject

class RequestLoggingConfiguration {
    var level = Level.TRACE
    var logger = LoggerFactory.getLogger("RequestLogger")
    var format: suspend (call: ApplicationCall, body: String) -> String = { call, body ->
        formatRequest(call, body)
    }
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

private fun formatRequest(call: ApplicationCall, body: String): String {
    val json = call.application.defaultJson
    return json.encodeToString(buildJsonObject {
        put("request", buildJsonObject {
            put("httpMethod", call.request.httpMethod.value)
            put("uri", call.request.uri)
            put("origin", buildJsonObject {
                put("scheme", call.request.origin.scheme)
                put("version", call.request.origin.version)
                put("localPort", call.request.origin.localPort)
                put("serverPort", call.request.origin.serverPort)
                put("localHost", call.request.origin.localHost)
                put("serverHost", call.request.origin.serverHost)
                put("localAddress", call.request.origin.localAddress)
                put("uri", call.request.origin.uri)
                put("method", call.request.origin.method.value)
                put("remoteHost", call.request.origin.remoteHost)
                put("remotePort", call.request.origin.remotePort)
                put("remoteAddress", call.request.origin.remoteAddress)
            })
            put("headers", call.request.headers.toJsonObject())
            put("queryParameters", call.request.queryParameters.toJsonObject())
        })
        put("parameters", call.parameters.toJsonObject())
        put("body", json.parseToJsonElement(body.ifEmpty { "{}" }))
    })
}