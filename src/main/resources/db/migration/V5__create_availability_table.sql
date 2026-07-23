CREATE TABLE availability (
    id binary(16) PRIMARY KEY NOT NULL,
    session_type varchar(50) NOT NULL,
    date date NOT NULL,
    time time NOT NULL,
    is_booked boolean NOT NULL
) ENGINE=InnoDB;