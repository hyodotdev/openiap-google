package dev.hyo.openiap.models

import org.junit.Assert.assertEquals
import org.junit.Test

class ProductRequestTypeTest {

    @Test
    fun productRequestType_acceptsHyphenAlias() {
        val result = ProductRequest.ProductRequestType.fromString("in-app")
        assertEquals(ProductRequest.ProductRequestType.InApp, result)
    }

    @Test
    fun openIapProductRequestType_acceptsHyphenAlias() {
        val result = OpenIapProductRequest.ProductRequestType.fromString("in-app")
        assertEquals(OpenIapProductRequest.ProductRequestType.InApp, result)
    }

    @Test
    fun productRequestType_handlesLegacyAliasUntilRemoval() {
        val result = ProductRequest.ProductRequestType.fromString("inapp")
        assertEquals(ProductRequest.ProductRequestType.InApp, result)
    }

    @Test
    fun openIapProductRequestType_handlesLegacyAliasUntilRemoval() {
        val result = OpenIapProductRequest.ProductRequestType.fromString("inapp")
        assertEquals(OpenIapProductRequest.ProductRequestType.InApp, result)
    }

    @Test
    fun productRequestTypes_exposeHyphenatedValue() {
        assertEquals("in-app", ProductRequest.ProductRequestType.InApp.value)
        assertEquals("in-app", OpenIapProductRequest.ProductRequestType.InApp.value)
        assertEquals("in-app", OpenIapProduct.ProductType.InApp.value)
    }
}
