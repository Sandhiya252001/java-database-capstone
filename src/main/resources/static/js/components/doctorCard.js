import { deleteDoctor } from "../js/services/doctorServices.js";
import { getPatientData } from "../js/services/patientServices.js";
import { showBookingOverlay } from "../js/components/modals.js";

export function createDoctorCard(doctor) {
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  const role = localStorage.getItem("userRole");

  // Doctor Info Section
  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  const name = document.createElement("h3");
  name.textContent = doctor.name;

  const specialization = document.createElement("p");
  specialization.textContent = `Specialization: ${doctor.specialty}`;

  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email}`;

  const availability = document.createElement("p");
  availability.textContent = `Available: ${doctor.availability.join(", ")}`;

  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(availability);

  // Card Actions Section
  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.textContent = "Delete";
    removeBtn.addEventListener("click", async () => {
      const confirmDelete = confirm(`Delete Dr. ${doctor.name}?`);
      if (!confirmDelete) return;

      const token = localStorage.getItem("token");
      try {
        await deleteDoctor(doctor.id, token);
        card.remove();
      } catch (error) {
        alert("Error deleting doctor. Please try again.");
        console.error(error);
      }
    });
    actionsDiv.appendChild(removeBtn);
  } else if (role === "patient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", () => {
      alert("Patient needs to login first.");
    });
    actionsDiv.appendChild(bookNow);
  } else if (role === "loggedPatient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", async (e) => {
      const token = localStorage.getItem("token");
      try {
        const patientData = await getPatientData(token);
        showBookingOverlay(e, doctor, patientData);
      } catch (error) {
        alert("Unable to retrieve patient info.");
        console.error(error);
      }
    });
    actionsDiv.appendChild(bookNow);
  }

  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);

  return card;
}
