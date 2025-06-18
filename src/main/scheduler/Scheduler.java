package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Create patient failed");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        
        if (!isStrongPassword(password)) {
            System.out.println("Create patient failed, please use a strong password (8+ char, at least one upper and one lower, at least one letter and one number, and at least one special character, from \"!\", \"@\", \"#\", \"?\")");
            return;
        }


        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again");
            return;
        }


        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        
        try {
            Patient patient = new Patient.PatientBuilder(username, salt, hash).build(); 
            // save to caregiver information to our database
            patient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Create patient failed.");
        }

    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patient WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already

        if (!isStrongPassword(password)) {
            System.out.println("Create caregiver failed, please use a strong password (8+ char, at least one upper and one lower, at least one letter and one number, and at least one special character, from \"!\", \"@\", \"#\", \"?\")");
            return;
        }

        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Caregiver caregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build(); 
            // save to caregiver information to our database
            caregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
        } finally {
            cm.closeConnection();
        }
        return true;
    }


    private static boolean isStrongPassword(String password) {

        if (password.length() < 8) {
            return false;
        }
    
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;
        
        String specialCharacters = "!@#?";
    

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
    
            if (Character.isUpperCase(ch)) {
                hasUpper = true;
            } else if (Character.isLowerCase(ch)) {
                hasLower = true;
            } else if (Character.isDigit(ch)) {
                hasNumber = true;
            } else if (specialCharacters.indexOf(ch) != -1) { 
                hasSpecial = true;
            }
        }
    
        
        return hasUpper && hasLower && hasNumber && hasSpecial;
    }

    private static void loginPatient(String[] tokens) {
        // TODO: Part 1
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in, try again");
            return;
        }
        
        if (tokens.length != 3) {
            System.out.println("Login patient failed");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login patient failed");
        }
        
        if (patient == null) {
            System.out.println("Login patient failed");
        } else {
            System.out.println("Logged in as " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in, try again");
            return;
        }
        
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        // TODO: Part 2
        if(currentCaregiver == null && currentPatient == null){
            System.out.println("Please login first");
            return;
        }

        if(tokens.length != 2){
            System.out.println("Please try again");
            return;
        }

        String date = tokens[1];
        Date d;

        try{
            d = Date.valueOf(date);
            
        }catch(IllegalArgumentException e){
            System.out.println("Please try again");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String checkCaregivers = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username ASC";

        String checkVaccines = "SELECT Name, Doses FROM Vaccines ORDER BY Name ASC";

        try{
          PreparedStatement caregiverStatement = con.prepareStatement(checkCaregivers);
          caregiverStatement.setDate(1, d); 
          ResultSet listOfCaregivers = caregiverStatement.executeQuery();


          System.out.println("Caregivers:");
          boolean hasCaregivers = false;
        
          while(listOfCaregivers.next()){
            hasCaregivers = true;
            System.out.println(listOfCaregivers.getString("Username"));

            
          }

          if(!hasCaregivers){
            System.out.println("No caregivers available");
          }

          caregiverStatement.close();
          listOfCaregivers.close();


          PreparedStatement vacStatement = con.prepareStatement(checkVaccines);

          ResultSet listOfVaccines = vacStatement.executeQuery();

          System.out.println("Vaccines:");

          boolean hasVaccines = false;

          while(listOfVaccines .next()){
            hasVaccines = true;
            String vacName = (listOfVaccines.getString("Name"));
            int numDoses = (listOfVaccines.getInt("Doses"));
            System.out.println(vacName + " " + numDoses);
          }

          if(!hasVaccines){
            System.out.println("No vaccines available");
          }

          vacStatement.close();
          listOfVaccines.close();




        }catch (SQLException e){
           System.out.println("Please try again");
        }finally{
            cm.closeConnection();
        }
            




    }

    private static void reserve(String[] tokens) {
       if(currentPatient == null){
            if(currentCaregiver != null){
                System.out.println("Please login as a patient");
            }else{
                System.out.println("Please login first");
            }
            return;
       }

       if(tokens.length != 3 ){
            System.out.println("Please try again");
            return;
        }


       String date = tokens[1];
       String vaccineName = tokens[2];
       Date d;

       try {
            d = Date.valueOf(date); 
        } catch (IllegalArgumentException e) {
            System.out.println("Please try again"); 
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        try{
            String caregiverQuery = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username ASC LIMIT 1";
            PreparedStatement caregiverStatement = con.prepareStatement(caregiverQuery);
            caregiverStatement.setDate(1, d);
            ResultSet caregiverResult = caregiverStatement.executeQuery();

            String selectedCaregiver = null;

            if(caregiverResult.next()){
                selectedCaregiver = caregiverResult.getString("Username");
            }else{
                System.out.println("No caregiver is available");
                caregiverStatement.close();
                caregiverResult.close();
                cm.closeConnection();
                return;
            }

            caregiverStatement.close();
            caregiverResult.close();


            Vaccine vaccine = new Vaccine.VaccineGetter(vaccineName).get();
            if (vaccine == null || vaccine.getAvailableDoses() <= 0) {
                System.out.println("Not enough available doses");
                cm.closeConnection();
                return;
            }


            String appointmentIdQuery = "SELECT MAX(AppointmentID) AS MaxID FROM Appointment";
            PreparedStatement idStatement = con.prepareStatement(appointmentIdQuery);
            ResultSet idResult = idStatement.executeQuery();
            int appointmentID = 1;
            if (idResult.next() && idResult.getObject("MaxID") != null) { // Handle NULL safely
                appointmentID = idResult.getInt("MaxID") + 1;
            }

            idStatement.close();
            idResult.close();

            


            String insertAppointment = "INSERT INTO Appointment (AppointmentID, PatientUsername, CaregiverUsername, VaccineName, Time) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = con.prepareStatement(insertAppointment);
            insertStmt.setInt(1, appointmentID);
            insertStmt.setString(2, currentPatient.getUsername());
            insertStmt.setString(3, selectedCaregiver);
            insertStmt.setString(4, vaccineName);
            insertStmt.setDate(5, d);
            insertStmt.executeUpdate();
            insertStmt.close();


            vaccine.decreaseAvailableDoses(1);


            String removeAvailability = "DELETE FROM Availabilities WHERE Username = ? AND Time = ?";
            PreparedStatement removeStatement = con.prepareStatement(removeAvailability);
            removeStatement.setString(1, selectedCaregiver);
            removeStatement.setDate(2, d);
            removeStatement.executeUpdate();
            removeStatement.close();


            System.out.println("Appointment ID " + appointmentID + ", Caregiver username " + selectedCaregiver);




        }catch(SQLException e){
            System.out.println("Please try again");
        }finally{
            cm.closeConnection();
        }


    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit

        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }
    
        
        if (tokens.length != 2) {
            System.out.println("Please try again");
            return;
        }
    
        int appointmentId;
        try {
            appointmentId = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            System.out.println("Please try again");
            return;
        }
    
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
    
        try {
           
            String query = "SELECT Time, VaccineName, CaregiverUsername FROM Appointment WHERE AppointmentID = ?";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, appointmentId);
            ResultSet rs = statement.executeQuery();
    
            if (!rs.next()) {
                System.out.println("Appointment ID " + appointmentId + " does not exist");
                statement.close();
                rs.close();
                cm.closeConnection();
                return;
            }
    
            Date appointmentDate = rs.getDate("Time");
            String vaccineName = rs.getString("VaccineName");
            String caregiverUsername = rs.getString("CaregiverUsername");
    
            statement.close();
            rs.close();
    
            
            String deleteAppointment = "DELETE FROM Appointment WHERE AppointmentID = ?";
            PreparedStatement deleteStmt = con.prepareStatement(deleteAppointment);
            deleteStmt.setInt(1, appointmentId);
            deleteStmt.executeUpdate();
            deleteStmt.close();
    
            
            String restoreCaregiver = "INSERT INTO Availabilities (Username, Time) VALUES (?, ?)";
            PreparedStatement restoreStmt = con.prepareStatement(restoreCaregiver);
            restoreStmt.setString(1, caregiverUsername);
            restoreStmt.setDate(2, appointmentDate);
            restoreStmt.executeUpdate();
            restoreStmt.close();
    
            
            Vaccine vaccine = new Vaccine.VaccineGetter(vaccineName).get();
            if (vaccine != null) {
                vaccine.increaseAvailableDoses(1);
            }
    
            System.out.println("Appointment ID " + appointmentId + " has been successfully canceled");
    
        } catch (SQLException e) {
            System.out.println("Please try again");
        } finally {
            cm.closeConnection();
        }
    
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        // TODO: Part 2

        if(currentPatient == null && currentCaregiver == null ){
            System.out.println("Please login first");
            return;
        }

        if (tokens.length != 1) {  
            System.out.println("Please try again");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String query;
        String user;

        if(currentPatient != null){
            query = "SELECT AppointmentID, VaccineName, Time, CaregiverUsername FROM Appointment WHERE PatientUsername = ? ORDER BY AppointmentID ASC";
            user = currentPatient.getUsername();
        }else{
            query = "SELECT AppointmentID, VaccineName, Time, PatientUsername FROM Appointment WHERE CaregiverUsername = ? ORDER BY AppointmentID ASC";
            user = currentCaregiver.getUsername();
        }


        try{
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, user);
            ResultSet resultSet = statement.executeQuery();


            boolean hasAppointments = false;

            while(resultSet.next()){
                hasAppointments = true;
                int appointmentID = resultSet.getInt("AppointmentID");
                String vaccineName = resultSet.getString("VaccineName");
                Date appointmentDate = resultSet.getDate("Time");
                String otherUser = resultSet.getString(4);

                System.out.println(appointmentID + " " + vaccineName + " " + appointmentDate + " " + otherUser);
            }

            if (!hasAppointments) {
                System.out.println("No appointments scheduled");
            }

            resultSet.close();
            statement.close();

        }catch (SQLException e){
            System.out.println("Please try again");
        }finally{
            cm.closeConnection();
        }
    }

    private static void logout(String[] tokens) {

        if (tokens.length != 1) {
            System.out.println("Please try again");
            return;
        }


        try {
            if (currentCaregiver == null && currentPatient == null) {
                System.out.println("Please login first");
                return;
            }
    
            currentCaregiver = null;
            currentPatient = null;
            System.out.println("Successfully logged out");
        } catch (Exception e) {
            System.out.println("Please try again");
        }
        
    }
}
