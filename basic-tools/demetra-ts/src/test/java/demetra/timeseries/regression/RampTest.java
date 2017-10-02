/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class RampTest {

    public RampTest() {
    }

    @Test
    public void testMonthlyRamp() {
        Ramp ramp = new Ramp(LocalDate.of(2017, Month.MARCH, 1).atStartOfDay(),
                LocalDate.of(2018, Month.MARCH, 31).atStartOfDay());

        // just before
        RegularDomain domain = RegularDomain.of(TsPeriod.monthly(2012, 4), 60);
        DataBlock data = DataBlock.make(domain.getLength());
        ramp.data(domain, Collections.singletonList(data));
        assertTrue(data.allMatch(x->x==-1));
        // just after
        domain = RegularDomain.of(TsPeriod.monthly(2018, 3), 60);
        data = DataBlock.make(domain.getLength());
        ramp.data(domain, Collections.singletonList(data));
        assertTrue(data.allMatch(x->x==0));
        
        // inside
        domain = RegularDomain.of(TsPeriod.monthly(2017, 4), 10);
        data = DataBlock.make(domain.getLength());
        ramp.data(domain, Collections.singletonList(data));
        assertTrue(data.allMatch(x->x!=0 && x != -1));

        // across the beginning
        domain = RegularDomain.of(TsPeriod.monthly(2017, 1), 10);
        data = DataBlock.make(domain.getLength());
        ramp.data(domain, Collections.singletonList(data));
        assertTrue(data.count(x->x == -1) == 3);
        // across the beginning
        domain = RegularDomain.of(TsPeriod.monthly(2017, 10), 10);
        data = DataBlock.make(domain.getLength());
        ramp.data(domain, Collections.singletonList(data));
        assertTrue(data.count(x->x != 0) == 5);
    }

    @Test
    public void testMonthlyRamp2() {
        Ramp ramp = new Ramp(LocalDate.of(2017, Month.MARCH, 2).atStartOfDay(),
                LocalDate.of(2018, Month.MARCH, 30).atStartOfDay());

        // just before
        RegularDomain domain = RegularDomain.of(TsPeriod.monthly(2012, 4), 60);
        DataBlock data = DataBlock.make(domain.getLength());
        ramp.data(domain, Collections.singletonList(data));
        assertTrue(data.allMatch(x->x==-1));
        // just after
        domain = RegularDomain.of(TsPeriod.monthly(2018, 3), 60);
        data = DataBlock.make(domain.getLength());
        ramp.data(domain, Collections.singletonList(data));
        assertTrue(data.allMatch(x->x==0));
    }
    
        @Test
    public void testDaiyRamp() {
        Ramp ramp = new Ramp(LocalDate.of(2017, Month.MARCH, 1).atStartOfDay(),
                LocalDate.of(2018, Month.MARCH, 31).atStartOfDay());

        // just before
        RegularDomain domain = RegularDomain.of(TsPeriod.daily(2017, 2, 1), 29);
        DataBlock data = DataBlock.make(domain.getLength());
        ramp.data(domain, Collections.singletonList(data));
        assertTrue(data.allMatch(x->x==-1));
        domain = RegularDomain.of(TsPeriod.daily(2017, 2, 1), 30);
        data = DataBlock.make(domain.getLength());
        ramp.data(domain, Collections.singletonList(data));
        assertTrue(data.count(x->x != -1) == 1);
        // just after
        domain = RegularDomain.of(TsPeriod.daily(2018, 3, 31), 60);
        data = DataBlock.make(domain.getLength());
        ramp.data(domain, Collections.singletonList(data));
        assertTrue(data.allMatch(x->x==0));
        domain = RegularDomain.of(TsPeriod.daily(2018, 3, 30), 60);
        data = DataBlock.make(domain.getLength());
        ramp.data(domain, Collections.singletonList(data));
        assertTrue(data.count(x->x != 0) == 1);
    }

}
