package de.vyacheslav.kushchenko.staff.vpn.service

import de.vyacheslav.kushchenko.staff.vpn.data.chat.model.Chat
import de.vyacheslav.kushchenko.staff.vpn.data.request.enum.AccessRequestDecisionType
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.ManualAccessType
import de.vyacheslav.kushchenko.staff.vpn.data.user.model.User
import de.vyacheslav.kushchenko.staff.vpn.telegram.BotMenuCallbacks
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.utils.builders.inlineKeyboardMarkup
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TelegramNotificationService(
    private val telegramBots: List<TelegramBot>,
) {
    private val log = LoggerFactory.getLogger(TelegramNotificationService::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun notifyAccessRequestApproved(user: User, decisionType: AccessRequestDecisionType, accessUntil: OffsetDateTime?) {
        val text = when (decisionType) {
            AccessRequestDecisionType.APPROVE_3_MONTH ->
                "Ваша заявка одобрена. Доступ к VPN активен до ${accessUntil?.format(dateFormatter)}."
            AccessRequestDecisionType.APPROVE_FOREVER ->
                "Ваша заявка одобрена. Доступ к VPN активен бессрочно."
            AccessRequestDecisionType.REJECT ->
                return
        }
        sendToUser(user.telegramId, text, openVpnMenuKeyboard())
    }

    fun notifyAccessRequestRejected(user: User) {
        sendToUser(
            user.telegramId,
            "Ваша заявка на доступ к VPN отклонена.",
            retryRequestKeyboard()
        )
    }

    fun notifyChatRequestApproved(user: User, chat: Chat) {
        sendToUser(
            user.telegramId,
            "Заявка на подключение чата ${chatDisplayName(chat)} одобрена.",
            openVpnMenuKeyboard()
        )
    }

    fun notifyChatRequestRejected(user: User, chat: Chat) {
        sendToUser(
            user.telegramId,
            "Заявка на подключение чата ${chatDisplayName(chat)} отклонена."
        )
    }

    fun notifyManualAccessGranted(user: User, accessType: ManualAccessType, accessUntil: OffsetDateTime?) {
        val text = when (accessType) {
            ManualAccessType.THREE_MONTHS ->
                "Ваш ручной доступ к VPN активен до ${accessUntil?.format(dateFormatter)}."
            ManualAccessType.FOREVER ->
                "Ваш ручной доступ к VPN активен бессрочно."
            ManualAccessType.NONE ->
                return
        }
        sendToUser(user.telegramId, text, openVpnMenuKeyboard())
    }

    fun notifyManualAccessRevoked(user: User) {
        sendToUser(
            user.telegramId,
            "Ваш ручной доступ к VPN был отозван.",
            retryRequestKeyboard()
        )
    }

    private fun chatDisplayName(chat: Chat): String =
        chat.title?.let { "\"$it\"" }
            ?: chat.username?.let { "@$it" }
            ?: chat.chatId.toString()

    private fun openVpnMenuKeyboard() = inlineKeyboardMarkup {
        callbackData("Открыть VPN-меню") { BotMenuCallbacks.MENU_MAIN }
    }

    private fun retryRequestKeyboard() = inlineKeyboardMarkup {
        callbackData("Подать заявку повторно") { BotMenuCallbacks.REQUEST_ACCESS }
    }

    private fun sendToUser(
        telegramId: Long,
        text: String,
        keyboard: eu.vendeli.tgbot.types.keyboard.InlineKeyboardMarkup? = null
    ) {
        val bot = telegramBots.firstOrNull() ?: run {
            log.warn("Telegram bot instance is not available for notifications.")
            return
        }

        try {
            runBlocking {
                val request = message(text)
                if (keyboard != null) {
                    request.markup { keyboard }.send(telegramId, bot)
                } else {
                    request.send(telegramId, bot)
                }
            }
        } catch (e: Exception) {
            log.warn("Failed to send Telegram notification to telegramId={}", telegramId, e)
        }
    }
}
