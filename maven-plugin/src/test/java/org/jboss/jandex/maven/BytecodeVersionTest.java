package org.jboss.jandex.maven;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Release builds (as well as downstream rebuilds) of Jandex are supposed to be built using JDK 17,
 * so that the {@code test-data} module doesn't have to download a second JDK during the build process,
 * but we want to make sure those builds still produce Java 8 bytecode.
 */
public class BytecodeVersionTest {
    @Test
    public void verifyJava8() throws IOException {
        try (InputStream in = BytecodeVersionTest.class.getResourceAsStream("/org/jboss/jandex/maven/JandexGoal.class")) {
            if (in == null) {
                fail("Could not find org.jboss.jandex.maven.JandexGoal");
            }

            DataInputStream data = new DataInputStream(new BufferedInputStream(in));
            verifyMagic(data);
            verifyVersion(data);
        }
    }

    private void verifyMagic(DataInputStream stream) throws IOException {
        byte[] buf = new byte[4];

        stream.readFully(buf);
        if (buf[0] != (byte) 0xCA || buf[1] != (byte) 0xFE || buf[2] != (byte) 0xBA || buf[3] != (byte) 0xBE) {
            fail("Invalid magic value: " + Arrays.toString(buf));
        }
    }

    private void verifyVersion(DataInputStream stream) throws IOException {
        int minor = stream.readUnsignedShort();
        int major = stream.readUnsignedShort();

        if (major != 52) { // Java 8
            fail("Unexpected class file format version: " + major + "." + minor + ", Jandex must be Java 8 bytecode");
        }
    }
}
