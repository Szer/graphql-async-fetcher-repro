package graphql.async.fetcher.repro

import com.expediagroup.graphql.generator.execution.SimpleKotlinDataFetcherFactoryProvider
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.execution.Async
import graphql.schema.DataFetcher
import graphql.schema.DataFetcherFactory
import graphql.schema.DataFetchingEnvironment
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

// Fixed version of PropertyDataFetcher which can work with CompletableFuture<T>
class AsyncPropertyDataFetcher(private val propertyGetter: KProperty.Getter<*>) : DataFetcher<Any?> {
    override fun get(environment: DataFetchingEnvironment): Any? = environment.getSource<Any?>()?.let { instance ->
        Async.toCompletableFuture(instance).thenApply {
            propertyGetter.call(it)
        }
    }
}

class NotSimpleKotlinDataFetcherFactoryProvider(
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
    private val defaultCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : SimpleKotlinDataFetcherFactoryProvider(objectMapper, defaultCoroutineContext) {

    override fun propertyDataFetcherFactory(kClass: KClass<*>, kProperty: KProperty<*>) = DataFetcherFactory {
        AsyncPropertyDataFetcher(kProperty.getter)
    }
}
