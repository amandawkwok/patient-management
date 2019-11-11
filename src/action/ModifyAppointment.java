package action;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import entity.Appointment;
import entity.Patient;
import helper.AppointmentFormHelper;
//import helper.DateHelper;
//import helper.PatientFormHelper;
import helper.TimeHelper;
//import helper.Validator;

@WebServlet("/ModifyAppointment")
public class ModifyAppointment extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Creates/updates an appointment from user input taken from appointment_form.jsp
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pageHeader = request.getParameter("pageHeader");
		
		String date = request.getParameter("date");
		String time = request.getParameter("time");
		String dayTime = date + " " + time;
		request.setAttribute("dayTime", dayTime);
		System.out.println("\ndayTime: " + request.getAttribute("dayTime") + "\n"); 
		System.out.println("ssn: " + postSSN + "\n");
		
//		Timestamp dayTime = TimeHelper.convertToSQLDateTime(date, time);
//		request.setAttribute("dayTime", dayTime);
//		System.out.println("dayTime: " + request.getAttribute("dayTime") + "\n");
		
			
		LinkedHashMap<String, String> lhm = AppointmentFormHelper.getFormFieldInputPairs(request);
		
		if (pageHeader.contentEquals("Edit")) {
			lhm.put("ssn", "" + postSSN);
		}
		
		for (String key : lhm.keySet()) {
	        System.out.println(lhm.get(key));
	    }
		List<String> errorMessages = AppointmentFormHelper.validateInput(request);
		
		if (errorMessages.size() != 0) {
			long ssn = 0L;

			if (pageHeader.equals("Edit")) {
				ssn = postSSN;
			} else {
				ssn = Long.parseLong(request.getParameter("ssn"));
			}
			
			Map<String, String> patientName = Patient.getAttributeValuePairsBySSN(ssn);
			
			for (Map.Entry<String, String> field : patientName.entrySet()) {
				request.setAttribute(field.getKey(), field.getValue());
			}
//			request.setAttribute("date", date);
//			System.out.println("modifyDate: " + request.getAttribute("date"));
//			request.setAttribute("time", time);
//			System.out.println("modifyTime: " + request.getAttribute("time"));
			for (String key : lhm.keySet()) {
				String value = lhm.get(key);
				System.out.println("key: " + key + " , " + "value: " + value);
				request.setAttribute(key, value);
		    }
			request.setAttribute("date", date);
			request.setAttribute("time", time);
			
			
			request.setAttribute("pageHeader", pageHeader);
			request.setAttribute("errorMessages", errorMessages);
			request.getRequestDispatcher("appointment_form.jsp").include(request, response);
		}
		else {
			// if edit appointment is successful, forward to view_patient.jsp to
			// review updates
			if (pageHeader.contentEquals("Edit")) {
				editAppointment(lhm);

				
				
				long patSSNLong = postSSN;
				System.out.println("dogetSSN : " + postSSN);
				
				// Retrieve patient information
				Map<String, String> patientInformation = Patient.getAttributeValuePairsBySSN(patSSNLong);
				for (Map.Entry<String, String> field : patientInformation.entrySet()) {
					request.setAttribute(field.getKey(), field.getValue());
				}

				// Retrieve appointment information
				ArrayList<ArrayList<String>> upcomingAppts = Appointment.getBySSNAndTimePeriod(patSSNLong,
						"upcoming");
				ArrayList<ArrayList<String>> pastAppts = Appointment.getBySSNAndTimePeriod(patSSNLong,
						"past");

				request.setAttribute("ssn", patSSNLong);
				request.setAttribute("upcomingAppts", upcomingAppts);
				request.setAttribute("pastAppts", pastAppts);
				
				RequestDispatcher rd = request.getRequestDispatcher("view_patient.jsp");
				request.setAttribute("bannerMessage", "Success! Appointment has been updated.");
				rd.include(request, response);
			}
			else {
				addAppointment(lhm);

				String patientSSN = lhm.get("ssn");
				long patSSNLong = Long.parseLong(patientSSN);
				
				// Retrieve patient information
				Map<String, String> patientInformation = Patient.getAttributeValuePairsBySSN(patSSNLong);
				for (Map.Entry<String, String> field : patientInformation.entrySet()) {
					request.setAttribute(field.getKey(), field.getValue());
				}

				// Retrieve appointment information
				ArrayList<ArrayList<String>> upcomingAppts = Appointment.getBySSNAndTimePeriod(patSSNLong,
						"upcoming");
				ArrayList<ArrayList<String>> pastAppts = Appointment.getBySSNAndTimePeriod(patSSNLong,
						"past");

				request.setAttribute("ssn", patSSNLong);
				request.setAttribute("upcomingAppts", upcomingAppts);
				request.setAttribute("pastAppts", pastAppts);
				
				// go to view_patient.jsp to view patient which the appt belongs to
				RequestDispatcher rd = request.getRequestDispatcher("view_patient.jsp");
				request.setAttribute("bannerMessage", "Success! Appointment has been added.");
				rd.include(request, response);
			}
		}
	}
	
	/**
	 * Retrieves appointment information to be loaded into appointment_form.jsp 
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//String ssn = request.getParameter("ssn");
//		String date = request.getParameter("date");
//		String time = request.getParameter("time");
		//System.out.println(dayTime);
		String pageHeader = request.getParameter("pageHeader");
		
		if (pageHeader.contentEquals("Edit")) {
			int appointmentId = Integer.parseInt(request.getParameter("appointmentID"));
			
			
			List<String> patientSSN = Appointment.getSSNFromId(appointmentId);
			long ssn = Long.parseLong(patientSSN.get(0));
			System.out.println(ssn);
			
			Map<String, String> pat = Patient.getAttributeValuePairsBySSN(ssn);
			for (Map.Entry<String, String> field : pat.entrySet()) {
				request.setAttribute(field.getKey(), field.getValue());
				//System.out.println(field.getKey() + " : " + field.getValue());
			}
			
			System.out.println();
			
			Map<String, String> appt = Appointment.getAttributeValuePairsById(appointmentId);
			
			for (Map.Entry<String, String> field : appt.entrySet()) {
				request.setAttribute(field.getKey(), field.getValue());
				System.out.println(field.getKey() + " : " + field.getValue());
			}
			String dayTime = appt.get("dayTime");
			String[] dayAndTime = dayTime.split(" ");
			String date = dayAndTime[0];
			String time = dayAndTime[1] + " " + dayAndTime[2];
			request.setAttribute("date", date);
			request.setAttribute("time", time);
			request.setAttribute("primaryKey", ssn);
			postSSN = ssn;
			System.out.println("Post SSN : " + request.getAttribute("postSSN"));

			postID = appointmentId;
			request.setAttribute("pageHeader", "Edit");
			
			request.getRequestDispatcher("appointment_form.jsp").include(request, response);
			//request.getRequestDispatcher("appointment_form.jsp").forward(request, response);
		}
		else if (pageHeader.contentEquals("Add")) {
			long ssn = Long.parseLong(request.getParameter("ssn"));
			
			Map<String, String> patientInformation = Patient.getAttributeValuePairsBySSN(ssn);
			
			for (Map.Entry<String, String> field : patientInformation.entrySet()) {
				request.setAttribute(field.getKey(), field.getValue());
			}
			
			request.setAttribute("pageHeader", "Add");
			
			request.getRequestDispatcher("appointment_form.jsp").include(request, response);
		}
	}

	private void addAppointment(LinkedHashMap<String, String> lhm) {
		try {
			long ssn = Long.parseLong(lhm.get("ssn"));
			
			String dayTimeString = lhm.get("dayTime");
			Timestamp dayTime = TimeHelper.convertToSQLDateTime(dayTimeString);
			
			Appointment.add(ssn, dayTime, lhm.get("status"), lhm.get("reason"));
			
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
	}
		
	private void editAppointment(LinkedHashMap<String, String> lhm) {
		try {
			String dayTimeString = lhm.get("dayTime");
			Timestamp dayTime = TimeHelper.convertToSQLDateTime(dayTimeString);
			
			//String idString = lhm.get("id");
			int id = postID;
			
			Appointment.update(id, dayTime,	lhm.get("status"), lhm.get("reason"));
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private int postID;
	private long postSSN;
}