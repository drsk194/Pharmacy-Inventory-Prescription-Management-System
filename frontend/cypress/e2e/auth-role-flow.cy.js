/* global describe, it, cy */
const accounts = { admin: { identifier: "admin@pipms.test", password: "Test@1234" }, technician: { identifier: "technician@pipms.test", password: "Test@1234" } };

describe("authentication and role routing", () => {
  it("remembers a protected destination after login", () => {
    cy.visit("/patients");
    cy.url().should("include", "/login");
    cy.loginAs(accounts.admin.identifier, accounts.admin.password);
    cy.url().should("include", "/patients");
  });

  it("blocks a technician from admin user management", () => {
    cy.loginAs(accounts.technician.identifier, accounts.technician.password);
    cy.visit("/admin/users");
    cy.url().should("include", "/not-authorized");
  });
});
