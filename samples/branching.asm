; Compare two values and branch when the first value is lower.
; Expected result: R2 = 1
mov r0, #1
mov r1, #2
cmp r0, r1
brlo lower
mov r2, #0
jmp done

lower:
mov r2, #1

done:
