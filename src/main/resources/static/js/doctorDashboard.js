import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

// ✅ Global variables
const tableBody = document.getElementById("patientTableBody");
let selectedDate = new Date().toISOString().slice(0, 10); // today's date
let token = localStorage.getItem("token");
let patientName = null;

// ✅ Search bar input listener
document.getElementById("searchBar").addEventListener("input", (e) => {
  patientName = e.target.value.trim() || "null";
  loadAppointments();
});

// ✅ Today's appointments button
document.getElementById("todayButton").addEventListener("click", () => {
  selectedDate = new Date().toISOString().slice(0, 10);
  document.getElementById("datePicker").value = selectedDate;
  loadAppointments();
});

// ✅ Date picker change listener
document.getElementById("datePicker").addEventListener("change", (e) => {
  selectedDate = e.target.value;
  loadAppointments();
});

// ✅ Load appointments function
async function loadAppointments() {
  try {
    const appointments = await getAllAppointments(selectedDate, patientName, token);
    tableBody.innerHTML = "";

    if (!appointments || appointments.length === 0) {
      const noRow = document.createElement("tr");
      noRow.innerHTML = `<td colspan="6">No Appointments found for selected date.</td>`;
      tableBody.appendChild(noRow);
      return;
    }

    appointments.forEach((appointment) => {
      const row = createPatientRow(appointment);
      tableBody.appendChild(row);
    });
  } catch (error) {
    console.error("Appointment fetch failed:", error);
    const errorRow = document.createElement("tr");
    errorRow.innerHTML = `<td colspan="6">Unable to load appointments. Please try again later.</td>`;
    tableBody.appendChild(errorRow);
  }
}

// ✅ Initial page load
window.addEventListener("DOMContentLoaded", () => {
  document.getElementById("datePicker").value = selectedDate;
  loadAppointments();
});
