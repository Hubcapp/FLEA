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

package flea.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class Crypto {
    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;

    private static final int XTEA_NUM_ROUNDS = 32;
    private static final int XTEA_DELTA = 0x9e3779b9;

    public static void init() {
        generateRSAKeys();
        loadRSAKeys();
    }

    public static BigInteger getPublicExponent() {
        return publicKey.getPublicExponent();
    }

    public static BigInteger getPublicModulus() {
        return publicKey.getModulus();
    }

    public static byte[] decryptXTEA(byte[] data, int offset, int length, int[] keys) {
        byte[] ret = new byte[length];
        int blocks = length / 8;

        ByteBuffer input = ByteBuffer.wrap(data, offset, length);
        ByteBuffer output = ByteBuffer.wrap(ret);
        for (int i = 0; i < blocks; i++) {
            int v0 = input.getInt();
            int v1 = input.getInt();

            int sum = XTEA_NUM_ROUNDS * XTEA_DELTA;
            for (int j = 0; j < XTEA_NUM_ROUNDS; j++) {
                v1 -= (((v0 << 4) ^ (v0 >>> 5)) + v0) ^ (sum + keys[(sum >>> 11) & 3]);
                sum -= XTEA_DELTA;
                v0 -= (((v1 << 4) ^ (v1 >>> 5)) + v1) ^ (sum + keys[sum & 3]);
            }

            output.putInt(v0);
            output.putInt(v1);
        }

        output.put(input);

        return ret;
    }

    public static byte[] decryptRSA(byte[] data, int offset, int length) {
        byte newData[] = new byte[length];
        System.arraycopy(data, offset, newData, 0, length);
        return new BigInteger(newData).modPow(privateKey.getPrivateExponent(), privateKey.getModulus()).toByteArray();
    }

    public static void generateRSAKeys() {
        try {
            File clientKeyFile = new File("client.pem");
            File serverKeyFile = new File("server.pem");

            if(!clientKeyFile.exists() || !serverKeyFile.exists()) {
                KeyPairGenerator keyPairGenerator;
                keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(512);
                KeyPair keyPair = keyPairGenerator.genKeyPair();
                PemObject pemObject = new PemObject("PUBLIC KEY", keyPair.getPublic().getEncoded());
                PemWriter output = new PemWriter(new OutputStreamWriter(new FileOutputStream(clientKeyFile)));
                output.writeObject(pemObject);
                output.close();
                pemObject = new PemObject("PRIVATE KEY", keyPair.getPrivate().getEncoded());
                output = new PemWriter(new OutputStreamWriter(new FileOutputStream(serverKeyFile)));
                output.writeObject(pemObject);
                output.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadRSAKeys() {
        try {
            PEMParser reader = new PEMParser(new FileReader(new File("client.pem")));
            PemObject pemObject = reader.readPemObject();
            reader.close();
            publicKey = (RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pemObject.getContent()));

            reader = new PEMParser(new FileReader(new File("server.pem")));
            pemObject = reader.readPemObject();
            reader.close();
            privateKey = (RSAPrivateKey)KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pemObject.getContent()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
