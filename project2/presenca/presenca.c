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
#include "dev/button-sensor.h"
#include "dev/leds.h"
#include "sys/etimer.h"
#include <stdio.h>

/*---------------------------------------------------------------------------*/
PROCESS(test_button_process, "Test button");
AUTOSTART_PROCESSES(&test_button_process);
/*---------------------------------------------------------------------------*/

PROCESS_THREAD(test_button_process, ev, data)
{
  PROCESS_BEGIN();
  static struct etimer et;
	static uint32_t seconds_interval = 1;

	etimer_set(&et, CLOCK_SECOND*seconds_interval);
  SENSORS_ACTIVATE(button_sensor);

  while(1) {
		PROCESS_WAIT_EVENT();

		if(etimer_expired(&et)) {  // If the event it's provoked by the timer expiration, then...
      leds_off(LEDS_YELLOW);	
			printf("off\n");
    }

    if(ev == sensors_event &&
			     data == &button_sensor){
		  char led = leds_get();		
			if(led & LEDS_YELLOW){
				etimer_restart(&et);
				printf("reset\n");
			}
			else{
				etimer_reset(&et);
				etimer_restart(&et);
				leds_on(LEDS_YELLOW);
				printf("on\n");
			}
		}
  }
  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
