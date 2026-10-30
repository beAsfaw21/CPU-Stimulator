package simulator;

// This is our representation of the ALU.
// As expected, it handles all operations related with 
// arithmetic and logic. The ALU also stores flags and updates
// them based on the result of a computation.
//
// The Z flag is set if a computation results in a zero value.
// The N flag is set if the result of a computation is negative. 
// The C flag is set if an addition results in a carry or a
// subtraction results in a borrow.
// The V flag is set if a computation results in an overflow.
public class ALU {
    public boolean Z; // zero    
    public boolean N; // negative/sign flag  
    public boolean C; // carry  
    public boolean V; // overflow 

    public int add(int a, int b) {
        a &= 0xffff; 
        b &= 0xffff;

        int result32 = a + b;
        int result16 = result32 & 0xffff;

        Z = (result16 == 0);
        N = ((result16 & 0x8000) != 0); 
        C = ((result32 & 0x10000) != 0); // carried out to the 17th bit.
        // overflow occurs if the two values have the same sign but their
        // sum has a different sign.
        V = (~(a ^ b) & (a ^ result16) & 0x8000) != 0;

        return result16;
    }

    public int sub(int a, int b) {
        a &= 0xffff; 
        b &= 0xffff;

        int result32 = a - b;
        int result16 = result32 & 0xffff;

        Z = (result16 == 0);
        N = ((result16 & 0x8000) != 0); 
        C = (a < b);
        // overflow occurs here if the two values have different signs
        // and the result has an incorrect sign.
        // an overflow can only occur in the following cases: a - (-b) and -a - b
        V = ((a ^ b) & (a ^ result16) & 0x8000) != 0;

        return result16;
    }

    public void cmp(int a, int b) {
        // cmp is the same as sub, but doesn't return the value
        a &= 0xffff; 
        b &= 0xffff;

        int result32 = a - b;
        int result16 = result32 & 0xffff;

        Z = (result16 == 0);
        N = ((result16 & 0x8000) != 0); 
        C = (a < b);
        V = ((a ^ b) & (a ^ result16) & 0x8000) != 0;
    }

    public int or(int a, int b) {
        int result = (a & 0xffff) | (b & 0xffff);

        Z = (result == 0);
        N = ((result & 0x8000) != 0);
        C = false;
        V = false;

        return result;
    }

    public int xor(int a, int b) {
        int result = (a & 0xffff) ^ (b & 0xffff);

        Z = (result == 0);
        N = ((result & 0x8000) != 0);
        C = false;
        V = false;

        return result;
    }

    public int and(int a, int b) {
        int result = (a & 0xffff) & (b & 0xffff);

        Z = (result == 0);
        N = ((result & 0x8000) != 0);
        C = false;
        V = false;

        return result;
    }
}

