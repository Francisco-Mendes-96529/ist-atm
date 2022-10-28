/*
 * Copyright (c) 2007, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Contiki operating system.
 *
 */

/**
 * \file
 *         An example of how to use the button and light sensor on
 *         the Tmote Sky platform.
 * \author
 *         Adam Dunkels <adam@sics.se>
 */

#include "contiki.h"
#include "dev/serial-line.h"
#include "dev/leds.h"
#include "sys/etimer.h"
#include <stdio.h>
#include <stdlib.h>

/*---------------------------------------------------------------------------*/
PROCESS(test_temperature_process, "Test temperature");
AUTOSTART_PROCESSES(&test_temperature_process);
/*---------------------------------------------------------------------------*/

PROCESS_THREAD(test_temperature_process, ev, data)
{
  PROCESS_BEGIN();
	static float temp_max = 35;
  printf("MAX:%.0f\n",temp_max);
  leds_on(LEDS_GREEN);

  while(1) {
		PROCESS_WAIT_EVENT();

    if(ev == serial_line_event_message){
      printf("%.2f - max:%.0f\n",atof(data),temp_max);

			if(atof(data) > temp_max){
				leds_off(LEDS_GREEN);
				leds_on(LEDS_RED);
				printf("too hot!\n");
			}
			else{
				leds_off(LEDS_RED);
				leds_on(LEDS_GREEN);
				printf("cold\n");
			}
		}
  }
  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
