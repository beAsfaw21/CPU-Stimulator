; Call a subroutine and return to the caller.
; Expected result: R0 = 42 and R1 = 7
call double
mov r1, #7
jmp done

double:
mov r0, #21
add r0, r0
ret

done:
