package com.cjcrafter.weaponmechanicssupport.lock

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

abstract class Lock(
    val name: String,
    val forumTag: String = name,
) {
    fun getTag(forum: ForumChannel) = forum.availableTags.find { tag -> tag.name == forumTag }

    fun applyLock(thread: ThreadChannel) {
        val tags = LinkedHashSet(thread.appliedTags)
        val newTag = getTag(thread.parentChannel as ForumChannel)!!

        if (tags.contains(newTag)) {
            println("Thread $thread already has lock $name... skipping")
            return
        }

        tags.add(newTag)
        thread.manager.setAppliedTags(tags).queue()
        sendLockAlert(thread)
        println("Applied lock $name to $thread")
    }

    abstract fun sendLockAlert(thread: ThreadChannel)

    abstract fun shouldUnlock(forum: ForumChannel, thread: ThreadChannel, message: Message, event: MessageReceivedEvent): Boolean

    abstract fun stillLockedAlert(message: Message)

    abstract fun removeLock(thread: ThreadChannel)
}