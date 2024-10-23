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

package io.github.deltacv.papervision.codegen.language.jvm

import io.github.deltacv.papervision.codegen.Visibility
import io.github.deltacv.papervision.codegen.build.*
import io.github.deltacv.papervision.codegen.build.type.JavaTypes
import io.github.deltacv.papervision.codegen.build.type.KotlinTypes
import io.github.deltacv.papervision.codegen.csv
import io.github.deltacv.papervision.codegen.language.LanguageBase

object KotlinLanguage : LanguageBase(usesSemicolon = false) {

    init {
        mutableExcludedImports.add(KotlinTypes.Unit)
    }

    override val BooleanType get() = KotlinTypes.Boolean

    override val IntType get() = KotlinTypes.Int
    override val LongType get() = KotlinTypes.Long
    override val FloatType get() = KotlinTypes.Float
    override val DoubleType get() = KotlinTypes.Double

    override val VoidType get() = KotlinTypes.Unit

    override val Parameter.string get() = "$name: ${type.shortNameWithGenerics}"

    override fun castValue(value: Value, castTo: Type) = ConValue(castTo, "($value as ${castTo.shortNameWithGenerics})")

    override fun instanceVariableDeclaration(
        vis: Visibility,
        variable: Variable,
        label: String?,
        isStatic: Boolean,
        isFinal: Boolean
    ): Pair<String?, String> {
        val ending = if(variable.variableValue.value != null) "= ${variable.variableValue.value}" else ""

        var modifiers = if(isFinal) "val" else "var"
        if(vis != Visibility.PUBLIC) {
            modifiers = "${vis.name.lowercase()} " + modifiers
        }

        return Pair(
            if(label != null) {
                variable.additionalImports(JavaTypes.LabelAnnotation)
                "@Label(name = \"$label\")"
            } else null,
            "$modifiers ${variable.name}: ${variable.type.shortNameWithGenerics} $ending"
        )
    }

    override fun localVariableDeclaration(variable: Variable, isFinal: Boolean): String {
        val ending = if(variable.variableValue.value != null) " = ${variable.variableValue.value}" else ""

        return "${if(isFinal) "val" else "var"} ${variable.name}: ${variable.type.shortNameWithGenerics}$ending"
    }

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
        val visibility = if(vis != Visibility.PUBLIC) "${vis.name.lowercase()} " else ""
        val open = if(!isFinal) "open " else ""
        val returnTypeStr = if(returnType != VoidType) ": ${returnType.className}" else ""

        return Pair(
            if(isSynchronized) "@Synchronized" else "",
            "${if(isOverride) "override " else ""}$visibility${open}fun $name(${parameters.csv()})$returnTypeStr"
        )
    }

    override fun foreachLoopDeclaration(variable: Value, iterable: Value) =
        "for(${variable.value} in ${iterable.value})"


    override fun classDeclaration(
        vis: Visibility,
        name: String,
        body: Scope,
        extends: Type?,
        vararg implements: Type,
        isStatic: Boolean,
        isFinal: Boolean
    ): String {
        val visibility = if(vis != Visibility.PUBLIC) "${vis.name.lowercase()} " else ""
        val open = if(isFinal) "" else "open "

        val extendsAndImplementsList = mutableListOf<String>()

        if(extends != null) extendsAndImplementsList.add("${extends.shortNameWithGenerics}()")

        for(type in implements) {
            extendsAndImplementsList.add(type.shortNameWithGenerics)
        }

        val extendsAndImplements = if(extendsAndImplementsList.isNotEmpty()) {
            " : ${extendsAndImplementsList.toTypedArray().csv()}"
        } else ""

        return "$visibility${open}class $name$extendsAndImplements "
    }

    override fun enumClassDeclaration(name: String, vararg values: String) = "enum class $name { ${values.csv() } "

    override fun new(type: Type, vararg parameters: Value) = ConValue(
        type, "${type.shortNameWithGenerics}(${parameters.csv()})"
    )

}