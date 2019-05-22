/**
 * Copyright (C) 2019+ furplag (https://github.com/furplag)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.furplag.sandbox.domino.misc.generic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.seasar.doma.Entity;

import jp.furplag.sandbox.domino.misc.origin.Origin;

class EntityInspectorTest {

  public static class NotAnEntity implements Origin {}

  @Entity
  public static class Zero implements Origin {}

  @Test
  void test() {
    // @formatter:off
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> Inspector.defaultInspector(null))
      , () -> assertThrows(IllegalArgumentException.class, () -> Inspector.defaultInspector(Origin.class))
      , () -> assertThrows(IllegalArgumentException.class, () -> Inspector.defaultInspector(NotAnEntity.class))
      , () -> assertEquals("Zero", Inspector.defaultInspector(EntityInspectorTest.Zero.class).getName())
    );
    // @formatter:on
  }

  @Test
  void paintItGreen() {
    // @formatter:off
    assertAll(
        () -> assertThrows(IllegalArgumentException.class, () -> new Origin() {}.inspector())
      , () -> assertThrows(IllegalArgumentException.class, () -> Inspector.defaultInspector(Origin.class))
      , () -> assertThrows(IllegalArgumentException.class, () -> Inspector.defaultInspector(NotAnEntity.class))
      , () -> assertEquals(Inspector.defaultInspector(EntityInspectorTest.Zero.class).getName(), new Zero().inspector().getName())
    );
    // @formatter:on
  }
}
