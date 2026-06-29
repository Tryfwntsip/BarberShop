package com.kourio.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Appointment {
    private int id;
    private int customerId;
    private int barberId;
    private int serviceId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal priceAtBooking;
    private int durationAtBooking;
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime statusUpdatedAt;
    private LocalDateTime createdAt;

    // Associated Entities (for MVC convenience in Views/Controllers)
    private User customer;
    private User barber;
    private Service service;

    // Constructors
    public Appointment() {
        this.status = AppointmentStatus.CONFIRMED;
    }

    public Appointment(int id, int customerId, int barberId, int serviceId, LocalDate appointmentDate, 
                       LocalTime startTime, LocalTime endTime, BigDecimal priceAtBooking, int durationAtBooking, 
                       AppointmentStatus status, String notes) {
        this.id = id;
        this.customerId = customerId;
        this.barberId = barberId;
        this.serviceId = serviceId;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priceAtBooking = priceAtBooking;
        this.durationAtBooking = durationAtBooking;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getBarberId() { return barberId; }
    public void setBarberId(int barberId) { this.barberId = barberId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public BigDecimal getPriceAtBooking() { return priceAtBooking; }
    public void setPriceAtBooking(BigDecimal priceAtBooking) { this.priceAtBooking = priceAtBooking; }

    public int getDurationAtBooking() { return durationAtBooking; }
    public void setDurationAtBooking(int durationAtBooking) { this.durationAtBooking = durationAtBooking; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getStatusUpdatedAt() { return statusUpdatedAt; }
    public void setStatusUpdatedAt(LocalDateTime statusUpdatedAt) { this.statusUpdatedAt = statusUpdatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Associated Entities Getters/Setters
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public User getBarber() { return barber; }
    public void setBarber(User barber) { this.barber = barber; }

    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }

    @Override
    public String toString() {
        return "Appointment #" + id + ": " + appointmentDate + " @ " + startTime + " - " + status;
    }
}
