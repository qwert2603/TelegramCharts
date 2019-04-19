package telegram_charts;

import com.qwert2603.telegram_charts.Utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChartViewTest {

    @Test
    public void te1() {
        assertEquals("0", Utils.formatY(0));
        assertEquals("1", Utils.formatY(1));
        assertEquals("14", Utils.formatY(14));
        assertEquals("102", Utils.formatY(102));
        assertEquals("142", Utils.formatY(142));
        assertEquals("1.0K", Utils.formatY(1000));
        assertEquals("1.0K", Utils.formatY(1040));
        assertEquals("1.3K", Utils.formatY(1300));
        assertEquals("1.3K", Utils.formatY(1380));
        assertEquals("1.3K", Utils.formatY(1389));
        assertEquals("135.3K", Utils.formatY(135389));
        assertEquals("1.3M", Utils.formatY(1353869));
        assertEquals("2.0M", Utils.formatY(2009999));
        assertEquals("1.0M", Utils.formatY(1053869));
        assertEquals("3.0M", Utils.formatY(3053869));
    }

    @Test
    public void t2() {
        assertEquals(0, Utils.commonEnd("12 фев. 2018", "12 мар. 2019"));
        assertEquals(4, Utils.commonEnd("12 фев. 2019", "12 мар. 2019"));
        assertEquals(9, Utils.commonEnd("23 фев. 2019", "12 фев. 2019"));
        assertEquals(9, Utils.commonEnd("23 фев. 2019", "24 фев. 2019"));
        assertEquals(12, Utils.commonEnd("24 фев. 2019", "24 фев. 2019"));
    }


    @Test
    public void t3() {
        assertEquals(16, Utils.floorToPowerOf2(16));
        assertEquals(16, Utils.floorToPowerOf2(17));
        assertEquals(16, Utils.floorToPowerOf2(22));
        assertEquals(16, Utils.floorToPowerOf2(31));
        assertEquals(32, Utils.floorToPowerOf2(32));
        assertEquals(32, Utils.floorToPowerOf2(42));
        assertEquals(1, Utils.floorToPowerOf2(1));
        assertEquals(2, Utils.floorToPowerOf2(2));
        assertEquals(2, Utils.floorToPowerOf2(3));
        assertEquals(0, Utils.floorToPowerOf2(0));
    }
}