; Calculate a Fibonacci value.
; Expected result: R2 = 89
mov r0, #0
mov r1, #1
mov r3, #10

.loop:
mov r2, r0
add r2, r1
mov r0, r1
mov r1, r2
sub r3, #1
brne .loop
