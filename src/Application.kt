package org.tyaa.kotlin.ktor

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import org.jetbrains.exposed.sql.Database
import org.tyaa.kotlin.ktor.dao.DAOFacadeDatabase
import org.tyaa.kotlin.ktor.model.Employee

val dao = DAOFacadeDatabase(Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver"))

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    dao.init()

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    routing {

        route("/employees"){
            get {
                call.respond(dao.getAllEmployees())
            }
            get("/{id}") {
                val id = call.parameters["id"]
                if(id != null)
                    call.respond(dao.getEmployee(id.toInt()) ?: HttpStatusCode.NotFound)
            }
            post {
                val emp = call.receive<Employee>()
                dao.createEmployee(emp.name, emp.email, emp.city)
                call.respond(HttpStatusCode.Created)
            }
            put {
                val emp = call.receive<Employee>()
                dao.updateEmployee(emp.id, emp.name, emp.email, emp.city)
                call.respond(HttpStatusCode.OK)
            }
            delete("/{id}") {
                val id = call.parameters["id"]
                if(id != null)
                    dao.deleteEmployee(id.toInt())
                call.respond(HttpStatusCode.OK)
            }
        }

        /* get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        } */
    }
}

