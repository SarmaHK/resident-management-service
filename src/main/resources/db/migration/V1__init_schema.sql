CREATE TABLE roles (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255),
    status VARCHAR(255)
);

CREATE TABLE user_roles (
    user_id VARCHAR(255) NOT NULL,
    role_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE profiles (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone VARCHAR(255),
    profile_type VARCHAR(255),
    emergency_contact VARCHAR(255),
    tax_id VARCHAR(255),
    department VARCHAR(255)
);

CREATE TABLE apartment_relationships (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    relationship_type VARCHAR(255) NOT NULL,
    unit_reference VARCHAR(255) NOT NULL,
    supporting_info VARCHAR(255),
    status VARCHAR(255),
    decision_reason VARCHAR(255)
);

CREATE TABLE user_status_history (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    previous_status VARCHAR(255),
    new_status VARCHAR(255),
    reason VARCHAR(255),
    changed_by VARCHAR(255),
    changed_at TIMESTAMP
);

CREATE TABLE email_verification_tokens (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    new_email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL
);
