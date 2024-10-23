/*
 * PaperVision
 * Copyright (C) 2024 Sebastian Erives, deltacv

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.deltacv.papervision.codegen.language.interpreted

import io.github.deltacv.papervision.codegen.Visibility
import io.github.deltacv.papervision.codegen.build.*
import io.github.deltacv.papervision.codegen.csv
import io.github.deltacv.papervision.codegen.language.LanguageBase

object JavascriptLanguage : LanguageBase(genInClass = false, optimizeImports = false) {

    override val Parameter.string get() = name

    override fun instanceVariableDeclaration(
        vis: Visibility,
        variable: Variable,
        label: String?,
        isStatic: Boolean,
        isFinal: Boolean
    ) = Pair(
        if(label != null) {
            "label(\"${variable.name}\", \"$label\")"
        } else null,
        "var ${variable.name} = ${variable.variableValue.value}${semicolonIfNecessary()}"
    )

    override fun localVariableDeclaration(
        variable: Variable,
        isFinal: Boolean
    ) = instanceVariableDeclaration(Visibility.PUBLIC, variable).second

    override fun instanceVariableSetDeclaration(variable: Variable, v: Value) = "${variable.name} = ${v.value!!}${semicolonIfNecessary()}"

    override fun methodDeclaration(
        vis: Visibility,
        returnType: Type,
        name: String,
        vararg parameters: Parameter,
        isStatic: Boolean,
        isFinal: Boolean,
        isSynchronized: Boolean,
        isOverride: Boolean
    ): Pair<String?, String> {
        return Pair("",
            "function $name(${parameters.csv()})"
        )
    }

    override fun foreachLoopDeclaration(variable: Value, iterable: Value) =
        "for(var ${variable.value} in ${iterable.value})"

    override fun classDeclaration(
        vis: Visibility,
        name: String,
        body: Scope,
        extends: Type?,
        vararg implements: Type,
        isStatic: Boolean,
        isFinal: Boolean
    ): String {
        throw UnsupportedOperationException("Class declarations are not supported in JavaScript")
    }

    override fun enumClassDeclaration(name: String, vararg values: String): String {
        val builder = StringBuilder()

        for((i, value) in values.withIndex()) {
            builder.append("$value: $i")
            if(i < values.size - 1) {
                builder.append(",")
            }
            builder.appendLine()
        }

        return """var $name = {  
            |${builder.toString().trim()}
            |}""".trimMargin()
    }

    override fun importDeclaration(importPath: String, className: String) = "importClass($importPath.$className)${semicolonIfNecessary()}"

    override fun new(type: Type, vararg parameters: Value) = ConValue(
        type, "new ${type.className}(${parameters.csv()})"
    )

}