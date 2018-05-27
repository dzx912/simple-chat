package org.training.chat.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import org.training.chat.data.CommonMessage;

/**
 * Codec для передачи CommonMessage
 */
public class CommonMessageCodec implements MessageCodec<CommonMessage, CommonMessage> {
    @Override
    public void encodeToWire(Buffer buffer, CommonMessage customMessage) {
        // Encode object to string
        String jsonToStr = Json.encode(customMessage);

        // Length of JSON: is NOT characters count
        int length = jsonToStr.getBytes().length;

        // Write data into given buffer
        buffer.appendInt(length);
        buffer.appendString(jsonToStr);
    }

    @Override
    public CommonMessage decodeFromWire(int position, Buffer buffer) {
        // Length of JSON
        int dataLength = buffer.getInt(position);

        // Get JSON string by it`s data length
        // Jump 4 because getInt() == 4 bytes
        final int SIZE_INT = 4;
        int startShiftToIntSize = position + SIZE_INT;
        int finishShitToLengthData = startShiftToIntSize + dataLength;

        String jsonStr = buffer.getString(startShiftToIntSize, finishShitToLengthData);
        return Json.decodeValue(jsonStr, CommonMessage.class);
    }

    @Override
    public CommonMessage transform(CommonMessage commonMessage) {
        // If a message is sent *locally* across the event bus.
        // This example sends message just as is
        return commonMessage;
    }

    @Override
    public String name() {
        // Each codec must have a unique name.
        // This is used to identify a codec when sending a message and for unregistering codecs.
        return this.getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        // Always -1
        return -1;
    }
}