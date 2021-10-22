/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.msts;

import java.util.ArrayList;
import java.util.List;
import demetra.data.DoubleSeq;
import demetra.math.functions.Optimizer;
import demetra.ssf.SsfInitialization;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class CompositeModel {

    private final List<StateItem> items = new ArrayList<>();
    private final List<ModelEquation> equations = new ArrayList<>();
    
    public CompositeModel duplicate(){
        CompositeModel m=new CompositeModel();
        for (StateItem item : items){
            m.items.add(item.duplicate());
        }
        for (ModelEquation eq : equations){
            m.equations.add(eq.duplicate());
        }
        return m;
    }

    public int getEquationsCount() {
        return equations.size();
    }

    public int getItemsCount() {
        return items.size();
    }

    public ModelItem getItem(int pos) {
        return items.get(pos);
    }

    public String[] getCmpsName() {
        return items.stream().map(item -> item.getName()).toArray(n -> new String[n]);
    }

    public ModelEquation getEquation(int pos) {
        return equations.get(pos);
    }

    public void add(StateItem item) {
        this.items.add(item);
    }

    public void add(ModelEquation eq) {
        this.equations.add(eq);
     }

    public MstsMapping mapping() {
        MstsMapping mapping = new MstsMapping();
        for (StateItem item : items) {
            item.addTo(mapping);
            if (!item.isScalable()) {
                mapping.setScalable(false);
            }
        }
        for (ModelEquation eq : equations) {
            eq.addTo(mapping);
        }
        return mapping;
    }


    public CompositeModelEstimation estimate(FastMatrix data, boolean marginal, boolean rescaling, SsfInitialization initialization, Optimizer optimizer, double eps, double[] parameters) {
        return CompositeModelEstimation.estimationOf(this.duplicate(), data, marginal, rescaling, initialization, optimizer, eps, parameters);
    }

    public CompositeModelEstimation compute(FastMatrix data, double[] parameters, boolean marginal, boolean concentrated) {
        
        return CompositeModelEstimation.computationOf(this.duplicate(), data, DoubleSeq.of(parameters), marginal, concentrated);
    }
}
