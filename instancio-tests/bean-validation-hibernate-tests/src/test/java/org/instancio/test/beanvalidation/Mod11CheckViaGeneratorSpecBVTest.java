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
package org.instancio.test.beanvalidation;

import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.instancio.test.pojo.beanvalidation.Mod11CheckBV.WithDefaults;
import org.instancio.test.pojo.beanvalidation.Mod11CheckBV.WithStartEndAndCheckDigitIndices;
import org.instancio.test.pojo.beanvalidation.Mod11CheckBV.WithStartEndIndices;
import org.instancio.test.support.tags.Feature;
import org.instancio.test.support.tags.FeatureTag;
import org.instancio.test.util.HibernateValidatorUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.instancio.test.support.util.Constants.SAMPLE_SIZE_DDD;

@FeatureTag(Feature.BEAN_VALIDATION)
@ExtendWith(InstancioExtension.class)
class Mod11CheckViaGeneratorSpecBVTest {

    @Test
    void withDefaults() {
        final Stream<WithDefaults> results = Instancio.of(WithDefaults.class)
                .generate(field(WithDefaults::getValue), gen -> gen.checksum().mod11())
                .stream()
                .limit(SAMPLE_SIZE_DDD);

        assertThat(results).hasSize(SAMPLE_SIZE_DDD).allSatisfy(HibernateValidatorUtil::assertValid);
    }

    @Test
    void withStartEndIndices() {
        final Stream<WithStartEndIndices> results = Instancio.of(WithStartEndIndices.class)
                .generate(field("value0"), gen -> gen.checksum().mod11().startIndex(0).endIndex(7))
                .generate(field("value1"), gen -> gen.checksum().mod11().startIndex(5).endIndex(10))
                .generate(field("value2"), gen -> gen.checksum().mod11().startIndex(1).endIndex(21).treatCheck10As('A').treatCheck11As('B'))
                .generate(field("value3"), gen -> gen.checksum().mod11().startIndex(100).endIndex(105))
                .generate(field("value4"), gen -> gen.checksum().mod11().startIndex(50).endIndex(60).threshold(5).treatCheck10As('C').treatCheck11As('N'))
                .generate(field("value5"), gen -> gen.checksum().mod11().startIndex(10).endIndex(30).threshold(9).leftToRight())
                .stream()
                .limit(SAMPLE_SIZE_DDD);

        assertThat(results).hasSize(SAMPLE_SIZE_DDD).allSatisfy(HibernateValidatorUtil::assertValid);
    }

    @Test
    void withStartEndAndCheckDigitIndices() {
        final Stream<WithStartEndAndCheckDigitIndices> results = Instancio.of(WithStartEndAndCheckDigitIndices.class)
                .generate(field("value0"), gen -> gen.checksum().mod11().startIndex(0).endIndex(7).checkDigitIndex(8))
                .generate(field("value1"), gen -> gen.checksum().mod11().startIndex(5).endIndex(10).checkDigitIndex(3))
                .generate(field("value2"), gen -> gen.checksum().mod11().startIndex(1).endIndex(21).checkDigitIndex(0).treatCheck10As('A').treatCheck11As('B'))
                .generate(field("value3"), gen -> gen.checksum().mod11().startIndex(100).endIndex(105).checkDigitIndex(150))
                .generate(field("value4"), gen -> gen.checksum().mod11().startIndex(50).endIndex(60).checkDigitIndex(70).threshold(5).treatCheck10As('C').treatCheck11As('N'))
                .generate(field("value5"), gen -> gen.checksum().mod11().startIndex(10).endIndex(30).checkDigitIndex(31).threshold(9).leftToRight())
                .stream()
                .limit(SAMPLE_SIZE_DDD);

        assertThat(results).hasSize(SAMPLE_SIZE_DDD).allSatisfy(HibernateValidatorUtil::assertValid);
    }
}
