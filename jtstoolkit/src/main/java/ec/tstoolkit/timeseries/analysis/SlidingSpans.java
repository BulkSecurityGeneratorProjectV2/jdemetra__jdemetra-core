/*
* Copyright 2013 National Bank of Belgium
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

package ec.tstoolkit.timeseries.analysis;

import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SlidingSpans<I extends IProcResults> {

    class MaxMin {

	double max;
	double min;
	int count;

	void add(double val) {
	    if (count == 0) {
		max = val;
		min = val;
	    } else {
		if (val > max)
		    max = val;
		if (val < min)
		    min = val;
	    }
	    ++count;
	}

	double value(DiagnosticInfo info) {
	    if (info == DiagnosticInfo.RelativeDifference)
		return (max - min) / min;
	    else
		return max - min;
	}
    }

    private static class Node<I>{
        TsDomain domain;
        I estimation;
    }
    
    private Node<I>[] m_estimation;

    private final ITsProcessing<I> m_processing;

    private final TsDomain m_domainT;

    private final I m_reference;

    private int m_spanLength = 8;

    private int m_spanCount = 4;

    private final int m_spanDistance = 1;

    private int m_spanMin = 2;

    /**
     * 
     * @param processing
     * @param domain
     */
    public SlidingSpans(ITsProcessing<I> processing, TsDomain domain)
    {
	m_processing = processing;
	m_domainT = domain;
	m_reference = processing.process(m_domainT);
    }

    private void addDel(int p,
	    HashMap<TsPeriod, SlidingSpans<I>.MaxMin> buffer, TsData data) {
	TsPeriod start = data.getStart();
	double[] obs = data.internalStorage();
	for (int i = p; i < obs.length; ++i) {
	    TsPeriod cur = start.plus(i);
	    MaxMin Mm = buffer.get(cur);
	    if (Mm == null) {
		Mm = new MaxMin();
		buffer.put(cur, Mm);
	    }
	    Mm.add(obs[i] - obs[i - p]);
	}
    }

    private void addPct(int p,
	    HashMap<TsPeriod, SlidingSpans<I>.MaxMin> buffer, TsData data) {
	TsPeriod start = data.getStart();
	double[] obs = data.internalStorage();
	for (int i = p; i < obs.length; ++i) {
	    TsPeriod cur = start.plus(i);
	    MaxMin Mm = buffer.get(cur);
	    if (Mm == null) {
		Mm = new MaxMin();
		buffer.put(cur, Mm);
	    }
	    Mm.add(obs[i] / obs[i - p]);
	}
    }

    private void addValue(HashMap<TsPeriod, SlidingSpans<I>.MaxMin> buffer,
	    TsData data) {
	TsPeriod start = data.getStart();
	double[] obs = data.internalStorage();
	for (int i = 0; i < obs.length; ++i) {
	    TsPeriod cur = start.plus(i);
	    MaxMin Mm = buffer.get(cur);
	    if (Mm == null) {
		Mm = new MaxMin();
		buffer.put(cur, Mm);
	    }
	    Mm.add(obs[i]);
	}
    }

    /**
     * 
     * @return
     */
    public int getMaxSpanCount()
    {
	return m_spanCount;
    }

    /**
     * 
     * @return
     */
    public int getMinSpanCount()
    {
	return m_spanMin;
    }

    /**
     * 
     * @return
     */
    public ITsProcessing<I> getProcessing()
    {
	return m_processing;
    }

    /**
     * 
     * @return
     */
    public I getReferenceInfo()
    {
	return m_reference;
    }

    public TsDomain getReferenceDomain(){
        return m_domainT;
    }

    /**
     * 
     * @return
     */
    public int getSpanCount()
    {
	if (m_estimation == null && !process())
	    return 0;
	return m_estimation.length;
    }

    /**
     * 
     * @return
     */
    public int getSpanLength()
    {
	return m_spanLength;
    }

    /**
     * 
     * @param idx
     * @return
     */
    public I info(int idx)
    {
	if (m_estimation == null && !process())
	    return null;
	return m_estimation.length <= idx ? null : m_estimation[idx].estimation;
    }
    
    public TsDomain getDomain(int idx){
	if (m_estimation == null && !process())
	    return null;
	return m_estimation.length <= idx ? null : m_estimation[idx].domain;
        
    }

    /**
     * 
     * @return
     */
    public boolean isValid()
    {
	if (m_estimation == null && !process())
	    return false;
	return m_estimation.length >= m_spanMin;
    }

    /**
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean process() {
	if (m_estimation != null)
	    return true;
	ArrayList<Node<I>> rslts = new ArrayList<>();
	int freq = m_domainT.getFrequency().intValue();
	int length = m_spanLength * freq;
	TsPeriod start = m_domainT.getLast().minus(length - 1);
	if (start.getPosition() != 0) {
	    length += start.getPosition();
	    start.move(-start.getPosition());
	}
	int idx = 0;
	while (idx < m_spanCount && start.isNotBefore(m_domainT.getStart()))
	    try {
		++idx;
		TsDomain cur = new TsDomain(start, length);
		I info = m_processing.process(cur);
		if (info == null)
		    break;
		else{
                    Node<I> node=new Node<>();
                    node.estimation=info;
                    node.domain=cur;
		    rslts.add(node);
                }
		start.move(-m_spanDistance * freq);
	    } catch (Exception err) {
		break;
	    }

	if (rslts.size() < m_spanMin)
	    return false;
	m_estimation = Jdk6.Collections.toArray(rslts, Node.class);
	ec.tstoolkit.utilities.Arrays2.reverse(m_estimation);
	return true;
    }

    /**
     * 
     * @param series
     * @return
     */
    public TsData referenceSeries(String series)
    {
	if (m_reference == null)
	    return null;
	return m_reference.getData(series, TsData.class);
    }

    /**
     * 
     * @param value
     */
    public void setMaxSpanCount(int value)
    {
	if (value != m_spanCount)
	    m_estimation = null;
	m_spanCount = value;
    }

    /**
     * 
     * @param value
     */
    public void setMinSpanCount(int value)
    {
	if (value < 2)
	    throw new DiagnosticException(
		    DiagnosticException.InvalidSlidingSpanArgument);
	m_spanMin = value;
    }

    /**
     * 
     * @param value
     */
    public void setSpanLength(int value)
    {
	if (value != m_spanLength)
	    m_estimation = null;
	m_spanLength = value;
    }

    /**
     * 
     * @param key
     * @param info
     * @return
     */
    public TsData Statistics(String key, DiagnosticInfo info)
    {
	if (m_estimation == null && !process())
	    return null;
	if (getSpanCount() < getMinSpanCount())
	    return null;
	HashMap<TsPeriod, MaxMin> buffer = new HashMap<>();
	for (int i = 0; i < m_estimation.length; ++i) {
	    TsData data = m_estimation[i].estimation.getData(key, TsData.class);
	    if (data != null)
		switch (info) {
		case PeriodToPeriodGrowthDifference:
		    addPct(1, buffer, data);
		    break;
		case AnnualGrowthDifference:
		    addPct(data.getFrequency().intValue(), buffer, data);
		    break;
		case PeriodToPeriodDifference:
		    addDel(1, buffer, data);
		    break;
		case AnnualDifference:
		    addDel(data.getFrequency().intValue(), buffer, data);
		    break;
		default:
		    addValue(buffer, data);
		    break;
		}
	}

	TsData rslt = new TsData(m_domainT);
	TsPeriod start = m_domainT.getStart();
	for (Entry<TsPeriod, MaxMin> kv : buffer.entrySet())
	    if (kv.getValue().count >= m_spanMin) {
		int idx = kv.getKey().minus(start);
		rslt.set(idx, kv.getValue().value(info));
	    }
	return rslt.cleanExtremities();
    }
}
