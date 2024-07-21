package org.kepocnhh.hegel.module.receiver

import org.kepocnhh.hegel.module.app.Injection
import sp.kx.http.HttpRequest
import sp.kx.http.HttpResponse
import sp.kx.http.HttpRouting

internal class ReceiverRouting(
    private val injection: Injection,
) : HttpRouting {
    private val logger = injection.loggers.create("[Receiver|Routing]")
    private val mapping = mapOf(
        "/v1/items/sync" to mapOf(
            "POST" to ::onPostItemsSync,
        ),
        "/v1/items/merge" to mapOf(
            "POST" to ::onPostItemsMerge,
        ),
    )

    private fun onPostItemsSync(request: HttpRequest): HttpResponse {
        TODO("ReceiverRouting:onPostItemsSync($request)")
    }

    private fun onPostItemsMerge(request: HttpRequest): HttpResponse {
        TODO("ReceiverRouting:onPostItemsMerge($request)")
    }

    override fun route(request: HttpRequest): HttpResponse {
        logger.debug(
            message = """
                <-- request
                ${request.method} ${request.query}
                headers: ${request.headers}
            """.trimIndent(),
        )
        val response = when (val route = mapping[request.query]) {
            null -> HttpResponse(
                version = "1.1",
                code = 404,
                message = "Not Found",
                headers = emptyMap(),
                body = null,
            )
            else -> when (val transform = route[request.method]) {
                null -> HttpResponse(
                    version = "1.1",
                    code = 405,
                    message = "Method Not Allowed",
                    headers = emptyMap(),
                    body = null,
                )
                else -> transform(request)
            }
        }
        logger.debug(
            message = """
                --> response
                ${response.code} ${response.message}
                headers: ${response.headers}
            """.trimIndent(),
        )
        return response
    }
}
