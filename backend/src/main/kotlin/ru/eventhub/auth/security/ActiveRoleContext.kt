package ru.eventhub.auth.security

import ru.eventhub.user.model.RoleName

object ActiveRoleContext {
    private val activeRoleHolder = ThreadLocal<RoleName>()

    fun set(roleName: RoleName) {
        activeRoleHolder.set(roleName)
    }

    fun get(): RoleName? {
        return activeRoleHolder.get()
    }

    fun clear() {
        activeRoleHolder.remove()
    }
}
