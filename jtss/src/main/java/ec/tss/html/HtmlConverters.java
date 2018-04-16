/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tss.html;

import ec.tss.Ts;
import ec.tss.html.implementation.HtmlArima;
import ec.tss.html.implementation.HtmlLikelihood;
import ec.tss.html.implementation.HtmlModelStatistics;
import ec.tss.html.implementation.HtmlSarimaModel;
import ec.tss.html.implementation.HtmlSingleTsData;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.modelling.arima.ModelStatistics;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public abstract class HtmlConverters {

    @FunctionalInterface
    public static interface IHtmlConverter<T> {

        IHtmlElement convert(T obj);
    }

    public abstract <T> void register(Class<T> tclass, IHtmlConverter<T> converter);

    public abstract <T> void unregister(Class<T> tclass);

    public abstract IHtmlElement convert(Object obj);

    public static HtmlConverters getDefault() {
        return instance_;
    }

    public static void setDefault(HtmlConverters cv) {
        instance_ = cv;
    }

    private static HtmlConverters instance_ = DefaultHtmlConverters.create();

    private static class DefaultHtmlConverters extends HtmlConverters {

        private static final Map<Class, IHtmlConverter> converters = new LinkedHashMap<>();

        private static HtmlConverters create() {
            DefaultHtmlConverters cv = new DefaultHtmlConverters();
            cv.register(TsData.class, (TsData obj) -> new HtmlSingleTsData(obj, null));
            cv.register(Ts.class, (Ts obj) -> {
                String name = obj.getName();
                TsData s = obj.getTsData();
                if (s != null) {
                    return new HtmlSingleTsData(s, name);
                } else {
                    return new HtmlFragment(name + ": no data");
                }
            });
            cv.register(IArimaModel.class, HtmlArima::new);
            cv.register(SarimaModel.class, HtmlSarimaModel::new);
            cv.register(LikelihoodStatistics.class, HtmlLikelihood::new);
            cv.register(ModelStatistics.class, HtmlModelStatistics::new);
            return cv;
        }

        @Override
        public <T> void register(Class<T> tclass, IHtmlConverter<T> converter) {
            converters.put(tclass, converter);
        }

        @Override
        public <T> void unregister(Class<T> tclass) {
            converters.remove(tclass);
        }

        @Override
        public IHtmlElement convert(Object obj) {
            if (obj == null) {
                return new HtmlFragment("");
            }
            IHtmlConverter cv = converters.get(obj.getClass());
            if (cv == null) {

                for (Class c : converters.keySet()) {
                    if (c.isInstance(obj)) {
                        cv = converters.get(c);
                        break;
                    }
                }
            }
            if (cv != null) {
                return cv.convert(obj);
            } else {
                return new HtmlFragment(obj.toString());
            }
        }
    }
}
