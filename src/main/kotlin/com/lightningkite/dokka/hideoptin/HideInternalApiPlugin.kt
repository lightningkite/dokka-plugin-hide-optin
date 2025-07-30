package com.lightningkite.dokka.hideoptin

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.templating.parseJson
import org.jetbrains.dokka.base.transformers.documentables.SuppressedByConditionDocumentableFilterTransformer
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.model.properties.*
import org.jetbrains.dokka.plugability.*

class HideInternalApiPlugin : DokkaPlugin() {
    val myFilterExtension by extending {
        plugin<DokkaBase>().preMergeDocumentableTransformer providing ::HideInternalApiTransformer
    }

    @OptIn(DokkaPluginApiPreview::class)
    override fun pluginApiPreviewAcknowledgement(): PluginApiPreviewAcknowledgement = PluginApiPreviewAcknowledgement
}

data class AnnotationsToHide(
    val fqns: List<String> = listOf(),
    val names: List<String> = listOf(),
)

class HideInternalApiTransformer(context: DokkaContext) : SuppressedByConditionDocumentableFilterTransformer(context) {

    val toSuppress = context.configuration.pluginsConfiguration
        .find { it.fqPluginName == "com.lightningkite.dokka.hideoptin.HideInternalApiPlugin" }
        ?.values
        ?.let { parseJson<AnnotationsToHide>(it) }
        ?: AnnotationsToHide(emptyList())

    override fun shouldBeSuppressed(d: Documentable): Boolean {
        return (d as? WithExtraProperties<*>)
            ?.extra
            ?.allOfType<Annotations>()
            ?.asSequence()
            ?.flatMap { it.directAnnotations.values.flatten() }
            ?.any { isInternalAnnotation(it) }
            ?: false
    }

    private fun isInternalAnnotation(annotation: Annotations.Annotation): Boolean {
        return toSuppress.fqns.any { fqn -> annotation.dri.run { "$packageName.$classNames" } == fqn }
                || toSuppress.names.any { name -> annotation.dri.classNames == name }
                || annotation.dri.classNames == "SuppressDokka"
                || annotation.dri.classNames?.let {
                 it.startsWith("Internal") && it.endsWith("Api")
        } == true
    }
}