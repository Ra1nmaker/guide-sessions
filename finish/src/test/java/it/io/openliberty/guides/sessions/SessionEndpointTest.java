// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package it.io.openliberty.guides.sessions;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SessionEndpointTest {
		private Client client;
    private static String server1port = System.getProperty("liberty.server1.port");
    private static String server2port = System.getProperty("liberty.server2.port");
    private final static String ITEM = "SpaceShip";
    private final static String PRICE = "20.0";

    @Before
    public void setup() {
        client = ClientBuilder.newClient();
    }

    @After
    public void teardown() {
        client.close();
    }

    // tag::testEmptyCart[]
    @Test
    public void testEmptyCart() {
        Response response = getResponse(fromCartURL(server1port), null);
        assertResponse(fromCartURL(server1port), response);

        String actual = response.readEntity(String.class);
        String expected = "[]";

        assertEquals("The cart should be empty on application start but was not", expected, actual);

        response.close();
    }
    // end::testEmptyCart[]
    // tag::testMatch[]
    @Test
    public void testMatchOneServer() {
        Response toCartResponse = getResponse(toCartURL(server1port), null);

        Map<String, NewCookie> cookies = toCartResponse.getCookies();
        Cookie cookie = ((NewCookie) cookies.values().iterator().next()).toCookie();
        Response fromCartResponse = getResponse(fromCartURL(server1port), cookie);

        String actualToCart = toCartResponse.readEntity(String.class);
        String expectedToCart = ITEM + " added to your cart and costs $" + PRICE;

        String actualFromCart = fromCartResponse.readEntity(String.class);
        String expectedFromCart = "[" + ITEM + " | $" + PRICE + "]";

        assertEquals("Adding item to cart response failed", expectedToCart, actualToCart);
        assertEquals("Cart response did not match expected string",expectedFromCart, actualFromCart);

        toCartResponse.close();
        fromCartResponse.close();
    }
    // end::testMatch[]
    // tag::testMatch[]
    @Test
     public void testMatchTwoServers() {
         Response toCartResponse = getResponse(toCartURL(server1port), null);

         Map<String, NewCookie> cookies = toCartResponse.getCookies();
         Cookie cookie = ((NewCookie) cookies.values().iterator().next()).toCookie();
         Response fromCartResponse = getResponse(fromCartURL(server2port), cookie);

         String actualToCart = toCartResponse.readEntity(String.class);
         String expectedToCart = ITEM + " added to your cart and costs $" + PRICE;

         String actualFromCart = fromCartResponse.readEntity(String.class);
         String expectedFromCart = "[" + ITEM + " | $" + PRICE + "]";

         assertEquals("Adding item to cart response failed", expectedToCart, actualToCart);
         assertEquals("Cart response did not match expected string", expectedFromCart, actualFromCart);

         toCartResponse.close();
         fromCartResponse.close();
     }
    // end::testMatch[]
    private Response getResponse(String url, Cookie cookie) {
        if(cookie == null){
            return client.target(url).request().get();
        } else{
            return client.target(url).request().cookie(cookie).get();
        }
    }

    private void assertResponse(String url, Response response) {
        assertEquals("Incorrect response code from " + url, 200, response.getStatus());
    }

    private String toCartURL(String port) {
    	return "http://localhost:" + port + "/SessionsGuide/sessions/cart/" + ITEM + "&" + PRICE;
    }
    
    private String fromCartURL(String port) {
    	return "http://localhost:" + port + "/SessionsGuide/sessions/cart";
    }
}
