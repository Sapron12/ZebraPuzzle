package org.qudisoft.io.json

import kotlinx.serialization.json.*
import org.qudisoft.model.*
import org.slf4j.LoggerFactory

// TODO: FUTURE DEVELOPMENT - Move error logic into the validation mechanism
class JsonParser {
    
    private val logger = LoggerFactory.getLogger(JsonParser::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    fun parseJsonString(jsonString: String): PuzzleInput {
        return try {
            logger.debug("Attempting direct JSON deserialization")
            json.decodeFromString<PuzzleInput>(jsonString)

        } catch (e: Exception) {
            logger.warn("Direct deserialization failed, attempting manual parsing with unknown constraint handling: ${e.message}")
            parseWithUnknownConstraintHandling(jsonString)
        }
    }
    
    private fun parseWithUnknownConstraintHandling(jsonString: String): PuzzleInput {
        logger.debug("Starting manual JSON parsing with unknown constraint handling")
        val puzzleElement = Json
            .parseToJsonElement(jsonString)
            .jsonObject["puzzle"]!!
            .jsonObject

        val (description, houses, attributes) = parsePuzzleMetadata(puzzleElement)
        val constraints = parseConstraints(puzzleElement)
        val questions = parseQuestions(puzzleElement)
        
        logger.info("Successfully completed manual parsing with ${constraints.size} constraints and ${questions.size} questions")
        return PuzzleInput(PuzzleData(description, houses, attributes, constraints, questions))
    }
    
    private fun parsePuzzleMetadata(puzzleElement: JsonObject): Triple<String, Int, PuzzleAttributes> {
        val description = puzzleElement["description"]!!.jsonPrimitive.content
        val houses = puzzleElement["houses"]!!.jsonPrimitive.int
        val attributes = json.decodeFromJsonElement<PuzzleAttributes>(puzzleElement["attributes"]!!)
        logger.debug("Parsed puzzle metadata: description='$description', houses=$houses, attributes=${attributes.attributes.size}")
        return Triple(description, houses, attributes)
    }
    
    private fun parseConstraints(puzzleElement: JsonObject): List<PuzzleConstraint> {
        val constraintsArray = puzzleElement["constraints"]!!.jsonArray
        logger.debug("Parsing ${constraintsArray.size} constraints")
        return constraintsArray.mapIndexed { index, constraintElement ->
            val constraint = parseConstraint(constraintElement.jsonObject)
            logger.debug("Parsed constraint ${index + 1}/${constraintsArray.size}: ${constraint::class.simpleName}")
            constraint
        }
    }
    
    private fun parseQuestions(puzzleElement: JsonObject): List<Question> {
        return puzzleElement["questions"]?.jsonArray?.let { questionsArray ->
            logger.debug("Parsing ${questionsArray.size} questions")
            questionsArray.mapIndexed { index, questionElement ->
                parseQuestion(questionElement.jsonObject, index + 1, questionsArray.size)
            }
        } ?: emptyList()
    }
    
    private fun parseQuestion(questionObj: JsonObject, questionIndex: Int, totalQuestions: Int): Question {
        try {
            logger.debug("Question $questionIndex available fields: ${questionObj.keys}")
            
            val description = questionObj["description"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Missing 'description' field in question $questionIndex")
            val targetAttribute = questionObj["targetAttribute"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Missing 'targetAttribute' field in question $questionIndex")
            val givenAttribute = questionObj["givenAttribute"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Missing 'givenAttribute' field in question $questionIndex")
            val givenValue = questionObj["givenValue"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Missing 'givenValue' field in question $questionIndex")
            
            val question = Question(description, targetAttribute, givenAttribute, givenValue)
            logger.debug("Parsed question $questionIndex/$totalQuestions: '${question.description}'")
            return question
        } catch (e: Exception) {
            logger.error("Failed to parse question $questionIndex: ${e.message}")
            throw e
        }
    }
    
    private fun parseConstraint(constraintObj: JsonObject): PuzzleConstraint {
        val type = constraintObj["type"]?.jsonPrimitive?.content ?: "unknown"
        val description = constraintObj["description"]?.jsonPrimitive?.content ?: "Unknown constraint"
        
        return when (type) {
                "direct" -> json.decodeFromJsonElement<DirectConstraint>(JsonObject(constraintObj))
                "position" -> json.decodeFromJsonElement<PositionConstraint>(JsonObject(constraintObj))
                "leftOf" -> json.decodeFromJsonElement<LeftOfConstraint>(JsonObject(constraintObj))
                "rightOf" -> json.decodeFromJsonElement<RightOfConstraint>(JsonObject(constraintObj))
                "neighbor" -> json.decodeFromJsonElement<NeighborConstraint>(JsonObject(constraintObj))
                else -> {
                    // Create UnknownConstraint for any unrecognized type
                    val properties = constraintObj.filterKeys { it != "type" && it != "description" }
                        .mapValues { it.value.jsonPrimitive.content }
                    UnknownConstraint(description, type, properties)
                }
            }
    }
}