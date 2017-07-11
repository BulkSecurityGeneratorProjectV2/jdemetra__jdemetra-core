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
package ec.tstoolkit.maths.linearfilters;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.utilities.Ref;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class Utilities
{

    final static double g_epsilon = 1e-9;

    // / <summary>
    // / Verifies that the absolute value of the roots a lower then a given
    // value
    // / </summary>
    // / <param name="roots"></param>
    // / <param name="nmax"></param>
    // / <returns></returns>
    /**
     * 
     * @param roots
     * @param nmax
     * @return
     */
    public static boolean checkRoots(final Complex[] roots, final double nmax) {
	if (roots == null)
	    return true;
	for (int i = 0; i < roots.length; ++i) {
	    double n = (roots[i].abs());
	    if (n < nmax)
		return false;
	}
	return true;
    }

    /**
     * Checks that the norm of the roots of a given polynomial are higher than rmin
     * @param c The coefficients of the polynomial. The polynomial is 1+c(0)x+...
     * @param rmin The limit of the roots
     * @return False if the polynomial contains quasi-unit roots or if the polynomial is a constant, true otherwise
     */
    public static boolean checkRoots(final IReadDataBlock c, final double rmin) {
	int nc = c.getLength();
        switch (nc) {
            case 0:
                return false;
            case 1:
                double cabs = Math.abs(c.get(0));
                return (1 / cabs) > rmin;
            case 2:
                double a = c.get(0), b = c.get(1);
                double ro = a * a - 4 * b;
                if (ro > 0) { // Roots are (-a+-sqrt(ro))/(2b)
                    double sro = Math.sqrt(ro);
                    double x0 = (-a + sro) / (2*b), x1 = (-a - sro) / (2*b);
                    return Math.abs(x0) > rmin && Math.abs(x1) > rmin;
                }
                else // Roots are (-a+-isqrt(-ro))/(2b). Abs(roots) = (1/2b)*sqrt((a*a - a*a+4*b))=1/sqr(b)
                    // b is necessary positive
                    return (1/Math.sqrt(b))>rmin;
            default:
                double[] ctmp = new double[nc + 1];
                ctmp[0] = 1;
                c.copyTo(ctmp, 1);
                Polynomial p = Polynomial.of(ctmp);
                return checkRoots(p.roots(), rmin);
        }
    }

    /**
     * 
     * @param c
     * @return
     */
    public static boolean checkStability(final IReadDataBlock c) {
	int nc = c.getLength();
	if (nc == 0)
	    return true;
	if (nc == 1)
	    return Math.abs(c.get(0)) < 1;
	double[] coeff = new double[nc];
	c.copyTo(coeff, 0);
	double[] pat = new double[nc];
	double[] pu = new double[nc];
	for (int i = coeff.length - 1; i >= 0; --i) {
	    pat[i] = coeff[i];
	    if (Math.abs(pat[i]) >= 1)
		return false;
	    for (int j = 0; j < i; ++j)
		pu[j] = coeff[i - j - 1];
	    double den = 1 - pat[i] * pat[i];
	    for (int j = 0; j < i / 2; ++j) {
		coeff[j] = (coeff[j] - pat[i] * pu[j]) / den;
		coeff[i - j - 1] = (coeff[i - j - 1] - pat[i] * pu[i - j - 1])
			/ den;
	    }
	    if (i % 2 != 0)
		coeff[i / 2] = pu[i / 2] / (1 + pat[i]);
	}
	return true;
    }

    public static boolean checkQuasiStability(final IReadDataBlock c, double rtol) {
	int nc = c.getLength();
	if (nc == 0)
	    return true;
	if (nc == 1)
	    return Math.abs(c.get(0)) < rtol;
        double[] coef=new double[nc+1];
        coef[0]=1;
        c.copyTo(coef, 1);
        Polynomial p=Polynomial.copyOf(coef);
        Complex[] roots=p.roots();
        for (int i=0; i<roots.length; ++i){
            if (roots[i].abs() < 1/rtol)
                return false;
        }
        return true;
    }
    /**
     * 
     * @param p
     * @return
     */
    public static boolean checkStability(final Polynomial p)
    {
	return checkStability(new DataBlock(p.getCoefficients()).drop(1, 0));
    }

    /**
     * 
     * @param data
     * @return
     */
    public static double[] compact(final double[] data) {
	int cur = data.length - 1;
	while (cur >= 0 && data[cur] == 0)
	    --cur;
	if (cur < 0)
	    return null;
	if (cur == data.length - 1)
	    return data;
	double[] cdata = new double[cur + 1];
	for (int i = 0; i <= cur; ++i)
	    cdata[i] = data[i];
	return cdata;
    }

    /**
     * 
     * @param c
     * @param lb
     * @param w
     * @return
     */
    public static Complex frequencyResponse(final double[] c, final int lb,
	    final double w) {
	Complex phase = Complex.cart(Math.cos(w * lb), Math.sin(w * lb));

	double cos = Math.cos(w), sin = Math.sin(w);
	Complex rslt = Complex.cart(c[0]);

	// computed by the iteration procedure : cos (i+1)w + cos (i-1)w= 2*cos
	// iw *cos w
	// sin (i+1)w + sin (i-1)w= 2*sin iw *cos w
	// or equivalentally:
	// e(i(n+1)w)+e(i(n-1)w)=e(inw)*2cos w.
	// starting conditions:
	// e(i0w) = 1 , e(i1w)=eiw

	int n = c.length;

	Complex c0 = Complex.ONE, c1 = Complex.cart(cos, sin);
	if (n > 1) {
	    rslt = rslt.plus(c1.times(c[1]));
	    for (int i = 2; i < n; ++i) {
		Complex eiw = c1.times(2 * cos).minus(c0);
		rslt = rslt.plus(eiw.times(c[i]));
		c0 = c1;
		c1 = eiw;
	    }
	}

	return rslt.times(phase);
    }

    /**
     * 
     * @param data
     * @return
     */
    public static double[] smooth(final double[] data) {
	return smooth(data, g_epsilon, true);
    }

    /**
     * 
     * @param data
     * @param epsilon
     * @param bcompact
     * @return
     */
    public static double[] smooth(final double[] data, final double epsilon,
	    final boolean bcompact) {
	for (int i = 0; i < data.length; ++i)
	    if (Math.abs(data[i]) < epsilon)
		data[i] = 0;
	if (bcompact)
	    return compact(data);
	else
	    return data;
    }

    /**
     * 
     * @param p
     * @param rmax
     * @param np
     * @return
     */
    public static boolean stabilize(final Polynomial p, final double rmax,
	    final Ref<Polynomial> np) {
	np.val = p;
	if (p != null) {
	    boolean rslt = false;
	    Complex[] roots = p.roots();
	    for (int i = 0; i < roots.length; ++i) {
		Complex root = roots[i];
		double n = (roots[i].abs());
		if (n < 1 / rmax) {
		    roots[i] = root.div(n * rmax);
		    rslt = true;
		}
	    }
	    if (rslt) {
		np.val = Polynomial.fromComplexRoots(roots);
		np.val = np.val.divide(np.val.get(0));
		return true;
	    }
	}
	return false;
    }

    /**
     * 
     * @param p
     * @param np
     * @return
     */
    public static boolean stabilize(final Polynomial p, final Ref<Polynomial> np) {
	if (p != null && !checkStability(p)) {
	    Complex[] roots = p.roots();
	    for (int i = 0; i < roots.length; ++i) {
		Complex root = roots[i];
		double n = (roots[i].abs());
		if (n < 1)
		    roots[i] = root.inv();
	    }
	    np.val = Polynomial.fromComplexRoots(roots);
	    np.val = np.val.divide(np.val.get(0));
	    return true;
	} else {
	    np.val = p;
	    return false;
	}
    }

    private Utilities() {
    }
}
