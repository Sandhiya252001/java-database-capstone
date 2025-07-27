import { createDoctorCard } from "./components/doctorCard.js";
import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors } from "./services/doctorServices.js";
import { patientLogin, patientSignup } from "./services/patientServices.js";

// ✅ Load all doctors on page load
document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();

  const signupBtn = document.getElementById("patientSignup");
  if (signupBtn) signupBtn.addEventListener("click", () => openModal("patientSignup"));

  const loginBtn = document.getElementById("patientLogin");
  if (loginBtn) loginBtn.addEventListener("click", () => openModal("patientLogin"));
});

// ✅ Search and filter event listeners
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);

// ✅ Load and render all doctors
async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Error loading doctors:", error);
    document.getElementById("content").innerHTML = "<p>Unable to load doctor data.</p>";
  }
}

// ✅ Render utility
function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";

  if (doctors.length === 0) {
    contentDiv.innerHTML = "<p>No doctors found.</p>";
    return;
  }

  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

// ✅ Filter logic
async function filterDoctorsOnChange() {
  const name = document.getElementById("searchBar").value.trim();
  const time = document.getElementById("filterTime").value;
  const specialty = document.getElementById("filterSpecialty").value;

  try {
    const doctors = await filterDoctors(name, time, specialty);
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Filter error:", error);
    document.getElementById("content").innerHTML = "<p>Unable to apply filters.</p>";
  }
}

// ✅ Patient Signup Handler
window.signupPatient = async function () {
  const name = document.getElementById("signupName").value;
  const email = document.getElementById("signupEmail").value;
  const password = document.getElementById("signupPassword").value;
  const phone = document.getElementById("signupPhone").value;
  const address = document.getElementById("signupAddress").value;

  const patient = { name, email, password, phone, address };

  try {
    const result = await patientSignup(patient);
    if (result.success) {
      alert("Signup successful!");
      document.getElementById("patientSignupModal").classList.remove("show");
      location.reload();
    } else {
      alert(result.message || "Signup failed.");
    }
  } catch (error) {
    console.error("Signup error:", error);
    alert("Error during signup.");
  }
};

// ✅ Patient Login Handler
window.loginPatient = async function () {
  const email = document.getElementById("loginEmail").value;
  const password = document.getElementById("loginPassword").value;

  try {
    const response = await patientLogin({ email, password });
    if (response && response.ok) {
      const result = await response.json();
      localStorage.setItem("token", result.token);
      window.location.href = "loggedPatientDashboard.html";
    } else {
      alert("Invalid credentials.");
    }
  } catch (error) {
    console.error("Login error:", error);
    alert("Login failed due to network error.");
  }
};
