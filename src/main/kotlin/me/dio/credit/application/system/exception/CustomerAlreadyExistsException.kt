package me.dio.credit.application.system.exception

data class CustomerAlreadyExistsException(override val message: String?) : RuntimeException(message)