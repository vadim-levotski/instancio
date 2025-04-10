/*
 * Copyright 2022-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.instancio.creation.misc;

import org.instancio.test.support.pojo.misc.MultipleClassesWithId;
import org.instancio.testsupport.templates.CreationTestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MultipleClassesWithIdCreationTest extends CreationTestTemplate<MultipleClassesWithId<UUID>> {

    @Override
    protected void verify(final MultipleClassesWithId<UUID> result) {
        assertThat(result.getA().getId()).isNotNull();
        assertThat(result.getA().getB().getId()).isNotNull();

        final MultipleClassesWithId.C<UUID> c = result.getA().getC();
        assertThat(c.getId()).isNotNull();
        assertThat(c.getB().getId()).isNotNull();
        assertThat(c.getD().getId()).isNotNull();
    }
}
