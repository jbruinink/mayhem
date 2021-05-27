package com.jdriven.mayhem

import com.fasterxml.jackson.databind.ObjectMapper
import com.jdriven.mayhem.game.*
import io.jenetics.IntegerGene
import io.jenetics.Phenotype
import io.jenetics.engine.Evaluator
import io.jenetics.util.ISeq
import io.jenetics.util.Seq
import io.netty.channel.EventLoopGroup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName
import java.net.InetAddress
import java.util.concurrent.CompletableFuture


@Component
class MayhemEvaluator(
    val eventLoopGroup: EventLoopGroup,
    val objectMapper: ObjectMapper,
    val accountGenerator: AccountGenerator,
    val serverConfigurationProperties: ServerConfigurationProperties
) : Evaluator<IntegerGene, Int> {
    private val log = LoggerFactory.getLogger(MayhemEvaluator::class.java)

    override fun eval(population: Seq<Phenotype<IntegerGene, Int>>): ISeq<Phenotype<IntegerGene, Int>> {
        val futures = population.chunked(9).map{list -> play(list)}

        return ISeq.of(
            population.zip(futures.flatMap { it.get() })
                .map<Pair<Phenotype<IntegerGene, Int>, GameResult>, Phenotype<IntegerGene, Int>?> { (phenotype, gameResult) ->
                    phenotype.withFitness(10000 * gameResult.wins + gameResult.healthDifference)
                })
    }

    fun play(phenotypes: List<Phenotype<IntegerGene, Int>>): CompletableFuture<List<GameResult>> {
        val network: Network = Network.newNetwork()

        val server: GenericContainer<*> =
            GenericContainer<Nothing>(DockerImageName.parse("robbert1/mayhem-server:1.1.0"))
//                .apply { withLogConsumer(Slf4jLogConsumer(log)) }
//                .apply { waitingFor(LogMessageWaitStrategy().withRegEx(".+Installed features: \\[[^\\]]*\\]\n")) }
                .apply { withNetwork(network) }
                .apply { withNetworkAliases("server") }
                .apply { withCommand("25") }
        server.start()

        val serverConfig = ServerConfigurationProperties(
            InetAddress.getByName(server.containerIpAddress),
            server.getMappedPort(1337),
            server.getMappedPort(8080)
        )

        val mediumBot: GenericContainer<*> =
            GenericContainer<Nothing>(DockerImageName.parse("robbert1/mayhem-bots"))
//            .apply { withLogConsumer(Slf4jLogConsumer(log)) }
//            .apply { waitingFor(LogMessageWaitStrategy().withRegEx(".+Installed features: \\[[^\\]]*\\]\n")) }
                .apply { withNetwork(network) }
                .apply { withCommand("server 1 1") }
        mediumBot.start()

//        val easyBot: GenericContainer<*> =
//            GenericContainer<Nothing>(DockerImageName.parse("robbert1/mayhem-bots"))
//            .apply { withLogConsumer(Slf4jLogConsumer(log)) }
//            .apply { waitingFor(LogMessageWaitStrategy().withRegEx(".+Installed features: \\[[^\\]]*\\]\n")) }
//                .apply { withNetwork(network) }
//                .apply { withCommand("server 0") }
//        easyBot.start()

        log.info("Starting matches, watch them at http://${serverConfig.address.canonicalHostName}:${serverConfig.webPort}")

        val client = MayhemClient(serverConfig, eventLoopGroup, objectMapper, accountGenerator)
        val futures = phenotypes.map { phenotype -> client.play(GeneticGameStrategy(phenotype.genotype()))}
        val future = CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { futures.map {it.join()} }

        return future.whenComplete { _, _ ->
//            easyBot.stop()
            mediumBot.stop()
            server.stop()
            network.close()
        }
    }
}