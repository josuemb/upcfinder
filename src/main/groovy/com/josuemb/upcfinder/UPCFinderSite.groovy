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

package com.josuemb.upcfinder


/**
 * <p>Configuration for a UPC Finder Site</p>
 * @author Josue Martinez Buenrrostro<josuemb@gmail.com>
 * @see com.josuemb.upcfinder.xmlrpc.UPCFinder
 */
class UPCFinderSite {
  /**
  * <p>Closure returning URI for the Site. Example: uri:{upc->"http://www.upcdatabase.com/rpc"},
  * @see java.net.URI
  */	
  Closure uri
  /**
  * <p>Closure for validating UPC was found. Example: findUPC:{remote,upc->remote.lookupEAN(upc)}
  */	
	Closure findUPC
  /**
  * <p>Closure finding UPC. Example: upcFounded:{response->response.get("message")=="Database entry found"}</p>
  */	
	Closure upcFounded
  /**
  * <p>Closure getting information founded. Example: getInformation:{remote,upc,response->response}
  */	
	Closure getInformation
}
