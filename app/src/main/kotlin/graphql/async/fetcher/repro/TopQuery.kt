package graphql.async.fetcher.repro

import com.expediagroup.graphql.server.operations.Query
import kotlinx.coroutines.delay

class TopQuery : Query {

    // this fails (function is suspended) with default KotlinDataFetcherFactoryProvider
    // with "object is not an instance of declaring class" error

    // this works with NotSimpleKotlinDataFetcherFactoryProvider (uncomment in App.kt)
    @AuthDirective("auth1")
    suspend fun auth1(): Response {
        delay(1000) // imitating some other operation
        return Response("token1")
    }

    // this always works (function is not suspended)
    @AuthDirective("auth2")
    fun auth2(): Response {
        return Response("token2")
    }
}
