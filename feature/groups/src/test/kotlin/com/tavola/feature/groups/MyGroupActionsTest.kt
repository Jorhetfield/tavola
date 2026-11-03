package com.tavola.feature.groups

import com.tavola.core.testing.fake.FakeAuthRepository
import com.tavola.core.testing.fake.FakeGroupRepository
import com.tavola.core.testing.rule.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class MyGroupActionsTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    @Test
    fun `every action of the screen is wired to the ViewModel, none is left doing nothing`() {
        val defaults = MyGroupActions()
        val wired = MyGroupViewModel(FakeGroupRepository(), FakeAuthRepository()).toActions()
        val actionGetters = MyGroupActions::class.java.methods.filter { method ->
            method.name.startsWith("get") && method.parameterCount == 0 && Function::class.java.isAssignableFrom(method.returnType)
        }

        assertTrue(actionGetters.isNotEmpty(), "No actions found: the check itself is broken")
        actionGetters.forEach { getter ->
            assertNotSame(getter.invoke(defaults), getter.invoke(wired), "${getter.name} is still the default action that does nothing")
        }
    }
}
