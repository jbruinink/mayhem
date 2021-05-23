package com.jdriven.mayhem

import com.fasterxml.jackson.databind.ObjectMapper
import com.jdriven.mayhem.game.AccountGenerator
import com.jdriven.mayhem.game.GameResult
import com.jdriven.mayhem.game.MayhemClient
import com.jdriven.mayhem.game.ServerConfigurationProperties
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
) : Evaluator<IntegerGene, Float> {
    private val log = LoggerFactory.getLogger(MayhemEvaluator::class.java)

    override fun eval(population: Seq<Phenotype<IntegerGene, Float>>): ISeq<Phenotype<IntegerGene, Float>> {
        val futures = population.chunked(8).map{list -> play(list)}

        return ISeq.of(
            population.zip(futures.flatMap { it.get() })
                .map<Pair<Phenotype<IntegerGene, Float>, GameResult>, Phenotype<IntegerGene, Float>?> { (phenotype, gameResult) ->
                    phenotype.withFitness(
                        4 * gameResult.wins + //win the match
                                gameResult.kills + //kill the enemy heroes
                                1000f / gameResult.totalMatchTime
                    ) // do it quickly
                })
    }

    fun play(phenotypes: List<Phenotype<IntegerGene, Float>>): CompletableFuture<List<GameResult>> {
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

//        val mediumBot: GenericContainer<*> =
//            GenericContainer<Nothing>(DockerImageName.parse("robbert1/mayhem-bots"))
//            .apply { withLogConsumer(Slf4jLogConsumer(log)) }
//            .apply { waitingFor(LogMessageWaitStrategy().withRegEx(".+Installed features: \\[[^\\]]*\\]\n")) }
//                .apply { withNetwork(network) }
//                .apply { withCommand("server 1 1") }
//        mediumBot.start()

        val easyBot: GenericContainer<*> =
            GenericContainer<Nothing>(DockerImageName.parse("robbert1/mayhem-bots"))
//            .apply { withLogConsumer(Slf4jLogConsumer(log)) }
//            .apply { waitingFor(LogMessageWaitStrategy().withRegEx(".+Installed features: \\[[^\\]]*\\]\n")) }
                .apply { withNetwork(network) }
                .apply { withCommand("server 0") }
        easyBot.start()

        log.info("Starting matches, watch them at http://${serverConfig.address.canonicalHostName}:${serverConfig.webPort}")

        val client = MayhemClient(serverConfig, eventLoopGroup, objectMapper, accountGenerator)
        val futures = phenotypes.map { phenotype -> client.play(GeneticGameStrategy(phenotype.genotype()))}
        val future = CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { futures.map {it.join()} }

        return future.whenComplete { _, _ ->
            easyBot.stop()
//            mediumBot.stop()
            server.stop()
            network.close()
        }
    }
}