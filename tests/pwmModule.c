#define period 	   	 0x5000
#define duty   	     0x5100
#define noOfCycles   0x5200
#define shamt 	     0x5300 
#define align        0x5400
#define deadband     0x5500
#define comEn        0x5600
#define kill 	     0x5700
#define enable 	     0x5800

#include "mmio.h"

int main(void)
{	
	//OUTPUT 1
	reg_write32(period       + 0*0x04, 20);
	reg_write32(duty         + 0*0x04, 10);
	reg_write32(noOfCycles   + 0*0x04, 0xffffffff);
	reg_write32(shamt        + 0*0x04, 0);
	reg_write32(align        + 0*0x04, 0);      //align = 0 -> right aligned
	reg_write32(deadband     + 0*0x04, 0);		//align = 1 -> center aligned
	reg_write32(comEn        + 0*0x04, 0);		//align = 2 -> left aligned

	//OUTPUT 2
	reg_write32(period       + 1*0x04, 20);
	reg_write32(duty         + 1*0x04, 10);
	reg_write32(noOfCycles   + 1*0x04, 5);
	reg_write32(shamt        + 1*0x04, 0);
	reg_write32(align        + 1*0x04, 0);
	reg_write32(deadband     + 1*0x04, 0);
	reg_write32(comEn        + 1*0x04, 1);

	//OUTPUT 3
	reg_write32(period       + 2*0x04, 20);
	reg_write32(duty         + 2*0x04, 10);
	reg_write32(noOfCycles   + 2*0x04, 5);
	reg_write32(shamt        + 2*0x04, 20);
	reg_write32(align        + 2*0x04, 0);
	reg_write32(deadband     + 2*0x04, 0);
	reg_write32(comEn        + 2*0x04, 0);

	//Master Enable
	reg_write32(enable, 1);

	//Kill individual outputs
	for(int i = 0; i < 3000; i++){
		asm("");
	}
	reg_write32(kill         + 0*0x04, 1);

	while(1);
	return 0;
}
