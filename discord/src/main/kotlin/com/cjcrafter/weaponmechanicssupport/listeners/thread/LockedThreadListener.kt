package com.cjcrafter.weaponmechanicssupport.listeners.thread

import com.cjcrafter.weaponmechanicssupport.lock.LatestLogLock
import com.cjcrafter.weaponmechanicssupport.lock.Lock
import com.cjcrafter.weaponmechanicssupport.lock.YamlLock
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.concurrent.TimeUnit

class LockedThreadListener : ThreadMessageListener {

    private val locks = listOf(
        LatestLogLock(),
        YamlLock()
    )

    override fun onThreadMessage(
        forum: ForumChannel,
        thread: ThreadChannel,
        message: Message,
        event: MessageReceivedEvent
    ) {
        // other people can still comment normally to provide context or help
        if (thread.ownerId != message.member?.id)
            return

        val stillLocked = mutableListOf<Lock>()
        val unlocks = mutableListOf<Lock>()

        for (lock in locks) {
            if (HashSet(thread.appliedTags).any { tag -> tag.name == lock.forumTag }) {
                val shouldUnlock = lock.shouldUnlock(forum, thread, message, event)
                if (shouldUnlock)
                    unlocks.add(lock)
                else
                    stillLocked.add(lock)
            }
        }

        // This if statement will send an alert to the user iff they have not
        // 'fixed' at least one of the locks. If they have fixed at least one,
        // no alerts are sent.
        if (unlocks.isNotEmpty()) {
            for (lock in unlocks) {
                lock.removeLock(thread)
            }
        } else {
            for (lock in stillLocked) {
                lock.stillLockedAlert(message)
            }

            // Delete the message the user sent, since their Thread is locked.
            if (stillLocked.isNotEmpty())
                message.delete().queueAfter(3, TimeUnit.SECONDS) { /* ignore deleted messages */ }
        }
    }
}