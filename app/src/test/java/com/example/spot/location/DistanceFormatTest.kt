package com.example.spot.location

import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceFormatTest {
    private val nbsp = '\u00A0'

    @Test fun shortFormHasNoAway() {
        assertEquals("200${nbsp}m", Distance.formatShort(203.0))
        assertEquals("10${nbsp}m", Distance.formatShort(2.0)) // never "0 m"
        assertEquals("1.2${nbsp}km", Distance.formatShort(1_234.0))
        assertEquals("120${nbsp}km", Distance.formatShort(120_400.0))
    }

    @Test fun longFormUnchanged() {
        assertEquals("200${nbsp}m${nbsp}away", Distance.format(203.0))
        assertEquals("1.2${nbsp}km${nbsp}away", Distance.format(1_234.0))
    }
}
