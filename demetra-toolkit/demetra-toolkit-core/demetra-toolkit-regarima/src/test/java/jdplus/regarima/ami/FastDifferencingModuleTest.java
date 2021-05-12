/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regarima.ami;

import jdplus.modelling.FastDifferencingModule;
import demetra.data.Data;
import demetra.timeseries.TsData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FastDifferencingModuleTest {

    public FastDifferencingModuleTest() {
    }

    public static void testInsee() {
        TsData[] insee = Data.insee();
        FastDifferencingModule d = FastDifferencingModule.builder()
                .centile(75)
                .build();

        for (int i = 0; i < insee.length; ++i) {
            int[] del = d.process(insee[i].getValues().fn(x->Math.log(x)), new int[]{12, 1}, null);
            System.out.print(del[0]);
            System.out.print('\t');
            System.out.println(del[1]);
        }
    }
    
    public static void main(String[] arg){
        testInsee();
    }

}
