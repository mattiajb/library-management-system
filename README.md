# Library Management System 📘

Desktop JavaFX application for managing a university library: books, users, and loans, with file-based persistence and a simple, usable GUI.

> 🏫 This project was developed as part of the 2025/26 **Software Engineering course** at the  
> **Università degli Studi di Salerno (University of Salerno, Italy).**

---

## 📌 Table of Contents

1. [Project Overview](#-project-overview)  
2. [Technologies & Dependencies](#-technologies--dependencies)   
3. [Main Features](#-main-features)  
4. [Project Skeleton](#-project-skeleton)  
5. [Design Principles](#-design-principles)
6. [Language & Naming Conventions](#-language--naming-conventions)
7. [Build & Run](#-build--run)
8. [Team Members](#-team-members)

---

## 🔍 Project Overview

The **Library Management System (LibraryMS)** is a desktop application that:

- Stores and manages **books**, **users** and **loans** in a central archive.
- Provides a **JavaFX GUI** to perform all operations required by the assignment:
  - CRUD Management (Create, Read, Update & Delete) books and users,
  - open and close loans,
  - filter and search entities,
  - detect overdue loans.

The project was developed as a complete case study in software engineering:
requirements, analysis, design, implementation and testing.

---

## 🛠 Technologies & Dependencies

![Java](https://img.shields.io/badge/Java-17+-ED8B00?logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-UI-007396?logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Apache%20Maven-Build-C71A36?logo=apachemaven&logoColor=white)
![Doxygen](https://img.shields.io/badge/Doxygen-Documentation-1f4277)
![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)

- **Language:** Java (JDK 17+ recommended; tested also with newer JDKs).
- **GUI:** JavaFX with FXML (views designed with tools such as Scene Builder).
- **Build Tool:** Apache Maven for lifecycle, dependency management and JavaFX plugin.
- **Testing:** JUnit 5.
- **Documentation:** Doxygen (for generating API documentation from comments).

---

## ✅ Main Features

- **📚 Books**
  - Register, edit and remove books.
  - Validation of ISBN, year, title and authors.
  - Search and sort by title, author, year, etc.

- **🧑‍🎓 Users**
  - Register, edit and remove users.
  - Validation of student code and institutional email (`@unisa.it`).
  - Search and sort by name, surname, email and matricola.

- **🔄 Loans**
  - Register new loans by selecting user and book from convenient combo boxes:
    - user displayed as `Student Code → Student Name`
    - book displayed as `ISBN → Title`
  - Automatic checks:
    - max number of active loans per user,
    - book copies availability,
    - due date not in the past.
  - Mark loans as returned, update available copies, and detect overdue loans.

- **📁 Persistence**
  - Archive stored in a serialized file (default: `library-archive.dat`).
  - Persistence is encapsulated in dedicated services.

- **🖥️ User Interface**
  - JavaFX views for:
    - **Books List**, **Users List**, **Loans List**
    - **Book Details / Add Book**
    - **User Details / Add User**
    - **Register Loan** and **Loan Details**
  - Consistent styling through a shared CSS file.

---

## 🧱 Project Skeleton

```markdown
java.swe.group04.libraryms
├── Main.java
├── models # Domain entities 
│   ├── LibraryArchive.java
│   └── Book.java
│   └── User.java
│   └── Loan.java
├── exceptions # Custom checked/unchecked exceptions
│   ├── MandatoryFieldsException.java
│   ├── InvalidEmailException.java
│   ├── InvalidIsbnException.java
│   ├── NoAvailableCopiesException.java
│   ├── MaxLoansReachedException.java
│   └── UserHasActiveLoansException.java
├── service # Business services, ServiceLocator, archive service
│   ├── LibraryArchiveService.java
│   ├── BookService.java
│   ├── ServiceLocator.java
│   ├── UserService.java
│   └── LoanService.java
├── persistence # persistence layer
│   ├── FileService.java
│   └── ArchiveFileService.java
├── controllers # JavaFX controllers for all views
│   ├── MainController.java
│   ├── BookController.java
│   ├── BookDetailsController.java
│   ├── AddBookController.java
│   ├── UserListController.java
│   ├── UserDetailsController.java
│   ├── AddUserController.java
│   ├── LoanListController.java
└── └── RegisterLoanController.java

resources.swe.group04.libraryms
├── css
└── view
```

---

## 🧩 Design Principles

The implementation follows several good design principles:

- **Separation of Concerns**
  - Model, business logic, persistence and presentation are clearly separated in distinct packages.
- **Single Responsibility Principle**
  - Each class has a focused purpose: e.g. `BookService` deals only with book-related operations;
    `ArchiveFileService` only knows how to load / save the archive.
- **Encapsulation**
  - The `LibraryArchive` is accessed only through dedicated services; direct manipulation of lists is avoided.
- **Validation & Robustness**
  - Inputs are validated early using domain-specific exceptions instead of generic errors.
- **Testability**
  - Services are designed to be testable in isolation using JUnit and temporary archive files.
- **Reusability**
  - Lower-level persistence (`FileService`) is reusable and independent from the domain.

---

## 🌐 Language & Naming Conventions

The project is developed for the Software Engineering course at the
University of Salerno (Università degli Studi di Salerno, Italy).

- **Business terminology and GUI labels** are in **Italian** (e.g. Libro, Prestito, Matricola),
following the original assignment language.

- **Source code identifiers** (class names, method names, package names) are in **English**,
following common Java conventions (BookService, LoanDetailsController, UserHasActiveLoanException, …).

This mixed approach keeps:

- the codebase readable within an international community,

- the user interface and documentation aligned with the Italian course context and exam requirements.

---

## 🚀 Build & Run

> ⚠️ Commands below assume a standard Maven + JavaFX setup.  
> If your `pom.xml` is different, adjust accordingly.

### Prerequisites

- **JDK 17+** installed and available on your `PATH`.
- **Maven 3+** installed.

### 1️⃣ Clone the repository
```bash
git clone https://github.com/mattiajb/library-management-system.git
cd <your-repo>
```

### 2️⃣ Build
```bash
mvn clean package
```

### 3️⃣ Run (via Maven JavaFX plugin)
```bash
mvn clean javafx:run
```

---

# 🐺 Team Members

- ◽ [Alfonso Aufiero](https://github.com/AlfonsoAufiero)
- ◽ [Mattia Gerardo Bavaro](https://github.com/mattiajb)
- ◽ [Andrea Botta](https://github.com/AndreaBotta333)
- ◽ [Christian Salvatore Bove](https://github.com/bove903)


