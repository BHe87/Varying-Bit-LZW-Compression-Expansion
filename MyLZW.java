import java.lang.Math;

public class MyLZW {
    private static final int R = 256;                   // number of input chars
    private static       int W = 9;                     // codeword width
    private static       int L = (int) Math.pow(2, W);  // number of codewords = 2^W
    private static final int M = 16;                    // max codeword width

    public static void compress(String mode) {
        W = 9;
        String input = BinaryStdIn.readString();
        int uncompressedData = 0;
        int compressedData = 0;
        double oldRatio = 0;
        double newRatio = 0;
        double compressionRatio = 0;
        boolean firstime = true;
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++) {
            st.put((char) i + "", i); // adding each ascii character to the symbol table, key: ascii character, value: ascii number
        }
        int code = R+1;  // R is codeword for EOF

        BinaryStdOut.write(mode.charAt(0)); // writing which mode we're in

        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();

            if (t < input.length() && code < L) {   // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            }

            input = input.substring(t);            // Scan past s in input.

            if (code == L && W < M) {   // incrementing the codeword width
                System.err.println("W: " + W);
                L = (int) Math.pow(2, ++W);
            }

            if (W == M && mode.equals("r")) { // in reset mode
                W = 9;
                L = (int) Math.pow(2, W);
                st = new TST<Integer>(); // resetting the symbol table or "codebook"
                for (int i = 0; i < R; i++) {
                    st.put((char) i + "", i);  // adding each ascii character to the symbol table, key: ascii character, value: ascii number
                }
                code = R + 1;  // R is codeword for EOF.
            }

            uncompressedData += (t * 8);
            compressedData += W;
            compressionRatio = (double) uncompressedData/compressedData;
            newRatio = compressionRatio;
            
            if (W == M && mode.equals("m")) { // in monitor mode
                if (firstime) {
                    oldRatio = compressionRatio;
                    firstime = false;
                    System.err.println(oldRatio);
                }

                double threshold = oldRatio/newRatio;
                if (threshold > 1.1) { //resetting the codebook
                    W = 9;
                    L = (int) Math.pow(2, W);
                    st = new TST<Integer>(); // symbol table or "codebook"
                    for (int i = 0; i < R; i++) {
                        st.put((char) i + "", i);  // adding each ascii character to the symbol table, key: ascii character, value: ascii number
                    }
                    code = R + 1;  // R is codeword for EOF.
                    firstime = true;
                }
            }
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 

    public static void expand() {
        W = 9;
        String[] st = new String[65536];
        int compressedData = 0;
        int expandedData = 0;
        double oldRatio = 0;
        double newRatio = 0;
        double expansionRatio = 0;
        boolean firstime = true;
        int i; // next available codeword value
  
        for (i = 0; i < R; i++) { // initialize symbol table with all 1-character strings
            st[i] = (char) i + "";
        }
        st[i++] = "";                        // (unused) lookahead for EOF

        char c = BinaryStdIn.readChar();       // reads which mode we're using
        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];
        expandedData += (val.length() * 8);
        compressedData += W;
        
        while (true) {
            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) {
                s = val + val.charAt(0);   // special case hack
            }
            if (i < L-1) {
                st[i++] = val + s.charAt(0);
            }
            val = s;

            if (i == L-1 && W < M) {
                L = (int) Math.pow(2, ++W);
            }

            if (W == M && c == 'r') { // in reset mode
                W = 9;
                L = (int) Math.pow(2, W);
                st = new String[st.length];
                for (i = 0; i < R; i++) {
                    st[i] = (char) i + "";
                }
                st[i++] = "";

                codeword = BinaryStdIn.readInt(W);
                if (codeword == R) return;
                val = st[codeword];
            }
            expandedData += (val.length() * 8);
            compressedData += W;
            expansionRatio = (double) expandedData/compressedData;
            newRatio = expansionRatio;

            if (W == M && c == 'm') { // in monitor mode
                if (firstime) {
                    oldRatio = expansionRatio;
                    firstime = false;
                }
                double threshold = oldRatio/newRatio;
                if (threshold > 1.1) { //resetting the codebook if our ratio is above the threshold
                    W = 9;
                    L = (int) Math.pow(2, W);
                    st = new String[st.length]; // clearing the symbol table or "codebook"
                    for (i = 0; i < R; i++) {
                        st[i] = (char) i + "";  // adding each ascii character to the symbol table, key: ascii character, value: ascii number
                    }
                    st[i++] = "";
                    firstime = true;

                    codeword = BinaryStdIn.readInt(W);
                    if (codeword == R) return;
                    val = st[codeword];
                    expandedData += (val.length() * 8);
                    compressedData += W;              
                }
            }
        }
        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if (args[0].equals("-")) compress(args[1]); // args[1] is the mode we're using
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}