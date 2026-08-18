package io.github.vinceglb.confettikit.core

import java.io.DataInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JavaCompatibilityTest {
    @Test
    fun libraryBytecodeTargetsJava17() {
        val classFile = assertNotNull(Party::class.java.getResourceAsStream("Party.class"))

        DataInputStream(classFile).use { input ->
            assertEquals(0xCAFEBABE.toInt(), input.readInt(), "Invalid JVM class file")
            input.readUnsignedShort() // Minor version
            assertEquals(JAVA_17_CLASS_FILE_VERSION, input.readUnsignedShort())
        }
    }

    private companion object {
        const val JAVA_17_CLASS_FILE_VERSION = 61
    }
}
