/*
* Copyright 2013 National Bank ofFunction Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofFunction the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofFunction the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/
package demetra.maths.linearfilters;

import demetra.data.Doubles;
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.maths.Complex;
import demetra.maths.polynomials.IRootsSolver;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.UnitRoots;
import java.util.function.IntToDoubleFunction;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class BackFilter extends AbstractFiniteFilter {

 
    /**
     * BackFilter(0)
     */
    public static final BackFilter ZERO = new BackFilter(Polynomial.ZERO);

    /**
     * BackFilter(1)
     */
    public static final BackFilter ONE = new BackFilter(Polynomial.ONE);
    
    /**
     * BackFilter(1 - x)
     */
    public static final BackFilter D1 = new BackFilter(UnitRoots.D1);

    /**
     * 
     * @param d
     * @param l
     * @return
     */
    public static BackFilter add(final double d, final BackFilter l) {
	Polynomial p = l.polynomial.plus(d);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @param l
     * @return
     */
    public static BackFilter multiply(final double d, final BackFilter l) {
	Polynomial p = l.polynomial.times(d);
	return new BackFilter(p);
    }

    /**
     * Create a new BackFilter from the specified coefficients.<br>
     * Note that a cached one can be returned if available (ONE, ZERO, ...)
     * @param coefficients
     * @return 
     */
    public static BackFilter ofInternal(double[] coefficients) {
        if (coefficients.length == 1) {
            if (coefficients[0] == 1.0)
                return BackFilter.ONE;
            else if (coefficients[0] == 0.0)
                return BackFilter.ZERO;
        }
        return new BackFilter(Polynomial.ofInternal(coefficients));
    }
    
    private final Polynomial polynomial;

    /**
     * 
     * @param p
     */
    public BackFilter(final Polynomial p) {
	polynomial = p;
    }

    /**
     * 
     * @param r
     * @return
     */
    public BackFilter divide(final BackFilter r) {
	Polynomial.Division div = Polynomial.divide(polynomial, r.polynomial);
	// if (!div.getRemainder().isNull())
	// throw new PolynomialException(PolynomialException.Division);
	return new BackFilter(div.getQuotient());
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final BackFilter other = (BackFilter) obj;
	return this.polynomial.equals(other.polynomial);
    }

    /**
     * 
     * @param idx
     * @return
     */
    public double get(final int idx) {
	return polynomial.get(idx);
    }

    /**
     * 
     * @return
     */
    public Doubles coefficients() {
	return polynomial.coefficients();
    }

    public Polynomial getPolynomial() {
        return polynomial;
    }
    
    /**
     * 
     * @return
     */
    public int getDegree() {
	return polynomial.getDegree();
    }

    /**
     * 
     * @return
     */
    @Override
    public int length() {
	return polynomial.getDegree() + 1;
    }

    @Override
    public int getLowerBound() {
	return -polynomial.getDegree();
    }

    /**
     * 
     * @return
     */
    @Override
    public int getUpperBound() {
	return 0;
    }


    /**
     * 
     * @return
     */
    @Override
    public IntToDoubleFunction weights() {
	return i->polynomial.get(-i);
   }

    @Override
    public int hashCode() {
	return polynomial.hashCode();
    }

    /**
     * 
     * @return
     */
    public boolean isIdentity() {
	return polynomial.isIdentity();
    }

    /**
     * 
     * @return
     */
    public boolean isNull() {
	return polynomial.isZero();
    }

    /**
     * 
     * @param r
     * @return
     */
    public BackFilter minus(final BackFilter r) {
	Polynomial p = polynomial.minus(r.polynomial);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @return
     */
    public BackFilter minus(final double d) {
	Polynomial p = polynomial.minus(d);
	return new BackFilter(p);
    }

    /**
     * 
     * @return
     */
    @Override
    public ForeFilter mirror() {
	return new ForeFilter(polynomial);
    }

    /**
     * 
     * @return
     */
    public BackFilter negate() {
	Polynomial p = polynomial.negate();
	return new BackFilter(p);
    }

    /**
     * 
     * @return
     */
    public BackFilter normalize() {
	/*
	 * int idx = 0; double[] c = polynomial.getCoefficients(); while (idx <
	 * c.length && c[idx] == 0) ++idx; if (idx == c.length) throw new
	 * LinearFilterException("Illegal operation", "BFilter.normalize"); if
	 * (idx != 0) { double[] nc = new double[c.length - idx]; for (int i =
	 * 0; i < nc.length; ++i) nc[i] = c[idx + i]; polynomial =
	 * Polynomial.promote(nc); }
	 */

	double r = polynomial.get(0);
	if (r == 0 || r == 1)
            return this;
        else
	    return new BackFilter(polynomial.times(1 / r));
    }

    /**
     * 
     * @param r
     * @return
     */
    public BackFilter plus(final BackFilter r) {
	Polynomial p = polynomial.plus(r.polynomial);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @return
     */
    public BackFilter plus(final double d) {
	Polynomial p = polynomial.plus(d);
	return new BackFilter(p);
    }

    /**
     * 
     * @return
     */
    public Complex[] roots() {
	return polynomial.roots();
    }

    /**
     * 
     * @param solver
     * @return
     */
    public Complex[] roots(final IRootsSolver solver) {
	return polynomial.roots(solver);
    }

    /**
     * 
     * @param r
     * @return
     */
    public BackFilter times(final BackFilter r) {
	Polynomial p = polynomial.times(r.polynomial);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @return
     */
    public BackFilter times(final double d) {
	Polynomial p = polynomial.times(d);
	return new BackFilter(p);
    }

    @Override
    public String toString() {
	return polynomial.toString('B', true);
    }
}