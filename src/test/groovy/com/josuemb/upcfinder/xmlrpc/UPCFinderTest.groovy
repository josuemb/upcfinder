package com.josuemb.upcfinder.xmlrpc

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import static org.junit.jupiter.api.Assertions.*

import java.util.concurrent.TimeUnit

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UPCFinderTest {

    @Test
    @Order(1)
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testUPCFound() {
        // Coca-Cola Classic 12oz can - well-known UPC
        def cocaColaInfo = UPCFinder.find("0049000006346")
        assertNotNull(cocaColaInfo, "Should find product info for Coca-Cola UPC")
        assertNotNull(cocaColaInfo.title, "Product should have a title")
        assertFalse(cocaColaInfo.title.isEmpty(), "Product title should not be empty")
        println "Found product: ${cocaColaInfo.title} (brand: ${cocaColaInfo.brand})"
    }

    @Test
    @Order(2)
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testInvalidUPC() {
        // Non-numeric gibberish - API returns INVALID_UPC code
        def invalidInfo = UPCFinder.find("INVALIDUPC")
        assertNull(invalidInfo, "Should return null for invalid UPC format")
    }
}
