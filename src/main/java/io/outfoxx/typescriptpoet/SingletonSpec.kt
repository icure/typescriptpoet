package io.outfoxx.typescriptpoet

class SingletonSpec
private constructor(
	builder: Builder,
) : TypeSpec<SingletonSpec, SingletonSpec.Builder>(builder) {
	companion object {
		private val ACCEPTED_MODIFIERS = setOf(
			Modifier.EXPORT,
		)
		private val ACCEPTED_PROPERTY_MODIFIERS = setOf(
			Modifier.READONLY,
		)

		@JvmStatic
		fun builder(name: String) = Builder(name)
	}

	override val name = builder.name
	val modifiers = builder.modifiers.toImmutableSet()
	val propertySpecs = builder.propertySpecs.toImmutableList()
	val functionSpecs = builder.functionSpecs.toImmutableList()
	val mixins = builder.mixins.toImmutableList()

	override fun emit(codeWriter: CodeWriter) {
		if (Modifier.EXPORT in modifiers) {
			codeWriter.emit("export ")
		}
		(modifiers - Modifier.EXPORT).also {
			require(it.size <= 1) { "Only one visibility modifier is allowed for a singleton" }
		}.forEach {
			codeWriter.emit(it.keyword)
			codeWriter.emit(" ")
		}
		codeWriter.emit("const ")
		codeWriter.emit(name)
		codeWriter.emit(": {\n")
		codeWriter.indent()
		propertySpecs.forEachIndexed { index, property ->
			if (index > 0) {
				codeWriter.emit(",\n")
			}
			emitPropertyType(codeWriter, property)
		}
		functionSpecs.forEachIndexed { index, func ->
			if (index > 0 || propertySpecs.isNotEmpty()) {
				codeWriter.emit(",\n")
			}
			emitFunctionType(codeWriter, func)
		}
		codeWriter.unindent()
		codeWriter.emit("\n}")
		mixins.forEach {
			codeWriter.emit(" & ")
			it.emit(codeWriter)
		}
		codeWriter.emit(" = {\n")
		codeWriter.indent()
		propertySpecs.forEachIndexed { index, property ->
			if (index > 0) {
				codeWriter.emit(",\n")
			}
			emitProperty(codeWriter, property)
		}
		functionSpecs.forEachIndexed { index, func ->
			if (index > 0 || propertySpecs.isNotEmpty()) {
				codeWriter.emit(",\n")
			}
			emitFunction(codeWriter, func)
		}
		codeWriter.unindent()
		codeWriter.emit("\n}\n")
	}

	private fun emitProperty(codeWriter: CodeWriter, propertySpec: PropertySpec) {
		codeWriter.emit(propertySpec.name)
		codeWriter.emit(": ")
		codeWriter.emitCode(propertySpec.initializer!!)
	}

	private fun emitPropertyType(codeWriter: CodeWriter, propertySpec: PropertySpec) {
		propertySpec.modifiers.forEach {
			require(it in ACCEPTED_PROPERTY_MODIFIERS) {
				"Modifier $it is not allowed for a singleton property"
			}
			codeWriter.emit(it.keyword)
			codeWriter.emit(" ")
		}
		codeWriter.emit(propertySpec.name)
		codeWriter.emit(": ")
		propertySpec.type.emit(codeWriter)
	}

	private fun emitFunction(codeWriter: CodeWriter, functionSpec: FunctionSpec) {
		TODO("Function support not yet implemented")
	}

	private fun emitFunctionType(codeWriter: CodeWriter, functionSpec: FunctionSpec) {
		TODO("Function support not yet implemented")
	}

	class Builder(
		name: String,
	) : TypeSpec.Builder<SingletonSpec, Builder>(name) {
		internal val modifiers = mutableListOf<Modifier>()
		internal val propertySpecs = mutableListOf<PropertySpec>()
		internal val functionSpecs = mutableListOf<FunctionSpec>()
		internal val mixins = mutableListOf<TypeName>()

		fun addModifiers(vararg modifiers: Modifier) = apply {
			modifiers.forEach {
				require(it in ACCEPTED_MODIFIERS) { "Modifier $it is not allowed for a singleton" }
			}
			this.modifiers += modifiers
		}

		fun addProperty(propertySpec: PropertySpec) = apply {
			require(propertySpec.initializer != null) {
				"Singleton properties must have initializers"
			}
			propertySpecs += propertySpec
		}

		fun addFunction(functionSpec: FunctionSpec) = apply {
			functionSpecs += functionSpec
		}

		fun addMixin(mixin: TypeName) = apply {
			mixins += mixin
		}

		override fun build(): SingletonSpec {
			return SingletonSpec(this)
		}
	}
}
