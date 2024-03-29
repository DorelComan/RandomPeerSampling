/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tum.group34.protocol;

import java.nio.ByteBuffer;

/**
 *
 * @author Sree Harsha Totakura
 */
public class StreamTokenizer {

    private enum ParseState {
        SIZE,
        BODY
    }

    private ParseState state;
    private final MessageHandler handler;
    private int expect;

    public StreamTokenizer(MessageHandler handler) {
        this.reset();
        this.handler = handler;
    }

    public final void reset() {
        this.state = ParseState.SIZE;
        this.expect = Protocol.SIZE_LENGTH;
    }

    /**
     * Extracts message tokens from the given input. If the input contains
     * multiple messages, they are tokenized in the order they appear in the
     * input. Tokens are then parsed into messages which are then given to the
     * message handler.
     *
     * @param buf the buffer containing the data stream to be tokenized.
     * @return If the given input is in-sufficient to construct a message, True
     * is returned signifying that more input is needed. If sufficient input is
     * provided, false is returned. If the input contains multiple messages,
     * then the sufficiency test is made for the last message in the input.
     * @throws ProtocolException
     * @throws MessageParserException
     */
    public boolean input(ByteBuffer buf)
            throws ProtocolException, MessageParserException {
        ByteBuffer tokenizedCopy;

        while (true) {
            if (buf.remaining() < this.expect) {
                return true;
            }
            switch (this.state) {
                case SIZE:
                    assert (Protocol.SIZE_LENGTH == this.expect);
                    buf.mark();
                    this.expect = Message.getUnsignedShort(buf);
                    buf.reset();
                    if (Protocol.MAX_MESSAGE_SIZE < this.expect) {
                        this.reset();
                        throw new ProtocolException("Protocol message is > 64KB");
                    }
                    this.state = ParseState.BODY;
                    continue;
                case BODY:
                    tokenizedCopy = buf.slice();
                    buf.position(buf.position() + this.expect);
                    handler.parseMessage(tokenizedCopy);
                    this.reset();
                    if (buf.remaining() == 0) {
                        return false;
                    }
            }
        }
    }
}
