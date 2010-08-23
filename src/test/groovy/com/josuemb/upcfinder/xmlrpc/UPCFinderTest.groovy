package com.josuemb.upcfinder.xmlrpc

import groovy.util.GroovyTestCase
import com.josuemb.upcfinder.*

class UPCFinderTest extends GroovyTestCase {

  protected void setUp() throws Exception {
  }

  void testInvalidFinderSite(){
    def sites = UPCFinder.upcFinders
    UPCFinder.upcFinders = [
    new UPCFinderSite(
        uri:{upc->"http://www.invalid.url/rpc"},
        findUPC:{remote,upc->remote.lookupEAN(upc)},
        upcFounded:{response->response.get("message")=="Database entry found"},
        getInformation:{remote,upc,response->response}
    )
    ]        
    UPCFinder.find("7501003631046") == null
    UPCFinder.upcFinders = sites
  }

  void testUPCFound() {
    assert UPCFinder.find("7501003631046") != null
    assert UPCFinder.find("7501003693679") != null
    assert UPCFinder.find("7501017004003") != null
  }

  void testUPCNotFound() {
    assert UPCFinder.find("7501053670637") == null
    assert UPCFinder.find("7501032332365") == null
    assert UPCFinder.find("7501032332013") == null
  }

  void testInvalidUPC() {
    assert UPCFinder.find("hasdhfasd77asdf") == null
    assert UPCFinder.find("7501032332017") == null
    assert UPCFinder.find("75") == null
  }

  protected void tearDown() throws Exception {
  }
}
