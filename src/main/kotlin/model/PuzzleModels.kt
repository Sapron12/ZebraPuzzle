package org.qudisoft.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
sealed class PuzzleConstraint {
    abstract val description: String
}

@Serializable
@SerialName("direct")
data class DirectConstraint(
    override val description: String,
    val attribute1: String,
    val value1: String,
    val attribute2: String,
    val value2: String
) : PuzzleConstraint() {
}

@Serializable
@SerialName("position")
data class PositionConstraint(
    override val description: String,
    val attribute: String,
    val value: String,
    val position: Int
) : PuzzleConstraint() {
}

@Serializable
@SerialName("leftOf")
data class LeftOfConstraint(
    override val description: String,
    val attribute1: String,
    val value1: String,
    val attribute2: String,
    val value2: String
) : PuzzleConstraint() {
}

@Serializable
@SerialName("rightOf")
data class RightOfConstraint(
    override val description: String,
    val attribute1: String,
    val value1: String,
    val attribute2: String,
    val value2: String
) : PuzzleConstraint() {
}

@Serializable
@SerialName("neighbor")
data class NeighborConstraint(
    override val description: String,
    val attribute1: String,
    val value1: String,
    val attribute2: String,
    val value2: String
) : PuzzleConstraint() {
}

@Serializable
@SerialName("unknown")
data class UnknownConstraint(
    override val description: String,
    val type: String? = null,
    val properties: Map<String, String> = emptyMap()
) : PuzzleConstraint()


@Serializable(with = PuzzleAttributesSerializer::class)
data class PuzzleAttributes(
    val attributes: Map<String, List<String>>
)

object PuzzleAttributesSerializer : KSerializer<PuzzleAttributes> {
    override val descriptor = MapSerializer(String.serializer(), ListSerializer(String.serializer())).descriptor

    override fun serialize(encoder: Encoder, value: PuzzleAttributes) {
        encoder.encodeSerializableValue(MapSerializer(String.serializer(), ListSerializer(String.serializer())), value.attributes)
    }

    override fun deserialize(decoder: Decoder): PuzzleAttributes {
        val map = decoder.decodeSerializableValue(MapSerializer(String.serializer(), ListSerializer(String.serializer())))
        return PuzzleAttributes(map)
    }
}

@Serializable
data class Question(
    val description: String,
    val targetAttribute: String,
    val givenAttribute: String,
    val givenValue: String
)

@Serializable
data class PuzzleData(
    val description: String,
    val houses: Int,
    val attributes: PuzzleAttributes,
    val constraints: List<PuzzleConstraint>,
    val questions: List<Question> = emptyList()
)

@Serializable
data class PuzzleInput(
    val puzzle: PuzzleData
)

@Serializable
data class HouseSolution(
    val house: Int,
    val attributes: Map<String, String>
)

@Serializable
data class QuestionAnswer(
    val question: String,
    val answer: String?
)

@Serializable
data class PuzzleSolution(
    val description: String,
    val solved: Boolean,
    val houses: List<HouseSolution>,
    val answers: List<QuestionAnswer>
)