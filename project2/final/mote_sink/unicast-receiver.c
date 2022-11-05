/*
 * Copyright (c) 2011, Swedish Institute of Computer Science.
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

#include "contiki.h"

#include "dev/button-sensor.h"

#include "net/ip/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ip/uip-debug.h"
#include "net/rpl/rpl.h"

#include "simple-udp.h"
#include "servreg-hack.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define UDP_PORT 1234
#define SERVICE_ID 190

static struct simple_udp_connection unicast_connection;

typedef struct Vector { // struct to save the information from the motes
  uip_ipaddr_t addr; // address of the sender mote
  int presence; // 0 = no movemment, 1 = movemment
  float temperature[5]; // last 5 values
}vector;
static vector *pvector; // dynamically allocated array
static int pvector_size; // size of the array

/*---------------------------------------------------------------------------*/
PROCESS(unicast_receiver_process, "Unicast receiver example process");
PROCESS(button_process, "Button process");
AUTOSTART_PROCESSES(&unicast_receiver_process, &button_process);
/*---------------------------------------------------------------------------*/
static void
fill_values(const uip_ipaddr_t *sender_addr,
  const uint8_t *data) {
  if (pvector == NULL) { // check if there is no data saved
    pvector = (vector *) malloc(sizeof(vector)); // allocate memory of one vector struct
    pvector_size = 1;
    uip_ip6addr(&pvector[0].addr, 0xaaaa, 0, 0, 0, 0, 0, 0, 0);
    uip_ipaddr_copy(&pvector[0].addr, sender_addr);
    if (data[0] == 'P') { // check if the incoming info is about presence or temperature
      sscanf((char * ) data, "P %d", & pvector[0].presence);
      int i;
      for (i = 0; i < 5; i++) pvector[0].temperature[i] = 0;
    } else if (data[0] == 'T') {
      pvector[0].presence = 0;
      sscanf((char *) data, "T %f %f %f %f %f", &pvector[0].temperature[0], &pvector[0].temperature[1], &pvector[0].temperature[2], &pvector[0].temperature[3], &pvector[0].temperature[4]);
    }
  } else { // if there's already data saved
    int exist = 0;
    int i;
    for (i = 0; i < pvector_size; i++) { // exist = 1, if there is data related to this mote
      if (uip_ipaddr_cmp(&pvector[i].addr, sender_addr)) { // compare the address
        exist = 1;
        break;
      }
    }
    if (!exist) { // if not exists, create
      pvector = (vector *) realloc(pvector, (++pvector_size)*sizeof(vector));
      uip_ip6addr(&pvector[i].addr, 0xaaaa, 0, 0, 0, 0, 0, 0, 0);
      uip_ipaddr_copy(&pvector[i].addr, sender_addr);
      pvector[i].presence = 0;
      int k;
      for (k = 0; k < 5; k++) pvector[i].temperature[k] = 0;
    }

    // save the information
    if (data[0] == 'P') {
      sscanf((char *) data, "P %d", &pvector[i].presence);
    } else if (data[0] == 'T') {
      sscanf((char *) data, "T %f %f %f %f %f", &pvector[i].temperature[0], &pvector[i].temperature[1], &pvector[i].temperature[2], &pvector[i].temperature[3], &pvector[i].temperature[4]);
    }
  }
}
/*---------------------------------------------------------------------------*/
static void
receiver(struct simple_udp_connection *c,
        const uip_ipaddr_t *sender_addr,
        uint16_t sender_port,
        const uip_ipaddr_t *receiver_addr,
        uint16_t receiver_port,
        const uint8_t *data,
        uint16_t datalen) {
  printf("Data received from ");
  uip_debug_ipaddr_print(sender_addr);
  printf(" on port %d from port %d with length %d: '%s'\n",
    receiver_port, sender_port, datalen, data);
  fill_values(sender_addr, data);
}
/*---------------------------------------------------------------------------*/
static uip_ipaddr_t * set_global_address(void) {
    static uip_ipaddr_t ipaddr;
    int i;
    uint8_t state;

    uip_ip6addr(&ipaddr, 0xaaaa, 0, 0, 0, 0, 0, 0, 0);
    uip_ds6_set_addr_iid(&ipaddr, &uip_lladdr);
    uip_ds6_addr_add(&ipaddr, 0, ADDR_AUTOCONF);

    printf("IPv6 addresses: ");
    for (i = 0; i < UIP_DS6_ADDR_NB; i++) {
      state = uip_ds6_if.addr_list[i].state;
      if (uip_ds6_if.addr_list[i].isused &&
        (state == ADDR_TENTATIVE || state == ADDR_PREFERRED)) {
        uip_debug_ipaddr_print(&uip_ds6_if.addr_list[i].ipaddr);
        printf("\n");
      }
    }

    return &ipaddr;
  }
/*---------------------------------------------------------------------------*/
static void
create_rpl_dag(uip_ipaddr_t *ipaddr) {
  struct uip_ds6_addr *root_if;

  root_if = uip_ds6_addr_lookup(ipaddr);
  if (root_if != NULL) {
    rpl_dag_t *dag;
    uip_ipaddr_t prefix;

    rpl_set_root(RPL_DEFAULT_INSTANCE, ipaddr);
    dag = rpl_get_any_dag();
    uip_ip6addr(&prefix, 0xaaaa, 0, 0, 0, 0, 0, 0, 0);
    rpl_set_prefix(dag, &prefix, 64);
    PRINTF("created a new RPL dag\n");
  } else {
    PRINTF("failed to create a new RPL DAG\n");
  }
}

/*---------------------------------------------------------------------------*/
/**
 * Processo responsável por estabelecer a ligação com os motes e 
 * receber/guardar informação.
 */

PROCESS_THREAD(unicast_receiver_process, ev, data) {
  uip_ipaddr_t *ipaddr;

  PROCESS_BEGIN();

  servreg_hack_init();

  ipaddr = set_global_address();

  create_rpl_dag(ipaddr);

  servreg_hack_register(SERVICE_ID, ipaddr);

  simple_udp_register(&unicast_connection, UDP_PORT,
    NULL, UDP_PORT, receiver);

  while (1) {
    PROCESS_WAIT_EVENT();
  }
  free(pvector);
  PROCESS_END();
}

/*---------------------------------------------------------------------------*/
/**
 * Processo que imprime as informações guardadas para visualização
 * do usuário quando o botão é acionado
 */

PROCESS_THREAD(button_process, ev, data) {
  PROCESS_BEGIN();

  SENSORS_ACTIVATE(button_sensor);

  while (1) {
    PROCESS_WAIT_EVENT();

    if (ev == sensors_event &&
      data == &button_sensor) {
      printf("\n\nDATA:\n");
      int i;
      for (i = 0; i < pvector_size; i++) {
        printf("ADDR: ");
        uip_debug_ipaddr_print(&pvector[i].addr);
        printf("\nP = %d\n", pvector[i].presence);
        printf("T = %.1f %.1f %.1f %.1f %.1f\n\n", pvector[i].temperature[0], pvector[i].temperature[1], pvector[i].temperature[2], pvector[i].temperature[3], pvector[i].temperature[4]);
      }
    }
  }

  PROCESS_END();
}

/*---------------------------------------------------------------------------*/