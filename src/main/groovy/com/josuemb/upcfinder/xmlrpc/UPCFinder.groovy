/*
 * Copyright 2003-2010 the original author or authors.
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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.net.xmlrpc.*
import com.josuemb.upcfinder.UPCFinderSite

/**
 * <p>Class for finding product information using UPC using XML-RPC sites.</p>
 * <p>Find information for a product using UPC code (bar code). For example:</p>
 * <pre>
 * def information = UPCFinder.find('7501003631046')
 * </pre>
 * <p>It can be used from command line as:</p>
 * <pre>
 * java -cp build/libs/upcfinder.jar com.josuemb.upcfinder.xmlrpc.UPCFinder 7501003631046
 * </pre>
 * <p>Note: When information cannot foud it returns null value.
 * @author Josue Martinez Buenrrostro<josuemb@gmail.com>
 * @see com.josuemb.upcfinder.UPCFinderSite
 * @see <a href="http://en.wikipedia.org/wiki/XML-RPC">XML-RPC</a>
 */
class UPCFinder {

  /**
  * <p>Logger for the class.</p>
  * @see logback.qos.ch
  * @see <a href="http://logback.qos.ch">logback.qos.ch</a>
  */  
  static final Logger logger = LoggerFactory.getLogger(UPCFinder.class)
	
  /**
  * <p>Configuration for different UPC finder sites.</p>
  * <p>Each site could be configurated as a UPCFinderSite into array. Example:</p>
  * <pre>
  *static upcFinders = [
  *    new UPCFinderSite(
  *        uri:{upc->"http://www.upcdatabase.com/rpc"},
  *  			findUPC:{remote,upc->remote.lookupEAN(upc)},
  *        upcFounded:{response->response.get("message")=="Database entry found"},
  *        getInformation:{remote,upc,response->response}
  *    )
  *]
  * </pre>
  * @see com.josuemb.upcfinder.UPCFinderSite
  */  
	static upcFinders = [
	    new UPCFinderSite(
	        uri:{upc->"http://www.upcdatabase.com/rpc"},
    			findUPC:{remote,upc->remote.lookupEAN(upc)},
	        upcFounded:{response->response.get("message")=="Database entry found"},
	        getInformation:{remote,upc,response->"Hello"}
	    )
	]
	
  /**
  * <p>Find for a upc (bar code) using a specific UPC find site.</p>
  * @param upcFinderSite UPC Finder Site
  * @param upc Universal Product Code
  * @see com.josuemb.upcfinder.UPCFinderSite
  * @see <a href="http://en.wikipedia.org/wiki/Universal_Product_Code">upc</a>
  * @return Product information
  */  
  static Object find(UPCFinderSite upcFinderSite, String upc){
      def info = null
      try {
          logger.info "Finding upc=$upc"
    			def uri = upcFinderSite.uri(upc)
          logger.debug "uri=$uri"
		      def remote = new XMLRPCServerProxy(uri)
		      def response = upcFinderSite.findUPC(remote, upc)
          logger.debug "response=$response"
		      def upcFounded = upcFinderSite.upcFounded(response)
          logger.debug  "upcFounded=$upcFounded"
          if(upcFounded==true) {
              info = response
          }            
      } catch(e){
          logger.error e.dump()
          info = null
      }
      return info
  }

  /**
  * <p>Find for a upc (bar code) using all configurated UPC find sites.</p>
  * @param upc Universal Product Code
  * @see <a href="http://en.wikipedia.org/wiki/Universal_Product_Code">upc</a>
  * @return Product information
  */  
	static Object find(String upc){
        def info = null
        logger.info "Finding UPC... > find(String upc)"
        upcFinders.each{upcFinderSite ->
            if(info==null)
                info = find(upcFinderSite, upc)
        }
        return info
    }

  /**
  * <p>Find for a upc (bar code) using all configurated UPC find sites.</p>
  * @param upcs Universal Product Code array
  * @see <a href="http://en.wikipedia.org/wiki/Universal_Product_Code">upc</a>
  * @return Product information
  */  
	static Object find(String[] upcs){
        def info = null
        def response = null
        logger.info "Finding UPC... > find(String[] upc)"        
        upcs.each{upc->
          upcFinders.each{upcFinderSite ->
              if(info == null)
                  info = find(upcFinderSite, upc)
          }
          if(info != null) {
            if(response == null)
              response = [:]
            response.put upc, info
          }
        }
        return response
    }

  /**
  * <p>Find for upc (bar code) using all configurated UPC find sites.</p>
  * <p>This method can be called from command line as:</p>
  * <pre>
  * java -cp build/libs/upcfinder.jar com.josuemb.upcfinder.xmlrpc.UPCFinder 7501003631046
  * </pre>
  * @param args Universal Product Code list
  * @see <a href="http://en.wikipedia.org/wiki/Universal_Product_Code">upc</a>
  * @return One line for each product with upc=information format 
  */  
	static main(args) {
		if(args.size() < 1) {
			println "UPCFinder code1[, code2, ..., code n]"
		}
		def itemInformation
		args.each {upcCode ->
			itemInformation = find(upcCode)
			println "$upcCode=$itemInformation"
		}
	}
}
