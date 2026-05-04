package com.example.eventhub

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventhubApplication

fun main(args: Array<String>) {
	runApplication<EventhubApplication>(*args)
}
