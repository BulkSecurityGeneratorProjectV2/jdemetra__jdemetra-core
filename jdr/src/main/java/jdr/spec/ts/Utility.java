/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.ts;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.timeseries.Day;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Utility {

    public Day of(String date) {
        try {
            return Day.fromString(date);
        } catch (ParseException ex) {
            throw new RuntimeException("Unvalid date format");
        }
    }

    public String toString(Date date) {
        return new Day(date).toString();
//        StringBuilder builder = new StringBuilder();
//        GregorianCalendar gc = new GregorianCalendar();
//        gc.setTime(date);
//        builder.append(gc.get(GregorianCalendar.YEAR)).append('-')
//                .append(gc.get(GregorianCalendar.MONTH)+1).append('-')
//                .append(gc.get(GregorianCalendar.DAY_OF_MONTH));
//        return builder.toString();
    }

    public String toString(Day day) {
        return day == null || day == Day.BEG || day == Day.END ? "" : day.toString();
    }

    public Parameter[] parameters(double[] values) {
        return parameters(values, null);
    }

    public Parameter[] parameters(double[] values, boolean[] fixed) {
        Parameter[] p = new Parameter[values.length];
        for (int i = 0; i < p.length; ++i) {
            if (Double.isFinite(values[i])) {
                if (fixed != null && fixed[i]) {
                    p[i] = new Parameter(values[i], ParameterType.Fixed);
                } else {
                    p[i] = new Parameter(values[i], ParameterType.Initial);
                }
            } else {
                p[i] = new Parameter();
            }
        }
        return p;
    }

}