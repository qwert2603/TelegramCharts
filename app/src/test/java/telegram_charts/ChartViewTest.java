package telegram_charts;

import com.qwert2603.telegram_charts.ChartView;
import com.qwert2603.telegram_charts.Utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChartViewTest {

    @Test
    public void te1() {
        assertEquals("0", ChartView.formatY(0));
        assertEquals("1", ChartView.formatY(1));
        assertEquals("14", ChartView.formatY(14));
        assertEquals("102", ChartView.formatY(102));
        assertEquals("142", ChartView.formatY(142));
        assertEquals("1.0K", ChartView.formatY(1000));
        assertEquals("1.0K", ChartView.formatY(1040));
        assertEquals("1.3K", ChartView.formatY(1300));
        assertEquals("1.3K", ChartView.formatY(1380));
        assertEquals("1.3K", ChartView.formatY(1389));
        assertEquals("135.3K", ChartView.formatY(135389));
        assertEquals("1.3M", ChartView.formatY(1353869));
        assertEquals("2.0M", ChartView.formatY(2009999));
        assertEquals("1.0M", ChartView.formatY(1053869));
        assertEquals("3.0M", ChartView.formatY(3053869));
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