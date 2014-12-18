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
package ec.tstoolkit.maths.polynomials;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.IntUtilities;
import ec.tstoolkit.maths.Simplifying;
import ec.tstoolkit.utilities.IntList;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class UnitRoots implements Cloneable {

    /**
     *
     */
    public static class SimplifyingTool extends Simplifying<UnitRoots> {

        /**
         *
         */
        public SimplifyingTool() {
        }

        // START CHANGED 21/6/07. JP
	/*
         * @Override public boolean simplify(final UnitRoots left, final
         * UnitRoots right) { m_left = left; m_right = right; m_common = new
         * UnitRoots();
         * 
         * if ((left == null) || (right == null)) return false; if
         * (!left.isValid() || !right.isValid()) return false; if
         * (left.isIdentity() || right.isIdentity()) return false; m_left = new
         * UnitRoots(left); m_right = new UnitRoots(right); // A || B
         * 
         * int ldiff = m_left.m_n.getSize() - m_left.m_d.getSize(), rdiff =
         * m_right.m_n .getSize() - m_right.m_d.getSize();
         * 
         * searchnum(); searchdenom(); // if (m_common.IsIdentity) // return
         * false;
         * 
         * m_right.simplify(); m_left.simplify();
         * 
         * step1();
         * 
         * m_left.simplify(); m_right.simplify();
         * 
         * step2();
         * 
         * m_left.simplify(); m_right.simplify();
         * 
         * step3(ldiff, rdiff); // finally, adjust for D1; if
         * (!m_common.isIdentity()) return true; else return false; }
         * 
         * void searchnum() { int kl = 0, kr = 0; // search common factors in
         * the Numerator while ((kl < m_left.m_n.getSize()) && (kr <
         * m_right.m_n.getSize())) { int lu = m_left.m_n.get(kl), ru =
         * m_right.m_n.get(kr); if ((lu == 1) || (ru == 1)) break; if (lu == ru)
         * { m_common.add(lu); m_right.remove(lu); m_left.remove(lu); ++kl;
         * ++kr; } else if (lu > ru) ++kl; else ++kr; } }
         * 
         * void searchdenom() { int kl = 0; int kr = 0; // search common factors
         * in the Denominator while ((kl < m_left.m_d.getSize()) && (kr <
         * m_right.m_d.getSize())) { int lu = m_left.m_d.get(kl), ru =
         * m_right.m_d.get(kr); if ((lu == 1) || (ru == 1)) break; if (lu == ru)
         * { m_common.remove(lu); m_right.add(lu); m_left.add(lu); ++kl; ++kr; }
         * else if (lu > ru) ++kl; else ++kr; } }
         * 
         * private void step1() { // search pgcd factors in the Numerator
         * 
         * int[] lnmax = new int[m_left.m_n.getSize()], rnmax = new
         * int[m_right.m_n .getSize()]; for (int i = 0; i < lnmax.length; ++i)
         * lnmax[i] = m_left.m_n.get(i); for (int i = 0; i < rnmax.length; ++i)
         * rnmax[i] = m_right.m_n.get(i);
         * 
         * for (int il = 0; il < m_left.m_n.getSize();) { if (m_left.m_n.get(il)
         * == 1) break;
         * 
         * int irbest = -1; int pmax = 1; for (int ir = 0; ir <
         * m_right.m_n.getSize(); ++ir) { if (m_right.m_n.get(ir) == 1) break;
         * int pcur = IntUtilities.PGCD(lnmax[il], rnmax[ir]); if ((irbest ==
         * -1) || (pcur >= pmax)) // always chose the // last // one... { pmax =
         * pcur; irbest = ir; } } if (pmax > 1) { m_common.add(pmax);
         * m_left.remove(pmax); m_right.remove(pmax); lnmax[il] =
         * nextdiv(m_left.m_n.get(il), lnmax[il], pmax); rnmax[irbest] =
         * nextdiv(m_right.m_n.get(irbest), rnmax[irbest], pmax); // don't
         * increment il. Second pass } else ++il; } }
         * 
         * private void step2() { // add denominators without counterparts.
         * 
         * if (!m_left.calcmaps()) { int[] tmp = m_left.m_dp; for (int i = 0; i
         * < tmp.length; ++i) { int u = m_left.m_d.get(i); if (u == 1) break; if
         * (tmp[i] < 0) { m_common.remove(u); m_left.add(u); m_right.add(u); } }
         * }
         * 
         * if (!m_right.calcmaps()) { int[] tmp = m_right.m_dp; for (int i = 0;
         * i < tmp.length; ++i) { int u = m_right.m_d.get(i); if (u == 1) break;
         * if ((tmp[i] < 0) && (u != 1)) { m_common.remove(u); m_left.add(u);
         * m_right.add(u); } } } }
         * 
         * private void step3(final int ldiff, final int rdiff) { int cdiff =
         * ldiff < rdiff ? ldiff : rdiff; int curdiff = m_common.m_n.getSize() -
         * m_common.m_d.getSize(); if (curdiff < cdiff) // not enough ur in c {
         * for (int i = curdiff; i < cdiff; ++i) { m_common.add(1);
         * m_left.remove(1); m_right.remove(1); } } else if (curdiff > cdiff) {
         * for (int i = cdiff; i < curdiff; ++i) { m_common.remove(1);
         * m_left.add(1); m_right.add(1); } } }
         */
        @Override
        public boolean simplify(final UnitRoots left, final UnitRoots right) {
            m_left = left;
            m_right = right;
            m_common = new UnitRoots();

            if ((left == null) || (right == null)) {
                return false;
            }
            if (!left.isValid() || !right.isValid()) {
                return false;
            }
            if (left.isIdentity() || right.isIdentity()) {
                return false;
            }
            m_left = left.clone();
            m_right = right.clone();
            // A || B

            int ldiff = m_left.m_n.getSize() - m_left.m_d.getSize(), rdiff = m_right.m_n.getSize()
                    - m_right.m_d.getSize();
            m_left.calcmaps();
            m_right.calcmaps();

            // common nums are removed, with all the corresponding denoms
            step1();
            step2(ldiff, rdiff);
            // finally, adjust for D1;
            if (!m_common.isIdentity()) {
                return true;
            } else {
                return false;
            }
        }

        private void step1() {
            // search common factors in the Numerator
            int lu = 0, ru = 0;
            do {
                int kl = 0, kr = 0;
                int pgcd = 0;
                while (kl < m_left.m_n.getSize() && kr < m_right.m_n.getSize()) {
                    lu = m_left.m_n.get(kl);
                    ru = m_right.m_n.get(kr);
                    pgcd = IntUtilities.gcd(lu, ru);
                    if (pgcd > 1 || lu == 1 || ru == 1) {
                        break;
                    } else if (lu > ru) {
                        ++kl;
                    } else {
                        ++kr;
                    }
                }
                if (pgcd > 1) {
                    // search possible common denominators
                    IntList d = new IntList();
                    for (int i = 0; i < m_left.m_dp.length; ++i) {
                        int dcur = IntUtilities.gcd(m_left.m_d.get(i), lu);
                        if (m_left.m_dp[i] == kl && !d.contains(dcur)) {
                            d.add(dcur);
                        }
                    }

                    for (int i = 0; i < m_right.m_dp.length; ++i) {
                        int dcur = IntUtilities.gcd(m_right.m_d.get(i), ru);
                        if (m_right.m_dp[i] == kr && !d.contains(dcur)) {
                            d.add(dcur);
                        }
                    }
                    int prod = 1;
                    for (int i = 0; i < d.size(); i++) {
                        prod *= d.get(i);
                    }

                    if (pgcd % prod != 0) {
                        break;
                    }

                    m_common.add(pgcd);
                    m_right.remove(pgcd);
                    m_left.remove(pgcd);
                    for (int i = 0; i < d.size(); i++) {
                        int id = d.get(i);
                        m_left.add(id);
                        m_right.add(id);
                        m_common.remove(id);
                    }
                    m_left.simplify();
                    m_left.calcmaps();
                    m_right.simplify();
                    m_right.calcmaps();
                    m_common.simplify();
                } else {
                    break;
                }
            } while (true);
        }

        private void step2(final int ldiff, final int rdiff) {
            int cdiff = ldiff < rdiff ? ldiff : rdiff;
            int curdiff = m_common.m_n.getSize() - m_common.m_d.getSize();
            if (curdiff < cdiff) {
                for (int i = curdiff; i < cdiff; ++i) {
                    m_common.add(1);
                    m_left.remove(1);
                    m_right.remove(1);
                }
            } else if (curdiff > cdiff) {
                for (int i = cdiff; i < curdiff; ++i) {
                    m_common.remove(1);
                    m_left.add(1);
                    m_right.add(1);
                }
            }
        }
        // END CHANGED 21/6/07. JP
    }
    /**
     * 1 - x
     */
    public static final Polynomial D1 = Polynomial.valueOf(1, -1);

    /**
     * 1 - x^n
     *
     * @param n
     * @return
     */
    public static Polynomial D(final int n) {
        double[] p = Polynomial.Doubles.fromDegree(n);
        p[n] = -1;
        p[0] = 1;
        return Polynomial.of(p);
    }

    /**
     * Computes (1 - x^lag)^d
     *
     * @param lag
     * @param d
     * @return
     */
    public static Polynomial D(final int lag, final int d) {
        if (d == 0) {
            return Polynomial.ONE;
        }
        Polynomial P = UnitRoots.D(lag);
        Polynomial D = P;
        for (int i = 1; i < d; ++i) {
            D = D.times(P);
        }
        return D;
    }

    private static int div(final double[] c, final int nc, final int d) {
        for (int i = d; i < nc; ++i) {
            c[i] += c[i - d];
        }
        for (int i = nc - d; i < nc; ++i) {
            if (Math.abs(c[i]) > g_epsilon) {
                return -1;
            }
        }
        return nc - d;

    }

    private static int div(final int[] c, final int nc, final int d) {
        for (int i = nc - 1; i >= d; --i) {
            c[i - d] += c[i];
            c[i] = -c[i];
        }
        for (int i = 0; i < d; ++i) {
            if (c[i] != 0) {
                return -1;
            }
        }

        for (int i = d; i < nc; ++i) {
            c[i - d] = c[i];
        }
        return nc - d;
    }

    /**
     *
     * @param p
     * @param ur
     * @return
     */
    public static Polynomial divide(final Polynomial p, final UnitRoots ur) {
        if (p.getDegree() < ur.getRootsCount()) {
            return null;
        }
        double[] tmp = new double[p.getDegree() + 1 + ur.getDenomDegree()];
        int nc = p.getDegree() + 1;
        for (int i = 0; i < nc; ++i) {
            tmp[i] = p.get(i);
        }
        int imax = ur.m_d == null ? 0 : ur.m_d.getSize();
        for (int i = 0; i < imax; ++i) {
            nc = mul(tmp, nc, ur.m_d.get(i));
        }
        imax = ur.m_n == null ? 0 : ur.m_n.getSize();
        for (int i = 0; i < imax; ++i) {
            nc = div(tmp, nc, ur.m_n.get(i));
            if (nc < 0) {
                return null;
            }
        }

        return Polynomial.of(Arrays.copyOf(tmp, nc));
    }

    private static int mul(final double[] c, final int nc, final int d) {
        for (int i = nc - 1; i >= 0; --i) {
            c[i + d] -= c[i];
        }
        return nc + d;
    }

    private static int mul(final int[] c, final int nc, final int d) {
        for (int i = nc - 1; i >= 0; --i) {
            c[i + d] -= c[i];
        }
        return nc + d;
    }

    /**
     *
     * @param p
     * @param ur
     * @return
     */
    public static Polynomial multiply(final Polynomial p, final UnitRoots ur) {
        double[] tmp = new double[p.getDegree() + 1 + ur.getNumDegree()];
        int nc = p.getDegree() + 1;
        for (int i = 0; i < nc; ++i) {
            tmp[i] = p.get(i);
        }
        int imax = ur.m_n == null ? 0 : ur.m_n.getSize();
        for (int i = 0; i < imax; ++i) {
            nc = mul(tmp, nc, ur.m_n.get(i));
        }
        imax = ur.m_d == null ? 0 : ur.m_d.getSize();
        for (int i = 0; i < imax; ++i) {
            nc = div(tmp, nc, ur.m_d.get(i));
        }

        return Polynomial.of(Arrays.copyOf(tmp, nc));
    }

    /**
     *
     * @param ur
     * @param p
     * @return
     */
    public static Polynomial multiply(final UnitRoots ur, final Polynomial p) {
        return multiply(p, ur);
    }

    /**
     * find the first divisor of x after d which is prime with p...
     *
     * @param x
     * @param d
     * @param p
     * @return 0 if no divisor is find
     */
    private static int nextdiv(final int x, final int d, final int p) {
        // should be optimized for large value of x
        for (int nd = d - 1; nd > 1; --nd) {
            if ((x % nd == 0) && (IntUtilities.gcd(nd, p) == 1)) {
                return nd;
            }
        }
        return 0;
    }

    /**
     * 1 + x^d + x^(2*d) + ... + x^(n*d) n=freq-1
     *
     * @param n
     * @param d
     * @return
     */
    public static Polynomial S(final int freq, final int d) {
        double[] p = Polynomial.Doubles.fromDegree((freq - 1) * d);
        for (int i = 0, j = 0; i < freq; ++i, j += d) {
            p[j] = 1;
        }
        return Polynomial.of(p);
    }

    /**
     * Computes (1 + x^d + x^(2*d) + ... + x^(n*d))^p
     *
     * @param freq
     * @param d
     * @return
     */
    public static Polynomial S(final int freq, final int d, final int p) {
        if (p == 0) {
            return Polynomial.ONE;
        }
        Polynomial s = S(freq, d);
        Polynomial q = s;
        for (int i = 1; i < p; ++i) {
            q = q.times(s);
        }
        return q;
    }

    private IVector m_n, m_d;
    private int[] m_dp;
    private final static double g_epsilon = 1e-6;
    @SuppressWarnings("unused")
    private static final UnitRootsSolver g_default = initDefault();

    private static UnitRootsSolver initDefault() {
        UnitRootsSolver def = new UnitRootsSolver();
        /*
         * ChainedRootsSearcher crf = new ChainedRootsSearcher(); crf.add(def);
         * Polynomial.setDefRootsSearcher(crf);
         */
        return def;
    }

    /**
     * Creates new UnitRoots
     */
    public UnitRoots() {
        m_n = new IVector();
        m_d = new IVector();
    }

    private UnitRoots(final IVector n, final IVector d) {
        m_n = n;
        m_d = d;
    }

    /**
     * Adds a set of unit roots
     *
     * @param ur The ur-th roots are added (equivalent to multiply by (1 - x^ur)
     */
    public void add(final int ur) {
        m_n.add(ur);
        internalclear();
    }

    /**
     * Adds another UnitRoots (equivalent to this *= ur)
     *
     * @param ur The added UnitRoots
     */
    public void add(final UnitRoots ur) {
        m_n.add(ur.m_n);
        m_d.add(ur.m_d);
        internalclear();
    }

    /**
     * Adds the x-roots of 1 that are not y-roots of 1, where y is a divisor of
     * x. For example, if x = 6, the polynomial (1 - x^6)(1 - x)/(1 - x^2)(1 -
     * x^3) = x^2 - x - 1 is added
     *
     * @param divs
     * @param cur
     */
    private void addonly(final int[] divs, final int cur) {
        int d = divs[cur];
        m_n.add(d);
        for (int j = cur - 1; j >= 0; --j) {
            if (d % divs[j] == 0) {
                removeonly(divs, j);
            }
        }
    }

    /**
     *
     * @param num
     * @param div
     */
    public void addOnly(final int num, final int div) {
        int n = 2;
        while (n * n < num) {
            ++n;
        }

        int[] divs = new int[n + 1];
        int ndivs = IntUtilities.divisors(num, divs);
        int i = 0;
        while ((divs[i] != div) && (i < ndivs)) {
            ++i;
        }
        if (i != ndivs) {
            addonly(divs, i);
        }
        internalclear();
    }

    /**
     *
     * @param divs
     * @param cur
     */
    public void addOnly(final int[] divs, final int cur) {
        addonly(divs, cur);
        internalclear();
    }

    /**
     * rslts from i0+nroots to i1
     *
     * @param data
     * @param i0
     * @param i1
     */
    public void backFilter(final double[] data, final int i0, final int i1) {
        // first, the differences (from the last obs to the first one)
        for (int i = 0; i < m_n.getSize(); ++i) {
            int d = m_n.get(i);
            for (int j = i1 - 1; j >= i0 + d; --j) {
                data[j] -= data[j - d];
            }
        }

        // and than the sums
        for (int i = 0; i < m_d.getSize(); ++i) {
            int s = m_d.get(i);
            for (int j = i0 + s; j < i1; ++j) {
                data[j] += data[j - s];
            }
        }
    }

    private boolean calcmaps() {
        int nn = m_n.getSize(), nd = m_d.getSize();
        if ((nn == 0) || (m_dp != null)) {
            return true;
        }
        int[] nmax = new int[nn];
        for (int i = 0; i < nn; ++i) {
            nmax[i] = m_n.get(i);
        }
        m_dp = new int[nd];
        for (int i = 0; i < nd; ++i) {
            m_dp[i] = -1;
        }

        boolean ok = true;
        for (int i = 0; i < nd; ++i) {
            if (m_d.get(i) == 1) {
                break;
            }

            int j = 0;
            for (; j < nn; ++j) {
                if ((nmax[j] != 0) && (nmax[j] % m_d.get(i) == 0)) {
                    // ok
                    m_dp[i] = j;
                    nmax[j] = nextdiv(m_n.get(j), nmax[j], m_d.get(i));
                    break;
                }
            }
            if (j == nn) {
                ok = false;
            }
        }
        return ok;

    }

    private boolean check() {
        if (m_dp != null) {
            return true;
        }
        simplify();
        if (m_d.getSize() > m_n.getSize()) {
            return false;
        }
        if (calcmaps()) {
            return true;
        } else {
            internalclear();
            return false;
        }
    }

    /**
     * Resets the UnitRoots (this = 1)
     */
    public void clear() {
        m_n.clear();
        m_d.clear();
        internalclear();
    }

    @Override
    public UnitRoots clone() {
        try {
            UnitRoots ur = (UnitRoots) super.clone();
            ur.m_d = m_d.clone();
            ur.m_n = m_n.clone();
            if (m_dp != null) {
                ur.m_dp = m_dp.clone();
            }
            return ur;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    /**
     *
     * @return
     */
    public double[] coefficients() {
        int[] tmp = new int[1 + getNumDegree()];
        int nc = 1;
        tmp[0] = 1;
        for (int i = 0; i < m_n.getSize(); ++i) {
            nc = mul(tmp, nc, m_n.get(i));
        }
        for (int i = 0; i < m_d.getSize(); ++i) {
            nc = div(tmp, nc, m_d.get(i));
        }
        double[] mc = new double[nc];
        for (int i = 0; i < nc; ++i) {
            mc[i] = tmp[i];
        }
        return mc;
    }

    /**
     * returns the different UR and their power. Each UR is followed by its
     * power: a1, power(a1), a2, power(a2), ...
     *
     * @return
     */
    public int[] denominator() {
        check();
        return m_d.RPowers();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof UnitRoots && equals((UnitRoots) obj));
    }

    private boolean equals(UnitRoots other) {
        return m_n.equals(other.m_n) && m_d.equals(other.m_d);
    }

    /**
     *
     * @param back
     * @param data
     * @return
     */
    public double[] filter(final boolean back, final double[] data) {
        if (data == null) {
            return null;
        }
        double[] tmp = data.clone();
        int i0 = 0, i1 = data.length;
        if (back) {
            backFilter(tmp, i0, i1);
        } else {
            filter(tmp, i0, i1);
        }
        i0 = getNumDegree();
        i1 -= getDenomDegree();
        double[] rslt = new double[i1 - i0];
        for (int i = 0; i < rslt.length; ++i) {
            rslt[i] = tmp[i0 + i];
        }
        return rslt;
    }

    /**
     * rslts from i0 to i1-nroots
     *
     * @param data
     * @param i0
     * @param i1
     */
    public void filter(final double[] data, final int i0, final int i1) {
        // first, the differences (from the last obs to the first one)
        for (int i = 0; i < m_n.getSize(); ++i) {
            int d = m_n.get(i);
            for (int j = i0; j < i1 - d; ++j) {
                data[j] -= data[j + d];
            }
        }

        // and than the sums
        for (int i = 0; i < m_d.getSize(); ++i) {
            int s = m_d.get(i);
            for (int j = i1 - s - 1; j >= i0; --j) {
                data[j] += data[j + s];
            }
        }

    }

    /**
     * The Idx-th item of the denominator
     *
     * @param idx
     * @return
     */
    public int getD(final int idx) {
        return m_d.get(idx);
    }

    /**
     * Number of items on the denominator
     *
     * @return
     */
    public int getDenomCount() {
        return m_d.getSize();
    }

    /**
     * Degree of the denominator (= sum of the items of the denominator)
     *
     * @return
     */
    public int getDenomDegree() {
        int d = 0;
        for (int i = 0; i < m_d.getSize(); ++i) {
            d += m_d.get(i);
        }
        return d;
    }

    /**
     * The Idx-th item of the numerator
     *
     * @param idx
     * @return
     */
    public int getN(final int idx) {
        return m_n.get(idx);
    }

    /**
     * Number of items on the numerator
     *
     * @return
     */
    public int getNumCount() {
        return m_n.getSize();
    }

    /**
     * Degree of the numerator (= sum of the items of the numerator)
     *
     * @return
     */
    public int getNumDegree() {
        int d = 0;
        for (int i = 0; i < m_n.getSize(); ++i) {
            d += m_n.get(i);
        }
        return d;
    }

    /**
     * Number of roots (= degree of the polynomial = NumDegree - DenomDegree)
     *
     * @return
     */
    public int getRootsCount() {
        int d = 0;
        for (int i = 0; i < m_n.getSize(); ++i) {
            d += m_n.get(i);
        }
        for (int i = 0; i < m_d.getSize(); ++i) {
            d -= m_d.get(i);
        }
        return d;
    }

    @Override
    public int hashCode() {
        return m_n.hashCode() + m_d.hashCode();
    }

    private void internalclear() {
        m_dp = null;
    }

    /**
     *
     * @return
     */
    public boolean isIdentity() {
        check();
        return (m_n.getSize() == 0) && (m_d.getSize() == 0);
    }

    /**
     * Checks if the current object is valid
     *
     * @return
     */
    public boolean isValid() {
        return check();
    }

    /**
     * returns the different UR and their power. Each UR is followed by its
     * power: a1, power(a1), a2, power(a2), ...
     *
     * @return
     */
    public int[] numerator() {
        check();
        return m_n.RPowers();
    }

    /**
     * Removes as set of unit roots
     *
     * @param ur The ur-th roots are removed (equivalent to divide by (1 - x^ur)
     */
    public void remove(final int ur) {
        m_d.add(ur);
        internalclear();
    }

    /**
     * Removes another UnitRoots (equivalent to this /= ur)
     *
     * @param ur The removed UnitRoots
     */
    public void remove(final UnitRoots ur) {
        m_n.add(ur.m_d);
        m_d.add(ur.m_n);
        internalclear();
    }

    /**
     * Removes the x-roots of 1 that are not y-roots of 1, where y is a divisor
     * of x. For example, if x = 6, the polynomial (1 - x^6)(1 - x)/(1 - x^2)(1
     * - x^3) = x^2 - x - 1 is removed
     *
     * @param divs
     * @param cur
     */
    private void removeonly(final int[] divs, final int cur) {
        int d = divs[cur];
        m_d.add(d);
        for (int j = cur - 1; j >= 0; --j) {
            if (d % divs[j] == 0) {
                addonly(divs, j);
            }
        }
    }

    /**
     *
     * @param num
     * @param div
     */
    public void removeOnly(final int num, final int div) {
        int n = 2;
        while (n * n < num) {
            ++n;
        }

        int[] divs = new int[2 * n];
        int ndivs = IntUtilities.divisors(num, divs);
        int i = 0;
        while ((divs[i] != div) && (i < ndivs)) {
            ++i;
        }
        if (i != ndivs) {
            removeonly(divs, i);
        }
        internalclear();
    }

    /**
     *
     * @param divs
     * @param cur
     */
    public void removeOnly(final int[] divs, final int cur) {
        removeonly(divs, cur);
        internalclear();
    }

    // should be improved !!!
    /**
     *
     * @return
     */
    public Complex[] roots() {
        /*
         * Complex[] ntmp = new Complex[NumDegree]; Complex[] dtmp = new
         * Complex[DenomDegree]; for (int i = 0, j = 0; i < m_n.getSize(); ++i)
         * { int n = m_n[i]; double x = 2 * System.Math.PI; x /= n; ntmp[j++] =
         * new Complex(1); if (n % 2 == 0) ntmp[j++] = new Complex(-1); for (int
         * k = 1; k < (n + 1) / 2; ++k) { Complex c = new
         * Complex(System.Math.Cos(k * x), System.Math.Sin(k * x)); ntmp[j++] =
         * c; ntmp[j++] = c.Conj(); } }
         * 
         * for (int i = 0, j = 0; i < m_d.getSize(); ++i) { int n = m_d[i];
         * double x = 2 * System.Math.PI; x /= n; dtmp[j++] = new Complex(1); if
         * (n % 2 == 0) dtmp[j++] = new Complex(-1); for (int k = 1; k < (n + 1)
         * / 2; ++k) { Complex c = new Complex(System.Math.Cos(k * x),
         * System.Math.Sin(k * x)); dtmp[j++] = c; dtmp[j++] = c.Conj(); } }
         * Complex.SimplifyingTool smp = new Complex.SimplifyingTool(); if
         * (smp.Simplify(ntmp, dtmp)) return smp.Left; else return ntmp;
         */

        int nroots = getRootsCount();
        if (nroots == 0) {
            return null;
        }
        Complex[] roots = new Complex[nroots];
        int ppcm = m_n.get(0);
        for (int i = 1; i < m_n.getSize(); ++i) {
            ppcm = IntUtilities.lcm(ppcm, m_n.get(i));
        }
        double m = Math.PI * 2 / ppcm;
        int imax = ppcm / 2;
        for (int i = 0, k = 0; i <= imax; ++i) {
            int nr = 0;
            for (int j = 0; j < m_n.getSize(); ++j) {
                if (i % (ppcm / m_n.get(j)) == 0) {
                    ++nr;
                }
            }
            for (int j = 0; j < m_d.getSize(); ++j) {
                if (i % (ppcm / m_d.get(j)) == 0) {
                    --nr;
                }
            }
            if (nr > 0) {
                Complex root;
                if (i == 0) {
                    root = Complex.ONE;
                } else if (ppcm == 2 * i) {
                    root = Complex.NEG_ONE;
                } else {
                    root = Complex.cart(Math.cos(m * i), Math.sin(m * i));
                }
                if (root.getIm() == 0) {
                    for (int j = 0; j < nr; ++j) {
                        roots[k++] = root;
                    }
                } else {
                    for (int j = 0; j < nr; ++j) {
                        roots[k++] = root;
                        roots[k++] = root.conj();
                    }
                }
            }
        }

        return roots;
    }

    private void simplify() // we suppose the arrays are sorted
    {
        if (IVector.simplify(m_n, m_d) != 0) {
            internalclear();
        }
    }

    /**
     * Square root. y * y = this.
     *
     * @return Could be null
     */
    public UnitRoots sqrt() {
        simplify();
        IVector n = m_n.sqrt();
        if (n == null) {
            return null;
        }
        IVector d = m_d.sqrt();
        if (d == null) {
            return null;
        }
        UnitRoots ur = new UnitRoots(n, d);
        return ur;
    }

    /**
     * this * this
     *
     * @return The square of this object
     */
    public UnitRoots squared() {
        simplify();
        return new UnitRoots(m_n.squared(), m_d.squared());
    }

    /**
     *
     * @param r
     * @return
     */
    public UnitRoots times(final UnitRoots r) {
        UnitRoots rslt = clone();
        rslt.add(r);
        rslt.simplify();
        return rslt;
    }

    /**
     *
     * @return
     */
    public Polynomial toPolynomial() {
        return Polynomial.of(coefficients());
    }
}