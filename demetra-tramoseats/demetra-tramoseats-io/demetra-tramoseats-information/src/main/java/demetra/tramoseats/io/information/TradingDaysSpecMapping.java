/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.tramoseats.io.information;

import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.information.InformationSet;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.tramo.RegressionTestType;
import demetra.timeseries.calendars.TradingDaysType;
import demetra.tramo.TradingDaysSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class TradingDaysSpecMapping {

    final String AUTO = "auto", MAUTO = "mauto", PFTD = "pftd", TDOPTION = "option", LPOPTION = "leapyear",
            HOLIDAYS = "holidays", USER = "user", TEST = "test", TESTTYPE = "testtype", W = "stocktd",
            LPCOEF = "lpcoef", TDCOEF = "tdcoef";

//    void fillDictionary(String prefix, Map<String, Class> dic) {
//        dic.put(InformationSet.item(prefix, AUTO), Boolean.class);
//        dic.put(InformationSet.item(prefix, MAUTO), String.class);
//        dic.put(InformationSet.item(prefix, PFTD), Double.class);
//        dic.put(InformationSet.item(prefix, TDOPTION), String.class);
//        dic.put(InformationSet.item(prefix, LPOPTION), String.class);
//        dic.put(InformationSet.item(prefix, USER), String[].class);
//        dic.put(InformationSet.item(prefix, HOLIDAYS), String.class);
//        dic.put(InformationSet.item(prefix, W), Integer.class);
//        dic.put(InformationSet.item(prefix, TESTTYPE), String.class);
//    }
//
    String lpName() {
        return "lp";
    }

    String tdName() {
        return "td";
    }

    void writeLegacy(InformationSet regInfo, TradingDaysSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return;
        }
        InformationSet cinfo = regInfo.subSet(RegressionSpecMapping.CALENDAR);
        InformationSet tdInfo = cinfo.subSet(CalendarSpecMapping.TD);

        writeProperties(tdInfo, spec, verbose);

        Parameter lcoef = spec.getLpCoefficient();
        RegressionSpecMapping.set(regInfo, lpName(), lcoef);
        Parameter[] tcoef = spec.getTdCoefficients();
        RegressionSpecMapping.set(regInfo, tdName(), tcoef);
    }

    InformationSet write(TradingDaysSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet tdInfo = new InformationSet();

        writeProperties(tdInfo, spec, verbose);

        Parameter lcoef = spec.getLpCoefficient();
        Parameter[] tcoef = spec.getTdCoefficients();
        if (lcoef != null) {
            tdInfo.set(LPCOEF, lcoef);
        }
        if (tcoef != null) {
            tdInfo.set(TDCOEF, tcoef);
        }
        return tdInfo;
    }

    void writeProperties(InformationSet tdInfo, TradingDaysSpec spec, boolean verbose) {

        if (verbose || spec.isAutomatic()) {
            tdInfo.set(MAUTO, spec.getAutomaticMethod().name());
        }
        if (verbose || spec.getProbabilityForFTest() != TradingDaysSpec.DEF_PFTD) {
            tdInfo.set(PFTD, spec.getProbabilityForFTest());
        }
        if (verbose || spec.getTradingDaysType() != TradingDaysType.None) {
            tdInfo.set(TDOPTION, spec.getTradingDaysType().name());
        }
        if (verbose || spec.getLengthOfPeriodType() != LengthOfPeriodType.None) {
            tdInfo.set(LPOPTION, spec.getLengthOfPeriodType().name());
        }
        if (spec.isHolidays()) {
            tdInfo.set(HOLIDAYS, spec.getHolidays());
        }
        if (spec.isUserDefined()) {
            tdInfo.set(USER, spec.getUserVariables());
        }
        if (verbose || spec.isStockTradingDays()) {
            tdInfo.set(W, spec.getStockTradingDays());
        }
        if (verbose || spec.isTest()) {
            tdInfo.set(TESTTYPE, spec.getRegressionTestType().name());
        }
    }

    TradingDaysSpec readLegacy(InformationSet regInfo) {
        InformationSet cinfo = regInfo.getSubSet(RegressionSpecMapping.CALENDAR);
        if (cinfo == null) {
            return TradingDaysSpec.none();
        }
        InformationSet tdInfo = cinfo.getSubSet(CalendarSpecMapping.TD);
        if (tdInfo == null) {
            return TradingDaysSpec.none();
        }
        Parameter lcoef = RegressionSpecMapping.coefficientOf(regInfo, lpName());
        Parameter[] tdcoef = RegressionSpecMapping.coefficientsOf(regInfo, tdName());

        return readProperties(tdInfo, lcoef, tdcoef);
    }

    TradingDaysSpec read(InformationSet tdInfo) {
        if (tdInfo == null) {
            return TradingDaysSpec.none();
        }
        Parameter lcoef = tdInfo.get(LPCOEF, Parameter.class);
        Parameter[] tdcoef = tdInfo.get(TDCOEF, Parameter[].class);

        return readProperties(tdInfo, lcoef, tdcoef);
    }

    TradingDaysSpec readProperties(InformationSet tdInfo, Parameter lcoef, Parameter[] tdcoef) {
        Boolean auto = tdInfo.get(AUTO, Boolean.class);
        String mauto = tdInfo.get(MAUTO, String.class);
        Double pftd = tdInfo.get(PFTD, Double.class);
        String td = tdInfo.get(TDOPTION, String.class);
        Boolean lp = tdInfo.get(LPOPTION, Boolean.class);
        String lpt = tdInfo.get(LPOPTION, String.class);
        String holidays = tdInfo.get(HOLIDAYS, String.class);
        String[] user = tdInfo.get(USER, String[].class);
        Integer w = tdInfo.get(W, Integer.class);
        Boolean test = tdInfo.get(TEST, Boolean.class);
        String testtype = tdInfo.get(TESTTYPE, String.class);

        TradingDaysType tdo = td == null ? TradingDaysType.None : TradingDaysType.valueOf(td);
        LengthOfPeriodType lpo = lp == null ? LengthOfPeriodType.None : LengthOfPeriodType.LeapYear;
        if (lpt != null) {
            lpo = LengthOfPeriodType.valueOf(lpt);
        }
        TradingDaysSpec.AutoMethod method=TradingDaysSpec.AutoMethod.Unused;
        if ((auto != null && auto) || mauto != null) {
            method = mauto == null ? TradingDaysSpec.AutoMethod.FTest : TradingDaysSpec.AutoMethod.valueOf(mauto);
        }
        if (method != TradingDaysSpec.AutoMethod.Unused){
            if (holidays != null) {
                return TradingDaysSpec.automaticHolidays(holidays, method, pftd == null ? TradingDaysSpec.DEF_PFTD : pftd);
            } else {
                return TradingDaysSpec.automatic(method, pftd == null ? TradingDaysSpec.DEF_PFTD : pftd);
            }
        }
        RegressionTestType reg = test != null ? RegressionTestType.Separate_T : RegressionTestType.None;
        if (testtype != null) {
            reg = RegressionTestType.valueOf(testtype);
        }
        if (user != null) {
            if (tdcoef != null) {
                return TradingDaysSpec.userDefined(user, tdcoef);
            } else {
                return TradingDaysSpec.userDefined(user, reg);
            }
        } else if (w != null && w != 0) {
            if (tdcoef != null) {
                return TradingDaysSpec.stockTradingDays(w, tdcoef);
            } else {
                return TradingDaysSpec.stockTradingDays(w, reg);
            }
        } else if (tdo == TradingDaysType.None && lpo == LengthOfPeriodType.None) {
            return TradingDaysSpec.none();
        } else if (holidays != null) {
            if (tdcoef != null || lcoef != null) {
                return TradingDaysSpec.holidays(holidays, tdo, lpo, tdcoef, lcoef);
            } else {
                return TradingDaysSpec.holidays(holidays, tdo, lpo, reg);
            }

        } else {
            if (tdcoef != null || lcoef != null) {
                return TradingDaysSpec.td(tdo, lpo, tdcoef, lcoef);
            } else {
                return TradingDaysSpec.td(tdo, lpo, reg);
            }
        }
    }
}
