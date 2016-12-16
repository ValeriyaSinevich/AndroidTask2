package com.example.valeriyasin.authorization;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
//import android.util.Base64;
import android.util.Base64OutputStream;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutionException;


import com.example.valeriyasin.authorization.AuthorizationActivity;
import com.example.valeriyasin.authorization.R;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.valeriyasin.authorization.Utils.STATUS_OK;
import static com.example.valeriyasin.authorization.Utils.getResponseString;

import javax.net.ssl.HttpsURLConnection;

class Base64 {
    /**
     * Default values for encoder/decoder flags.
     */
    public static final int DEFAULT = 0;

    /**
     * Encoder flag bit to omit the padding '=' characters at the end
     * of the output (if any).
     */
    public static final int NO_PADDING = 1;

    /**
     * Encoder flag bit to omit all line terminators (i.e., the output
     * will be on one long line).
     */
    public static final int NO_WRAP = 2;

    /**
     * Encoder flag bit to indicate lines should be terminated with a
     * CRLF pair instead of just an LF.  Has no effect if {@code
     * NO_WRAP} is specified as well.
     */
    public static final int CRLF = 4;

    /**
     * Encoder/decoder flag bit to indicate using the "URL and
     * filename safe" variant of Base64 (see RFC 3548 section 4) where
     * {@code -} and {@code _} are used in place of {@code +} and
     * {@code /}.
     */
    public static final int URL_SAFE = 8;

    /**
     * Flag to pass to {@link Base64OutputStream} to indicate that it
     * should not close the output stream it is wrapping when it
     * itself is closed.
     */
    public static final int NO_CLOSE = 16;

    //  --------------------------------------------------------
    //  shared code
    //  --------------------------------------------------------

    /* package */ static abstract class Coder {
        public byte[] output;
        public int op;

        /**
         * Encode/decode another block of input data.  this.output is
         * provided by the caller, and must be big enough to hold all
         * the coded data.  On exit, this.opwill be set to the length
         * of the coded data.
         *
         * @param finish true if this is the final call to process for
         *        this object.  Will finalize the coder state and
         *        include any final bytes in the output.
         *
         * @return true if the input so far is good; false if some
         *         error has been detected in the input stream..
         */
        public abstract boolean process(byte[] input, int offset, int len, boolean finish);

        /**
         * @return the maximum number of bytes a call to process()
         * could produce for the given number of input bytes.  This may
         * be an overestimate.
         */
        public abstract int maxOutputSize(int len);
    }

    //  --------------------------------------------------------
    //  decoding
    //  --------------------------------------------------------

    /**
     * Decode the Base64-encoded data in input and return the data in
     * a new byte array.
     *
     * <p>The padding '=' characters at the end are considered optional, but
     * if any are present, there must be the correct number of them.
     *
     * @param str    the input String to decode, which is converted to
     *               bytes using the default charset
     * @param flags  controls certain features of the decoded output.
     *               Pass {@code DEFAULT} to decode standard Base64.
     *
     * @throws IllegalArgumentException if the input contains
     * incorrect padding
     */
    public static byte[] decode(String str, int flags) {
        return decode(str.getBytes(), flags);
    }

    /**
     * Decode the Base64-encoded data in input and return the data in
     * a new byte array.
     *
     * <p>The padding '=' characters at the end are considered optional, but
     * if any are present, there must be the correct number of them.
     *
     * @param input the input array to decode
     * @param flags  controls certain features of the decoded output.
     *               Pass {@code DEFAULT} to decode standard Base64.
     *
     * @throws IllegalArgumentException if the input contains
     * incorrect padding
     */
    public static byte[] decode(byte[] input, int flags) {
        return decode(input, 0, input.length, flags);
    }

    /**
     * Decode the Base64-encoded data in input and return the data in
     * a new byte array.
     *
     * <p>The padding '=' characters at the end are considered optional, but
     * if any are present, there must be the correct number of them.
     *
     * @param input  the data to decode
     * @param offset the position within the input array at which to start
     * @param len    the number of bytes of input to decode
     * @param flags  controls certain features of the decoded output.
     *               Pass {@code DEFAULT} to decode standard Base64.
     *
     * @throws IllegalArgumentException if the input contains
     * incorrect padding
     */
    public static byte[] decode(byte[] input, int offset, int len, int flags) {
        // Allocate space for the most data the input could represent.
        // (It could contain less if it contains whitespace, etc.)
        Decoder decoder = new Decoder(flags, new byte[len*3/4]);

        if (!decoder.process(input, offset, len, true)) {
            throw new IllegalArgumentException("bad base-64");
        }

        // Maybe we got lucky and allocated exactly enough output space.
        if (decoder.op == decoder.output.length) {
            return decoder.output;
        }

        // Need to shorten the array, so allocate a new one of the
        // right size and copy.
        byte[] temp = new byte[decoder.op];
        System.arraycopy(decoder.output, 0, temp, 0, decoder.op);
        return temp;
    }

    /* package */ static class Decoder extends Coder {
        /**
         * Lookup table for turning bytes into their position in the
         * Base64 alphabet.
         */
        private static final int DECODE[] = {
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
                52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1,
                -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
                15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
                -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        };

        /**
         * Decode lookup table for the "web safe" variant (RFC 3548
         * sec. 4) where - and _ replace + and /.
         */
        private static final int DECODE_WEBSAFE[] = {
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1,
                52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1,
                -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
                15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63,
                -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        };

        /** Non-data values in the DECODE arrays. */
        private static final int SKIP = -1;
        private static final int EQUALS = -2;

        /**
         * States 0-3 are reading through the next input tuple.
         * State 4 is having read one '=' and expecting exactly
         * one more.
         * State 5 is expecting no more data or padding characters
         * in the input.
         * State 6 is the error state; an error has been detected
         * in the input and no future input can "fix" it.
         */
        private int state;   // state number (0 to 6)
        private int value;

        final private int[] alphabet;

        public Decoder(int flags, byte[] output) {
            this.output = output;

            alphabet = ((flags & URL_SAFE) == 0) ? DECODE : DECODE_WEBSAFE;
            state = 0;
            value = 0;

        }

        /**
         * @return an overestimate for the number of bytes {@code
         * len} bytes could decode to.
         */
        public int maxOutputSize(int len) {
            return len * 3/4 + 10;
        }

        /**
         * Decode another block of input data.
         *
         * @return true if the state machine is still healthy.  false if
         *         bad base-64 data has been detected in the input stream.
         */
        public boolean process(byte[] input, int offset, int len, boolean finish) {
            if (this.state == 6) return false;

            int p = offset;
            len += offset;

            // Using local variables makes the decoder about 12%
            // faster than if we manipulate the member variables in
            // the loop.  (Even alphabet makes a measurable
            // difference, which is somewhat surprising to me since
            // the member variable is final.)
            int state = this.state;
            int value = this.value;
            int op = 0;
            final byte[] output = this.output;
            final int[] alphabet = this.alphabet;

            while (p < len) {
                // Try the fast path:  we're starting a new tuple and the
                // next four bytes of the input stream are all data
                // bytes.  This corresponds to going through states
                // 0-1-2-3-0.  We expect to use this method for most of
                // the data.
                //
                // If any of the next four bytes of input are non-data
                // (whitespace, etc.), value will end up negative.  (All
                // the non-data values in decode are small negative
                // numbers, so shifting any of them up and or'ing them
                // together will result in a value with its top bit set.)
                //
                // You can remove this whole block and the output should
                // be the same, just slower.
                if (state == 0) {
                    while (p+4 <= len &&
                            (value = ((alphabet[input[p] & 0xff] << 18) |
                                    (alphabet[input[p+1] & 0xff] << 12) |
                                    (alphabet[input[p+2] & 0xff] << 6) |
                                    (alphabet[input[p+3] & 0xff]))) >= 0) {
                        output[op+2] = (byte) value;
                        output[op+1] = (byte) (value >> 8);
                        output[op] = (byte) (value >> 16);
                        op += 3;
                        p += 4;
                    }
                    if (p >= len) break;
                }

                // The fast path isn't available -- either we've read a
                // partial tuple, or the next four input bytes aren't all
                // data, or whatever.  Fall back to the slower state
                // machine implementation.

                int d = alphabet[input[p++] & 0xff];

                switch (state) {
                    case 0:
                        if (d >= 0) {
                            value = d;
                            ++state;
                        } else if (d != SKIP) {
                            this.state = 6;
                            return false;
                        }
                        break;

                    case 1:
                        if (d >= 0) {
                            value = (value << 6) | d;
                            ++state;
                        } else if (d != SKIP) {
                            this.state = 6;
                            return false;
                        }
                        break;

                    case 2:
                        if (d >= 0) {
                            value = (value << 6) | d;
                            ++state;
                        } else if (d == EQUALS) {
                            // Emit the last (partial) output tuple;
                            // expect exactly one more padding character.
                            output[op++] = (byte) (value >> 4);
                            state = 4;
                        } else if (d != SKIP) {
                            this.state = 6;
                            return false;
                        }
                        break;

                    case 3:
                        if (d >= 0) {
                            // Emit the output triple and return to state 0.
                            value = (value << 6) | d;
                            output[op+2] = (byte) value;
                            output[op+1] = (byte) (value >> 8);
                            output[op] = (byte) (value >> 16);
                            op += 3;
                            state = 0;
                        } else if (d == EQUALS) {
                            // Emit the last (partial) output tuple;
                            // expect no further data or padding characters.
                            output[op+1] = (byte) (value >> 2);
                            output[op] = (byte) (value >> 10);
                            op += 2;
                            state = 5;
                        } else if (d != SKIP) {
                            this.state = 6;
                            return false;
                        }
                        break;

                    case 4:
                        if (d == EQUALS) {
                            ++state;
                        } else if (d != SKIP) {
                            this.state = 6;
                            return false;
                        }
                        break;

                    case 5:
                        if (d != SKIP) {
                            this.state = 6;
                            return false;
                        }
                        break;
                }
            }

            if (!finish) {
                // We're out of input, but a future call could provide
                // more.
                this.state = state;
                this.value = value;
                this.op = op;
                return true;
            }

            // Done reading input.  Now figure out where we are left in
            // the state machine and finish up.

            switch (state) {
                case 0:
                    // Output length is a multiple of three.  Fine.
                    break;
                case 1:
                    // Read one extra input byte, which isn't enough to
                    // make another output byte.  Illegal.
                    this.state = 6;
                    return false;
                case 2:
                    // Read two extra input bytes, enough to emit 1 more
                    // output byte.  Fine.
                    output[op++] = (byte) (value >> 4);
                    break;
                case 3:
                    // Read three extra input bytes, enough to emit 2 more
                    // output bytes.  Fine.
                    output[op++] = (byte) (value >> 10);
                    output[op++] = (byte) (value >> 2);
                    break;
                case 4:
                    // Read one padding '=' when we expected 2.  Illegal.
                    this.state = 6;
                    return false;
                case 5:
                    // Read all the padding '='s we expected and no more.
                    // Fine.
                    break;
            }

            this.state = state;
            this.op = op;
            return true;
        }
    }

    //  --------------------------------------------------------
    //  encoding
    //  --------------------------------------------------------

    /**
     * Base64-encode the given data and return a newly allocated
     * String with the result.
     *
     * @param input  the data to encode
     * @param flags  controls certain features of the encoded output.
     *               Passing {@code DEFAULT} results in output that
     *               adheres to RFC 2045.
     */
    public static String encodeToString(byte[] input, int flags) {
        try {
            return new String(encode(input, flags), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            // US-ASCII is guaranteed to be available.
            throw new AssertionError(e);
        }
    }

    /**
     * Base64-encode the given data and return a newly allocated
     * String with the result.
     *
     * @param input  the data to encode
     * @param offset the position within the input array at which to
     *               start
     * @param len    the number of bytes of input to encode
     * @param flags  controls certain features of the encoded output.
     *               Passing {@code DEFAULT} results in output that
     *               adheres to RFC 2045.
     */
    public static String encodeToString(byte[] input, int offset, int len, int flags) {
        try {
            return new String(encode(input, offset, len, flags), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            // US-ASCII is guaranteed to be available.
            throw new AssertionError(e);
        }
    }

    /**
     * Base64-encode the given data and return a newly allocated
     * byte[] with the result.
     *
     * @param input  the data to encode
     * @param flags  controls certain features of the encoded output.
     *               Passing {@code DEFAULT} results in output that
     *               adheres to RFC 2045.
     */
    public static byte[] encode(byte[] input, int flags) {
        return encode(input, 0, input.length, flags);
    }

    /**
     * Base64-encode the given data and return a newly allocated
     * byte[] with the result.
     *
     * @param input  the data to encode
     * @param offset the position within the input array at which to
     *               start
     * @param len    the number of bytes of input to encode
     * @param flags  controls certain features of the encoded output.
     *               Passing {@code DEFAULT} results in output that
     *               adheres to RFC 2045.
     */
    public static byte[] encode(byte[] input, int offset, int len, int flags) {
        Encoder encoder = new Encoder(flags, null);

        // Compute the exact length of the array we will produce.
        int output_len = len / 3 * 4;

        // Account for the tail of the data and the padding bytes, if any.
        if (encoder.do_padding) {
            if (len % 3 > 0) {
                output_len += 4;
            }
        } else {
            switch (len % 3) {
                case 0: break;
                case 1: output_len += 2; break;
                case 2: output_len += 3; break;
            }
        }

        // Account for the newlines, if any.
        if (encoder.do_newline && len > 0) {
            output_len += (((len-1) / (3 * Encoder.LINE_GROUPS)) + 1) *
                    (encoder.do_cr ? 2 : 1);
        }

        encoder.output = new byte[output_len];
        encoder.process(input, offset, len, true);

        assert encoder.op == output_len;

        return encoder.output;
    }

    /* package */ static class Encoder extends Coder {
        /**
         * Emit a new line every this many output tuples.  Corresponds to
         * a 76-character line length (the maximum allowable according to
         * <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>).
         */
        public static final int LINE_GROUPS = 19;

        /**
         * Lookup table for turning Base64 alphabet positions (6 bits)
         * into output bytes.
         */
        private static final byte ENCODE[] = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
                'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/',
        };

        /**
         * Lookup table for turning Base64 alphabet positions (6 bits)
         * into output bytes.
         */
        private static final byte ENCODE_WEBSAFE[] = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
                'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_',
        };

        final private byte[] tail;
        /* package */ int tailLen;
        private int count;

        final public boolean do_padding;
        final public boolean do_newline;
        final public boolean do_cr;
        final private byte[] alphabet;

        public Encoder(int flags, byte[] output) {
            this.output = output;

            do_padding = (flags & NO_PADDING) == 0;
            do_newline = (flags & NO_WRAP) == 0;
            do_cr = (flags & CRLF) != 0;
            alphabet = ((flags & URL_SAFE) == 0) ? ENCODE : ENCODE_WEBSAFE;

            tail = new byte[2];
            tailLen = 0;

            count = do_newline ? LINE_GROUPS : -1;
        }

        /**
         * @return an overestimate for the number of bytes {@code
         * len} bytes could encode to.
         */
        public int maxOutputSize(int len) {
            return len * 8/5 + 10;
        }

        public boolean process(byte[] input, int offset, int len, boolean finish) {
            // Using local variables makes the encoder about 9% faster.
            final byte[] alphabet = this.alphabet;
            final byte[] output = this.output;
            int op = 0;
            int count = this.count;

            int p = offset;
            len += offset;
            int v = -1;

            // First we need to concatenate the tail of the previous call
            // with any input bytes available now and see if we can empty
            // the tail.

            switch (tailLen) {
                case 0:
                    // There was no tail.
                    break;

                case 1:
                    if (p+2 <= len) {
                        // A 1-byte tail with at least 2 bytes of
                        // input available now.
                        v = ((tail[0] & 0xff) << 16) |
                                ((input[p++] & 0xff) << 8) |
                                (input[p++] & 0xff);
                        tailLen = 0;
                    };
                    break;

                case 2:
                    if (p+1 <= len) {
                        // A 2-byte tail with at least 1 byte of input.
                        v = ((tail[0] & 0xff) << 16) |
                                ((tail[1] & 0xff) << 8) |
                                (input[p++] & 0xff);
                        tailLen = 0;
                    }
                    break;
            }

            if (v != -1) {
                output[op++] = alphabet[(v >> 18) & 0x3f];
                output[op++] = alphabet[(v >> 12) & 0x3f];
                output[op++] = alphabet[(v >> 6) & 0x3f];
                output[op++] = alphabet[v & 0x3f];
                if (--count == 0) {
                    if (do_cr) output[op++] = '\r';
                    output[op++] = '\n';
                    count = LINE_GROUPS;
                }
            }

            // At this point either there is no tail, or there are fewer
            // than 3 bytes of input available.

            // The main loop, turning 3 input bytes into 4 output bytes on
            // each iteration.
            while (p+3 <= len) {
                v = ((input[p] & 0xff) << 16) |
                        ((input[p+1] & 0xff) << 8) |
                        (input[p+2] & 0xff);
                output[op] = alphabet[(v >> 18) & 0x3f];
                output[op+1] = alphabet[(v >> 12) & 0x3f];
                output[op+2] = alphabet[(v >> 6) & 0x3f];
                output[op+3] = alphabet[v & 0x3f];
                p += 3;
                op += 4;
                if (--count == 0) {
                    if (do_cr) output[op++] = '\r';
                    output[op++] = '\n';
                    count = LINE_GROUPS;
                }
            }

            if (finish) {
                // Finish up the tail of the input.  Note that we need to
                // consume any bytes in tail before any bytes
                // remaining in input; there should be at most two bytes
                // total.

                if (p-tailLen == len-1) {
                    int t = 0;
                    v = ((tailLen > 0 ? tail[t++] : input[p++]) & 0xff) << 4;
                    tailLen -= t;
                    output[op++] = alphabet[(v >> 6) & 0x3f];
                    output[op++] = alphabet[v & 0x3f];
                    if (do_padding) {
                        output[op++] = '=';
                        output[op++] = '=';
                    }
                    if (do_newline) {
                        if (do_cr) output[op++] = '\r';
                        output[op++] = '\n';
                    }
                } else if (p-tailLen == len-2) {
                    int t = 0;
                    v = (((tailLen > 1 ? tail[t++] : input[p++]) & 0xff) << 10) |
                            (((tailLen > 0 ? tail[t++] : input[p++]) & 0xff) << 2);
                    tailLen -= t;
                    output[op++] = alphabet[(v >> 12) & 0x3f];
                    output[op++] = alphabet[(v >> 6) & 0x3f];
                    output[op++] = alphabet[v & 0x3f];
                    if (do_padding) {
                        output[op++] = '=';
                    }
                    if (do_newline) {
                        if (do_cr) output[op++] = '\r';
                        output[op++] = '\n';
                    }
                } else if (do_newline && op > 0 && count != LINE_GROUPS) {
                    if (do_cr) output[op++] = '\r';
                    output[op++] = '\n';
                }

                assert tailLen == 0;
                assert p == len;
            } else {
                // Save the leftovers in tail to be consumed on the next
                // call to encodeInternal.

                if (p == len-1) {
                    tail[tailLen++] = input[p];
                } else if (p == len-2) {
                    tail[tailLen++] = input[p];
                    tail[tailLen++] = input[p+1];
                }
            }

            this.op = op;
            this.count = count;

            return true;
        }
    }

    public Base64() { }   // don't instantiate
}


/**
 * Created by valeriyasin on 11/15/16.
 */
public class AuthorizationActivity extends AppCompatActivity implements ActivityListener  {
//    public static final String ACCESS_TOKEN = "access_token";
//    public static final String EXPIRATION_DATE = "expiration_date";
//    public static final String AUTH_ERROR = "error";
//
    private static final String EXTRA_CLIENT_ID = "client_id";
    private static final String EXTRA_CLIENT_SECRET = "client_secret";
//
    private String clientId;
    private String clientSecret;

    private String authUrlTemplate;
    private String tokenUrlTemplate;
    private String redirectUrl;

    SharedPreferences sPref;
//    final String SAVED_TOKEN = "saved_token";

    private WebView webView;

//    Boolean getTokenStarted = false;

    AccessTokenGetter accessTokenGetter;

    String baseUrl;

    Utils utils;

    String GET_TOCKEN_STARTED = "GET_TOCKEN_STARTED";


    public static Intent createAuthActivityIntent(Context context, String clientId, String secret) {
        Intent intent = new Intent(context, AuthorizationActivity.class);
        intent.putExtra(EXTRA_CLIENT_ID, clientId);
        intent.putExtra(EXTRA_CLIENT_SECRET, secret);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_acitivity);
        webView = (WebView) findViewById(R.id.web_auth_view);

        AsyncTaskHandler.getInstance().turnOnAuthorizationActivityAlive(this);

//        baseUrl = getResources().getString(R.id.bas)

        utils = new Utils(this);

        clientId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
        clientSecret = getIntent().getStringExtra(EXTRA_CLIENT_SECRET);

        authUrlTemplate = getString(R.string.auth_url);
        System.out.println(authUrlTemplate);
        tokenUrlTemplate = getString(R.string.token_url);
        redirectUrl = getString(R.string.callback_url);

        if (savedInstanceState == null) {//        super.onResume();
            onAuthStarted();
            String url = String.format(authUrlTemplate, clientId, "&", redirectUrl, "&");
            URI uri = URI.create(url);
            System.out.println("URL: " + url);
//            getTokenStarted = false;
            webView.setWebViewClient(new OAuthWebClient(AsyncTaskHandler.getInstance()));
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(uri.toString());
        }
        else {
            webView.restoreState(savedInstanceState);
            webView.setWebViewClient(new OAuthWebClient(AsyncTaskHandler.getInstance()));
            webView.getSettings().setJavaScriptEnabled(true);
//            getTokenStarted = savedInstanceState.getBoolean(GET_TOCKEN_STARTED);
            AsyncTaskHandler.getInstance().turnOnAuthorizationActivityAlive(this);
//            if (AsyncTaskHandler.getInstance().getAuthorizationActivityAlive().get()) {
//
//            }
//            if (accessTokenGetter != null) {
//                accessTokenGetter = (AccessTokenGetter)savedInstanceState.getSerializable("GET_TOKEN_ASUNC");
//                accessTokenGetter.listener = this;
//            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("ge\n");

    }

    public void saveToken(TokenObject tokenObject) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(utils.SAVED_TOKEN, tokenObject.getToken());
        editor.putString(utils.SAVED_EXPIRATION_DATE, tokenObject.getExpirationDate());
        editor.commit();
    }

    public void onAuthStarted() {
        InternetConnectionChecker internetConnectionChecker = new InternetConnectionChecker(AsyncTaskHandler.getInstance());
        try {
            Boolean connectionOk = internetConnectionChecker.execute("").get();
            if (!connectionOk) {
                finish();
            }

        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
        }
//        System.out.println("oauth started\n");
    }

    @Override
    public void onComplete(TokenObject tokenObject) {
        Intent intent = new Intent();
        intent.putExtra(utils.ACCESS_TOKEN, tokenObject.getToken());
        intent.putExtra(utils.ACCESS_EXPIRATION_DATE, tokenObject.getExpirationDate());
        setResult(utils.RESULT_OK, intent);
        saveToken(tokenObject);
        finish();
    }

    @Override
    public void onError(String error) {
        Intent intent = new Intent();
        intent.putExtra(utils.AUTH_ERROR, error);
        setResult(utils.RESULT_CANCELED, intent);
        finish();
    }

    private class OAuthWebClient extends WebViewClient {
        private AsyncTaskHandler asyncTaskHandler;

        public OAuthWebClient(AsyncTaskHandler asyncTaskHandler) {
            this.asyncTaskHandler = asyncTaskHandler;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            System.out.println(url);
        }


//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            System.out.println("IM HERE");
//            return super.shouldOverrideUrlLoading(view, request);
//        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String request) {
            System.out.println("IM HERE2");
            String url = request.toString();
            if (url.startsWith(view.getResources().getString(R.string.callback_url))) {
                String[] urls = url.split("=");
                if (!AsyncTaskHandler.getInstance().getAsyncTaskStarted().get()) {
                    accessTokenGetter = new AccessTokenGetter(AsyncTaskHandler.getInstance());
                    accessTokenGetter.execute(urls[1]);
                }
//                if (!getTokenStarted) {
//                    accessTokenGetter = new AccessTokenGetter(listener);
//                    accessTokenGetter.execute(urls[1]);
//                }
                AsyncTaskHandler.getInstance().turnOnAsyncTaskStarted();
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            asyncTaskHandler.onError(error.toString());
        }
    }

    class InternetConnectionChecker extends  AsyncTask<String, Void, Boolean> {
        private AsyncTaskHandler asyncTaskHandler;

        public void setListener(AsyncTaskHandler asyncTaskHandler){
            this.asyncTaskHandler = asyncTaskHandler;
        }

        InternetConnectionChecker(AsyncTaskHandler asyncTaskHandler) {
            this.asyncTaskHandler = asyncTaskHandler;
        }

        public boolean isInternetAvailable() {
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
                return !ipAddr.equals("");

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean connectionOk = isInternetAvailable();
            if (!connectionOk) {
                asyncTaskHandler.onError(String.valueOf(utils.NO_INTERNET_CONNECTION));
            }
            return connectionOk;
        }

        @Override
        protected void onPreExecute () {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute (Boolean connectionOk){
            super.onPreExecute();
        }
    }


    private class AccessTokenGetter extends AsyncTask<String, Void, TokenObject>
            implements Serializable {
        private AsyncTaskHandler asyncTaskHandler;

        AccessTokenGetter(AsyncTaskHandler asyncTaskHandler) {
            this.asyncTaskHandler = asyncTaskHandler;
        }

        @Override
        protected TokenObject doInBackground(String... params) {
            String url = tokenUrlTemplate; //String.format(tokenUrlTemplate, clientId, "&", clientSecret, "&", params[0]);
            try {
                System.out.println(params[0]);
                System.out.println("------------------------");
                System.out.println(redirectUrl);
                String baseURL = getResources().getString(R.string.baseURL);
                BasicHttpClient httpClient = new BasicHttpClient(baseURL);
                String nBaseurl = "/o/token/";
                String appCredentials = clientId + ":" + clientSecret;
                String auth = "Basic " + new String(new Base64().encode(appCredentials.getBytes("UTF-8"), 0)).replace("\n", "");
                ParameterMap parameters = httpClient.newParams()
                        .add("redirect_uri", redirectUrl)
                        .add("grant_type", "authorization_code")
                        .add("code", params[0]);
                httpClient.addHeader("Authorization", auth);
//                httpClient.addHeader("Content-Type", "form-data");
                System.out.println(httpClient.toString());
                System.out.println("---------------------");
                System.out.println(parameters.toString());
                HttpResponse response1 = httpClient.post(nBaseurl, parameters);
                System.out.println(response1.getBodyAsString());
                int status = response1.getStatus();
                if (status != STATUS_OK) {
                    return new TokenObject("Error with status " + status, "default_date");
                } else {

                    String response = response1.getBodyAsString();
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
//                        testToken(jsonResponse.getString("access_token"));
                        return new TokenObject(jsonResponse.getString("access_token"),
                                 getAccessTokenDateExpiration(jsonResponse.getString("expires_in")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected  void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(TokenObject tokenObject) {
            if (tokenObject != null) {
                if (tokenObject.getToken().contains("Error")) {
                    asyncTaskHandler.onError(tokenObject.getToken());
                } else {
                    asyncTaskHandler.onComplete(tokenObject);
                }
            }

            super.onPreExecute();
        }
    }

    private String getAccessTokenDateExpiration(String expires_in) {
        int secs = Integer.parseInt(expires_in);
        Date date = new Date();
        date.setTime(date.getTime() + secs * 1000);
        return date.toString();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putBoolean(GET_TOCKEN_STARTED, getTokenStarted);
        savedInstanceState.putSerializable("GET_TOKEN_ASYNC", accessTokenGetter);
        webView.saveState(savedInstanceState);
//        savedInstanceState.putInt(AUTHORIZED_ACTIVITY_STARTED, authorizedActivityStarted);

        super.onSaveInstanceState(savedInstanceState);
    }


}
