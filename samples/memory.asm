; Store and load a 16-bit value.
; Expected result: memory[100..101] = 0x1234 and R2 = 0x1234
mov r0, #100
mov r1, #0x1234
st r0, r1
mov r1, #0
ld r2, r0
