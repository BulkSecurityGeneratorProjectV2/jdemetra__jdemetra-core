/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.calendar.r;

import jdplus.math.matrices.Matrix;
import demetra.timeseries.calendars.DayEvent;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holiday;
import jdplus.timeseries.calendars.HolidaysUtility;
import demetra.timeseries.calendars.PrespecifiedHoliday;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.ValidityPeriod;

/**
 *
 * @author PALATEJ
 */
@Deprecated
public class Holidays {

    private final List<Holiday> holidays = new ArrayList<>();

    private boolean add(Holiday fday) {
        if (!holidays.contains(fday)) {
            holidays.add(fday);
            return true;
        } else {
            return false;
        }
    }

    private Holiday[] elements() {
        return holidays.toArray(new Holiday[holidays.size()]);
    }

    public boolean add(String holiday, int offset, double weight, boolean julian) {
        try {
            PrespecifiedHoliday cur = PrespecifiedHoliday.builder()
                    .event(DayEvent.valueOf(holiday))
                    .offset(offset)
                    .weight(weight)
                    .julian(julian)
                    .build();
            return add(cur);
        } catch (Exception err) {
            return false;
        }
    }

    public boolean addFixedDay(int month, int day, double weight, boolean julian) {
        FixedDay cur = new FixedDay(month, day, weight, ValidityPeriod.ALWAYS);
        return add(cur);
    }

    public MatrixType holidays(String date, int length, int[] nonworking, String type) {
        LocalDate start = LocalDate.parse(date);
        Holiday[] elements = elements();
        Matrix m = Matrix.make(length, elements.length);
        switch (type) {
            case "Skip":
                HolidaysUtility.fillDays(elements, m, start, nonworking, true);
                break;
            case "NextWorkingDay":
                HolidaysUtility.fillNextWorkingDays(elements, m, start, nonworking);
                break;
            case "PreviousWorkingDay":
                HolidaysUtility.fillPreviousWorkingDays(elements, m, start, nonworking);
                break;
            default:
                HolidaysUtility.fillDays(elements, m, start, nonworking, false);
        }
        return m.unmodifiable();
    }

}
