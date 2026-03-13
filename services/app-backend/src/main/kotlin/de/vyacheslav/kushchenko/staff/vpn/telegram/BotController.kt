package de.vyacheslav.kushchenko.staff.vpn.telegram

import de.vyacheslav.kushchenko.staff.vpn.data.chat.enum.ChatType as TrustedChatType
import de.vyacheslav.kushchenko.staff.vpn.data.request.enum.AccessRequestStatus
import de.vyacheslav.kushchenko.staff.vpn.data.request.model.AccessRequest
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.ManualAccessType
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.UserRole
import de.vyacheslav.kushchenko.staff.vpn.data.user.model.User as AppUser
import de.vyacheslav.kushchenko.staff.vpn.service.AccessRequestService
import de.vyacheslav.kushchenko.staff.vpn.service.AuthenticationService
import de.vyacheslav.kushchenko.staff.vpn.service.ChatService
import de.vyacheslav.kushchenko.staff.vpn.service.VpnLinkService
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.annotations.CommonHandler
import eu.vendeli.tgbot.annotations.UpdateHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.chat.setChatMenuButton
import eu.vendeli.tgbot.api.getFile
import eu.vendeli.tgbot.api.getUserProfilePhotos
import eu.vendeli.tgbot.api.message.editMessageReplyMarkup
import eu.vendeli.tgbot.api.message.editMessageText
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.chat.ChatType
import eu.vendeli.tgbot.types.internal.CallbackQueryUpdate
import eu.vendeli.tgbot.types.internal.MessageUpdate
import eu.vendeli.tgbot.types.internal.MyChatMemberUpdate
import eu.vendeli.tgbot.types.internal.UpdateType
import eu.vendeli.tgbot.types.internal.getOrNull
import eu.vendeli.tgbot.types.keyboard.MenuButton
import eu.vendeli.tgbot.types.keyboard.WebAppInfo
import eu.vendeli.tgbot.utils.builders.inlineKeyboardMarkup
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller

@Controller
class BotController(
    private val authenticationService: AuthenticationService,
    private val accessRequestService: AccessRequestService,
    private val chatService: ChatService,
    private val vpnLinkService: VpnLinkService,
    @Value("\${TELEGRAM_USERNAME:}") private val botUsername: String,
    @Value("\${app.admin.webapp-url:}") private val adminWebAppUrl: String,
    @Value("\${app.vpn.instruction-url:https://telegra.ph/Ustanovka-VPN-rukovodstvo-11-10}") private val instructionUrl: String,
    @Value("\${app.support.url:}") private val supportUrl: String,
) {
    private val log = LoggerFactory.getLogger(BotController::class.java)
    private val menuMainCallback = BotMenuCallbacks.MENU_MAIN
    private val requestAccessCallback = BotMenuCallbacks.REQUEST_ACCESS
    private val menuLinkCallback = BotMenuCallbacks.MENU_LINK
    private val menuStatusCallback = BotMenuCallbacks.MENU_STATUS
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    @CommandHandler(["/start"])
    suspend fun handleStart(user: User, bot: TelegramBot, update: MessageUpdate) {
        val commandText = (update.text ?: "").trim()
        val chat = update.message.chat
        if (chat.type != ChatType.Private) {
            handleGroupStart(bot, update, commandText)
            return
        }

        sendPrivateMenu(user, bot, update.user)
    }

    private suspend fun handleGroupStart(bot: TelegramBot, update: MessageUpdate, commandText: String) {
        val messageUpdate = update.message
        val chat = messageUpdate.chat
        val trustedChatType = when (chat.type) {
            ChatType.Group -> TrustedChatType.GROUP
            ChatType.Supergroup -> TrustedChatType.SUPERGROUP
            else -> return
        }
        if (!isGroupRegistrationCommand(commandText)) {
            return
        }

        val savedUser = ensureUser(update.user, bot)
        when (savedUser.role) {
            UserRole.USER -> {
                message("У вас нет прав на подключение этого чата. Запрос может создать только OWNER или ADMIN.")
                    .send(chat.id, bot)
            }
            UserRole.ADMIN,
            UserRole.OWNER -> {
                val registeredChat = chatService.registerChat(
                    chatId = chat.id,
                    type = trustedChatType,
                    title = chat.title,
                    username = chat.username,
                    initiatedBy = savedUser.id!!,
                    initiatedByRole = savedUser.role,
                    joinedAt = OffsetDateTime.now(ZoneOffset.UTC)
                )
                val responseText = if (registeredChat.status == de.vyacheslav.kushchenko.staff.vpn.data.chat.enum.ChatStatus.APPROVED) {
                    "Чат сразу одобрен. Участники смогут получить доступ после обращения к боту в личке."
                } else {
                    "Запрос на подключение чата создан и отправлен OWNER на подтверждение."
                }
                message(responseText).send(chat.id, bot)
            }
        }
    }

    @CommandHandler(["/ping"])
    suspend fun handlePing(user: User, bot: TelegramBot) {
        message("pong").send(user, bot)
    }

    @CommonHandler.Text([".*"])
    suspend fun handleText(user: User, bot: TelegramBot, update: MessageUpdate) {
        val text = update.text ?: return
        if (text.startsWith("/")) return
        if (update.message.chat.type != ChatType.Private) return
        sendPrivateMenu(user, bot, update.user)
    }

    @UpdateHandler(type = [UpdateType.CALLBACK_QUERY])
    suspend fun handleCallback(update: CallbackQueryUpdate, bot: TelegramBot) {
        val callback = update.callbackQuery
        val data = callback.data ?: return
        val callbackMessage = callback.message ?: return
        val snapshot = buildSnapshot(update.user, bot)

        when (data) {
            menuMainCallback -> {
                answerCallbackQuery(callback.id).send(update.user.id, bot)
                renderMenuPage(
                    messageId = callbackMessage.messageId,
                    chatId = callbackMessage.chat.id,
                    page = MenuPage.MAIN,
                    snapshot = snapshot,
                    bot = bot
                )
            }
            requestAccessCallback -> handleRequestAccess(
                callbackId = callback.id,
                messageId = callbackMessage.messageId,
                chatId = callbackMessage.chat.id,
                snapshot = snapshot,
                bot = bot
            )
            menuLinkCallback -> {
                answerCallbackQuery(callback.id).send(update.user.id, bot)
                renderMenuPage(
                    messageId = callbackMessage.messageId,
                    chatId = callbackMessage.chat.id,
                    page = MenuPage.LINK,
                    snapshot = snapshot,
                    bot = bot
                )
            }
            menuStatusCallback -> {
                answerCallbackQuery(callback.id).send(update.user.id, bot)
                renderMenuPage(
                    messageId = callbackMessage.messageId,
                    chatId = callbackMessage.chat.id,
                    page = MenuPage.STATUS,
                    snapshot = snapshot,
                    bot = bot
                )
            }
        }
    }

    @UpdateHandler(type = [UpdateType.MY_CHAT_MEMBER])
    suspend fun handleMyChatMember(update: MyChatMemberUpdate) {
        val event = update.myChatMember
        val chat = event.chat
        when (event.newChatMember) {
            is eu.vendeli.tgbot.types.chat.ChatMember.Left,
            is eu.vendeli.tgbot.types.chat.ChatMember.Banned,
            is eu.vendeli.tgbot.types.chat.ChatMember.Restricted -> {
                chatService.markRejected(chat.id)
            }
            else -> Unit
        }
    }

    private suspend fun sendPrivateMenu(user: User, bot: TelegramBot, tgUser: User) {
        val snapshot = buildSnapshot(tgUser, bot)
        configurePrivateMenuButton(user, bot, snapshot.savedUser.role)
        val mainPage = renderPage(snapshot, MenuPage.MAIN)
        message(mainPage.text).markup { mainPage.keyboard }.send(user, bot)
    }

    private suspend fun handleRequestAccess(
        callbackId: String,
        messageId: Long,
        chatId: Long,
        snapshot: AccessSnapshot,
        bot: TelegramBot
    ) {
        when {
            snapshot.hasAnyAccess -> {
                answerCallbackQuery(callbackId).options {
                    text = "Доступ уже активен."
                    showAlert = true
                }.send(chatId, bot)
                renderMenuPage(messageId, chatId, MenuPage.MAIN, snapshot, bot)
            }
            snapshot.latestRequest?.status == AccessRequestStatus.PENDING -> {
                answerCallbackQuery(callbackId).options {
                    text = "Заявка уже находится на рассмотрении."
                    showAlert = true
                }.send(chatId, bot)
                renderMenuPage(messageId, chatId, MenuPage.MAIN, snapshot, bot)
            }
            else -> {
                accessRequestService.createOrGetPending(snapshot.savedUser.id!!)
                answerCallbackQuery(callbackId).options {
                    text = "Заявка отправлена."
                    showAlert = false
                }.send(chatId, bot)
                val updatedSnapshot = snapshot.copy(
                    latestRequest = accessRequestService.getLatestForUser(snapshot.savedUser.id!!)
                )
                renderMenuPage(messageId, chatId, MenuPage.MAIN, updatedSnapshot, bot)
            }
        }
    }

    private suspend fun renderMenuPage(
        messageId: Long,
        chatId: Long,
        page: MenuPage,
        snapshot: AccessSnapshot,
        bot: TelegramBot
    ) {
        val pageState = renderPage(snapshot, page)
        when (page) {
            MenuPage.LINK -> {
                val vpnLink = vpnLinkService.getLinkForUser(snapshot.savedUser.id!!)
                editMessageText(messageId) {
                    "Ваша индивидуальная ссылка:\n<blockquote>${escapeHtml(vpnLink)}</blockquote>"
                }.options {
                    parseMode = ParseMode.HTML
                }.send(chatId, bot)
            }
            else -> {
                editMessageText(messageId) { pageState.text }.send(chatId, bot)
            }
        }
        editMessageReplyMarkup(messageId)
            .markup(pageState.keyboard)
            .send(chatId, bot)
    }

    private suspend fun buildSnapshot(tgUser: User, bot: TelegramBot): AccessSnapshot {
        val savedUser = ensureUser(tgUser, bot)
        val hasPrivilegedAccess = savedUser.role == UserRole.OWNER || savedUser.role == UserRole.ADMIN
        val hasManualAccess = savedUser.hasActiveManualAccess()
        val hasChatAccess = chatService.hasUserInApprovedChat(tgUser.id, bot)
        val latestRequest = accessRequestService.getLatestForUser(savedUser.id!!)
        return AccessSnapshot(
            savedUser = savedUser,
            hasPrivilegedAccess = hasPrivilegedAccess,
            hasManualAccess = hasManualAccess,
            hasChatAccess = hasChatAccess,
            latestRequest = latestRequest
        )
    }

    private suspend fun configurePrivateMenuButton(user: User, bot: TelegramBot, role: UserRole) {
        if ((role == UserRole.ADMIN || role == UserRole.OWNER) && adminWebAppUrl.isNotBlank()) {
            log.info(
                "Setting admin web app menu button for telegramId={} role={} url={}",
                user.id,
                role,
                adminWebAppUrl
            )
            val menuButton = MenuButton.WebApp("Админка", WebAppInfo(adminWebAppUrl))
            setChatMenuButton(menuButton).send(user, bot)
            return
        }

        log.info(
            "Resetting menu button to default for telegramId={} role={} hasAdminWebAppUrl={}",
            user.id,
            role,
            adminWebAppUrl.isNotBlank()
        )
        setChatMenuButton(MenuButton.Default()).send(user, bot)
    }

    private fun renderPage(snapshot: AccessSnapshot, page: MenuPage): MenuPageState {
        val user = snapshot.savedUser
        if (!snapshot.hasAnyAccess) {
            val text = when (snapshot.latestRequest?.status) {
                AccessRequestStatus.PENDING -> "Заявка уже отправлена и ожидает рассмотрения."
                AccessRequestStatus.REJECTED -> "В доступе отказано. Можно подать заявку повторно."
                else -> "Доступ не активен. Чтобы получить VPN, подайте заявку."
            }
            val keyboard = inlineKeyboardMarkup {
                if (snapshot.latestRequest?.status != AccessRequestStatus.PENDING) {
                    callbackData("Подать заявку") { requestAccessCallback }
                }
                if (supportUrl.isNotBlank()) {
                    url("Поддержка") { supportUrl }
                }
            }
            return MenuPageState(text = text, keyboard = keyboard)
        }

        return when (page) {
            MenuPage.MAIN -> {
                val keyboard = inlineKeyboardMarkup {
                    callbackData("Статус доступа") { menuStatusCallback }
                    callbackData("Моя ссылка") { menuLinkCallback }
                }
                MenuPageState(
                    text = "VPN-меню. Выберите нужный раздел.",
                    keyboard = keyboard
                )
            }
            MenuPage.STATUS -> {
                val keyboard = inlineKeyboardMarkup {
                    callbackData("Назад") { menuMainCallback }
                }
                MenuPageState(
                    text = buildStatusText(snapshot),
                    keyboard = keyboard
                )
            }
            MenuPage.LINK -> {
                val keyboard = inlineKeyboardMarkup {
                    url("Инструкция") { instructionUrl }
                    callbackData("Назад") { menuMainCallback }
                }
                MenuPageState(
                    text = "Ваша индивидуальная ссылка:",
                    keyboard = keyboard
                )
            }
        }
    }

    private fun buildStatusText(snapshot: AccessSnapshot): String {
        val user = snapshot.savedUser
        return when {
            snapshot.hasPrivilegedAccess ->
                "Доступ активен по системной роли ${user.role.name}."
            user.manualAccessType == ManualAccessType.THREE_MONTHS && snapshot.hasManualAccess ->
                "Ручной доступ активен до ${user.manualAccessUntil?.format(dateFormatter)}."
            user.manualAccessType == ManualAccessType.FOREVER && snapshot.hasManualAccess ->
                "Ручной доступ активен бессрочно."
            snapshot.hasChatAccess ->
                "Доступ активен через одобренный чат. Он сохранится, пока вы остаетесь участником хотя бы одного одобренного чата."
            snapshot.latestRequest?.status == AccessRequestStatus.PENDING ->
                "Заявка ожидает рассмотрения."
            snapshot.latestRequest?.status == AccessRequestStatus.REJECTED ->
                "Последняя заявка отклонена."
            else ->
                "Доступ не активен."
        }
    }

    private fun ensureUser(tgUser: User, bot: TelegramBot): AppUser {
        return try {
            val avatarUrl = fetchAvatarUrl(tgUser, bot)
            authenticationService.upsertTelegramUser(
                telegramId = tgUser.id,
                firstName = tgUser.firstName,
                lastName = tgUser.lastName,
                username = tgUser.username,
                avatarUrl = avatarUrl,
            )
        } catch (e: Exception) {
            log.warn("Telegram upsert failed for telegramId={}", tgUser.id, e)
            throw e
        }
    }

    private fun isGroupRegistrationCommand(commandText: String): Boolean {
        val normalizedUsername = botUsername.trim().removePrefix("@")
        if (normalizedUsername.isBlank()) {
            log.warn("TELEGRAM_USERNAME is blank. Group /start command will be ignored.")
            return false
        }

        return commandText.equals("/start@$normalizedUsername", ignoreCase = true)
    }

    private fun fetchAvatarUrl(tgUser: User, bot: TelegramBot): String? {
        return try {
            val photos = runBlocking {
                getUserProfilePhotos(tgUser, 0, 1).sendReturning(bot).getOrNull()
            }
            val firstSet = photos?.photos?.firstOrNull() ?: return null
            val photo = firstSet.maxByOrNull { it.fileSize ?: 0 } ?: return null
            val file = runBlocking {
                getFile(photo.fileId).sendReturning(bot).getOrNull()
            } ?: return null
            bot.getFileDirectUrl(file)
        } catch (e: Exception) {
            log.debug("Avatar fetch failed for telegramId={}", tgUser.id, e)
            null
        }
    }

    private fun escapeHtml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private data class AccessSnapshot(
        val savedUser: AppUser,
        val hasPrivilegedAccess: Boolean,
        val hasManualAccess: Boolean,
        val hasChatAccess: Boolean,
        val latestRequest: AccessRequest?,
    ) {
        val hasAnyAccess: Boolean
            get() = hasPrivilegedAccess || hasManualAccess || hasChatAccess
    }

    private enum class MenuPage {
        MAIN,
        STATUS,
        LINK
    }

    private data class MenuPageState(
        val text: String,
        val keyboard: eu.vendeli.tgbot.types.keyboard.InlineKeyboardMarkup,
    )
}
