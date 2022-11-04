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
 *         code for a mote with temperature sensor that activates an alarm (led red) when goes over a limit
 *         and a presence sensor that activates the lights (led yellow) 
 * \author
 *         David and Francisco (group 7)
 */

#include "contiki.h"

#include "servreg-hack.h"

#include "dev/button-sensor.h"
#include "dev/leds.h"
#include "dev/serial-line.h"

#include "net/ip/simple-udp.h"
#include "net/ip/uip.h"
#include "net/ip/uip-debug.h"
#include "net/ipv6/uip-ds6.h"

#include "sys/etimer.h"
#include "sys/node-id.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define UDP_PORT 1234
#define SERVICE_ID 190

#define STR(x) #x
#define LIGHTS_TIME (10 * CLOCK_SECOND)
#define TEMPERATURE_MAX 35

static struct simple_udp_connection unicast_connection;
static int temp_k = 0;
static float temperature[] = {0,0,0,0,0,0};

/*---------------------------------------------------------------------------*/
PROCESS(button_process, "Test button");
PROCESS(temperature_process, "Test temperature");
PROCESS(unicast_sender_process, "Unicast sender example process");
AUTOSTART_PROCESSES(&button_process, &temperature_process, &unicast_sender_process);
/*---------------------------------------------------------------------------*/
static void
receiver(struct simple_udp_connection *c,
         const uip_ipaddr_t *sender_addr,
         uint16_t sender_port,
         const uip_ipaddr_t *receiver_addr,
         uint16_t receiver_port,
         const uint8_t *data,
         uint16_t datalen)
{
  printf("Data received on port %d from port %d with length %d\n",
         receiver_port, sender_port, datalen);
}
/*---------------------------------------------------------------------------*/
static void
set_global_address(void)
{
  uip_ipaddr_t ipaddr;
  int i;
  uint8_t state;

  uip_ip6addr(&ipaddr, 0xaaaa, 0, 0, 0, 0, 0, 0, 0);
  uip_ds6_set_addr_iid(&ipaddr, &uip_lladdr);
  uip_ds6_addr_add(&ipaddr, 0, ADDR_AUTOCONF);

  printf("IPv6 addresses: ");
  for(i = 0; i < UIP_DS6_ADDR_NB; i++) {
    state = uip_ds6_if.addr_list[i].state;
    if(uip_ds6_if.addr_list[i].isused &&
       (state == ADDR_TENTATIVE || state == ADDR_PREFERRED)) {
      uip_debug_ipaddr_print(&uip_ds6_if.addr_list[i].ipaddr);
      printf("\n");
    }
  }
}

/*---------------------------------------------------------------------------*/

PROCESS_THREAD(button_process, ev, data)
{
  PROCESS_BEGIN();
  static struct etimer et;

  SENSORS_ACTIVATE(button_sensor);

  while(1) {
	  PROCESS_WAIT_EVENT();
    
    if(ev == sensors_event &&
		       data == &button_sensor){
	    char led = leds_get();	
		  if(led & LEDS_YELLOW){
			  etimer_restart(&et);
			  printf("restart\n");
		  }
		  else{
        etimer_set(&et, LIGHTS_TIME);
			  leds_on(LEDS_YELLOW);
			  printf("on\n");
        process_post_synch(&unicast_sender_process, PROCESS_EVENT_CONTINUE, "P 1");
		  }
	  }
    else if(ev == PROCESS_EVENT_TIMER) {  // If the event it's provoked by the timer expiration, then...
      leds_off(LEDS_YELLOW);	
		  printf("off\n");
      process_post_synch(&unicast_sender_process, PROCESS_EVENT_CONTINUE, "P 0");
    }
  }

  PROCESS_END();
}

/*---------------------------------------------------------------------------*/

PROCESS_THREAD(temperature_process, ev, data)
{
  PROCESS_BEGIN();
  printf("MAX:%.1f\n",(float)TEMPERATURE_MAX);
  leds_on(LEDS_GREEN);

  while(1) {
		PROCESS_WAIT_EVENT();

    if(ev == serial_line_event_message){
      float temp = atof(data);
      temp_k++;
      printf("%.2f - max:%.1f\n",temp,(float)TEMPERATURE_MAX);
      
      temperature[temp_k-1] = temp;
      if(temp_k==5){
        // send values
        char  msg[50];
        sprintf(msg, "T %.1f %.1f %.1f %.1f %.1f", temperature[0], temperature[1], temperature[2], temperature[3], temperature[4]);
        process_post_synch(&unicast_sender_process, PROCESS_EVENT_CONTINUE, msg);
        
        // default
        temp_k=0;
      }

			if(temp > TEMPERATURE_MAX){
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

PROCESS_THREAD(unicast_sender_process, ev, data)
{
  uip_ipaddr_t *addr;

  PROCESS_BEGIN();
  printf("%s\n",data);

  servreg_hack_init();

  set_global_address();

  simple_udp_register(&unicast_connection, UDP_PORT,
                      NULL, UDP_PORT, receiver);

  while(1) {
    PROCESS_WAIT_EVENT();
    if(ev == PROCESS_EVENT_CONTINUE){
      addr = servreg_hack_lookup(SERVICE_ID);
      if(addr != NULL) {

        printf("Sending unicast to ");
        uip_debug_ipaddr_print(addr);
        printf("\n");
        simple_udp_sendto(&unicast_connection, data, strlen(data) + 1, addr);

      } else {
        printf("Service %d not found\n", SERVICE_ID);
      }
    }
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
