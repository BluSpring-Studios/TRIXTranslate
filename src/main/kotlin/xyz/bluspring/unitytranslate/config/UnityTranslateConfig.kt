package xyz.bluspring.unitytranslate.config

import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.util.TriState
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.client.gui.TranscriptBox
import xyz.bluspring.unitytranslate.client.transcribers.TranscriberType
import java.util.*

@Serializable
data class UnityTranslateConfig(
    val client: ClientConfig = ClientConfig(),
    val server: CommonConfig = CommonConfig()
) {
    @Serializable
    data class ClientConfig(
        var enabled: Boolean = true,
        var openBrowserWithoutPrompt: Boolean = false,
        var muteTranscriptWhenVoiceChatMuted: Boolean = false,

        var transcriptBoxes: MutableList<TranscriptBox> = mutableListOf(),
        var transcriber: TranscriberType = TranscriberType.BROWSER,
        var language: Language = Language.ENGLISH
    )

    @Serializable
    data class CommonConfig(
        var translatePriority: Set<TranslationPriority> = EnumSet.of(
            TranslationPriority.CLIENT_GPU, // highest priority, prioritize using CUDA on the client-side.
            TranslationPriority.SERVER_GPU, // if supported, use CUDA on the server-side.
            TranslationPriority.SERVER_CPU, // otherwise, translate on the CPU.
            TranslationPriority.OFFLOADED,  // use alternative servers if available
            TranslationPriority.CLIENT_CPU, // worst case scenario, use client CPU.
        ),
        var shouldUseCuda: Boolean = true,
        var shouldRunTranslationServer: Boolean = false,
        var offloadServers: MutableList<OffloadedLibreTranslateServer> = mutableListOf(
            OffloadedLibreTranslateServer("https://trans.zillyhuhn.com"),
            OffloadedLibreTranslateServer("https://libretranslate.devos.gay")
        ),

        // Interval for when the batch translations will be sent.
        // This is done so redundant translations don't go through,
        // which puts unnecessary stress on the translation instances.
        @Range(from = 0.5, to = 5.0)
        var batchTranslateInterval: Double = 0.5 // 500ms
    )

    @Serializable
    data class OffloadedLibreTranslateServer(
        var url: String, // follows http://127.0.0.1:5000 - the /translate endpoint will be appended at the end automatically.
        var authKey: String? = null,
        var weight: Int = 100,
        var maxConcurrentTranslations: Int = 20
    )

    enum class TranslationPriority(val usesCuda: TriState) {
        SERVER_GPU(TriState.TRUE),
        SERVER_CPU(TriState.FALSE),
        CLIENT_GPU(TriState.TRUE),
        CLIENT_CPU(TriState.FALSE),
        OFFLOADED(TriState.DEFAULT)
    }
}