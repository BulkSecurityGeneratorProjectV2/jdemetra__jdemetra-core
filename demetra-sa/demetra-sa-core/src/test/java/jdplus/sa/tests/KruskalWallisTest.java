/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.tests;

import demetra.data.Data;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class KruskalWallisTest {
    
    public KruskalWallisTest() {
    }

    @Test
    public void testSomeMethod() {
        KruskalWallis test=new KruskalWallis(Data.TS_ABS_RETAIL.getValues(), 12);
        System.out.println(test.build());
    }
    
}
