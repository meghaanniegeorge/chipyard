#define txfifo 0x2000
#define rxfifo 0x2004
#define txctrl 0x2008
#define txmark 0x200a
#define rxctrl 0x200c
#define rxmark 0x200e
#define ie     0x2010
#define ip     0x2014
#define div    0x2018

#include "mmio.h"

int main(void)
{	reg_write32(txctrl, 0x1);

	reg_write32(rxctrl, 0x1);

	reg_write32(ie, 0x00);
	reg_write32(ip, 0x00);
	reg_write32(div, 18);

	reg_write8(txfifo, 0x10);
	for(int i = 0; i < 1000; i++){
		asm("");
	}
	if(0x10 == reg_read8(rxfifo))
		return 0;
	return 1;
}
