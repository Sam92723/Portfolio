# Vaccine Scheduler

A command-line based vaccine appointment scheduling system designed for hospitals and clinics to efficiently manage patients, caregivers, vaccine inventory, and reservations. This project simulates a real-world healthcare scheduling environment while focusing on secure authentication, role-based access, and scalable system design.

## **Features**
**User Roles:** Supports account creation and login for both patients and caregivers

**Secure Authentication:** Implements password salting and hashing using Java cryptography

**Appointment Management:** Patients can search for caregiver availability and reserve vaccine appointments

**Inventory Tracking:** Real-time dose tracking for multiple vaccines

**Role-Based Views:** Users can view appointments relevant to their role (patient or caregiver)

**Command Line Interface:** Clean, structured CLI with built-in error handling for invalid inputs

**Scalable Architecture:** Modular design with separate data models and database schema

## **Tech Stack**
Language: Java

Database: SQLite

Security: Java SecureRandom, PBEKeySpec, and SecretKeyFactory for password hashing

Design Tools: ER Diagram, SQL schema, and modular Java classes

## **Project Structure**
src/main/scheduler/ — Main logic and CLI

src/main/scheduler/model/ — Java data models: Patient, Caregiver, Vaccine

src/main/resources/ — ER diagram and SQL schema files

src/main/resources/sqlite/create.sql — SQLite table creation script

## **Sample Operations**

create_patient <username> <password>

login_patient <username> <password>

search_caregiver_schedule <date>

reserve <date> <vaccine>

show_appointments

logout

## **Security Highlight**
Passwords are never stored in plain text. A secure salt and hashing process ensures strong protection of user credentials using PBKDF2 with HMAC-SHA1.

## **Learning Outcomes**
Designing full-stack systems from ER modeling to implementation

Handling real-world constraints like secure login, role management, and inventory handling

Writing clean, testable, and maintainable code across the stack
