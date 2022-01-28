@file:Suppress("OverridingDeprecatedMember")

package graphql.async.fetcher.repro

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.directives.KotlinDirectiveWiringFactory
import com.expediagroup.graphql.generator.execution.GraphQLContext
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.generator.toSchema
import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import com.expediagroup.graphql.server.execution.GraphQLRequestHandler
import com.expediagroup.graphql.server.execution.GraphQLRequestParser
import com.expediagroup.graphql.server.execution.GraphQLServer
import com.expediagroup.graphql.server.types.GraphQLRequest
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import graphql.GraphQL
import kotlinx.coroutines.runBlocking

val hooks = object : SchemaGeneratorHooks {
    override val wiringFactory = KotlinDirectiveWiringFactory(
        manualWiring = mapOf(
            "requiresAuth" to AuthSchemaDirectiveWiring(),
        )
    )
}

val graphQl: GraphQL = run {
    val packages = listOf("graphql.async.fetcher.repro")
    val config = SchemaGeneratorConfig(
        supportedPackages = packages,
        hooks = hooks,
        // uncomment this line to fix the issue
        // dataFetcherFactoryProvider = NotSimpleKotlinDataFetcherFactoryProvider()
    )
    val schema = toSchema(config, listOf(TopLevelObject(TopQuery())))
    GraphQL.newGraphQL(schema).build()
}

val gqlServer = GraphQLServer(
    requestParser = object : GraphQLRequestParser<Request> {
        private val json = ObjectMapper().registerKotlinModule()
        override suspend fun parseRequest(request: Request) =
            json.readValue(request.query, GraphQLRequest::class.java)
    },
    contextFactory = object : GraphQLContextFactory<GraphQLContext, Request> {
        override suspend fun generateContext(request: Request) = request
    },
    requestHandler = GraphQLRequestHandler(graphQl),
)

fun main() {
    runBlocking {
        val request = Request("""{"query":"query{auth1{token}}"}""", "user secret")
        val response = gqlServer.execute(request) as GraphQLResponse<*>
        if (response.errors?.isNotEmpty() == true) {
            error("Error: " + response.errors!!.joinToString(", ") { it.message })
        } else {
            println("Ok - $response")
        }
    }
}
