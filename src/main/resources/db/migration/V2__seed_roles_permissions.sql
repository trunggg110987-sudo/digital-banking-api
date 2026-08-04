-- V2__seed_roles_permissions.sql
-- Seed initial roles and permissions

-- Insert roles
INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'Administrator with full access'),
    ('CUSTOMER', 'Regular customer with standard access'),
    ('SUPPORT', 'Support staff with limited admin access');

-- Insert permissions
INSERT INTO permissions (name, description) VALUES
    ('VIEW_USER', 'View user profile'),
    ('EDIT_USER', 'Edit user profile'),
    ('VIEW_ACCOUNT', 'View account details'),
    ('CREATE_ACCOUNT', 'Create new account'),
    ('CLOSE_ACCOUNT', 'Close account'),
    ('FREEZE_ACCOUNT', 'Freeze account'),
    ('VIEW_TRANSACTION', 'View transactions'),
    ('CREATE_TRANSFER', 'Create transfer'),
    ('APPROVE_LOAN', 'Approve loan application'),
    ('VIEW_LOAN', 'View loan details'),
    ('APPLY_LOAN', 'Apply for loan'),
    ('REPAY_LOAN', 'Repay loan'),
    ('ISSUE_CARD', 'Issue card'),
    ('BLOCK_CARD', 'Block card'),
    ('MANAGE_USERS', 'Manage all users'),
    ('MANAGE_ACCOUNTS', 'Manage all accounts'),
    ('MANAGE_LOANS', 'Manage all loans');

-- Assign permissions to ADMIN role (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- Assign permissions to CUSTOMER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'CUSTOMER' AND p.name IN (
    'VIEW_USER', 'EDIT_USER', 'VIEW_ACCOUNT', 'CREATE_ACCOUNT',
    'VIEW_TRANSACTION', 'CREATE_TRANSFER', 'VIEW_LOAN', 'APPLY_LOAN',
    'REPAY_LOAN', 'ISSUE_CARD', 'BLOCK_CARD'
);

-- Assign permissions to SUPPORT role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'SUPPORT' AND p.name IN (
    'VIEW_USER', 'VIEW_ACCOUNT', 'VIEW_TRANSACTION', 'VIEW_LOAN',
    'MANAGE_USERS', 'MANAGE_ACCOUNTS'
);
