import { openModal } from "../components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

// ✅ Load Doctor Cards on Page Load
window.addEventListener("DOMContentLoaded", loadDoctorCards);

// Opens "Add Doctor" Modal
document.getElementById("addDocBtn").addEventListener("click", () => {
  openModal("addDoctor");
});

// Filter and Search Logic
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);

// ✅ Load and Render All Doctors
async function loadDoctorCards() {
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";

  const doctors = await getDoctors();
  renderDoctorCards(doctors);
}

// ✅ Render Doctor Cards
function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";

  if (doctors.length === 0) {
    contentDiv.innerHTML = "<p>No doctors found</p>";
    return;
  }

  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

// ✅ Filtering Doctors
async function filterDoctorsOnChange() {
  const name = document.getElementById("searchBar").value.trim();
  const time = document.getElementById("filterTime").value;
  const specialty = document.getElementById("filterSpecialty").value;

  const filteredDoctors = await filterDoctors(name, time, specialty);
  renderDoctorCards(filteredDoctors);
}

// ✅ Handle Adding Doctor
window.adminAddDoctor = async function () {
  const name = document.getElementById("docName").value.trim();
  const email = document.getElementById("docEmail").value.trim();
  const password = document.getElementById("docPassword").value;
  const mobile = document.getElementById("docMobile").value;
  const specialty = document.getElementById("docSpecialty").value;
  const availability = [];

  document.querySelectorAll(".availabilityCheckbox:checked").forEach((checkbox) => {
    availability.push(checkbox.value);
  });

  const doctor = { name, email, password, mobile, specialty, availability };
  const token = localStorage.getItem("token");

  if (!token) {
    alert("Admin not authenticated");
    return;
  }

  const result = await saveDoctor(doctor, token);
  if (result.success) {
    alert("Doctor added successfully");
    document.getElementById("addDoctorModal").classList.remove("show"); // close modal
    loadDoctorCards(); // refresh doctor list
  } else {
    alert(result.message || "Failed to add doctor");
  }
};
