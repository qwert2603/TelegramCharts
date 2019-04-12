package telegram_charts;

import com.qwert2603.telegram_charts.chart_delegates.ChartViewDelegateBars;
import com.qwert2603.telegram_charts.Utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChartViewTest {

    @Test
    public void te1() {
        assertEquals("0", ChartViewDelegateBars.formatY(0));
        assertEquals("1", ChartViewDelegateBars.formatY(1));
        assertEquals("14", ChartViewDelegateBars.formatY(14));
        assertEquals("102", ChartViewDelegateBars.formatY(102));
        assertEquals("142", ChartViewDelegateBars.formatY(142));
        assertEquals("1.0K", ChartViewDelegateBars.formatY(1000));
        assertEquals("1.0K", ChartViewDelegateBars.formatY(1040));
        assertEquals("1.3K", ChartViewDelegateBars.formatY(1300));
        assertEquals("1.3K", ChartViewDelegateBars.formatY(1380));
        assertEquals("1.3K", ChartViewDelegateBars.formatY(1389));
        assertEquals("135.3K", ChartViewDelegateBars.formatY(135389));
        assertEquals("1.3M", ChartViewDelegateBars.formatY(1353869));
        assertEquals("2.0M", ChartViewDelegateBars.formatY(2009999));
        assertEquals("1.0M", ChartViewDelegateBars.formatY(1053869));
        assertEquals("3.0M", ChartViewDelegateBars.formatY(3053869));
    }

    @Test
    public void t2() {
        assertEquals(0, Utils.commonEnd("12 фев. 2018", "12 мар. 2019"));
        assertEquals(4, Utils.commonEnd("12 фев. 2019", "12 мар. 2019"));
        assertEquals(9, Utils.commonEnd("23 фев. 2019", "12 фев. 2019"));
        assertEquals(9, Utils.commonEnd("23 фев. 2019", "24 фев. 2019"));
        assertEquals(12, Utils.commonEnd("24 фев. 2019", "24 фев. 2019"));
    }
}