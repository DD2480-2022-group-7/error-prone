/*
 * Copyright 2020 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.CompilationTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link IgnoredPureGetter}. */
@RunWith(JUnit4.class)
public final class IgnoredPureGetterTest {
  private final CompilationTestHelper helper =
      CompilationTestHelper.newInstance(IgnoredPureGetter.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringHelper =
      BugCheckerRefactoringTestHelper.newInstance(IgnoredPureGetter.class, getClass());

  @Test
  public void autoValue() {
    helper
        .addSourceLines(
            "A.java", //
            "import com.google.auto.value.AutoValue;",
            "@AutoValue",
            "abstract class A {",
            "  abstract int foo();",
            "}")
        .addSourceLines(
            "B.java", //
            "class B {",
            "  void test(A a) {",
            "    // BUG: Diagnostic contains:",
            "    a.foo();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void autoValueBuilder() {
    helper
        .addSourceLines(
            "Animal.java",
            "import com.google.auto.value.AutoValue;",
            "@AutoValue",
            "abstract class Animal {",
            "  abstract String name();",
            "  @AutoValue.Builder",
            "  abstract static class Builder {",
            "    abstract Builder setName(String name);",
            "    abstract Animal build();",
            "  }",
            "}")
        .addSourceLines(
            "B.java",
            "class B {",
            "  void test(Animal.Builder builder) {",
            // The setters are OK
            "    builder.setName(\"dog\");",
            "    // BUG: Diagnostic contains:",
            "    builder.build();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void autoValueStepBuilder() {
    helper
        .addSourceLines(
            "Animal.java",
            "import com.google.auto.value.AutoValue;",
            "@AutoValue",
            "public abstract class Animal {",
            "  public abstract String name();",
            "  public abstract int legs();",
            "  public interface NameStep { LegStep setName(String name); }",
            "  public interface LegStep { Build setLegs(int legs); }",
            "  public interface Build { Animal build(); }",
            "  @AutoValue.Builder",
            "  abstract static class Builder implements NameStep, LegStep, Build {}",
            "}")
        .addSourceLines(
            "B.java",
            "class B {",
            "  void test(Animal.Builder builder) {",
            // The setters are OK
            "    builder.setName(\"dog\");",
            "    builder.setLegs(4);",
            // We don't currently catch this, but maybe we should?
            "    builder.build();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void autoValue_secondFix() {
    refactoringHelper
        .addInputLines(
            "A.java", //
            "import com.google.auto.value.AutoValue;",
            "@AutoValue",
            "abstract class A {",
            "  abstract int foo();",
            "  static A of(int foo) {",
            "    return null;",
            "  }",
            "}")
        .expectUnchanged()
        .addInputLines(
            "B.java", //
            "class B {",
            "  void test() {",
            "    A.of(1).foo();",
            "  }",
            "}")
        .addOutputLines(
            "B.java", //
            "class B {",
            "  void test() {",
            "    A.of(1);",
            "  }",
            "}")
        .setFixChooser(FixChoosers.SECOND)
        .doTest();
  }

  @Test
  public void refactoringHelper() {
    refactoringHelper
        .addInputLines(
            "Test.java",
            "import com.google.errorprone.bugpatterns.proto.ProtoTest.TestProtoMessage;",
            "class Test {",
            "  void test(TestProtoMessage message) {",
            "    message.getMessage();",
            "    message.hasMessage();",
            "  }",
            "}")
        .addOutputLines(
            "Test.java",
            "import com.google.errorprone.bugpatterns.proto.ProtoTest.TestProtoMessage;",
            "class Test {",
            "  void test(TestProtoMessage message) {",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void repeatedFieldNotFlagged() {
    helper
        .addSourceLines(
            "Test.java",
            "import com.google.errorprone.bugpatterns.proto.ProtoTest.TestProtoMessage;",
            "class Test {",
            "  void test(TestProtoMessage message) {",
            "    message.getMultiField(1);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void protoReturnValueIgnored_noFinding() {
    helper
        .addSourceLines(
            "Test.java",
            "import com.google.errorprone.bugpatterns.proto.ProtoTest.TestProtoMessage;",
            "class Test {",
            "  void test(TestProtoMessage message) {",
            "    Object o = message.getMessage();",
            "    boolean b = message.hasMessage();",
            "  }",
            "}")
        .doTest();
  }
}
