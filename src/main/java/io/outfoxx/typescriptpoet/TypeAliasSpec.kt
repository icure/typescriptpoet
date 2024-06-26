/*
 * Copyright 2017 Outfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.outfoxx.typescriptpoet

/** A generated typealias declaration */
class TypeAliasSpec
private constructor(
  builder: Builder,
) : TypeSpec<TypeAliasSpec, TypeAliasSpec.Builder>(builder) {

  override val name = builder.name
  val type = builder.type
  val tsDoc = builder.tsDoc.build()
  val modifiers = builder.modifiers.toImmutableSet()
  val typeVariables = builder.typeVariables.toImmutableList()

  override fun emit(codeWriter: CodeWriter) {
    codeWriter.emitTSDoc(tsDoc)
    codeWriter.emitModifiers(modifiers)
    codeWriter.emitCode(CodeBlock.of("type %L", name))
    codeWriter.emitTypeVariables(typeVariables)
    codeWriter.emitCode(CodeBlock.of(" = %T", type))
    codeWriter.emit(";\n")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null) return false
    if (javaClass != other.javaClass) return false
    return toString() == other.toString()
  }

  override fun hashCode() = toString().hashCode()

  override fun toString() = buildCodeString { emit(this) }

  fun toBuilder(): Builder {
    val builder = Builder(name, type)
    builder.tsDoc.add(tsDoc)
    builder.modifiers += modifiers
    builder.typeVariables += typeVariables
    return builder
  }

  class Builder internal constructor(
    name: String,
    internal val type: TypeName,
  ) : TypeSpec.Builder<TypeAliasSpec, Builder>(name) {

    internal val tsDoc = CodeBlock.builder()
    internal val modifiers = mutableSetOf<Modifier>()
    internal val typeVariables = mutableSetOf<TypeName.TypeVariable>()

    init {
      require(name.isName) { "not a valid name: $name" }
    }

    fun addTSDoc(format: String, vararg args: Any) = apply {
      tsDoc.add(format, *args)
    }

    fun addTSDoc(block: CodeBlock) = apply {
      tsDoc.add(block)
    }

    fun addModifiers(vararg modifiers: Modifier) = apply {
      modifiers.forEach(this::addModifier)
    }

    fun addModifier(modifier: Modifier) {
      require(modifier in setOf(Modifier.EXPORT)) {
        "unexpected typealias modifier $modifier"
      }
      this.modifiers.add(modifier)
    }

    fun addTypeVariables(typeVariables: Iterable<TypeName.TypeVariable>) = apply {
      this.typeVariables += typeVariables
    }

    fun addTypeVariable(typeVariable: TypeName.TypeVariable) = apply {
      typeVariables += typeVariable
    }

    override fun build() = TypeAliasSpec(this)
  }

  companion object {

    @JvmStatic
    fun builder(name: String, type: TypeName) = Builder(
      name,
      type,
    )
  }
}
