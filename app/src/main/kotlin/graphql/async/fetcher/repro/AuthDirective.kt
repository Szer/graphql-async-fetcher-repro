@file:Suppress("DEPRECATION")

package graphql.async.fetcher.repro

import com.expediagroup.graphql.generator.annotations.GraphQLDirective
import com.expediagroup.graphql.generator.directives.KotlinFieldDirectiveEnvironment
import com.expediagroup.graphql.generator.directives.KotlinSchemaDirectiveWiring
import graphql.introspection.Introspection
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.future

@GraphQLDirective(
    name = SCOPE_DIRECTIVE_NAME,
    description = DESCRIPTION,
    locations = [Introspection.DirectiveLocation.FIELD_DEFINITION]
)
annotation class AuthDirective(val authName: String)

internal const val SCOPE_DIRECTIVE_NAME = "requiresAuth"
private const val DESCRIPTION = "Marks query or mutation to be available only with the specified Auth Scheme"

class AuthSchemaDirectiveWiring : KotlinSchemaDirectiveWiring {

    private val validValues = setOf("auth1", "auth2")

    @OptIn(ExperimentalStdlibApi::class, DelicateCoroutinesApi::class)
    override fun onField(environment: KotlinFieldDirectiveEnvironment): GraphQLFieldDefinition {
        val auth = environment.directive.getArgument("authName").argumentValue.value as String
        if (!validValues.contains(auth)) {
            error("Invalid auth scheme `$auth`. expected one of ${validValues.joinToString()}")
        }

        // save old fetcher
        val oldFetcher = environment.getDataFetcher()

        // construct new fetcher with auth check
        @Suppress("UNCHECKED_CAST")
        val newFetcher = DataFetcher { env: DataFetchingEnvironment ->
            GlobalScope.future {

                // imitate suspended calls to external auth services to verify user auth token
                delay(1000)

                val ctx = env.getContext<Request>()

                // check auth
                if (ctx.authHeader != "user secret") {
                    throw IllegalArgumentException("Unathorized")
                }

                return@future oldFetcher.get(env)
            }
        } as DataFetcher<Any>
        environment.setDataFetcher(newFetcher)
        return environment.element
    }
}
