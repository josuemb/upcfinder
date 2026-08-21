/*
 * Copyright 2003-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.josuemb.upcfinder.xmlrpc

import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * <p>Class for finding product information using UPC codes via the UPCitemdb REST API.</p>
 * <p>Find information for a product using UPC code (bar code). For example:</p>
 * <pre>
 * def information = UPCFinder.find('0049000006346')
 * </pre>
 * <p>It can be used from command line as:</p>
 * <pre>
 * java -cp build/libs/upcfinder.jar com.josuemb.upcfinder.xmlrpc.UPCFinder 0049000006346
 * </pre>
 * <p>Note: When information cannot be found it returns null value.
 * <p>Uses the UPCitemdb trial API (100 requests/day, no API key required).</p>
 * @author Josue Martinez Buenrrostro<josuemb@gmail.com>
 * @see <a href="https://www.upcitemdb.com/wp/docs/main/development/api/">UPCitemdb API</a>
 */
class UPCFinder {

    /**
     * Logger for the class.
     */
    static final Logger logger = LoggerFactory.getLogger(UPCFinder.class)

    /**
     * Base URL for the UPCitemdb trial API.
     */
    static final String API_BASE_URL = 'https://api.upcitemdb.com/prod/trial/lookup'

    /**
     * Maximum number of retries when rate-limited.
     */
    static final int MAX_RETRIES = 3

    /**
     * Delay in milliseconds between retries when rate-limited.
     */
    static final long RETRY_DELAY_MS = 7000

    /**
     * <p>Look up a single UPC code via the UPCitemdb REST API.</p>
     * @param upc Universal Product Code
     * @see <a href="http://en.wikipedia.org/wiki/Universal_Product_Code">UPC</a>
     * @return Map with product information (title, brand, description, etc.) or null if not found
     */
    static Map find(String upc) {
        logger.info "Finding upc=$upc"
        int retries = 0

        while (retries <= MAX_RETRIES) {
            try {
                def url = "${API_BASE_URL}?upc=${URLEncoder.encode(upc, 'UTF-8')}"
                logger.debug "url=$url"

                def connection = new URL(url).openConnection()
                connection.setRequestProperty('Accept', 'application/json')
                connection.setRequestProperty('User-Agent', 'UPCFinder/1.0')
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                def responseCode = connection.responseCode
                logger.debug "responseCode=$responseCode"

                String responseText
                if (responseCode == 200) {
                    responseText = connection.inputStream.text
                } else if (responseCode == 429) {
                    // Rate limited via HTTP status
                    logger.warn "Rate limited (HTTP 429) for upc=$upc, retry ${retries + 1}/${MAX_RETRIES}"
                    retries++
                    if (retries <= MAX_RETRIES) {
                        Thread.sleep(RETRY_DELAY_MS)
                        continue
                    }
                    return null
                } else {
                    // Try to read error stream
                    responseText = connection.errorStream?.text
                    if (!responseText) {
                        logger.warn "API returned HTTP $responseCode for upc=$upc with no body"
                        return null
                    }
                }

                logger.debug "response=$responseText"

                def jsonSlurper = new JsonSlurper()
                def jsonResponse = jsonSlurper.parseText(responseText)

                // Handle rate limiting in JSON response
                if (jsonResponse.code == 'TOO_FAST') {
                    logger.warn "Rate limited (TOO_FAST) for upc=$upc, retry ${retries + 1}/${MAX_RETRIES}"
                    retries++
                    if (retries <= MAX_RETRIES) {
                        Thread.sleep(RETRY_DELAY_MS)
                        continue
                    }
                    return null
                }

                if (jsonResponse.code != 'OK' || jsonResponse.total == 0 || !jsonResponse.items) {
                    logger.info "UPC not found: upc=$upc, code=${jsonResponse.code}, total=${jsonResponse.total}"
                    return null
                }

                def item = jsonResponse.items[0]
                def productInfo = [
                    title      : item.title ?: '',
                    brand      : item.brand ?: '',
                    description: item.description ?: '',
                    upc        : item.upc ?: upc,
                    ean        : item.ean ?: '',
                    model      : item.model ?: '',
                    color      : item.color ?: '',
                    size       : item.size ?: '',
                    dimension  : item.dimension ?: '',
                    weight     : item.weight ?: '',
                    category   : item.category ?: '',
                    currency   : item.currency ?: '',
                    lowest_price : item.lowest_recorded_price ?: '',
                    highest_price: item.highest_recorded_price ?: '',
                    images     : item.images ?: [],
                ]

                logger.info "Found product: ${productInfo.title} (brand: ${productInfo.brand})"
                return productInfo

            } catch (Exception e) {
                logger.error "Error looking up UPC $upc: ${e.message}"
                return null
            }
        }
        return null
    }

    /**
     * <p>Find product info for multiple UPC codes.</p>
     * @param upcs Array of Universal Product Codes
     * @return Map of upc -> product info for found items, or null if none found
     */
    static Map find(String[] upcs) {
        def foundProducts = [:]
        logger.info "Finding UPC codes: ${upcs}"
        upcs.each { upc ->
            def productInfo = find(upc)
            if (productInfo != null) {
                foundProducts[upc] = productInfo
            }
        }
        return foundProducts.isEmpty() ? null : foundProducts
    }

    /**
     * <p>Command line entry point.</p>
     * <pre>
     * java -cp build/libs/upcfinder.jar com.josuemb.upcfinder.xmlrpc.UPCFinder 0049000006346
     * </pre>
     * @param args Universal Product Code list
     */
    static main(args) {
        if (args.size() < 1) {
            println "UPCFinder code1[, code2, ..., code n]"
            return
        }
        args.each { upcCode ->
            def itemInformation = find(upcCode)
            println "$upcCode=$itemInformation"
        }
    }
}
