# WIP MOST-Server

MOST-Server (Munich Open Sensor Things API Server) implements an opinionated version of the OGC Sensor Things API.
Alternative to FROST Server

### Based on

* [Spring Reactive](https://spring.io/reactive) Familiar and modern development platform
* [OData](https://www.odata.org/) Standard for interaction
* [Apache Olingo](https://olingo.apache.org/) OData handling
* [OAuth 2.0](https://oauth.net/2/) General user and device authentication and authorization
* [UMA](https://docs.kantarainitiative.org/uma/rec-uma-core.html) Resource based authorization
* [MQTT Sparkplug B](https://sparkplug.eclipse.org/specification/) *TBD*

## Features

* Permission-based authorization
* OData compatible API
* Object auditing
* Optimistic locking

## Design Targets

* Use of existing standards - *Easy integration into custom environment*
* Extensive use of already existing libraries - *Keep code-base small. Less code, means less code to maintain and less code to understand data flow**
* Permission-based access control - *allow fine granular control over resources and flexible user patterns*
* Offer interface for commonly used data interactions out-of-the-box
* Extendability of data interface for custom use cases
* Scalability - *native support for clustering*
* High-availability - *native support for distributed deployment*
* Functional programming style approach - *easy understanding of code flow and testability*