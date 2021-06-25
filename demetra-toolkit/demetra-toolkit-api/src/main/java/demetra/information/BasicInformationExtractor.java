/*
 * Copyright 2021 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.information;

import nbbrd.design.Development;
import demetra.util.WildCards;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * New implementation for JD3
 * @author Jean Palate
 * @param <S>
 */
@Development(status = Development.Status.Release)
public interface BasicInformationExtractor<S> {

    public static final char SEP = '.';
    public static final String STRSEP = new String(new char[]{SEP});

    public static String concatenate(String... s) {
        switch (s.length) {
            case 0:
                return "";
            case 1:
                return s[0];
            default:
                boolean first = true;
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < s.length; ++i) {
                    if (s[i] != null) {
                        if (!first) {
                            builder.append(SEP);
                        } else {
                            first = false;
                        }
                        builder.append(s[i]);
                    }
                }
                return builder.toString();
        }
    }

    void fillDictionary(String prefix, Map<String, Class> dic, boolean compact);

    boolean contains(String id);

    <T> T getData(S source, String id, Class<T> tclass);

    <T> void searchAll(S source, WildCards wc, Class<T> tclass, Map<String, T> map);

    static <S, Q> BasicInformationExtractor<S> extractor(final String name, final Class<Q> targetClass,
            final Function<S, Q> fn) {
        return new BasicInformationExtractors.AtomicExtractor<>(name, targetClass, fn);
    }

    static <S, Q> BasicInformationExtractor<S> delegate(final String name, final Class<Q> target, final Function<S, Q> fn) {
        return new BasicInformationExtractors.ExtractorDelegate<>(name, target, fn);
    }

    static <S, Q> BasicInformationExtractor<S> delegateArray(final String name, final int start, final int end, final Class<Q> target, final BiFunction<S, Integer, Q> fn) {
        return new BasicInformationExtractors.ArrayExtractorDelegate<>(name, start, end, target, fn);
    }

    static <S, Q> BasicInformationExtractor<S> array(final String name, final int start, final int end,
            final Class<Q> targetClass, final BiFunction<S, Integer, Q> fn) {
        return new BasicInformationExtractors.ArrayExtractor<>(name, start, end, targetClass, fn);
    }

    static <S, Q> BasicInformationExtractor<S> array(final String name, final int defparam,
            final Class<Q> targetClass, final BiFunction<S, Integer, Q> fn) {
        return new BasicInformationExtractors.ArrayExtractor<>(name, defparam, defparam, targetClass, fn);
    }

}
