# Home Automation Server

## Background

This is a side project I threw together because I wasn't happy with the complexity and capabilities available
in OpenHab2 or any other automation server I could find.

The problem was that OpenHab is primarily focused on reading sensor data. It seems that controlling and
management of devices is a bit of an afterthought and not properly modeled. For instance, OpenHab can turn
switches off and on, but doesn't have any support for scheduling when to do so.

I had tried to hack in this capability and while mostly possible, the UI options to work with the calendar
were unfortunately pretty awful. My needs were pretty simple, so I decided to create my own from scratch.

## Overview

This initial version of the Home Automation Server is driven by my own needs to manage my sprinkler system.
It can manage the state of irrigation valves either via direct control or through a schedule. It includes a
fairly simple, but effective UI with authentication through Google so that it's a little safer to run on a
home server, but expose through a router so you can manage it anywhere.

## Architecture

The general architecture consists of an automation management server, a UI that communicates with the server
through a REST API, and one or more nodes that operate the devices.

### UI

The UI is written in Typescript and built on the [Angular 2](https://angular.io) version 
of [Ionic 4](https://ionicframework.com). My forte is mainly in server side software, so expect that I made
some number of fundamental mistakes in the client side UI. It does work, though.

The UI currently has a **Control** tab and a **Schedule** tab. The **Control** tab has toggle switches for
each irrigation valve. The **Schedule** tab has daily settings for when to run the irrigation. Each valve has
its own setting for what days and the time of day to run. The schedule as a whole may be paused until a 
specified date or stopped entirely.

### Server

The server is written in [Kotlin](https://kotlinlang.org) and built on [Ktor](https://ktor.io), a 
Kotlin focused server side framework. It has a REST API that is primarily focused on serving the needs of
the UI, although its methods and resources are not UI specific. 

The state of the devices is persisted to a file using an embedded [H2](https://www.h2database.com) database.
The devices have an associated schedule that will turn them off/on at specific times of the day on specific
days of the week. The scheduling my be paused or stopped.

The basic structure is there to use for other automation needs, but would require modifications at the 
moment. In the future, I'll look to generalize it a bit, but don't expect some sort of OpenHab competitor any
time soon or probably ever.

#### Configuration

The server looks for

### Node

The devices are controlled by a node service, likely running on something like a Raspberry Pi. The node 
service is also written in Kotlin and built on Ktor. It does not have a persistence layer and always responds
with the current runtime state of the GPIO pins controlling the valves.