package com.lumen.app

import com.lumen.app.vision.PerceptualHash
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerceptualHashTest {

    private val hasher = PerceptualHash()

    @Test
    fun `identical hashes have zero hamming distance and full similarity`() {
        val hash = 0b1010101010101010L
        assertEquals(0, hasher.hammingDistance(hash, hash))
        assertEquals(100, hasher.similarityPercent(hash, hash))
    }

    @Test
    fun `fully inverted hashes have maximum hamming distance`() {
        val a = 0L
        val b = -1L // all bits set
        assertEquals(64, hasher.hammingDistance(a, b))
        assertEquals(0, hasher.similarityPercent(a, b))
    }

    @Test
    fun `similarity decreases as bits differ`() {
        val a = 0b0000L
        val bCloser = 0b0001L
        val bFarther = 0b1111L
        assertTrue(hasher.similarityPercent(a, bCloser) > hasher.similarityPercent(a, bFarther))
    }
}
