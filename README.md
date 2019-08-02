# Enhanced PWM

This version of chipyard contains a PWM directory inside the example directory which enables the following features:

1. Configurable number of PWM outputs 

2. Configurable number of PWM output pulses

   a. Finite number of output pulses 
   
   b. Infinite number of output pulses
   
3. Configurable polarity of PWM outputs

   a. Active high
   
   b. Active low

4. Configurable deadband for complementary outputs

5. Configurable alignment of PWM outputs

   a. Left-Aligned
   
   b. Right-Aligned
   
   c. Center-Aligned

6. Configurable phase shift

7. Master enable 

8. Individual PWM output kill option

## How to use it?

A test file named PWMModule.c can be found in the test directory, which uses the "mmio.h" header file to read and write to the appropriate memmory locations. The above mentioned features can be accessed by writing required values to the following registers with appropriate offset:

period: Specify the PWM period here.

duty: Specify the PWM duty cycle here.

noOfCycles: Specify the required number of PWM output pulses.

shamt: Specify the required phase shift here. 

align: Specify the correct option for the required feature here.

                Option      Feature
                0x00    -   Right-Aligned
                0x01    -   Center-Aligned
                0x02    -   Left-Aligned
                
deadband: Specify the required deadband in clock cycles here.   

comEn: Assert if complement of PWM output is required.

kill: Assert if the individual PWM output needs to be disabled.

enable: Assert to enable all the PWM outputs synchronously.
