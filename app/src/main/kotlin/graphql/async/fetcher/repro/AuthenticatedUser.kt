package graphql.async.fetcher.repro

import com.expediagroup.graphql.generator.execution.GraphQLContext

data class Request(val query: String, val authHeader: String) : GraphQLContext

data class Response(val token: String)