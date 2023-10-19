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

package io.outfoxx.typescriptpoet.test

import io.outfoxx.typescriptpoet.ClassSpec
import io.outfoxx.typescriptpoet.CodeBlock
import io.outfoxx.typescriptpoet.FileSpec
import io.outfoxx.typescriptpoet.FunctionSpec
import io.outfoxx.typescriptpoet.ModuleSpec
import io.outfoxx.typescriptpoet.SymbolSpec
import io.outfoxx.typescriptpoet.TypeName
import io.outfoxx.typescriptpoet.TypeName.Companion.STRING
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

@DisplayName("CodeWriter Tests")
class CodeWriterTests {

  @Test
  fun `test long line wrapping`() {
    val testFunc = FunctionSpec.builder("test")
      .returns(STRING)
      .addStatement("return X(aaaaa,%Wbbbbb,%Wccccc,%Wddddd,%Weeeee,%Wfffff,%Wggggg,%Whhhhh,%Wiiiii,%Wjjjjj,%Wkkkkk,%Wlllll,%Wmmmmm,%Wnnnnn,%Wooooo,%Wppppp,%Wqqqqq)")
      .build()

    MatcherAssert.assertThat(
      testFunc.toString(),
      CoreMatchers.equalTo(
        """
            function test(): string {
              return X(aaaaa, bbbbb, ccccc, ddddd, eeeee, fffff, ggggg, hhhhh, iiiii, jjjjj, kkkkk, lllll,
                  mmmmm, nnnnn, ooooo, ppppp, qqqqq);
            }

        """.trimIndent()
      )
    )
  }

  @Test
  fun `test CodeBlock with import`() {
    val typeName = TypeName.namedImport("X", "x")

    val testFunc = FunctionSpec.builder("test")
      .returns(typeName)
      .addCode(CodeBlock.of("return new %T();\n", typeName))
      .build()

    val testFileSpec = FileSpec.builder("test")
      .addFunction(testFunc)
      .build()

    val fileContent = StringWriter().use { writer ->
      testFileSpec.writeTo(writer)
      writer.toString()
    }

    MatcherAssert.assertThat(
      fileContent,
      CoreMatchers.equalTo(
        """
            import {X} from 'x';


            function test(): X {
              return new X();
            }

        """.trimIndent()
      )
    )
  }

  @Test
  fun `test import of nested object`() {
    val typeName = TypeName.namedImport("foo", "x").nested("Bar")

    val testFunc = FunctionSpec.builder("test")
      .returns(typeName)
      .addCode(CodeBlock.of("return new %T();\n", typeName))
      .build()

    val testFileSpec = FileSpec.builder("test")
      .addFunction(testFunc)
      .build()

    val fileContent = StringWriter().use { writer ->
      testFileSpec.writeTo(writer)
      writer.toString()
    }

    MatcherAssert.assertThat(
      fileContent,
      CoreMatchers.equalTo(
          """
              import {foo} from 'x';
              

              function test(): foo.Bar {
                return new foo.Bar();
              }

          """.trimIndent()
      )
    )
  }

  @Test
  fun `test import and local namespace with same top level name`() {
    val symbol1 = SymbolSpec.importsName("foo", "x").nested("bar1").nested("Test")
    val symbol2 = SymbolSpec.implicit("foo").nested("bar2").nested("Test")

    val testFileSpec = FileSpec.builder("test")
      .addModule(ModuleSpec.builder("foo")
        .addModule(ModuleSpec.builder("bar2")
          .addClass(ClassSpec.builder("Test").build())
          .addFunction(FunctionSpec.builder("test1")
            .returns(TypeName.standard(symbol1))
            .addCode(CodeBlock.of("return new %Q();\n", symbol1))
            .build())
          .addFunction(FunctionSpec.builder("test2")
            .returns(TypeName.standard(symbol2))
            .addCode(CodeBlock.of("return new %Q();\n", symbol2))
            .build())
          .build())
        .build())
      .build()

    val fileContent = StringWriter().use { writer ->
        testFileSpec.writeTo(writer)
        writer.toString()
    }

    MatcherAssert.assertThat(
      fileContent,
      CoreMatchers.equalTo(
          """
              import {foo as foo_} from 'x';
              
              
              namespace foo {
              
                namespace bar2 {
              
                  class Test {
                  }
              
                  function test1(): foo_.bar1.Test {
                    return new foo_.bar1.Test();
                  }
              
                  function test2(): Test {
                    return new Test();
                  }
              
                }
              
              }

          """.trimIndent()
      )
    )
  }

}
