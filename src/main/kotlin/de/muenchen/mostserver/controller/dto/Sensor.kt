package de.muenchen.mostserver.controller.dto

import org.springframework.data.annotation.Id
import java.util.UUID

class Sensor(@Id val id: UUID, val manufacturer: String, val name: String) {
}