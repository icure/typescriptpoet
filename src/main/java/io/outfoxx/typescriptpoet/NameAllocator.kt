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

import java.util.UUID

/**
 * Assigns TypeScript identifier names to avoid collisions, keywords, and invalid characters. To use,
 * first create an instance and allocate all of the names that you need. Typically this is a
 * mix of user-supplied names and constants:
 *
 * ```
 * val nameAllocator = NameAllocator()
 * for (property in properties) {
 *   nameAllocator.newName(property.name, property)
 * }
 * nameAllocator.newName("sb", "string builder")
 * ```
 *
 * Pass a unique tag object to each allocation. The tag scopes the name, and can be used to look up
 * the allocated name later. Typically the tag is the object that is being named. In the above
 * example we use `property` for the user-supplied property names, and `"string builder"` for our
 * constant string builder.
 *
 * Once we've allocated names we can use them when generating code:
 *
 * ```
 * val builder = FunctionSpec.name("toString")
 *   .returns(DeclaredTypeName.STRING)
 *
 * builder.addStatement("var %N = ''", nameAllocator.get("string builder"))
 *
 * for (property in properties) {
 *   builder.addStatement("%N += '${%N}'", nameAllocator.get("string builder"), nameAllocator.get(property))
 * }
 * builder.addStatement("return %N", nameAllocator.get("string builder"))
 *
 * return builder.build()
 * ```
 *
 * The above code generates unique names if presented with conflicts. Given user-supplied properties
 * with names `ab` and `sb` this generates the following:
 *
 * ```
 * toString(): String {
 *   var sb_ = '';
 *   sb_ += '${ab}';
 *   sb_ += '${sb}';
 *   return sb_;
 * }
 * ```
 *
 * The underscore is appended to `sb` to avoid conflicting with the user-supplied `sb` property.
 * Underscores are also prefixed for names that start with a digit, and used to replace name-unsafe
 * characters like space or dash.
 *
 * When dealing with multiple independent inner scopes, use a [copy][NameAllocator.copy] of the
 * NameAllocator used for the outer scope to further refine name allocation for a specific inner
 * scope.
 */
class NameAllocator private constructor(
  private val allocatedNames: MutableSet<String>,
  private val tagToName: MutableMap<Any, String>,
) {
  constructor() : this(mutableSetOf(), mutableMapOf())

  /**
   * Return a new name using `suggestion` that will not be a TypeScript identifier or clash with other
   * names. The returned value can be queried multiple times by passing `tag` to
   * [NameAllocator.get].
   */
  @JvmOverloads fun newName(
    suggestion: String,
    tag: Any = UUID.randomUUID().toString(),
  ): String {
    var result = toTypeScriptIdentifier(suggestion)
    while (result.isKeyword || !allocatedNames.add(result)) {
      result += "_"
    }

    val replaced = tagToName.put(tag, result)
    if (replaced != null) {
      tagToName[tag] = replaced // Put things back as they were!
      throw IllegalArgumentException("tag $tag cannot be used for both '$replaced' and '$result'")
    }

    return result
  }

  /** Retrieve a name created with [NameAllocator.newName]. */
  operator fun get(tag: Any): String = requireNotNull(tagToName[tag]) { "unknown tag: $tag" }

  /**
   * Create a deep copy of this NameAllocator. Useful to create multiple independent refinements
   * of a NameAllocator to be used in the respective definition of multiples, independently-scoped,
   * inner code blocks.
   *
   * @return A deep copy of this NameAllocator.
   */
  fun copy(): NameAllocator {
    return NameAllocator(allocatedNames.toMutableSet(), tagToName.toMutableMap())
  }

  fun tagsToNames() = tagToName.toImmutableMap()
}

/**
 * TODO Replace Java identifier checks with valid TypeScript code point checks
 */
private fun toTypeScriptIdentifier(suggestion: String) = buildString {
  var i = 0
  while (i < suggestion.length) {
    val codePoint = suggestion.codePointAt(i)
    if (i == 0 &&
      !Character.isJavaIdentifierStart(codePoint) &&
      Character.isJavaIdentifierPart(codePoint)
    ) {
      append("_")
    }

    val validCodePoint: Int = if (Character.isJavaIdentifierPart(codePoint)) {
      codePoint
    } else {
      '_'.code
    }
    appendCodePoint(validCodePoint)
    i += Character.charCount(codePoint)
  }
}
