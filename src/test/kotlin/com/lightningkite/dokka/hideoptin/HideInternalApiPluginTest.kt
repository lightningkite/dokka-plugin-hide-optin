package com.lightningkite.dokka.hideoptin

import org.jetbrains.dokka.base.testApi.testRunner.BaseAbstractTest
import org.junit.Test
import kotlin.test.assertEquals

class HideInternalApiPluginTest : BaseAbstractTest() {

    @Test
    fun `should hide annotated functions`() {
        val configuration = dokkaConfiguration {
            sourceSets {
                sourceSet {
                    sourceRoots = listOf("src/main/kotlin/basic/Test.kt")
                }
            }
        }
        val hideInternalPlugin = HideInternalApiPlugin()

        testInline(
            """
            |/src/main/kotlin/basic/Test.kt
            |package org.jetbrains.dokka.internal.test
            |
            |annotation class Internal
            |
            |fun shouldBeVisible() {}
            |
            |@Internal
            |fun shouldBeExcludedFromDocumentation() {}
        """.trimMargin(),
            configuration = configuration,
            pluginOverrides = listOf(hideInternalPlugin)
        ) {
            preMergeDocumentablesTransformationStage = { modules ->
                val testModule = modules.single { it.name == "root" }
                val testPackage = testModule.packages.single { it.name == "org.jetbrains.dokka.internal.test" }

                val packageFunctions = testPackage.functions
                assertEquals(1, packageFunctions.size)
                assertEquals("shouldBeVisible", packageFunctions[0].name)
            }
        }
    }
}