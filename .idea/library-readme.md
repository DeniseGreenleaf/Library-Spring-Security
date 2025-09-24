# 📚 Biblioteket – Library Management System

**Welcome to the Library!**  
A modern, full-stack library management system. The system implements a three-layer architecture (Controller, Service, Repository) and uses SQLite as the database. The application provides a RESTful API to manage books, users, and borrowing records, focusing on clean architecture, database design, and maintainable code.Built with **Spring Boot**, **React**, and **SQLite**, the library allows you to manage books, users, and borrowing records effortlessly. Right now I am working on adding security and log in features for users and admin with **spring security**

---

## 🚀 Live Demo
Coming soon..  
👉 Check out the live app: [Insert your deployed link here]

---

## 🛠️ Tech Stack

- **Spring Boot** – Backend framework for RESTful APIs
- **React** – Frontend library for dynamic UI
- **SQLite** – Lightweight relational database
- **Spring Data JPA** – Repository layer for database operations
- **Spring Security** – Authentication and role-based authorization
- **REST API** – Communication between frontend and backend
- **Java 11/17** – Backend programming language
- **HTML5, CSS3, JavaScript (React)** – Frontend development
- **Git & GitHub** – Version control and code hosting

---

## ✨ Features

- **➕ Book Management** – Add, edit, delete, and list books
- **👤 User Management** – Admin and regular users with secure login
- **🔄 Borrow & Return System** – Track borrowed books and return dates
- **🔍 Search & Filter** – Quickly find books or users
- **💻 RESTful API** – Clean endpoints for all operations
- **🔒 Security & Authentication** – Powered by Spring Security
- **🗃️ Persistent Storage** – Data stored in SQLite

---

## 🏗️ Architecture

- **Controller Layer** – Handles HTTP requests/responses
- **Service Layer** – Implements business logic and validation
- **Repository Layer** – Database operations via Spring Data JPA
- **Frontend (React)** – Communicates with backend via REST API

---

## 💻 Run Locally

### Backend (Spring Boot)

```bash
git clone git@github.com:DeniseGreenleaf/library.git
cd library/backend
./mvnw spring-boot:run
