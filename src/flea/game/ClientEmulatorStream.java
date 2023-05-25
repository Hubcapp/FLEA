/**
 * FLEA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Authors: Based on rscminus server by Ornox, <https://github.com/RSCPlus/rscminus>
 *          FLEA was created by Logg aka Hubcapp, <https://github.com/Hubcapp/FLEA>
 *          In preparation for OpenRSC, <https://gitlab.com/open-runescape-classic/FLEA>
 */

package flea.game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

// Mostly copied from 204 deob.
// It is stripped down networking for just the purpose
// of copying bytes given to it by an actual client.

public class ClientEmulatorStream implements Runnable {
    private InputStream instream;
    private OutputStream outstream;
    private Socket socket;
    private boolean closing;
    private byte buffer[];
    private int endoffset;
    private int offset;
    private boolean closed;
    protected boolean socketException;
    protected String socketExceptionMessage;

    public ClientEmulatorStream(String serverAddress, int port)
            throws IOException {
        closing = false;
        closed = true;
        this.socket = createSocket(serverAddress, port);
        instream = socket.getInputStream();
        outstream = socket.getOutputStream();
        closed = false;
        socketException = false;
        socketExceptionMessage = "";
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    protected Socket createSocket(String serverAddress, int port) throws IOException {
        Socket socket = new Socket(InetAddress.getByName(serverAddress), port);
        socket.setSoTimeout(30000);
        socket.setTcpNoDelay(true);
        return socket;
    }

    public void closeStream() {
        closing = true;
        try {
            if (instream != null)
                instream.close();
            if (outstream != null)
                outstream.close();
            if (socket != null)
                socket.close();
        } catch (IOException Ex) {
            System.out.println("Error closing stream");
        }
        closed = true;
        synchronized (this) {
            notify();
        }
        buffer = null;
    }

    public int readStream()
            throws IOException {
        if (closing)
            return 0;
        else
            return instream.read();
    }

    public int availableStream()
            throws IOException {
        if (closing)
            return 0;
        else
            return instream.available();
    }

    public void readStreamBytes(int len, int off, byte buff[])
            throws IOException {
        if (closing)
            return;
        int k = 0;
        boolean flag = false;
        int read = 0;
        for (; k < len; k += read)
            if ((read = instream.read(buff, k + off, len - k)) <= 0)
                throw new IOException("EOF");
    }

    public void writeStreamBytes(byte buff[], int off, int len)
            throws IOException {
        if (closing)
            return;
        if (buffer == null)
            buffer = new byte[5000];
        synchronized (this) {
            for (int l = 0; l < len; l++) {
                buffer[offset] = buff[l + off];
                offset = (offset + 1) % 5000;
                if (offset == (endoffset + 4900) % 5000)
                    throw new IOException("buffer overflow");
            }

            notify();
        }
    }

    @Override
    public void run() {
        System.out.println("??? it's running!");
        while (!closed) {
            System.out.println("??? it's not closed!");
            int len;
            int off;
            synchronized (this) {
                if (offset == endoffset)
                    try {
                        wait();
                    } catch (InterruptedException Ex) {
                    }
                if (closed)
                    return;
                off = endoffset;
                if (offset >= endoffset)
                    len = offset - endoffset;
                else
                    len = 5000 - endoffset;
            }
            if (len > 0) {
                try {
                    outstream.write(buffer, off, len);
                } catch (IOException ioexception) {
                    socketException = true;
                    socketExceptionMessage = "Twriter:" + ioexception;
                }
                endoffset = (endoffset + len) % 5000;
                try {
                    if (offset == endoffset)
                        outstream.flush();
                } catch (IOException ioexception1) {
                    socketException = true;
                    socketExceptionMessage = "Twriter:" + ioexception1;
                }
            }
        }
    }
}
