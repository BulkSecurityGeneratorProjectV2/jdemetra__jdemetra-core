/*
 * Copyright 2013 National Bank of Belgium
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
package ec.satoolkit.x11;

import ec.satoolkit.DecompositionMode;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.utilities.Jdk6;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Frank Osaer, Jean Palate, BAYENSK
 */
@Development(status = Development.Status.Alpha)
public class X11Specification implements IProcSpecification, Cloneable {

    public static final double DEF_LSIGMA = 1.5, DEF_USIGMA = 2.5;
    public static final int DEF_FCASTS = -1, DEF_BCASTS = 0;

    public static final String MODE = "mode",
            SEASONAL = "seasonal",
            LSIGMA = "lsigma",
            USIGMA = "usigma",
            TRENDMA = "trendma",
            SEASONALMA = "seasonalma",
            BCASTS = "bcasts",
            FCASTS = "fcasts",
            CALENDARSIGMA = "calendarsigma",
            SIGMAVEC = "sigmavec",
            EXCLUDEFCAST = "excludeforcast",
            BIAS = "bias";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, MODE), String.class);
        dic.put(InformationSet.item(prefix, SEASONAL), Boolean.class);
        dic.put(InformationSet.item(prefix, LSIGMA), Double.class);
        dic.put(InformationSet.item(prefix, USIGMA), Double.class);
        dic.put(InformationSet.item(prefix, TRENDMA), Integer.class);
        dic.put(InformationSet.item(prefix, SEASONALMA), String[].class);
        dic.put(InformationSet.item(prefix, FCASTS), Integer.class);
        dic.put(InformationSet.item(prefix, BCASTS), Integer.class);
        dic.put(InformationSet.item(prefix, CALENDARSIGMA), String.class);
        //  dic.put(InformationSet.item(prefix, MODE), String.class);
        dic.put(InformationSet.item(prefix, SIGMAVEC), String[].class);
        dic.put(InformationSet.item(prefix, EXCLUDEFCAST), String.class);
        dic.put(InformationSet.item(prefix, BIAS), String.class);
    }

    private DecompositionMode mode_ = DecompositionMode.Undefined;
    private boolean seasonal_ = true;
    private SeasonalFilterOption[] filters_;
    private double lsigma_ = DEF_LSIGMA, usigma_ = DEF_USIGMA;
    private int henderson_ = 0;
    private int fcasts_ = DEF_FCASTS, bcasts_ = DEF_BCASTS;
    private CalendarSigma calendarsigma_ = CalendarSigma.None;
    private SigmavecOption[] sigmavec_;
    private boolean excludefcast_ = false;
    private BiasCorrection bias = BiasCorrection.Legacy;

    /**
     * Number of forecasts used in X11. By default, 0. When pre-processing is
     * used, the number of forecasts corresponds usually to 1 year. Negative
     * values correspond to full years (-3 = 3 years)
     *
     * @return the forecastsHorizon
     */
    public int getForecastHorizon() {
        return fcasts_;
    }

    /**
     * Number of backcasts used in X11. By default, 0. Negative values
     * correspond to full years (-3 = 3 years)
     *
     * @return the backcastsHorizon
     */
    public int getBackcastHorizon() {
        return bcasts_;
    }

    /**
     * Length of the Henderson filter [trendma option in X12-Arima]. When the
     * length is 0, an automatic estimation of the length of the Henderson
     * filter is computed by the algorithm.
     *
     * @return
     */
    public int getHendersonFilterLength() {
        return henderson_;
    }

    /**
     * Option of Calendarsigma[X12], specifies the calculation of the standard
     * error calculation used for outlier detection in the X11 part
     *
     * @return
     */
    public CalendarSigma getCalendarSigma() {
        return calendarsigma_;
    }

    public SigmavecOption[] getSigmavec() {
        return sigmavec_;
    }

    public double getLowerSigma() {
        return lsigma_;
    }

    public double getUpperSigma() {
        return usigma_;
    }

    public DecompositionMode getMode() {
        return mode_;
    }

    public SymmetricFilter getTrendFilter() {
        return TrendCycleFilterFactory.makeHendersonFilter(13);
    }

    public SeasonalFilterOption[] getSeasonalFilters() {
        return filters_;
    }

    public boolean isSeasonal() {
        return seasonal_;
    }

    public void setSeasonal(boolean seas) {
        seasonal_ = seas;
    }

    public boolean isExcludefcst() {
        return excludefcast_;
    }

    /**
     *
     * @param excludefcst default is false; true then the forcasts are ignored
     * for the extreme value calculation
     */
    public void setExcludefcst(boolean excludefcst) {
        excludefcast_ = excludefcst;
    }

    public boolean isDefault() {
        if (!seasonal_ || mode_ != DecompositionMode.Multiplicative) {
            return false;
        }

        if (calendarsigma_ != CalendarSigma.None) {
            return false;
        }

        if (fcasts_ != DEF_FCASTS) {
            return false;
        }
        if (filters_ != null) {
            for (int i = 0; i < filters_.length; ++i) {
                if (filters_[i] != SeasonalFilterOption.Msr) {
                    return false;
                }
            }
        }

        if (sigmavec_ != null) {
            for (int i = 0; i < sigmavec_.length; ++i) {
                if (sigmavec_[i] != SigmavecOption.Group1) {
                    return false;
                }
            }
        }
        if (lsigma_ != DEF_LSIGMA) {
            return false;
        }
        if (usigma_ != DEF_USIGMA) {
            return false;
        }

        if (!excludefcast_) {
            return false;
        }

        return isAutoHenderson();

    }

    public boolean isAutoHenderson() {
        return henderson_ <= 0;
    }

    /**
     * Number of forecasts used in X11. By default, 0. When pre-processing is
     * used, the number of forecasts corresponds usually to 1 year.
     *
     * @param forecastsHorizon The forecasts horizon to set When
     * forecastsHorizon is negative, its absolute value corresponds to the
     * number of years of forecasting. For example, setForecastHorizon(-1) is
     * equivalent to setForecastHorizon(12) for monthly data and to
     * setForecastHorizon(4) for quarterly data.
     */
    public void setForecastHorizon(int forecastsHorizon) {
        this.fcasts_ = forecastsHorizon;
    }

    /**
     * Number of forecasts used in X11. By default, 0. When pre-processing is
     * used, the number of forecasts corresponds usually to 1 year.
     *
     * @param backcastsHorizon The backcasts horizon to set When
     * backcastsHorizon is negative, its absolute value corresponds to the
     * number of years of backcasting. For example, setBackcastHorizon(-1) is
     * equivalent to setBackcastHorizon(12) for monthly data and to
     * setBackcastHorizon(4) for quarterly data.
     */
    public void setBackcastHorizon(int backcastsHorizon) {
        this.bcasts_ = backcastsHorizon;
    }

    /**
     * Option of Calendarsigma[X12], specifies the calculation of the standard
     * error calculation used for outlier detection in the X11 part
     *
     * @param calendarsigma
     */
    public void setCalendarSigma(CalendarSigma calendarsigma) {
        calendarsigma_ = calendarsigma;
    }

    public void setSigmavec(SigmavecOption[] sigmavec) {
        sigmavec_ = sigmavec.clone();
    }

    /**
     * Set the decomposition mode of X11
     *
     * @param mode
     */
    public void setMode(DecompositionMode mode) {
        mode_ = mode;
    }

    /**
     * Parameters for extreme values detection [sigmalim option in X12-arima].
     *
     * @param lsigma Lower sigma value for extreme values detection
     * @param usigma Upper sigma value for extreme values detection lsigma
     * should be lower than usigma and higher than .5.
     * @exception A X11Exception is thrown if lsigma and/or usigma are invalid.
     */
    public void setSigma(double lsigma, double usigma) {
        if (usigma <= lsigma || lsigma <= 0.5) {
            throw new X11Exception("Invalid sigma options");
        }
        lsigma_ = lsigma;
        usigma_ = usigma;
    }

    public void setLowerSigma(double lsigma) {
        if (usigma_ <= lsigma) {
            setSigma(lsigma, lsigma + .5);
        } else {
            setSigma(lsigma, usigma_);
        }
    }

    public void setUpperSigma(double usigma) {
        if (usigma <= lsigma_) {
            setSigma(usigma - .5, usigma);
        } else {
            setSigma(lsigma_, usigma);
        }
    }

    /**
     * Length of the Henderson filter [trendma option in X12-arima]
     *
     * @param len Length of the Henderson filter. When the length is 0, an
     * automatic estimation is made by the program. Otherwise, the length should
     * be an odd number in the range [1, 101].
     */
    public void setHendersonFilterLength(int len) {
        if (len < 0 || len > 101 || (len != 0 && len % 2 == 0)) {
            throw new X11Exception("Invalid henderson length");
        }
        henderson_ = len;
    }

    public void setSeasonalFilters(SeasonalFilterOption[] seasonalFilter) {
        filters_ = seasonalFilter.clone();
    }

    public void setSeasonalFilter(SeasonalFilterOption seasonalFilter) {
        filters_ = new SeasonalFilterOption[]{seasonalFilter};
    }

    public BiasCorrection getBiasCorrection() {
        return bias;
    }

    public void setBiasCorrection(BiasCorrection bias) {
        this.bias = bias;
    }

    @Override
    public X11Specification clone() {
        try {
            X11Specification cspec = (X11Specification) super.clone();
            if (filters_ != null) {
                cspec.filters_ = filters_.clone();
            }
            if (sigmavec_ != null) {
                cspec.sigmavec_ = sigmavec_.clone();
            }
            return cspec;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof X11Specification && equals((X11Specification) obj));
    }

    private boolean equals(X11Specification spec) {
        return spec.fcasts_ == fcasts_ && spec.bcasts_ == bcasts_
                && Arrays.deepEquals(spec.filters_, filters_)
                && Arrays.deepEquals(spec.sigmavec_, sigmavec_)
                && spec.seasonal_ == seasonal_
                && spec.henderson_ == henderson_
                && spec.lsigma_ == lsigma_
                && spec.usigma_ == usigma_
                && spec.mode_ == mode_
                && spec.calendarsigma_ == calendarsigma_
                && spec.excludefcast_ == excludefcast_
                && spec.bias == bias;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.mode_);
        hash = 11 * hash + Jdk6.Double.hashCode(this.lsigma_);
        hash = 11 * hash + Jdk6.Double.hashCode(this.usigma_);
        hash = 11 * hash + this.henderson_;
        hash = 11 * hash + this.fcasts_;
        return hash;
    }

    //////////////////////////////////////////////////////////////////////////
    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || mode_ != DecompositionMode.Undefined) {
            info.add(MODE, mode_.name());
        }
        if (verbose || !seasonal_) {
            info.add(SEASONAL, seasonal_);
        }
        if (verbose || lsigma_ != DEF_LSIGMA) {
            info.add(LSIGMA, lsigma_);
        }
        if (verbose || usigma_ != DEF_USIGMA) {
            info.add(USIGMA, usigma_);
        }
        if (verbose || !isAutoHenderson()) {
            info.add(TRENDMA, henderson_);
        }
        if (filters_ != null) {
            String[] filters = new String[filters_.length];
            for (int i = 0; i < filters.length; ++i) {
                filters[i] = filters_[i].name();
            }
            info.add(SEASONALMA, filters);
        }
        if (verbose || fcasts_ != DEF_FCASTS) {
            info.add(FCASTS, fcasts_);
        }
        if (verbose || bcasts_ != DEF_BCASTS) {
            info.add(BCASTS, bcasts_);
        }

        if (verbose || calendarsigma_ != CalendarSigma.None) {
            info.add(CALENDARSIGMA, calendarsigma_.name());
        }
        if (sigmavec_ != null) {
            String[] sigmavec = new String[sigmavec_.length];
            for (int i = 0; i < sigmavec.length; ++i) {
                sigmavec[i] = sigmavec_[i].name();
            }
            info.add(SIGMAVEC, sigmavec);
        }

        if (verbose || excludefcast_) {
            info.add(EXCLUDEFCAST, excludefcast_);
        }
        if (verbose || bias != BiasCorrection.Legacy) {
            info.add(BIAS, bias.name());
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            String mode = info.get(MODE, String.class);
            if (mode != null) {
                mode_ = DecompositionMode.valueOf(mode);
            }
            Boolean seasonal = info.get(SEASONAL, Boolean.class);
            if (seasonal != null) {
                seasonal_ = seasonal;
            }
            Double lsig = info.get(LSIGMA, Double.class);
            if (lsig != null) {
                lsigma_ = lsig;
            }
            Double usig = info.get(USIGMA, Double.class);
            if (usig != null) {
                usigma_ = usig;
            }
            Integer trendma = info.get(TRENDMA, Integer.class);
            if (trendma != null) {
                henderson_ = trendma;
            }
            Integer fcasts = info.get(FCASTS, Integer.class);
            if (fcasts != null) {
                fcasts_ = fcasts;
            }
            Integer bcasts = info.get(BCASTS, Integer.class);
            if (bcasts != null) {
                bcasts_ = bcasts;
            }
            String[] sfilters = info.get(SEASONALMA, String[].class);
            if (sfilters != null) {
                filters_ = new SeasonalFilterOption[sfilters.length];
                for (int i = 0; i < sfilters.length; ++i) {
                    filters_[i] = SeasonalFilterOption.valueOf(sfilters[i]);
                }
            }

            String calendarsigma = info.get(CALENDARSIGMA, String.class);
            if (calendarsigma != null) {
                calendarsigma_ = CalendarSigma.valueOf(calendarsigma);
            }

            String[] sigmavec = info.get(SIGMAVEC, String[].class);
            if (sigmavec != null) {
                sigmavec_ = new SigmavecOption[sigmavec.length];
                for (int i = 0; i < sigmavec.length; ++i) {
                    sigmavec_[i] = SigmavecOption.valueOf(sigmavec[i]);
                }
            }

            Boolean excludefcst = info.get(EXCLUDEFCAST, Boolean.class);
            if (excludefcst != null) {
                excludefcast_ = excludefcst;
            }

            String sbias = info.get(BIAS, String.class);
            if (sbias != null) {
                bias = BiasCorrection.valueOf(sbias);
            } else {
                bias = BiasCorrection.Legacy;
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }

}
