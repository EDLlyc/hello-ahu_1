
# AHU Alumni Honor Wall

A simple Spring Boot application for managing and displaying alumni information.

## Features

* **Statistics**: Shows total alumni count with a live counter.
* **Admin System**: Admin login required to Add, Edit, or Delete data.
* **Image Support**: Supports profile picture uploads.
* **Search**: Allows searching alumni by name.

## Tech Stack

* Spring Boot 3 & Java 17.
* Thymeleaf (Frontend templates).
* Spring Data JPA (Database access).
* MySQL (Data storage).

## How to Run

1. Create a MySQL database named `hello_ahu`.
2. Update `src/main/resources/application.properties` with your MySQL username and password.
3. Run the project using your IDE or Maven: `mvn spring-boot:run`.
4. Access the web page at: `http://localhost:8080/alumni-wall`.

## Admin Login

* **Username**: admin
* **Password**: 123456

---
