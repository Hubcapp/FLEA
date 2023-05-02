/**
 * rscminus
 *
 * This file is part of rscminus.
 *
 * rscminus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * rscminus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with rscminus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * Authors: see <https://github.com/OrN/rscminus>
 */

package rscminus.common;

import rscminus.game.NetworkStream;
import rscminus.game.entity.player.ChatMessage;

import java.util.HashMap;

public class ChatCipher {

    /**
     * Unicode characters that are mapped to specific byte values
     * The array is for byte->char, the dict is for char->byte
     * Note: We found no evidence of these unicode characters in scraped replays.
     * While we can't be sure that the server would actually transmit these mapped
     * characters, it seems likely that it did.
     */
    private final static char[] specialCharacters = new char[] { '\u20ac', '?', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021', '\u02c6',
            '\u2030', '\u0160', '\u2039', '\u0152', '?', '\u017d', '?', '?', '\u2018', '\u2019', '\u201c',
            '\u201d', '\u2022', '\u2013', '\u2014', '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153', '?',
            '\u017e', '\u0178' };
    private final static HashMap<Character, Character> specialCharacterMap = new HashMap<>();

    /**
     * Used to build the cipher blocks. Copied from the client.
     * Represents the number of bits that each char 0-255 will encipher to
     */
    private final static byte[] init = new byte[] { 22, 22, 22, 22, 22, 22, 21, 22, 22, 20, 22, 22, 22, 21, 22, 22,
            22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 3, 8, 22, 16, 22, 16, 17, 7, 13, 13, 13, 16,
            7, 10, 6, 16, 10, 11, 12, 12, 12, 12, 13, 13, 14, 14, 11, 14, 19, 15, 17, 8, 11, 9, 10, 10, 10, 10, 11, 10,
            9, 7, 12, 11, 10, 10, 9, 10, 10, 12, 10, 9, 8, 12, 12, 9, 14, 8, 12, 17, 16, 17, 22, 13, 21, 4, 7, 6, 5, 3,
            6, 6, 5, 4, 10, 7, 5, 6, 4, 4, 6, 10, 5, 4, 4, 5, 7, 6, 10, 6, 10, 22, 19, 22, 14, 22, 22, 22, 22, 22, 22,
            22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
            22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
            22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
            22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
            22, 22, 22, 22, 22, 22, 21, 22, 21, 22, 22, 22, 21, 22, 22 };

    /**
     * Cipher blocks
     */
    private final static int[] cipherBlock = new int[init.length];
    private static int[] cipherDictionary = new int[8];

    /**
     * Used for both enciphering and deciphering chat
     */
    private static final char[] chatBuffer = new char[5096];

    private final static StringBuilder messageBuilder = new StringBuilder();

    /**
     * Builds the arrays needed by the algorithms
     */
   public static void init() {
        //Initialize the special character map
        for (int i=0; i < specialCharacters.length; ++i)
            specialCharacterMap.put(specialCharacters[i], (char)(i - 128));

        //Initialize cipher blocks
        final int[] blockBuilder = new int[33];
        int cipherDictIndexTemp = 0;
        for (int initPos = 0; initPos < init.length; ++initPos)
        {
            final int initValue = init[initPos];
            final int builderBitSelector = 1 << (32 - initValue);
            final int builderValue = blockBuilder[initValue];
            cipherBlock[initPos] = builderValue;
            int builderValueBit;
            if ((builderValue & builderBitSelector) == 0)
            {
                builderValueBit = builderValue | builderBitSelector;
                for (int initValueCounter = initValue - 1; initValueCounter > 0; --initValueCounter)
                {
                    final int builderValue2 = blockBuilder[initValueCounter];
                    if (builderValue != builderValue2)
                        break;
                    final int builderValue2BitSelector = 1 << (32 - initValueCounter);
                    if ((builderValue2 & builderValue2BitSelector) == 0)
                        blockBuilder[initValueCounter] = builderValue2BitSelector | builderValue2;
                    else
                    {
                        blockBuilder[initValueCounter] = blockBuilder[initValueCounter - 1];
                        break;
                    }
                }
            }
            else
            {
                builderValueBit = blockBuilder[initValue + -1];
            }
            blockBuilder[initValue] = builderValueBit;
            for (int initValueCounter = initValue + 1; initValueCounter <= 32; ++initValueCounter)
            {
                if (builderValue == blockBuilder[initValueCounter])
                    blockBuilder[initValueCounter] = builderValueBit;
            }
            int cipherDictIndex = 0;
            for (int initValueCounter = 0; initValueCounter < initValue; ++initValueCounter)
            {
                int builderBitSelector2 = 0x80000000 >>> initValueCounter;
                if ((builderValue & builderBitSelector2) == 0)
                    cipherDictIndex++;
                else
                {
                    if (cipherDictionary[cipherDictIndex] == 0)
                        cipherDictionary[cipherDictIndex] = cipherDictIndexTemp;

                    cipherDictIndex = cipherDictionary[cipherDictIndex];
                }
                if (cipherDictionary.length <= cipherDictIndex)
                {
                    final int[] newCipherDict = new int[cipherDictionary.length * 2];
                    System.arraycopy(cipherDictionary, 0, newCipherDict, 0, cipherDictionary.length);
                    cipherDictionary = newCipherDict;
                }
            }
            cipherDictionary[cipherDictIndex] = ~initPos;
            if (cipherDictIndex >= cipherDictIndexTemp)
                cipherDictIndexTemp = cipherDictIndex + 1;
        }
    }

    /**
     * Converts a string message into an encoded byte array
     * @param message message to be encoded
     * @param chatMessage players' chat message buffer
     */
    public static void encipher(String message, ChatMessage chatMessage) {
        convertMessageToBytes(message);
        int encipheredByte = 0;
        int outputBitOffset = 0;
        for (int messageIndex = 0; message.length() > messageIndex; ++messageIndex)
        {
            final int messageCharacter = chatBuffer[messageIndex] & 0xff;
            final int cipherBlockValue = cipherBlock[messageCharacter];
            final int initValue = init[messageCharacter];

            int outputByteOffset = outputBitOffset >> 3;
            int cipherBlockShifter = 0x7 & outputBitOffset;
            encipheredByte &= -cipherBlockShifter >> 31;
            final int outputByteOffset2 = outputByteOffset + ((cipherBlockShifter + initValue - 1) >> 3);
            outputBitOffset += initValue;
            cipherBlockShifter += 24;
            encipheredByte |= (cipherBlockValue >>> cipherBlockShifter);
            chatMessage.messageBuffer[outputByteOffset] = (byte) (encipheredByte);
            if (outputByteOffset2 > outputByteOffset)
            {
                outputByteOffset++;
                cipherBlockShifter -= 8;
                chatMessage.messageBuffer[outputByteOffset] = (byte) (encipheredByte = cipherBlockValue >>> cipherBlockShifter);
                if (outputByteOffset < outputByteOffset2)
                {
                    outputByteOffset++;
                    cipherBlockShifter -= 8;
                    chatMessage.messageBuffer[outputByteOffset] = (byte) (encipheredByte = cipherBlockValue >>> cipherBlockShifter);
                    if (outputByteOffset2 > outputByteOffset)
                    {
                        outputByteOffset++;
                        cipherBlockShifter -= 8;
                        chatMessage.messageBuffer[outputByteOffset] = (byte) (encipheredByte = cipherBlockValue >>> cipherBlockShifter);
                        if (outputByteOffset2 > outputByteOffset)
                        {
                            cipherBlockShifter -= 8;
                            outputByteOffset++;
                            chatMessage.messageBuffer[outputByteOffset] = (byte) (encipheredByte = cipherBlockValue << -cipherBlockShifter);
                        }
                    }
                }
            }
        }
        chatMessage.decipheredLength = message.length();
        chatMessage.encipheredLength = (((outputBitOffset + 7) >> 3));
    }

    /**
     * Reads an enciphered message from a network stream
     * @param m_packetStream network stream containing the message
     * @return decoded String containing the deciphered message
     */
    public static String decipher(final NetworkStream m_packetStream) {
        int decipheredLength = m_packetStream.readVariableSize();

        int bufferIndex = 0;
        int decipherIndex = 0;
        int cipherDictValue;

        while (bufferIndex < decipheredLength && m_packetStream.getAvailable() > 0)
        {
            final byte encipheredByte = m_packetStream.readByte();
            decipherIndex = encipheredByte < 0 ? cipherDictionary[decipherIndex] : decipherIndex + 1;

            if (0 > (cipherDictValue = cipherDictionary[decipherIndex]))
            {
                chatBuffer[bufferIndex++] =  (char)(~cipherDictValue);
                if (bufferIndex >= decipheredLength)
                    break;

                decipherIndex = 0;
            }

            int andVal = 0x40;
            while (andVal > 0) {
                decipherIndex = (encipheredByte & andVal) == 0 ? decipherIndex + 1 : cipherDictionary[decipherIndex];
                if ((cipherDictValue = cipherDictionary[decipherIndex]) < 0) {
                    chatBuffer[bufferIndex++] = (char)(~cipherDictValue);
                    if (bufferIndex >= decipheredLength)
                        break;

                    decipherIndex = 0;
                }
                andVal >>= 1;
            }
        }

        messageBuilder.setLength(0);
        //Swap bytes for unicode characters
        for (bufferIndex = 0; bufferIndex < decipheredLength; ++bufferIndex)
        {
            int bufferValue = chatBuffer[bufferIndex] & 0xFF;
            if (bufferValue != 0)
            {
                if (bufferValue >= 128 && bufferValue < 160)
                    bufferValue = specialCharacters[bufferValue - 128];

                messageBuilder.append((char)bufferValue);
            }
        }
        return messageBuilder.toString();
    }

    /**
     * Converts a String message into a char array and
     * replaces mapped unicode characters with the proper char
     * @param charSequence Message to convert. Gets stored in ChatCipher.chatBuffer
     */
    public static void convertMessageToBytes(CharSequence charSequence) {
        for (int messageIndex = 0; messageIndex < charSequence.length() ; ++messageIndex)
        {
            final char c = charSequence.charAt(messageIndex);
            if ((('\0' >= c) || ('\u0080' <= c)) && (('\u00a0' > c) || ('\u00ff' < c)))
                chatBuffer[messageIndex] = specialCharacterMap.getOrDefault(c, '?');
            else
                chatBuffer[messageIndex] = c;
        }
    }
}
