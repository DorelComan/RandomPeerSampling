/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tum.group34.protocol;

/**
 *
 * @author totakura
 */
public class MessageParserException extends RuntimeException {

    public MessageParserException(String description) {
        super(description);
    }

    public MessageParserException() {
        super();
    }
}
