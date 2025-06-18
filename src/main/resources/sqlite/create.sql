CREATE TABLE Caregivers (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Availabilities (
    Time date,
    Username varchar(255) REFERENCES Caregivers,
    PRIMARY KEY (Time, Username)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

CREATE TABLE Patient (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Appointment(
    AppointmentID varchar(255) PRIMARY KEY,
    PatientUsername varchar(255),
    CaregiverUsername varchar(255),
    VaccineName varchar(255),
    Time date,
    FOREIGN KEY (PatientUsername) REFERENCES Patients(Username),
    FOREIGN KEY (CaregiverUsername) REFERENCES Caregivers(Username),
    FOREIGN KEY (VaccineName) REFERENCES Vaccines(Name)
);

