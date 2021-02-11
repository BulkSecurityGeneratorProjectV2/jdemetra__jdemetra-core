/*
 * Copyright 2021 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.x13.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.regarima.EasterSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class EasterProto {

    public void fill(EasterSpec spec, X13Protos.RegArimaSpec.EasterSpec.Builder builder) {
        builder.setType(X13ProtosUtility.convert(spec.getType()))
                .setDuration(spec.getDuration())
                .setTest(X13ProtosUtility.convert(spec.getTest()));
    }

    public X13Protos.RegArimaSpec.EasterSpec convert(EasterSpec spec) {
        X13Protos.RegArimaSpec.EasterSpec.Builder builder = X13Protos.RegArimaSpec.EasterSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public byte[] toBuffer(EasterSpec spec) {
        return convert(spec).toByteArray();
    }

    public EasterSpec convert(X13Protos.RegArimaSpec.EasterSpec spec) {
        return EasterSpec.builder()
                .automatic(spec.getDuration() == 0)
                .duration(spec.getDuration())
                .type(X13ProtosUtility.convert(spec.getType()))
                .test(X13ProtosUtility.convert(spec.getTest()))
                .build();

    }

    public EasterSpec of(byte[] bytes) throws InvalidProtocolBufferException {
        X13Protos.RegArimaSpec.EasterSpec spec = X13Protos.RegArimaSpec.EasterSpec.parseFrom(bytes);
        return convert(spec);
    }

}
