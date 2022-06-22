package api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.jms.annotation.EnableJms


@SpringBootApplication
@EnableJms
open class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
