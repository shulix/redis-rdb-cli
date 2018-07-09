package com.moilioncircle.redis.cli.tool.ext;

import com.moilioncircle.redis.cli.tool.glossary.Guard;
import com.moilioncircle.redis.cli.tool.io.GuardOutputStream;
import com.moilioncircle.redis.cli.tool.util.OutputStreams;
import com.moilioncircle.redis.replicator.io.RawByteListener;

import java.io.IOException;
import java.io.OutputStream;

import static com.moilioncircle.redis.replicator.util.CRC64.crc64;
import static com.moilioncircle.redis.replicator.util.CRC64.longToByteArray;

/**
 * @author Baoyi Chen
 */
@SuppressWarnings("unchecked")
public class GuardRawByteListener implements RawByteListener {
    private int version;
    private GuardOutputStream out;
    private OutputStream internal;

    public GuardRawByteListener(int cap, OutputStream internal) {
        this.internal = internal;
        this.out = new GuardOutputStream(cap, internal);
    }

    public <T extends OutputStream> T getOutputStream() {
        return (T) this.internal;
    }

    public GuardRawByteListener(byte type, int version, int cap, OutputStream internal) throws IOException {
        this.internal = internal;
        this.out = new GuardOutputStream(cap, internal);
        this.out.write((int) type);
        this.version = version;
    }

    public void reset(OutputStream out) {
        this.internal = out;
        this.out.reset(out);
    }

    public void setGuard(Guard guard) {
        this.out.setGuard(guard);
    }

    @Override
    public void handle(byte... raw) {
        OutputStreams.writeQuietly(raw, out);
    }

    public byte[] getBytes() throws IOException {
        this.out.write((byte) version);
        this.out.write((byte) 0x00);
        byte[] bytes = this.out.array();
        byte[] crc = longToByteArray(crc64(bytes));
        this.out.write(crc);
        return this.out.array();
    }
}