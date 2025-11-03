package de.muenchen.mostserver.controller.dto

import java.time.LocalDateTime
import java.util.UUID
import jakarta.persistence.ManyToOne

class Datastream(val id: UUID, val name: String, val phenomomTime: LocalDateTime, @ManyToOne val sensor: Sensor) {
}