/*
 * Copyright (C) 2016 Sree Harsha Totakura <sreeharsha@totakura.in>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tum.group34.protocol.gossip;

import de.tum.group34.protocol.Message;
import de.tum.group34.protocol.MessageParserException;
import de.tum.group34.protocol.Protocol;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 *
 * @author Sree Harsha Totakura <sreeharsha@totakura.in>
 */
public class NotifyMessage extends ApiMessage {
    protected int datatype;
    protected int reserved;

    public NotifyMessage(int reserved, int datatype) {
        assert (65535 >= datatype);
        super.addHeader(Protocol.MessageType.API_GOSSIP_NOTIFY);
        this.reserved = reserved;
        this.datatype = datatype;
        this.size += 2 + 2; //2 reserved; 2 datatype
    }

    public NotifyMessage(int datatype) {
        this(0, datatype);
    }

    public int getDatatype() {
        return datatype;
    }

    @Override
    public void send(ByteBuffer out) {
        super.send(out);
        out.putShort((short) reserved);
        out.putShort((short) datatype);
    }

    public static ApiMessage parse(ByteBuffer buf) throws MessageParserException {
        ApiMessage message;
        int datatype;

        try {
        buf.position(buf.position() + 6);//skip over the reserved part and header for US
        datatype = Message.unsignedIntFromShort(buf.getShort());
            message = new NotifyMessage(datatype);
        } catch (BufferUnderflowException | IllegalArgumentException exp) {
            throw new MessageParserException();
        }
        return message;
    }

}
