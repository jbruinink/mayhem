package com.jdriven.mayhem

import com.fasterxml.jackson.databind.ObjectMapper
import com.jdriven.mayhem.game.AccountGenerator
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
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.utility.DockerImageName
import java.net.InetAddress


@Component
class MayhemEvaluator(
    val eventLoopGroup: EventLoopGroup,
    val objectMapper: ObjectMapper,
    val accountGenerator: AccountGenerator
) : Evaluator<IntegerGene, Int> {
    private val log = LoggerFactory.getLogger(MayhemEvaluator::class.java)

    override fun eval(population: Seq<Phenotype<IntegerGene, Int>>): ISeq<Phenotype<IntegerGene, Int>> {
        val container: GenericContainer<*> =
            GenericContainer<Nothing>(DockerImageName.parse("robbert1/mayhem-server:1.1.0"))
//                .apply { withLogConsumer(Slf4jLogConsumer(log)) }
                .apply { waitingFor(LogMessageWaitStrategy().withRegEx(".+Installed features: \\[[^\\]]*\\]\n")) }
                .apply { withCommand("50")}
        container.start()

        val serverConfig = ServerConfigurationProperties(
            InetAddress.getByName(container.containerIpAddress),
            container.getMappedPort(1337)
        )
        log.info(serverConfig.toString())

        val client = MayhemClient(serverConfig, eventLoopGroup, objectMapper, accountGenerator)
        val futures = population.map { phenotype -> client.play(GeneticGameStrategy(phenotype.genotype())) }.toList()

        val result = ISeq.of(population.zip(futures).map { (phenotype, future) ->
            val gameResult = future.get()
            phenotype.withFitness((100000 * gameResult.wins) / gameResult.totalMatchTime)
        })

        container.stop()

        return result
    }
}