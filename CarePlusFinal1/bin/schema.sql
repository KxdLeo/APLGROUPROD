create database if not exists careplus_hospital;
use careplus_hospital;

create table patients (
    patient_id varchar(12) primary key,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    contact_number varchar(30) not null,
    password varchar(60) not null,
    medical_history varchar(500)
);

create table employees (
    staff_id varchar(12) primary key,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    contact_number varchar(30) not null,
    password varchar(60) not null,
    department varchar(80) not null,
    role varchar(20) not null,
    specialty varchar(80)
);

create table complaints (
    complaint_id int primary key auto_increment,
    patient_id varchar(12) not null,
    category varchar(80) not null,
    description varchar(1000) not null,
    date_submitted date not null,
    status varchar(30) not null,
    assigned_employee_id varchar(12),
    response varchar(1000),
    response_date date,
    responded_by varchar(12),
    constraint fk_complaint_patient foreign key (patient_id) references patients(patient_id),
    constraint fk_complaint_assigned foreign key (assigned_employee_id) references employees(staff_id),
    constraint fk_complaint_responder foreign key (responded_by) references employees(staff_id)
);

create table appointments (
    appointment_id int primary key auto_increment,
    patient_id varchar(12) not null,
    doctor_id varchar(12) not null,
    appointment_date datetime not null,
    status varchar(30) not null,
    constraint fk_appointment_patient foreign key (patient_id) references patients(patient_id),
    constraint fk_appointment_doctor foreign key (doctor_id) references employees(staff_id)
);

create table medical_records (
    record_id int primary key auto_increment,
    patient_id varchar(12) not null,
    doctor_id varchar(12),
    diagnosis varchar(500),
    treatment_notes varchar(1000),
    vital_signs varchar(300),
    nursing_notes varchar(1000),
    follow_up_date date,
    constraint fk_record_patient foreign key (patient_id) references patients(patient_id),
    constraint fk_record_doctor foreign key (doctor_id) references employees(staff_id)
);

create table payments (
    payment_id int primary key auto_increment,
    patient_id varchar(12) not null,
    amount_paid decimal(10,2) not null,
    payment_date date not null,
    outstanding_balance decimal(10,2) not null,
    constraint fk_payment_patient foreign key (patient_id) references patients(patient_id)
);

create table chat_messages (
    chat_id int primary key auto_increment,
    sender_id varchar(12) not null,
    receiver_role varchar(20) not null,
    message varchar(1000) not null,
    sent_at datetime not null
);

insert into patients values
('P1001', 'Alicia', 'Grant', '876-555-1001', 'pass123', 'Asthma; penicillin allergy'),
('P1002', 'Marcus', 'Brown', '876-555-1002', 'pass123', 'Hypertension');

insert into employees values
('D2001', 'Nadia', 'Lewis', '876-555-2001', 'staff123', 'Outpatient Care', 'Doctor', 'General Medicine'),
('N3001', 'Tamara', 'Reid', '876-555-3001', 'staff123', 'Nursing', 'Nurse', null),
('R4001', 'Kevin', 'Morgan', '876-555-4001', 'staff123', 'Front Desk', 'Receptionist', null);
