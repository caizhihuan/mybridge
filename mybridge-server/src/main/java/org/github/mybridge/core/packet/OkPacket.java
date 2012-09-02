package org.github.mybridge.core.packet;

import org.github.mybridge.core.buffer.ByteBuffer;

/**
 * <pre>
 * From server to client in response to command, if no error and no result set.
 * 
 * VERSION 4.0
 * Bytes                       Name
 * -----                       ----
 * 1   (Length Coded Binary)   field_count, always = 0
 * 1-9 (Length Coded Binary)   affected_rows
 * 1-9 (Length Coded Binary)   insert_id
 * 2                           server_status
 * n   (until end of packet)   message
 * 
 * VERSION 4.1
 * Bytes                       Name
 * -----                       ----
 * 1   (Length Coded Binary)   field_count, always = 0
 * 1-9 (Length Coded Binary)   affected_rows
 * 1-9 (Length Coded Binary)   insert_id
 * 2                           server_status
 * 2                           warning_count
 * n   (until end of packet)   message
 * 
 * field_count:     always = 0
 * 
 * affected_rows:   = number of rows affected by INSERT/UPDATE/DELETE
 * 
 * insert_id:       If the statement generated any AUTO_INCREMENT number, 
 *                 the number is returned here. Otherwise this field contains 0.
 *                 Note: when using for example a multiple row INSERT the
 *                 insert_id will be from the first row inserted, not from
 *                 last.
 * 
 * server_status:   = The client can use this to check if the
 *                 command was inside a transaction.
 * 
 * warning_count:   number of warnings
 * 
 * message:         For example, after a multi-line INSERT, message might be
 *                 "Records: 3 Duplicates: 0 Warnings: 0"
 * The message field is optional.
 * 
 * Alternative terms: OK Packet is also known as "okay packet" or "ok packet" or "OK-Packet". field_count is also known as "number of rows" or "marker for ok packet". message is also known as "Messagetext". OK Packets (and result set packets) are also called "Result packets".
 * 
 * Relevant files in MySQL source:
 * (client) sql/client.c mysql_read_query_result()
 * (server) sql/protocol.cc send_ok()
 * Example OK Packet
 *                     Hexadecimal                ASCII
 *                     -----------                -----
 * field_count         00                         .
 * affected_rows       01                         .
 * insert_id           00                         .
 * server_status       02 00                      ..
 * warning_count       00 00                  
 *     ..
 * In the example, the optional message field is missing (the client can determine this by examining the packet length). This is a packet that the server returns after a successful INSERT of a single row that contains no auto_increment columns.
 * </pre>
 * 
 * @author xiaog
 * 
 */
public class OkPacket extends BasePacket {
	public byte type = 0;
	public long affectedRows = 0;
	public long insertId = 0;
	public int serverStatus = 0;
	public int warningCount = 0;
	public String message = "";

	@Override
	public byte[] getBytes() {
		int len = 1 + ByteBuffer.getLCBLen(affectedRows)
				+ ByteBuffer.getLCBLen(insertId) + 4
				+ message.getBytes().length;
		ByteBuffer buf = new ByteBuffer(len);
		buf.putByte(type);
		buf.putLCB(affectedRows);
		buf.putLCB(insertId);
		buf.putUInt16(serverStatus);
		buf.putUInt16(warningCount);
		buf.putRemainString(message);
		return buf.getBytes();
	}

	@Override
	public void putBytes(byte[] bs) {

	}

}
