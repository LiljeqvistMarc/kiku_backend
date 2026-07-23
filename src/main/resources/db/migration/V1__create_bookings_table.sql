CREATE TABLE bookings (
    id binary(16) PRIMARY KEY NOT NULL,
    session_type varchar(50) NOT NULL,
    date date NOT NULL,
    time time NOT NULL,
    name varchar(256) NOT NULL,
    email varchar(256) NOT NULL,
    phone varchar(20) DEFAULT NULL,
    description varchar(1024) NULL,
    status varchar(256) NOT NULL,
    created_at datetime NOT NULL
) ENGINE=InnoDB;