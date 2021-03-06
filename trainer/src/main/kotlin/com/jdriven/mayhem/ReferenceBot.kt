package com.jdriven.mayhem

import com.fasterxml.jackson.databind.ObjectMapper
import com.jdriven.mayhem.game.GeneticGameStrategy
import com.jdriven.mayhem.game.StatusResponse
import io.jenetics.Genotype
import io.jenetics.IntegerGene
import ninja.robbert.mayhem.api.ActionMessage
import ninja.robbert.mayhem.api.StatusMessage
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.FileReader
import kotlin.streams.asSequence

@Component
class ReferenceBot(objectMapper: ObjectMapper) {
    private val responses: Map<StatusMessage, List<ActionMessage>> =
//        BufferedReader(FileReader("recorded-1621802674330.json")).lines().asSequence()
//        BufferedReader(FileReader("recorded-1621802675433.json")).lines().asSequence()
//        BufferedReader(FileReader("recorded-1621866621375.json")).lines().asSequence()
        BufferedReader(FileReader("recorded-1621866649525.json")).lines().asSequence()
//        BufferedReader(FileReader("recorded-1621871003578.json")).lines().asSequence()
            .map { objectMapper.readValue(it, StatusResponse::class.java) }
            .map { it.statusMessage to it.actionMessages }
            .toMap()

    fun fitness(genotype: Genotype<IntegerGene>): Long {
        val strategy = GeneticGameStrategy(genotype)

        return responses
            .map { (status, reference) ->
                var error = 0L

                val actual = strategy.createUnfilteredResponse(status)

                reference.map { msg ->
                    val scoreOfReferenceAction =
                        actual[msg.hero]?.firstOrNull() { it.skill.id == msg.skill && it.targetId == msg.target }?.score

                    if (scoreOfReferenceAction != null) {
                        if (scoreOfReferenceAction < 0) {
                            error += -scoreOfReferenceAction + 100000000
                        }

                        error += actual[msg.hero]?.sumOf { action ->
                            if ((action.heroId != msg.hero || action.skill.id != msg.skill || action.targetId != msg.target) &&
                                action.score >= scoreOfReferenceAction
                            ) {
                                100000000 + action.score - scoreOfReferenceAction
                            } else {
                                0
                            }
                        } ?: 0
                    } else {
                        println()
                    }
                }

                error += actual
                    .filter { (key, _) -> reference.none { it.hero == key } }
                    .flatMap { (_, value) -> value.filter { it.score > 0 } }
                    .sumOf { it.score + 100000000 }

                error
            }.sum()
    }

}
