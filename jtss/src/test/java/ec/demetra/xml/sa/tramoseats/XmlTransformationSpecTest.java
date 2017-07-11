/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.demetra.xml.sa.tramoseats;

import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Validator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.xml.sax.SAXException;
import xml.Schemas;
import xml.TestErrorHandler;

/**
 *
 * @author Jean Palate
 */
public class XmlTransformationSpecTest {

    private static final String FILE = "c:\\localdata\\trs_transformationspec.xml";

    public XmlTransformationSpecTest() {
    }

    @Test
    @Ignore
    public void testMarshal() throws FileNotFoundException, JAXBException, IOException {

        TramoSpecification spec = TramoSpecification.TRfull;
        TransformSpec tspec = spec.getTransform().clone();
        tspec.setFunction(DefaultTransformationType.Auto);
        tspec.setFct(1);
        
        XmlTransformationSpec xspec=XmlTransformationSpec.MARSHALLER.marshal(tspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        FileOutputStream ostream = new FileOutputStream(FILE);
        try (OutputStreamWriter writer = new OutputStreamWriter(ostream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xspec, writer);
            writer.flush();
        }

        XmlTransformationSpec rslt = null;
        FileInputStream istream = new FileInputStream(FILE);
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (XmlTransformationSpec) unmarshaller.unmarshal(reader);
            TransformSpec nspec = new TransformSpec();
            XmlTransformationSpec.UNMARSHALLER.unmarshal(rslt, nspec);

            assertTrue(nspec.equals(tspec));
        }
    }

}
