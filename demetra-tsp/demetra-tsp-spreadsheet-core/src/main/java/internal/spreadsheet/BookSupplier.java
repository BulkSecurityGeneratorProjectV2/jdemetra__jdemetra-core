/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.spreadsheet;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.BookFactoryLoader;
import java.io.File;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
public interface BookSupplier {

    Book.@Nullable Factory getFactory(@NonNull File file);

    default boolean hasFactory(@NonNull File file) {
        return getFactory(file) != null;
    }

    @NonNull
    static BookSupplier usingServiceLoader() {
        return new BookSupplier() {
            @Override
            public Book.Factory getFactory(File file) {
                for (Book.Factory o : BookFactoryLoader.get()) {
                    if (o.canLoad() && o.accept(file)) {
                        return o;
                    }
                }
                return null;
            }
        };
    }
}